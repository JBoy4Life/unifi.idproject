package id.unifi.service.provider.rfid;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.Uninterruptibles;
import com.impinj.octane.*;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.RawDetection;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.detection.ReaderConfig;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ImpinjReaderController implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(ImpinjReaderController.class);

    private static final int KEEPALIVE_INTERVAL_MILLIS = 10_000;
    private static final int MAX_HEALTHY_KEEPALIVE_INTERVAL_MILLIS = 20_000;
    private static final String METRIC_NAME_PREFIX = "id.unifi.service.rfid-provider";
    private static final int INTERVAL_BETWEEN_RECONNECTIONS_MILLIS = 5_000;

    private final TagReportListener impinjTagReportListener;
    private final ReaderConfig config;
    private final Thread connectionThread;
    private final CountDownLatch connectionCloseLatch;
    private final MetricRegistry registry;

    private final List<String> metricNames;
    private final Map<Integer, Meter> antennaDetectionMeters;
    private final Map<Integer, Boolean> antennaConnected;

    private volatile boolean closing; // writes on connection thread
    private volatile long lastKeepaliveMillis; // writes on connection thread

    ImpinjReaderController(ReaderConfig config,
                           Consumer<RawDetectionReport> detectionConsumer,
                           MetricRegistry registry) {
        this.config = config;
        this.registry = registry;
        this.antennaConnected = new ConcurrentHashMap<>(
                Arrays.stream(config.enabledAntennae).boxed().collect(toMap(n -> n, n -> false)));

        // Register health and detection rate metrics
        List<String> metricNames = new ArrayList<>();

        String readerHealthMetricName = name(METRIC_NAME_PREFIX, "reader", config.readerSn, "health");
        registry.gauge(readerHealthMetricName, () -> () -> checkReaderHealth() ? 1 : 0);
        metricNames.add(readerHealthMetricName);

        Map<Integer, Meter> antennaDetectionMeters = new HashMap<>();
        for (int portNumber : config.enabledAntennae) {
            String antennaName = config.readerSn + "_" + portNumber;
            String detectionsMetricName = name(METRIC_NAME_PREFIX, "antenna", antennaName, "detections");
            antennaDetectionMeters.put(portNumber, registry.meter(detectionsMetricName));
            metricNames.add(detectionsMetricName);

            String antennaHealthMetricName = name(METRIC_NAME_PREFIX, "antenna", antennaName, "health");
            registry.gauge(antennaHealthMetricName, () -> () -> checkAntennaHealth(portNumber) ? 1 : 0);
            metricNames.add(antennaHealthMetricName);
        }
        this.antennaDetectionMeters = Collections.unmodifiableMap(antennaDetectionMeters);
        this.metricNames = Collections.unmodifiableList(metricNames);

        this.impinjTagReportListener = (reader, report) -> {
            log.trace("Report received {}: {} tags", reader.getAddress(), report.getTags().size());
            List<RawDetection> detections = report.getTags().stream().map(tag ->
                    new RawDetection(
                            instantFromTimestamp(tag.getLastSeenTime()),
                            tag.getAntennaPortNumber(),
                            tag.getEpc().toHexString(),
                            DetectableType.UHF_EPC,
                            tag.getPeakRssiInDbm()))
                    .collect(toList());
            detections.forEach(d -> this.antennaDetectionMeters.get(d.portNumber).mark());
            detectionConsumer.accept(new RawDetectionReport(reader.getName(), detections));
        };

        this.connectionCloseLatch = new CountDownLatch(1);

        connectionThread = new Thread(this::configureFromScratch, "reader-" + config.readerSn + "-connection");
        connectionThread.start();
    }

    public void close() {
        closing = true;
        metricNames.forEach(registry::remove);
        log.info("Disconnecting from reader {}", config);
        connectionThread.interrupt();
        Uninterruptibles.awaitUninterruptibly(connectionCloseLatch);
    }

    private synchronized void configureFromScratch() {
        HostAndPort endpoint = config.endpoint;

        while (!closing) {
            antennaConnected.replaceAll((n, c) -> false);
            ImpinjReader reader = new ImpinjReader();
            CountDownLatch lostLatch = new CountDownLatch(1);
            try {
                log.info("Configuring reader {}", config);
                reader.setConnectionLostListener(r -> lostLatch.countDown());
                reader.setConnectionCloseListener((r, e) -> log.info("Connection closed {} {}", config));
                reader.setAntennaChangeListener((r, e) -> onAntennaEvent(e));
                reader.setKeepaliveListener((r, e) -> lastKeepaliveMillis = System.currentTimeMillis());
                reader.setTagReportListener(impinjTagReportListener);

                reader.connect(endpoint.getHost(), endpoint.getPort());
                lastKeepaliveMillis = System.currentTimeMillis();
                if (Thread.interrupted()) throw new InterruptedException();

                updateAntennaStatus(reader);
                checkSerialNumber(reader);
                reader.setName(config.readerSn);
                applySettings(reader);

                log.info("Starting detection on {}", config);
                reader.start();
                lostLatch.await();
                log.info("Lost connection to reader {}", config);
                reader.disconnect();
                log.info("Disconnected from reader {}", config);
            } catch (InterruptedException e) {
                log.info("Stopping reader {}", config);
                try {
                    reader.stop();
                } catch (OctaneSdkException e1) {
                    log.error("Error while stopping reader {}", reader, e);
                }
                reader.disconnect();
                log.info("Reader disconnected {}", config);
                connectionCloseLatch.countDown();
                return;
            } catch (Exception e) {
                if (closing) {
                    log.error("Error while disconnecting from reader {}", reader, e);
                    connectionCloseLatch.countDown();
                    return;
                }

                log.error("Reader error. Retrying shortly.", e);
                try {
                    Thread.sleep(INTERVAL_BETWEEN_RECONNECTIONS_MILLIS);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    private void applySettings(ImpinjReader reader) throws OctaneSdkException {
        Settings settings = reader.queryDefaultSettings();

        settings.getLowDutyCycle().setIsEnabled(false);
        settings.setHoldReportsOnDisconnect(true);
        settings.getKeepalives().setEnabled(true);
        settings.getKeepalives().setPeriodInMs(KEEPALIVE_INTERVAL_MILLIS);

        ReportConfig reportConfig = settings.getReport();
        reportConfig.setIncludeAntennaPortNumber(true);
        reportConfig.setIncludePeakRssi(true);
        reportConfig.setIncludeLastSeenTime(true);
        reportConfig.setMode(ReportMode.Individual);

        settings.getAntennas().disableAll();
        settings.getAntennas().enableById(Arrays.stream(config.enabledAntennae).boxed().collect(toList()));

        reader.applySettings(settings);
    }

    private void onAntennaEvent(AntennaEvent e) {
        boolean connected;
        switch (e.getState()) {
            case AntennaConnected:
                log.info("Antenna {} reconnected to reader {}.", e.getPortNumber(), config.readerSn);
                connected = true;
                break;

            case AntennaDisconnected:
                log.info("Antenna {} disconnected from reader {}.", e.getPortNumber(), config.readerSn);
                connected = false;
                break;

            default:
                throw new AssertionError();
        }
        antennaConnected.put((int) e.getPortNumber(), connected);
    }

    private void checkSerialNumber(ImpinjReader reader) throws OctaneSdkException {
        FeatureSet featureSet = reader.queryFeatureSet();
        String actualSn = featureSet.getSerialNumber().replaceAll("-", "");
        if (!config.readerSn.equals(actualSn)) {
            throw new RuntimeException("Reader serial number mismatch for " + config.endpoint
                    + ": Expected " + config.readerSn + ", got " + actualSn);
        }
    }

    private void updateAntennaStatus(ImpinjReader reader) throws OctaneSdkException {
        List<AntennaStatus> antennaList = reader.queryStatus().getAntennaStatusGroup().getAntennaList();
        antennaList.forEach(status -> {
            int portNumber = status.getPortNumber();
            if (antennaConnected.containsKey(portNumber)) {
                if (!status.isConnected()) log.warn("Antenna {} on reader {} not connected.");
                antennaConnected.put(portNumber, status.isConnected());
            } else if (status.isConnected()) {
                log.warn("Antenna {} on reader {} isn't configured.", portNumber, config.readerSn);
            }
        });
    }

    private boolean checkAntennaHealth(int portNumber) {
        return checkReaderHealth() && antennaConnected.get(portNumber);
    }

    private boolean checkReaderHealth() {
        return System.currentTimeMillis() - lastKeepaliveMillis < MAX_HEALTHY_KEEPALIVE_INTERVAL_MILLIS;
    }

    private static Instant instantFromTimestamp(ImpinjTimestamp timestamp) {
        // This is horrible but Octane "exposes" the full microsecond resolution only as a stringy long
        long microsecondsSinceEpoch = Long.parseLong(timestamp.ToString());
        long nanoAdjustment = (microsecondsSinceEpoch % 1_000_000) * 1000;
        return Instant.ofEpochSecond(microsecondsSinceEpoch / 1_000_000, nanoAdjustment);
    }
}
