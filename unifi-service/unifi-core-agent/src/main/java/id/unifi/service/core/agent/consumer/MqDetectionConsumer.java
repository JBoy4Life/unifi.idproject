package id.unifi.service.core.agent.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterables;
import com.rabbitmq.client.Channel;
import static com.rabbitmq.client.MessageProperties.PERSISTENT_BASIC;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.common.mq.MqUtils;
import static id.unifi.service.common.mq.MqUtils.drainQueue;
import id.unifi.service.common.mq.Tagged;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MqDetectionConsumer implements DetectionConsumer {
    private static final Logger log = LoggerFactory.getLogger(MqDetectionConsumer.class);

    private static final TypeReference<SiteDetectionReport> SITE_DETECTION_REPORT_TYPE = new TypeReference<>() {};
    private static final String OUTBOUND_QUEUE_NAME = "core-agent.report.outbound";
    private static final int PREFETCH_COUNT = 100;

    private final BlockingQueue<Tagged<SiteDetectionReport>> reportsQueue;
    private final MqConfig config;
    private final SiteDetectionReportConsumer consumer;
    private Channel channel;
    private Thread forwarderThread;

    public static MqDetectionConsumer create(MqConfig config, SiteDetectionReportConsumer consumer) throws IOException {
        var detectionConsumer = new MqDetectionConsumer(config, consumer);
        detectionConsumer.start();
        return detectionConsumer;
    }

    private MqDetectionConsumer(MqConfig config, SiteDetectionReportConsumer consumer) {
        this.config = config;
        this.consumer = consumer;
        this.reportsQueue = new ArrayBlockingQueue<>(PREFETCH_COUNT);
        this.channel = null;
        this.forwarderThread = new Thread(this::runForwardLoop);
    }

    private void start() throws IOException {
        var connection = MqUtils.connect(config);
        this.channel = connection.createChannel();
        channel.queueDeclare(OUTBOUND_QUEUE_NAME, true, false, false, null);
        channel.basicConsume(OUTBOUND_QUEUE_NAME,
                MqUtils.unmarshallingConsumer(SITE_DETECTION_REPORT_TYPE, channel, reportsQueue::put));
        forwarderThread.start();
        log.info("Starting RabbitMQ detection consumer on {}", config.endpoint());
    }

    private void runForwardLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            var taggedReports = drainQueue(reportsQueue);
            if (!taggedReports.isEmpty()) {
                var reports = taggedReports.stream().map(r -> r.payload).collect(toList());
                consumer.accept(reports, ackCallback(Iterables.getLast(taggedReports)));
            }
        }
        log.info("Detection consumer thread interrupted, stopping");
    }

    private Runnable ackCallback(Tagged<SiteDetectionReport> report) {
        return () -> {
            try {
                channel.basicAck(report.deliveryTag, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void accept(SiteDetectionReport report) {
        try {
            channel.basicPublish("", OUTBOUND_QUEUE_NAME, PERSISTENT_BASIC, MqUtils.marshal(report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
