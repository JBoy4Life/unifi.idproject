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
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ImpinjReaderController {
    private static final Logger log = LoggerFactory.getLogger(ImpinjReaderController.class);
    private final TagReportListener impinjTagReportListener;
    private final ReaderConfig config;
    private final Thread connectionThread;
    private final CountDownLatch connectionCloseLatch;
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

        this.connectionCloseLatch = new CountDownLatch(1);

        connectionThread = new Thread(this::configureFromScratch, "reader-" + config.readerSn + "-connection");
        connectionThread.start();
    }

    private synchronized void configureFromScratch() {
        HostAndPort endpoint = config.endpoint;

        while (!closing) {
            ImpinjReader reader = new ImpinjReader();
            CountDownLatch lostLatch = new CountDownLatch(1);
            try {
                log.info("Configuring reader {}", config);
                reader.setConnectionLostListener(r -> lostLatch.countDown());
                reader.setConnectionCloseListener((r, e) -> log.info("Connection closed {} {}", config));
                reader.connect(endpoint.getHost(), endpoint.getPort());
                if (Thread.interrupted()) throw new InterruptedException();

                FeatureSet featureSet = reader.queryFeatureSet();
                String actualSn = featureSet.getSerialNumber().replaceAll("-", "");
                if (!config.readerSn.equals(actualSn)) {
                    throw new RuntimeException("Reader serial number mismatch for " + endpoint
                            + ": Expected " + config.readerSn + ", got " + actualSn);
                }

                reader.setName(config.readerSn);

                Settings settings = reader.queryDefaultSettings();

                settings.getLowDutyCycle().setIsEnabled(false);
                settings.setHoldReportsOnDisconnect(true);
                settings.getKeepalives().setEnabled(true);
                settings.getKeepalives().setPeriodInMs(10_000);

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
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    public void close() {
        closing = true;
        log.info("Disconnecting from reader {}", config);
        connectionThread.interrupt();
        try {
            connectionCloseLatch.await();
        } catch (InterruptedException ignored) {}
    }

    private static Instant instantFromTimestamp(ImpinjTimestamp timestamp) {
        // This is horrible but Octane "exposes" the full microsecond resolution only as a stringy long
        long microsecondsSinceEpoch = Long.parseLong(timestamp.ToString());
        long nanoAdjustment = (microsecondsSinceEpoch % 1_000_000) * 1000;
        return Instant.ofEpochSecond(microsecondsSinceEpoch / 1_000_000, nanoAdjustment);
    }
}
