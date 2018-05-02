package id.unifi.service.demo.gallagher;

import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.provider.security.gallagher.FtcApi;
import id.unifi.service.provider.security.gallagher.IFTMiddleware2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;

public class GallagherAdapter implements IFTMiddleware2 {
    private static final Logger log = LoggerFactory.getLogger(GallagherAdapter.class);

    private final FtcApi ftcApi;
    private final Thread processingThread;
    private final BlockingQueue<Runnable> detectionQueue;

    private volatile CountDownLatch registerLatch;

    public static GallagherAdapter create() {
        var adapter = new GallagherAdapter();
        adapter.start();
        return adapter;
    }

    private GallagherAdapter() {
        this.detectionQueue = new SynchronousQueue<>();
        var ftcApiHost = "10.0.99.3";
        this.ftcApi = new FtcApi(ftcApiHost, "localhost", "Administrator", "TestPass123");
        this.processingThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                log.info("Connecting to FTC server {}", ftcApiHost);
                try {
                    registerLatch = new CountDownLatch(1);
                    log.info("Registering middleware");
                    ftcApi.registerMiddleware(this);
                    registerLatch.await();

                    while (true) {
                        try {
                            detectionQueue.take().run();
                        } catch (Exception e) {
                            log.error("Error processing detection, closing connection", e);
                            break;
                        }
                    }
                    
                    ftcApi.close();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void start() {
        processingThread.start();
    }

    public void process(DetectionMatch taggedMatch, Runnable onSuccess) throws InterruptedException {
        detectionQueue.put(() -> {
            if (taggedMatch.detection.detectable.detectableType == DetectableType.UHF_TID) {
                logDetection(taggedMatch);
            }
            onSuccess.run();
        });
    }

    private void logDetection(DetectionMatch match) {
        var eventId = 0; // corr ID; 0 for none
        var detection = match.detection;
        var detectableId = detection.detectable.detectableId;
        final var hasRestoral = false;
        var eventType = 2;
        var cardNumberFormatType = 2;
        var facilityCode = 12345;
        var systemId = "unifi.id";
        var itemId = String.format("%s:%d", detection.readerSn, detection.portNumber);

        log.trace("Logging {} to Gallagher", detection);

        ftcApi.logLongCardEvent2(
                eventType,
                eventId,
                detection.detectionTime,
                hasRestoral,
                cardNumberFormatType,
                detectableId,
                facilityCode,
                systemId,
                itemId,
                String.format("Card %s detected on %s", detectableId, itemId),
                "No details.");
        log.trace("Logged {} to Gallagher", detection);
    }

    public void notifyItemRegistered(String systemId, String itemId, String config) {
        log.info("Item registered: {} / {}", systemId, itemId);
        //registerLatch.countDown();
    }

    public void notifyItemDeregistered(String systemId, String itemId) {
        log.info("Item deregistered: {} / {}", systemId, itemId);
    }

    public void notifySystemRegistered(String systemId, String typeId, String config) {
        log.info("System registered: {} / {} / {}", systemId, typeId, config);
        registerLatch.countDown();
    }

    public void notifySystemDeregistered(String systemId) {
        log.info("System deregistered: {}", systemId);
    }

    public void notifyAlarmAcknowledged(String systemId, int eventId) {
        log.info("Alarm acknowledged: {} / {}", systemId, eventId);
    }
}
