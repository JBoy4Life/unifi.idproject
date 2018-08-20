package id.unifi.service.core.agent.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
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
    private static final int MAX_REPORTS = 100;

    private final BlockingQueue<Tagged<SiteDetectionReport>> reportsQueue;
    private final MqConfig config;
    private final SiteDetectionReportConsumer consumer;
    private Connection connection;
    private Channel channel;
    private Channel pubChannel;
    private Thread forwarderThread;

    public static MqDetectionConsumer create(MqConfig config, SiteDetectionReportConsumer consumer) {
        var detectionConsumer = new MqDetectionConsumer(config, consumer);
        detectionConsumer.start();
        return detectionConsumer;
    }

    private MqDetectionConsumer(MqConfig config, SiteDetectionReportConsumer consumer) {
        this.config = config;
        this.consumer = consumer;
        this.reportsQueue = new ArrayBlockingQueue<>(MAX_REPORTS);
        this.connection = null;
        this.forwarderThread = new Thread(this::runForwardLoop, "mq-detection-forwarder");
    }

    private void start() {
        this.connection = MqUtils.connect(config);
        try {
            this.channel = connection.createChannel();
            this.pubChannel = connection.createChannel();
            channel.basicQos(MAX_REPORTS * 2); // Always have another batch ready
            channel.queueDeclare(OUTBOUND_QUEUE_NAME, true, false, false, null);
            channel.basicConsume(OUTBOUND_QUEUE_NAME,
                    new DefaultConsumer(channel) {
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body) throws IOException {
                            var unmarshalled = MqUtils.unmarshal(body, SITE_DETECTION_REPORT_TYPE);
                            try {
                                reportsQueue.put(new Tagged<>(unmarshalled, envelope.getDeliveryTag()));
                            } catch (InterruptedException e) {
                                throw new AssertionError("Unexpected consumer thread interrupt");
                            }
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        connection.addShutdownListener(cause -> log.error("MQ connection closed:", cause));
        forwarderThread.start();
        log.info("Starting RabbitMQ detection consumer on {}", config.endpoint());
    }

    private void runForwardLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            var taggedReports = drainQueue(reportsQueue);
            if (!taggedReports.isEmpty()) {
                var reports = taggedReports.stream().map(r -> r.payload).collect(toList());
                consumer.accept(reports, () -> taggedReports.forEach(t -> {
                    try {
                        channel.basicAck(t.deliveryTag, false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        }
        log.info("Detection consumer thread interrupted, stopping");
    }

    public void accept(SiteDetectionReport report) {
        try {
            pubChannel.basicPublish("", OUTBOUND_QUEUE_NAME, PERSISTENT_BASIC, MqUtils.marshal(report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
