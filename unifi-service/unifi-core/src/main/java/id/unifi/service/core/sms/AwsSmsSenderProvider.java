package id.unifi.service.core.sms;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.GetSMSAttributesRequest;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Name;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.mq.MqUtils;
import id.unifi.service.core.util.AwsCredentials;
import id.unifi.service.core.util.EnvyAwsCredentialsProvider;
import id.unifi.service.core.util.RegionsValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class AwsSmsSenderProvider implements SmsSenderProvider {
    private static final long MESSAGE_TTL_MILLIS = 86_400_000;

    private static final Logger log = LoggerFactory.getLogger(AwsSmsSenderProvider.class);
    private static final AMQP.BasicProperties messageProps = new AMQP.BasicProperties.Builder()
            .deliveryMode(2) // persistent
            .expiration(Long.toString(MESSAGE_TTL_MILLIS))
            .build();

    private static final TypeReference<FullSmsMessage> SMS_MESSAGE_TYPE = new TypeReference<>() {};
    private static final String OUTBOUND_QUEUE_NAME = "core.sms.outbound";
    private static final int PREFETCH_COUNT = 20;
    private static final int WAIT_ON_FAILURE_MILLIS = 600_000;
    
    private final AmazonSNS snsClient;
    private final Connection connection;
    private final Channel channel;

    @Prefix("unifi.sms")
    public interface Config {
        Optional<Regions> awsRegion();

        @Name("aws")
        Optional<AwsCredentials> awsCredentials();
    }

    public static class FullSmsMessage {
        public final String phoneNumber;
        public final String message;
        public final boolean promotional;

        public FullSmsMessage(String phoneNumber, String message, boolean promotional) {
            this.phoneNumber = phoneNumber;
            this.message = message;
            this.promotional = promotional;
        }
    }

    public AwsSmsSenderProvider(MqConfig mqConfig) {
        var config = Envy.configure(Config.class, UnifiConfigSource.get(), RegionsValueParser.instance);

        var region = getRegion(config).orElseThrow(() -> new RuntimeException("SNS region not set"));
        log.info("Starting AWS SNS SMS sender with region: {}, AWS key ID: {}",
                region.getName(), config.awsCredentials().map(AwsCredentials::accessKeyId).orElse("<not known yet>"));

        this.snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new EnvyAwsCredentialsProvider(config.awsCredentials()))
                .build();

        // Display default SMS attributes; this also checks we're in a supported region
        var smsAttributes = snsClient.getSMSAttributes(new GetSMSAttributesRequest());
        log.info("Default SMS attributes: {}", smsAttributes);

        this.connection = MqUtils.connect(mqConfig);
        try {
            this.channel = connection.createChannel();
            initMq();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void queue(String phoneNumber, String message, boolean promotional) {
        var fullMessage = new FullSmsMessage(phoneNumber, message, promotional);
        try {
            channel.basicPublish("", OUTBOUND_QUEUE_NAME, messageProps, MqUtils.marshal(fullMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initMq() throws IOException {
        var consumer = MqUtils.unmarshallingConsumer(SMS_MESSAGE_TYPE, channel, tagged -> {
            try {
                try {
                    send(tagged.payload);
                } catch (InvalidParameterException e) {
                    log.error("Invalid message parameters, dropping message", e);
                }

                try {
                    channel.basicAck(tagged.deliveryTag, false);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            } catch (Exception e) {
                log.warn("Failed to send a message to {}, re-queuing", tagged.payload.phoneNumber, e);
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

    private void send(FullSmsMessage fullMessage) {
        var result = snsClient.publish(new PublishRequest()
                .withMessage(fullMessage.message)
                .withPhoneNumber(fullMessage.phoneNumber)
                .withMessageAttributes(Map.of(
                        "AWS.SNS.SMS.SMSType",
                        new MessageAttributeValue()
                                .withStringValue(fullMessage.promotional ? "Promotional" : "Transactional")
                                .withDataType("String"))));
        log.debug("SMS for {} queued in AWS SNS, messageId: {}", fullMessage.phoneNumber, result.getMessageId());
    }

    private static Optional<Regions> getRegion(Config config) {
        try {
            return config.awsRegion()
                    .or(() -> Optional.ofNullable(new DefaultAwsRegionProviderChain().getRegion())
                            .map(Regions::fromName)); // Fall back on EC2 instance metadata, i.e. current region
        } catch (SdkClientException e) { // Required until AWS SDK 2.0
            return Optional.empty();
        }
    }
}
