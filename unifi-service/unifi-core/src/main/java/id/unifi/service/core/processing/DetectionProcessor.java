package id.unifi.service.core.processing;

import com.rabbitmq.client.Connection;
import static com.rabbitmq.client.MessageProperties.PERSISTENT_BASIC;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.detection.DetectionMatchListener;
import id.unifi.service.common.detection.DetectionMatchMqConsumer;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.common.mq.MqUtils;
import static id.unifi.service.common.mq.MqUtils.DETECTION_MATCH_EXCHANGE_NAME;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Matches detections against zone and holder, sends results and to subscribed listeners and a RabbitMQ exchange.
 */
public class DetectionProcessor {
    private final DetectionMatcher detectionMatcher;
    private final Set<DetectionMatchListener> listeners;
    private Connection connection;

    public DetectionProcessor(MqConfig mqConfig,
                              DetectionMatcher detectionMatcher,
                              Set<DetectionMatchMqConsumer> consumers,
                              Set<DetectionMatchListener> listeners) {
        this.detectionMatcher = detectionMatcher;
        this.listeners = listeners;

        var connection = initMq(mqConfig);
        consumers.forEach(c -> {
            try {
                c.start(connection, DETECTION_MATCH_EXCHANGE_NAME);
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void process(List<Detection> detections) {
        var detectionMatches = detections.stream()
                .flatMap(detection -> detectionMatcher.match(detection).stream())
                .collect(toList());
        processDetectionMatches(detectionMatches);
    }

    public void process(String clientId, List<SiteDetectionReport> reports) {
        var detectionMatches = reports.stream()
                .flatMap(r -> r.detections.stream().flatMap(d ->
                        detectionMatcher.match(new Detection(clientId, r.readerSn, d)).stream()))
                .collect(toList());
        processDetectionMatches(detectionMatches);
    }

    private void processDetectionMatches(List<DetectionMatch> detectionMatches) {
        if (!detectionMatches.isEmpty()) {
            listeners.forEach(l -> l.accept(detectionMatches));
        }

        try (var channel = connection.createChannel()) {
            for (var match : detectionMatches) {
                channel.basicPublish(DETECTION_MATCH_EXCHANGE_NAME, "", PERSISTENT_BASIC, MqUtils.marshal(match));
            }
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection initMq(MqConfig mqConfig) {
        connection = MqUtils.connect(mqConfig);

        try (var channel = connection.createChannel()) {
            channel.exchangeDeclare(DETECTION_MATCH_EXCHANGE_NAME, "fanout");
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}
