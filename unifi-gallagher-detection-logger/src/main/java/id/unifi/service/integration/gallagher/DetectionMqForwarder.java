package id.unifi.service.integration.gallagher;

import com.rabbitmq.client.Channel;
import id.unifi.service.common.detection.DetectionMatch;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_EXCHANGE_NAME;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_TYPE;
import id.unifi.service.common.mq.MqUtils.InterruptibleConsumer;
import static id.unifi.service.common.mq.MqUtils.unmarshallingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class DetectionMqForwarder {
    private static final Logger log = LoggerFactory.getLogger(DetectionMqForwarder.class);

    private static final String PROCESSING_QUEUE_NAME = "integration.gallagher.logger";

    private final InterruptibleConsumer<DetectionMatch> consumer;
    private final Channel channel;

    public static DetectionMqForwarder create(Channel channel, InterruptibleConsumer<DetectionMatch> consumer) {
        var forwarder = new DetectionMqForwarder(channel, consumer);
        forwarder.start();
        return forwarder;
    }

    private DetectionMqForwarder(Channel channel, InterruptibleConsumer<DetectionMatch> consumer) {
        this.channel = channel;
        this.consumer = consumer;
    }

    private void start() {
        try {
            channel.queueDeclare(PROCESSING_QUEUE_NAME, true, false, false, null);
            channel.queueBind(PROCESSING_QUEUE_NAME, DETECTION_MATCH_EXCHANGE_NAME, "");
            channel.basicQos(10);

            var mqConsumer = unmarshallingConsumer(DETECTION_MATCH_TYPE, channel, taggedMatch -> {
                try {
                    log.trace("Processing {} ", taggedMatch);
                    consumer.accept(taggedMatch.payload);
                    channel.basicAck(taggedMatch.deliveryTag, false);
                } catch (Exception e) {
                    log.error("Fatal error", e);
                    System.exit(1);
                }
            });
            channel.basicConsume(PROCESSING_QUEUE_NAME, mqConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
