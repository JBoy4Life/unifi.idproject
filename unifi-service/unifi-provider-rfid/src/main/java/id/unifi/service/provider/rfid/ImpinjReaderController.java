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
import id.unifi.service.provider.rfid.config.ReaderConfig;
import id.unifi.service.provider.rfid.config.ReaderFullConfig;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ReaderFullConfig fullConfig;
    private final ReaderConfig config;
    private final Thread connectionThread;
    private final CountDownLatch connectionCloseLatch;
    private final MetricRegistry registry;

    private final Set<String> metricNames;
    private final Map<Integer, Boolean> antennaConnected;
    private final HostAndPort endpoint;

    private volatile boolean closing; // writes on connection thread
    private volatile long lastKeepaliveMillis; // writes on connection thread

    ImpinjReaderController(ReaderFullConfig fullConfig,
                           Consumer<RawDetectionReport> detectionConsumer,
                           MetricRegistry registry) {
        this.fullConfig = fullConfig;
        this.config = fullConfig.config.orElse(ReaderConfig.empty);
        this.registry = registry;
        Set<Integer> configuredPortNumbers = config.ports.map(Map::keySet).orElse(Set.of());
        this.antennaConnected = new ConcurrentHashMap<>(
                configuredPortNumbers.stream().collect(toMap(n -> n, n -> false)));
        this.endpoint = fullConfig.endpoint.get();

        // Register health and detection rate metrics
        this.metricNames = new HashSet<>();

        fullConfig.readerSn.ifPresent(readerSn -> {
            String readerHealthMetricName = name(METRIC_NAME_PREFIX, "reader", readerSn, "health");
            registry.gauge(readerHealthMetricName, () -> () -> checkReaderHealth() ? 1 : 0);
            metricNames.add(readerHealthMetricName);
        });

        Map<Integer, Meter> antennaDetectionMeters = new HashMap<>();
        for (int portNumber : configuredPortNumbers) {
            antennaDetectionMeters.put(portNumber, createAntennaDetectionMeter(portNumber));

            String antennaName = getAntennaMetricNameElement(portNumber);
            String antennaHealthMetricName = name(METRIC_NAME_PREFIX, "antenna", antennaName, "health");
            registry.gauge(antennaHealthMetricName, () -> () -> checkAntennaHealth(portNumber) ? 1 : 0);
            metricNames.add(antennaHealthMetricName);
        }

        this.impinjTagReportListener = (reader, report) -> {
            log.trace("Report received {}: {} tags", reader.getAddress(), report.getTags().size());
            try {

                List<RawDetection> detections = report.getTags().stream().map(tag ->
                        new RawDetection(
                                instantFromTimestamp(tag.getLastSeenTime()),
                                tag.getAntennaPortNumber(),
                                tag.getEpc().toHexString(),
                                DetectableType.UHF_EPC,
                                tag.getPeakRssiInDbm()))
                        .collect(toList());
                detectionConsumer.accept(new RawDetectionReport(reader.getName(), detections));
                detections.forEach(d -> {
                    Meter meter = antennaDetectionMeters.get(d.portNumber);
                    if (meter != null) meter.mark();
                });
            } catch (RuntimeException e) {
                log.error("Error while processing detection", e);
            }
        };

        this.connectionCloseLatch = new CountDownLatch(1);

        connectionThread = new Thread(this::configureFromScratch, "reader-" + fullConfig.readerSn + "-connection");
        connectionThread.start();
    }

    public void close() {
        // TODO: Synchronize properly
        closing = true;
        metricNames.forEach(registry::remove);
        log.info("Disconnecting from reader {}", fullConfig);
        connectionThread.interrupt();
        Uninterruptibles.awaitUninterruptibly(connectionCloseLatch);
    }

    private synchronized void configureFromScratch() {
        while (!closing) {
            antennaConnected.replaceAll((n, c) -> false);
            ImpinjReader reader = new ImpinjReader();
            CountDownLatch lostLatch = new CountDownLatch(1);
            try {
                log.info("Configuring reader {}", fullConfig);
                reader.setConnectionLostListener(r -> lostLatch.countDown());
                reader.setConnectionCloseListener((r, e) -> log.info("Connection closed {} {}", fullConfig));
                reader.setAntennaChangeListener((r, e) -> onAntennaEvent(e));
                reader.setKeepaliveListener((r, e) -> lastKeepaliveMillis = System.currentTimeMillis());
                reader.setTagReportListener(impinjTagReportListener);

                reader.connect(endpoint.getHost(), endpoint.getPort());
                lastKeepaliveMillis = System.currentTimeMillis();
                if (Thread.interrupted()) throw new InterruptedException();

                updateAntennaStatus(reader);
                String readerSn = checkSerialNumber(reader);
                reader.setName(readerSn);
                applySettings(reader);

                log.info("Starting detection on {}", fullConfig);
                reader.start();
                lostLatch.await();
                log.info("Lost connection to reader {}", fullConfig);
                reader.disconnect();
                log.info("Disconnected from reader {}", fullConfig);
            } catch (InterruptedException e) {
                log.info("Stopping reader {}", fullConfig);
                try {
                    reader.stop();
                } catch (OctaneSdkException e1) {
                    log.error("Error while stopping reader {}", reader, e);
                }
                reader.disconnect();
                log.info("Reader disconnected {}", fullConfig);
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

        if (config.ports.isPresent()) {
            settings.getAntennas().disableAll();
            settings.getAntennas().enableById(new ArrayList<>(config.ports.get().keySet()));
        }

        reader.applySettings(settings);
    }

    private void onAntennaEvent(AntennaEvent e) {
        boolean connected;
        switch (e.getState()) {
            case AntennaConnected:
                log.info("Antenna {} reconnected to reader {}.", e.getPortNumber(), fullConfig.readerSn);
                connected = true;
                break;

            case AntennaDisconnected:
                log.info("Antenna {} disconnected from reader {}.", e.getPortNumber(), fullConfig.readerSn);
                connected = false;
                break;

            default:
                throw new AssertionError();
        }
        antennaConnected.put((int) e.getPortNumber(), connected);
    }

    private Meter createAntennaDetectionMeter(int portNumber) {
        String antennaName = getAntennaMetricNameElement(portNumber);
        String detectionsMetricName = name(METRIC_NAME_PREFIX, "antenna", antennaName, "detections");
        Meter meter = registry.meter(detectionsMetricName);
        metricNames.add(detectionsMetricName);
        return meter;
    }

    private String getAntennaMetricNameElement(int portNumber) {
        return fullConfig.readerSn.orElse(endpoint.toString()) + "_" + portNumber;
    }

    private String checkSerialNumber(ImpinjReader reader) throws OctaneSdkException {
        FeatureSet featureSet = reader.queryFeatureSet();
        String actualSn = featureSet.getSerialNumber().replaceAll("-", "");
        if (!fullConfig.readerSn.stream().allMatch(actualSn::equals)) {
            throw new RuntimeException("Reader serial number mismatch for " + fullConfig.endpoint
                    + ": Expected " + fullConfig.readerSn.get() + ", got " + actualSn);
        }
        return actualSn;
    }

    private void updateAntennaStatus(ImpinjReader reader) throws OctaneSdkException {
        List<AntennaStatus> antennaList = reader.queryStatus().getAntennaStatusGroup().getAntennaList();
        boolean startingFromEmpty = antennaConnected.isEmpty();
        antennaList.forEach(status -> {
            int portNumber = status.getPortNumber();
            if (startingFromEmpty || antennaConnected.containsKey(portNumber)) {
                if (!status.isConnected()) log.warn("Antenna {} on reader {} not connected.");
                antennaConnected.put(portNumber, status.isConnected());
            } else if (status.isConnected()) {
                log.warn("Antenna {} on reader {} isn't configured.", portNumber, fullConfig.readerSn);
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
