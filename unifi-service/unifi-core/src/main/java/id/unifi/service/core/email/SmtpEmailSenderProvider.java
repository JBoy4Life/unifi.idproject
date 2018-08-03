package id.unifi.service.core.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HostAndPort;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import static com.rabbitmq.client.MessageProperties.PERSISTENT_BASIC;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.mq.MqUtils;
import id.unifi.service.common.provider.EmailSenderProvider;
import org.simplejavamail.MailException;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public class SmtpEmailSenderProvider implements EmailSenderProvider {
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderProvider.class);
    private static final TypeReference<FullEmailMessage> EMAIL_MESSAGE_TYPE = new TypeReference<>() {};
    private static final String OUTBOUND_QUEUE_NAME = "core.smtp.outbound";
    private static final int PREFETCH_COUNT = 20;
    private static final int MESSAGE_TTL_MILLIS = 86_400_000;
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
        String defaultSenderAddress();

        @Default("30 seconds")
        Duration sessionTimeout();
    }

    public static class FullEmailMessage {
        public final String recipientName;
        public final String recipientAddress;
        public final EmailMessage message;

        public FullEmailMessage(String recipientName, String recipientAddress, EmailMessage message) {
            this.recipientName = recipientName;
            this.recipientAddress = recipientAddress;
            this.message = message;
        }
    }

    public SmtpEmailSenderProvider(MqConfig mqConfig) {
        this.config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);
        log.info("Starting SMTP email sender with server: {}, username: {}, default sender: {}",
                config.server(), config.username(), config.defaultSenderAddress());

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

    public void send(String name, String address, EmailMessage message) {
        var fullMessage = new FullEmailMessage(name, address, message);
        try {
            channel.basicPublish("", OUTBOUND_QUEUE_NAME, PERSISTENT_BASIC, MqUtils.marshal(fullMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initMq() throws IOException {
        var consumer = MqUtils.unmarshallingConsumer(EMAIL_MESSAGE_TYPE, channel, tagged -> {
            try {
                sendNow(tagged.payload);
                try {
                    channel.basicAck(tagged.deliveryTag, false);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            } catch (Exception e) {
                log.error("Failed to send a message to {}", tagged.payload.recipientAddress, e);
                try {
                    channel.basicReject(tagged.deliveryTag, true);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
                Thread.sleep(WAIT_ON_FAILURE_MILLIS);
            }
        });

        channel.queueDeclare(OUTBOUND_QUEUE_NAME, true, false, false, Map.of("x-message-ttl", MESSAGE_TTL_MILLIS));
        channel.basicQos(PREFETCH_COUNT);
        channel.basicConsume(OUTBOUND_QUEUE_NAME, consumer);
    }

    private void sendNow(FullEmailMessage fullMessage) {
        log.debug("Sending email to {}", fullMessage.recipientAddress);
        var message = fullMessage.message;
        var email = EmailBuilder.startingBlank()
                .from(config.defaultSenderAddress())
                .to(fullMessage.recipientName, fullMessage.recipientAddress)
                .withSubject(message.subject)
                .withHTMLText(message.htmlBody)
                .withPlainText(message.textBody)
                .buildEmail();

        try {
            mailer.validate(email);
        } catch (MailException e) {
            log.warn("Validation failed, discarding email", e);
            return;
        }

        mailer.sendMail(email);
    }
}
