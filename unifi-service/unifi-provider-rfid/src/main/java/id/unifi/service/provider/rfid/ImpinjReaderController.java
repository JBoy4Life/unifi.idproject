package id.unifi.service.provider.rfid;

import com.google.common.net.HostAndPort;
import com.impinj.octane.FeatureSet;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.ImpinjTimestamp;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.Settings;
import com.impinj.octane.TagReportListener;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.RawDetection;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.detection.ReaderConfig;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ImpinjReaderController {
    private static final Logger log = LoggerFactory.getLogger(ImpinjReaderController.class);
    private final TagReportListener impinjTagReportListener;
    private final ReaderConfig config;
    private final ExecutorService configExecutor;
    private volatile ImpinjReader reader;
    private volatile boolean closing;

    ImpinjReaderController(ReaderConfig config, Consumer<RawDetectionReport> detectionConsumer) {
        this.config = config;
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
            detectionConsumer.accept(new RawDetectionReport(reader.getName(), detections));
        };

        this.configExecutor = Executors.newSingleThreadExecutor();
        configExecutor.submit(this::configureFromScratch);
    }

    private synchronized void configureFromScratch() {
        log.info("Configuring reader {}", config);
        HostAndPort endpoint = config.endpoint;

        this.reader = new ImpinjReader();
        reader.setConnectionLostListener(r -> onConnectionLost());
        reader.setConnectionCloseListener((r, e) -> onConnectionLost());
        
        FeatureSet featureSet;
        while (true) {
            try {
                reader.connect(endpoint.getHost(), endpoint.getPort());
                featureSet = reader.queryFeatureSet();
                String actualSn = featureSet.getSerialNumber().replaceAll("-", "");
                if (!config.readerSn.equals(actualSn)) {
                    throw new RuntimeException("Reader serial number mismatch for " + endpoint
                            + ": Expected " + config.readerSn + ", got " + actualSn);
                }

                reader.setName(config.readerSn);

                Settings settings = reader.queryDefaultSettings();

                settings.getLowDutyCycle().setIsEnabled(false);
                settings.setHoldReportsOnDisconnect(true);

                ReportConfig reportConfig = settings.getReport();
                reportConfig.setIncludeAntennaPortNumber(true);
                reportConfig.setIncludePeakRssi(true);
                reportConfig.setIncludeLastSeenTime(true);
                reportConfig.setMode(ReportMode.Individual);

                settings.getAntennas().disableAll();
                settings.getAntennas().enableById(Arrays.stream(config.enabledAntennae).boxed().collect(toList()));

                reader.applySettings(settings);
                reader.setTagReportListener(impinjTagReportListener);
                log.info("Starting detection on {}", config);
                reader.start();
                break;
            } catch (Exception e) {
                log.error("Error configuring reader. Retrying shortly.", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    public void close() {
        closing = true;
        configExecutor.shutdownNow();
        try {
            configExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        try {
            if (reader != null) {
                log.info("Stopping reader {}", config);
                reader.stop();
                reader.disconnect();
                reader = null;
            }
        } catch (OctaneSdkException ignored) {}
    }

    private void onConnectionLost() {
        if (!closing) {
            log.info("Connection to reader {} lost, reconnecting", config);
            configExecutor.submit(this::configureFromScratch);
        }
    }

    private static Instant instantFromTimestamp(ImpinjTimestamp timestamp) {
        // This is horrible but Octane "exposes" the full microsecond resolution only as a stringy long
        long microsecondsSinceEpoch = Long.parseLong(timestamp.ToString());
        long nanoAdjustment = (microsecondsSinceEpoch % 1_000_000) * 1000;
        return Instant.ofEpochSecond(microsecondsSinceEpoch / 1_000_000, nanoAdjustment);
    }
}
