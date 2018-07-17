package id.unifi.service.core.agent.consumer;

import id.unifi.service.common.detection.SiteDetectionReport;
import static id.unifi.service.common.mq.MqUtils.drainQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InMemoryDetectionConsumer implements DetectionConsumer {
    private static final Logger log = LoggerFactory.getLogger(InMemoryDetectionConsumer.class);
    private static final int QUEUE_CAPACITY = 100_000;

    private final BlockingQueue<SiteDetectionReport> reportsQueue;
    private final SiteDetectionReportConsumer callback;
    private final Thread forwarderThread;

    public static InMemoryDetectionConsumer create(SiteDetectionReportConsumer consumer) {
        var detectionConsumer = new InMemoryDetectionConsumer(consumer);
        detectionConsumer.start();
        return detectionConsumer;
    }

    private InMemoryDetectionConsumer(SiteDetectionReportConsumer callback) {
        this.callback = callback;
        this.reportsQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.forwarderThread = new Thread(this::runForwardLoop);

    }

    private void start() {
        forwarderThread.start();
        log.info("Starting in-memory detection consumer");

    }

    private void runForwardLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            var reports = drainQueue(reportsQueue);
            if (!reports.isEmpty()) callback.accept(reports, () -> {});
        }
        log.info("Detection consumer thread interrupted, stopping");
    }

    public void accept(SiteDetectionReport report) {
        reportsQueue.offer(report);
    }
}
