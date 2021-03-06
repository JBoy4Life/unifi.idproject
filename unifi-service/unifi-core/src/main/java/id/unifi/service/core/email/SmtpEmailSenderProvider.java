package id.unifi.service.core.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HostAndPort;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.mq.MqUtils;
import id.unifi.service.common.provider.EmailSenderProvider;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

public class SmtpEmailSenderProvider implements EmailSenderProvider {
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderProvider.class);
    private static final TypeReference<FullEmailMessage> EMAIL_MESSAGE_TYPE = new TypeReference<>() {};
    private static final String OUTBOUND_QUEUE_NAME = "core.smtp.outbound";
    private static final int PREFETCH_COUNT = 20;
    private static final int MESSAGE_TTL_MILLIS = 86_400_000;
    private static final AMQP.BasicProperties messageProps = new AMQP.BasicProperties.Builder()
            .deliveryMode(2) // persistent
            .expiration(Long.toString(MESSAGE_TTL_MILLIS))
            .build();
    private static final int WAIT_ON_FAILURE_MILLIS = 10_000;

    private final Mailer mailer;
    private final Config config;
    private final Connection connection;
    private Channel channel;

    @Prefix("unifi.smtp")
    private interface Config {
        HostAndPort server();

        String username();

        String password();

        @Default("info@unifi.id")
        String defaultFromAddress();

        @Default("30 seconds")
        Duration sessionTimeout();
    }

    public static class FullEmailMessage {
        public final String fromAddress;
        public final String toName;
        public final String toAddress;
        public final EmailMessage message;

        public FullEmailMessage(String fromAddress, String toName, String toAddress, EmailMessage message) {
            this.fromAddress = fromAddress;
            this.toName = toName;
            this.toAddress = toAddress;
            this.message = message;
        }
    }

    public SmtpEmailSenderProvider(MqConfig mqConfig) {
        this.config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);
        log.info("Starting SMTP email sender with server: {}, username: {}, default sender: {}",
                config.server(), config.username(), config.defaultFromAddress());

        this.mailer = MailerBuilder
                .withSMTPServer(
                        config.server().getHost(),
                        config.server().getPortOrDefault(587),
                        config.username(),
                        config.password())
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withTransportModeLoggingOnly(false)
                .withSessionTimeout((int) config.sessionTimeout().toMillis())
                .buildMailer();
        this.connection = MqUtils.connect(mqConfig);
        try {
            this.channel = connection.createChannel();
            initMq();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void queue(String fromAddress, String toName, String toAddress, EmailMessage message) {
        queue(Optional.of(fromAddress), toName, toAddress, message);
    }

    public void queue(String toName, String toAddress, EmailMessage message) {
        queue(Optional.empty(), toName, toAddress, message);
    }

    private void queue(Optional<String> fromAddress, String toName, String toAddress, EmailMessage message) {
        var fullMessage =
                new FullEmailMessage(fromAddress.orElse(config.defaultFromAddress()), toName, toAddress, message);
        mailer.validate(buildSimpleJavaMailEmail(fullMessage));

        try {
            channel.basicPublish("", OUTBOUND_QUEUE_NAME, messageProps, MqUtils.marshal(fullMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initMq() throws IOException {
        var consumer = MqUtils.unmarshallingConsumer(EMAIL_MESSAGE_TYPE, channel, tagged -> {
            try {
                send(tagged.payload);
                try {
                    channel.basicAck(tagged.deliveryTag, false);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            } catch (Exception e) {
                log.error("Failed to send a message to {}", tagged.payload.toAddress, e);
                try {
                    channel.basicReject(tagged.deliveryTag, true);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
                Thread.sleep(WAIT_ON_FAILURE_MILLIS);
            }
        });

        channel.queueDeclare(OUTBOUND_QUEUE_NAME, true, false, false, null);
        channel.basicQos(PREFETCH_COUNT);
        channel.basicConsume(OUTBOUND_QUEUE_NAME, consumer);
    }

    private void send(FullEmailMessage fullMessage) {
        log.trace("Sending email to {}", fullMessage.toAddress);
        mailer.sendMail(buildSimpleJavaMailEmail(fullMessage));
    }

    private static Email buildSimpleJavaMailEmail(FullEmailMessage fullMessage) {
        var message = fullMessage.message;
        return EmailBuilder.startingBlank()
                .from(fullMessage.fromAddress)
                .to(fullMessage.toName, fullMessage.toAddress)
                .withSubject(message.subject)
                .withHTMLText(message.htmlBody)
                .withPlainText(message.textBody)
                .buildEmail();
    }
}
