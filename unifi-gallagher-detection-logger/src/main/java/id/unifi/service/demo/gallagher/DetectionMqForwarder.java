package id.unifi.service.demo.gallagher;

import com.rabbitmq.client.Channel;
import id.unifi.service.common.detection.DetectionMatch;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_EXCHANGE_NAME;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_TYPE;
import static id.unifi.service.common.mq.MqUtils.unmarshallingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BiConsumer;

class DetectionMqForwarder {
    private static final Logger log = LoggerFactory.getLogger(DetectionMqForwarder.class);

    private static final String PROCESSING_QUEUE_NAME = "integration.gallagher.logger";

    private final BiConsumer<DetectionMatch, Runnable> detectionConsumer;
    private final Channel channel;

    public static DetectionMqForwarder create(Channel channel, BiConsumer<DetectionMatch, Runnable> consumer) {
        var forwarder = new DetectionMqForwarder(channel, consumer);
        forwarder.start();
        return forwarder;
    }

    private DetectionMqForwarder(Channel channel, BiConsumer<DetectionMatch, Runnable> detectionConsumer) {
        this.channel = channel;
        this.detectionConsumer = detectionConsumer;
    }

    private void start() {
        try {
            channel.queueDeclare(PROCESSING_QUEUE_NAME, true, false, false, null);
            channel.queueBind(PROCESSING_QUEUE_NAME, DETECTION_MATCH_EXCHANGE_NAME, "");

            var consumer = unmarshallingConsumer(DETECTION_MATCH_TYPE, channel, taggedMatch -> {
                var success = false;
                while (!success) {
                    try {
                        log.trace("Processing {}", taggedMatch);
                        detectionConsumer.accept(taggedMatch.payload, () -> {
                            try {
                                channel.basicAck(taggedMatch.deliveryTag, false);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        success = true;
                    } catch (Exception e) {
                        log.info("Failed to process {}, retrying", taggedMatch);
                    }
                }
            });
            channel.basicConsume(PROCESSING_QUEUE_NAME, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}