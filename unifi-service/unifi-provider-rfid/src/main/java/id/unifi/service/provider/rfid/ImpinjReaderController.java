package id.unifi.service.provider.rfid;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.Uninterruptibles;
import com.impinj.octane.*;
import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.common.detection.SiteRfidDetection;
import id.unifi.service.common.types.client.ClientDetectable;
import id.unifi.service.provider.rfid.config.ReaderConfig;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ImpinjReaderController implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(ImpinjReaderController.class);

    private static final int DEFAULT_LLRP_PORT = 5084;
    private static final int KEEPALIVE_INTERVAL_MILLIS = 10_000;
    private static final int MAX_HEALTHY_KEEPALIVE_INTERVAL_MILLIS = 20_000;
    private static final String METRIC_NAME_PREFIX = "id.unifi.service.rfid-provider";
    private static final int INTERVAL_BETWEEN_RECONNECTIONS_MILLIS = 5_000;

    private final TagReportListener impinjTagReportListener;
    private final ReaderFullConfig<ReaderConfig> fullConfig;
    private final ReaderConfig config;
    private final Thread connectionThread;
    private final CountDownLatch connectionCloseLatch;
    private final MetricRegistry registry;

    private final Set<String> metricNames;
    private final Map<Integer, Boolean> antennaConnected;
    private final HostAndPort endpoint;
    private final String readerName;

    private volatile boolean closing; // writes on connection thread
    private volatile long lastKeepaliveMillis; // writes on connection thread

    ImpinjReaderController(ReaderFullConfig<ReaderConfig> fullConfig,
                           Consumer<SiteDetectionReport> detectionConsumer,
                           MetricRegistry registry) {
        this.fullConfig = fullConfig;
        this.config = fullConfig.config.orElse(ReaderConfig.empty);
        this.registry = registry;
        var configuredPortNumbers = config.ports.map(Map::keySet).orElse(Set.of());
        this.antennaConnected = new ConcurrentHashMap<>(
                configuredPortNumbers.stream().collect(toMap(n -> n, n -> false)));
        this.endpoint = fullConfig.endpoint.get().withDefaultPort(DEFAULT_LLRP_PORT);
        this.readerName = fullConfig.readerSn.orElse("?") + "/" + endpoint;

        // Register health and detection rate metrics
        this.metricNames = new HashSet<>();

        fullConfig.readerSn.ifPresent(readerSn -> {
            var readerHealthMetricName = name(METRIC_NAME_PREFIX, "reader", readerSn, "health");
            registry.gauge(readerHealthMetricName, () -> () -> checkReaderHealth() ? 1 : 0);
            metricNames.add(readerHealthMetricName);
        });

        Map<Integer, Meter> antennaDetectionMeters = new HashMap<>();
        for (int portNumber : configuredPortNumbers) {
            antennaDetectionMeters.put(portNumber, createAntennaDetectionMeter(portNumber));

            var antennaName = getAntennaMetricNameElement(portNumber);
            var antennaHealthMetricName = name(METRIC_NAME_PREFIX, "antenna", antennaName, "health");
            registry.gauge(antennaHealthMetricName, () -> () -> checkAntennaHealth(portNumber) ? 1 : 0);
            metricNames.add(antennaHealthMetricName);
        }

        this.impinjTagReportListener = (reader, report) -> {
            log.trace("Report received {}: {} tags", reader.getAddress(), report.getTags().size());
            try {
                var detections = report.getTags().stream().flatMap(tag -> {
                    var timestamp = instantFromTimestamp(tag.getLastSeenTime());
                    var rssi = Optional.of(tag.getPeakRssiInDbm())
                            .filter(r -> tag.isPeakRssiInDbmPresent() && r < 0.0 && r > -1000.0)
                            .map(BigDecimal::valueOf);

                    var epcDetection = new SiteRfidDetection(
                            timestamp,
                            tag.getAntennaPortNumber(),
                            new ClientDetectable(tag.getEpc().toHexString(), DetectableType.UHF_EPC),
                            rssi,
                            tag.getTagSeenCount());

                    var tidDetection = !tag.isFastIdPresent() ? null : new SiteRfidDetection(
                            timestamp,
                            tag.getAntennaPortNumber(),
                            new ClientDetectable(tag.getTid().toHexString(), DetectableType.UHF_TID),
                            rssi,
                            tag.getTagSeenCount());
                    return Stream.of(epcDetection, tidDetection).filter(Objects::nonNull);
                }).collect(toList());

                detectionConsumer.accept(new SiteDetectionReport(reader.getName(), detections));
                detections.forEach(d -> {
                    var meter = antennaDetectionMeters.get(d.portNumber);
                    if (meter != null) meter.mark(d.count);
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
            var reader = new ImpinjReader();
            var lostLatch = new CountDownLatch(1);
            boolean disconnectedOperation = config.disconnectedOperation.orElse(false);
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
                var featureSet = reader.queryFeatureSet();
                var readerSn = checkSerialNumber(featureSet);
                reader.setName(readerSn);

                if (disconnectedOperation) {
                    log.info("Attempting to resume from disconnected operation");
                    reader.resumeEventsAndReports();
                }

                log.info("Starting detection on {}/{}", readerName, featureSet.getModelName());
                applySettings(reader);

                lostLatch.await();
                log.info("Lost connection to reader {}", fullConfig);
                reader.disconnect();
                log.info("Disconnected from reader {}", fullConfig);
            } catch (InterruptedException e) {
                log.info("Stopping reader {}", fullConfig);
                try {
                    if (!disconnectedOperation) reader.stop();
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
        var settings = reader.queryDefaultSettings();

        settings.getLowDutyCycle().setIsEnabled(false);
        settings.setHoldReportsOnDisconnect(config.disconnectedOperation.orElse(false));
        settings.getKeepalives().setEnabled(true);
        settings.getKeepalives().setPeriodInMs(KEEPALIVE_INTERVAL_MILLIS);

        config.readerMode.ifPresent(settings::setReaderMode);
        config.searchMode.ifPresent(settings::setSearchMode);
        config.session.ifPresent(settings::setSession);
        config.tagPopulationEstimate.ifPresent(settings::setTagPopulationEstimate);
        config.txFrequencies.ifPresent(freqs -> settings.setTxFrequenciesInMhz(new ArrayList<>(freqs)));
        config.filter.ifPresent(filter -> {
            var filters = new FilterSettings();
            var tagFilter = new TagFilter();
            tagFilter.setBitPointer(BitPointers.Epc);
            tagFilter.setMemoryBank(MemoryBank.Epc);
            tagFilter.setFilterOp(filter.action);
            tagFilter.setBitCount(filter.detectableIdPrefix.length() * 4);
            tagFilter.setTagMask(filter.detectableIdPrefix);
            filters.setMode(TagFilterMode.OnlyFilter1);
            filters.setTagFilter1(tagFilter);
            settings.setFilters(filters);
        });

        var reportConfig = settings.getReport();
        config.enableFastId.ifPresent(reportConfig::setIncludeFastId);
        reportConfig.setIncludeAntennaPortNumber(true);
        reportConfig.setIncludePeakRssi(true);
        reportConfig.setIncludeLastSeenTime(true);
        reportConfig.setIncludeSeenCount(true);
        reportConfig.setMode(ReportMode.Individual);

        if (config.ports.isPresent()) {
            settings.getAntennas().disableAll();
            settings.getAntennas().enableById(new ArrayList<>(config.ports.get().keySet()));
            for (var e : config.ports.get().entrySet()) {
                int portNumber = e.getKey();
                var antennaConfig = e.getValue();
                var antenna = settings.getAntennas().getAntenna(portNumber);
                antenna.setEnabled(true);
                antenna.setIsMaxTxPower(false);
                antenna.setIsMaxRxSensitivity(false);
                antennaConfig.txPower.ifPresentOrElse(antenna::setTxPowerinDbm,
                        () -> antenna.setIsMaxTxPower(true));
                antennaConfig.rxSensitivity.ifPresentOrElse(antenna::setRxSensitivityinDbm,
                        () -> antenna.setIsMaxRxSensitivity(true));
            }
        }

        settings.getAutoStart().setMode(AutoStartMode.Immediate);

        reader.applySettings(settings);
        reader.saveSettings();
    }

    private void onAntennaEvent(AntennaEvent e) {
        boolean connected;
        switch (e.getState()) {
            case AntennaConnected:
                log.info("Antenna {} reconnected to reader {}.", e.getPortNumber(), readerName);
                connected = true;
                break;

            case AntennaDisconnected:
                log.info("Antenna {} disconnected from reader {}.", e.getPortNumber(), readerName);
                connected = false;
                break;

            default:
                throw new AssertionError();
        }
        antennaConnected.put((int) e.getPortNumber(), connected);
    }

    private Meter createAntennaDetectionMeter(int portNumber) {
        var antennaName = getAntennaMetricNameElement(portNumber);
        var detectionsMetricName = name(METRIC_NAME_PREFIX, "antenna", antennaName, "detections");
        var meter = registry.meter(detectionsMetricName);
        metricNames.add(detectionsMetricName);
        return meter;
    }

    private String getAntennaMetricNameElement(int portNumber) {
        return fullConfig.readerSn.orElse(endpoint.toString()) + "_" + portNumber;
    }

    private String checkSerialNumber(FeatureSet featureSet) {
        var actualSn = featureSet.getSerialNumber().replaceAll("-", "");
        if (!fullConfig.readerSn.stream().allMatch(actualSn::equals)) {
            throw new RuntimeException("Reader serial number mismatch for " + fullConfig.endpoint
                    + ": Expected " + fullConfig.readerSn.get() + ", got " + actualSn);
        }
        return actualSn;
    }

    private void updateAntennaStatus(ImpinjReader reader) throws OctaneSdkException {
        var antennaList = reader.queryStatus().getAntennaStatusGroup().getAntennaList();
        var startingFromEmpty = antennaConnected.isEmpty();
        antennaList.forEach(status -> {
            int portNumber = status.getPortNumber();
            if (startingFromEmpty || antennaConnected.containsKey(portNumber)) {
                if (!status.isConnected()) {
                    log.warn("Antenna {} on reader {} not connected.", portNumber, readerName);
                }
                antennaConnected.put(portNumber, status.isConnected());
            } else if (status.isConnected()) {
                log.warn("Antenna {} on reader {} isn't configured.", portNumber, readerName);
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
        var microsecondsSinceEpoch = Long.parseLong(timestamp.ToString());
        var nanoAdjustment = (microsecondsSinceEpoch % 1_000_000) * 1000;
        return Instant.ofEpochSecond(microsecondsSinceEpoch / 1_000_000, nanoAdjustment);
    }
}
