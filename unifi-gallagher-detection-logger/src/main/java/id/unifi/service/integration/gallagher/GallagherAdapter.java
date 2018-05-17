package id.unifi.service.integration.gallagher;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import id.unifi.service.common.agent.ReaderHealth;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.DetectionMatch;
import id.unifi.service.common.types.client.ClientAntenna;
import id.unifi.service.provider.security.gallagher.FtcApi;
import id.unifi.service.provider.security.gallagher.IFTMiddleware2;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Pattern;

public class GallagherAdapter implements IFTMiddleware2 {
    private static final String METRIC_NAME_PREFIX = "id.unifi.service.integration.gallagher";
    private static final Duration REGISTER_MIDDLEWARE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration ESI_REGISTRATION_TIMEOUT = Duration.ofSeconds(10);

    private static final Logger log = LoggerFactory.getLogger(GallagherAdapter.class);
    private static final Pattern esiPattern = Pattern.compile("reader\\.([0-9]+)\\.([0-9]+)");

    private final Meter detectionsMeter;
    private FtcApi ftcApi;
    private final Thread processingThread;
    private final BlockingQueue<FutureTask<?>> processingQueue;
    private final FtcApiConfig config;
    private final Set<ClientAntenna> externalSystemItemReady;

    private volatile CountDownLatch registerLatch;
    private volatile boolean connected;

    public static GallagherAdapter create(MetricRegistry registry, FtcApiConfig config) {
        var adapter = new GallagherAdapter(registry, config);
        adapter.start();
        return adapter;
    }

    private GallagherAdapter(MetricRegistry registry, FtcApiConfig config) {
        this.config = config;
        this.processingQueue = new SynchronousQueue<>();
        this.externalSystemItemReady = new CopyOnWriteArraySet<>();

        registry.gauge(name(METRIC_NAME_PREFIX, "ftcapi", "health"), () -> () -> connected ? 1 : 0);
        this.detectionsMeter = registry.meter(name(METRIC_NAME_PREFIX, "detections"));

        log.info("Connecting to FTC server {}", config.server());
        this.processingThread = new Thread(() -> {
            try {
                ftcApi = new FtcApi(config.server(), config.domain(), config.username(), config.password());
                registerLatch = new CountDownLatch(1);
                log.info("Registering middleware");
                ftcApi.registerMiddleware(this);

                if (!registerLatch.await(REGISTER_MIDDLEWARE_TIMEOUT.toNanos(), NANOSECONDS)) {
                    throw new RuntimeException("Timed out while registering middleware");
                }

                connected = true;
                log.info("System registered, waiting for item notifications...");

                Thread.sleep(ESI_REGISTRATION_TIMEOUT.toMillis());
                log.info("Registered ESI antennae: {}", externalSystemItemReady);

                while (!Thread.currentThread().isInterrupted()) {
                    var currentTask = processingQueue.take();
                    currentTask.run();
                }
            } catch (Exception e) {
                log.error("Fatal error", e);
                System.exit(1);
            }
        });
    }

    private void start() {
        processingThread.start();
    }

    public void process(DetectionMatch match) throws InterruptedException {
        var future = new FutureTask<>(() -> logDetection(match), null);
        processingQueue.put(future);
        try {
            future.get();
        } catch (ExecutionException e) {
            throw e.getCause() instanceof RuntimeException ? (RuntimeException) e.getCause() : new RuntimeException(e);
        }
    }

    public void reportHealth(List<ReaderHealth> readerHealth) {
        for (var reader : readerHealth) {
            for (var entry : reader.antennaHealth.entrySet()) {
                var antenna = new ClientAntenna(reader.readerSn, entry.getKey());
                if (externalSystemItemReady.contains(antenna)) {
                    var itemId = antennaToEsi(antenna);
                    var offline = !entry.getValue();
                    var message = itemId + " is " + (offline ? "offline." : "online.");
                    ftcApi.notifyStatus(config.systemId(), itemId, 1, false, offline, message);
                }
            }
        }
    }

    private void logDetection(DetectionMatch match) {
        if (match.detection.detectable.detectableType != DetectableType.UHF_TID) return;

        var antenna = new ClientAntenna(match.detection.readerSn, match.detection.portNumber);
        if (!externalSystemItemReady.contains(antenna)) {
            throw new RuntimeException("External system item for " + antenna + " not registered");
        }

        var eventId = 0; // correlation ID; 0 for none
        var detection = match.detection;
        var detectableId = detection.detectable.detectableId;
        final var hasRestoral = false;

        var cardNumberFormatType = 2;
        var itemId = antennaToEsi(antenna);

        log.trace("Logging {} to Gallagher", detection);

        ftcApi.logLongCardEvent2(
                config.eventType(),
                eventId,
                detection.detectionTime,
                hasRestoral,
                cardNumberFormatType,
                detectableId,
                config.facilityCode(),
                config.systemId(),
                itemId,
                String.format("UHF-TID card %s detected on %s", detectableId, itemId),
                "No details.");
        log.trace("Logged {} to Gallagher", detection);
        detectionsMeter.mark();
    }

    public void notifyItemRegistered(String systemId, String itemId, String config) {
        var antenna = esiToAntenna(itemId);
        if (antenna != null) {
            externalSystemItemReady.add(antenna);
        } else {
            log.warn("Unknown external system item: {}", itemId);
        }
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

    private static ClientAntenna esiToAntenna(String itemId) {
        var matcher = esiPattern.matcher(itemId);
        return matcher.find()
                ? new ClientAntenna(matcher.group(1), Integer.parseInt(matcher.group(2)))
                : null;
    }

    private static String antennaToEsi(ClientAntenna antenna) {
        return String.format("reader.%s.%d", antenna.readerSn, antenna.portNumber);
    }
}
