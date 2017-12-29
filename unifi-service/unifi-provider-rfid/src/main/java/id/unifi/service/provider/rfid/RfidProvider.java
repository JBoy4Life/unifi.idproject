package id.unifi.service.provider.rfid;

import com.google.common.collect.Multimap;
import com.google.common.net.HostAndPort;
import com.impinj.octane.*;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RfidProvider {
    private static final Logger log = LoggerFactory.getLogger(RfidProvider.class);

    private final TagReportListener impinjTagReportListener;

    public RfidProvider(Consumer<RfidDetectionReport> detectionConsumer) {
        this.impinjTagReportListener = (reader, report) -> {
            log.info("Report received {}: {} tags", reader.getAddress(), report.getTags().size());
            List<RfidDetection> detections = report.getTags().stream().map(tag ->
                    new RfidDetection(
                            instantFromTimestamp(tag.getLastSeenTime()),
                            tag.getAntennaPortNumber(),
                            tag.getEpc().toHexString(),
                            tag.getPeakRssiInDbm()))
                    .collect(toList());
            detectionConsumer.accept(new RfidDetectionReport(reader.getName(), detections));
        };
    }

    public void startDetecting(Multimap<String, Integer> readersAndAntennaPorts, Map<String, HostAndPort> endpoints) {
        for (Map.Entry<String, HostAndPort> readerSpec : endpoints.entrySet()) {
            String serialNumber = readerSpec.getKey();
            HostAndPort endpoint = readerSpec.getValue();

            ImpinjReader reader = new ImpinjReader();
            FeatureSet featureSet;
            try {
                reader.connect(endpoint.getHost(), endpoint.getPort());
                featureSet = reader.queryFeatureSet();
                String queriedSerialNumber = featureSet.getSerialNumber();
                if (!serialNumber.equals(queriedSerialNumber)) {
                    throw new RuntimeException("Serial number mismatch for " + endpoint
                            + ": Expected " + serialNumber + ", got " + queriedSerialNumber);
                }
            } catch (OctaneSdkException e) {
                throw new RuntimeException("Error connecting to reader", e);
            }

            serialNumber = featureSet.getSerialNumber();
            reader.setName(serialNumber);


            Settings settings = reader.queryDefaultSettings();

            settings.getLowDutyCycle().setIsEnabled(false);

            ReportConfig reportConfig = settings.getReport();
            reportConfig.setIncludeAntennaPortNumber(true);
            reportConfig.setIncludePeakRssi(true);
            reportConfig.setIncludeLastSeenTime(true);
            reportConfig.setMode(ReportMode.Individual);

            settings.getAntennas().disableAll();
            try {
                List<Integer> integers = new ArrayList<>(readersAndAntennaPorts.get(serialNumber));
                settings.getAntennas().enableById(integers);
            } catch (OctaneSdkException e) {
                e.printStackTrace();
            }

            try {
                reader.applySettings(settings);
            } catch (OctaneSdkException e) {
                throw new RuntimeException("Failed to apply reader settings", e);
            }

            reader.setTagReportListener(impinjTagReportListener);
            try {
                reader.start();
            } catch (OctaneSdkException e) {
                throw new RuntimeException("Failed to start reader", e);
            }
        }
    }

    private static Instant instantFromTimestamp(ImpinjTimestamp timestamp) {
        // This is horrible but Octane "exposes" the full microsecond resolution only as a stringy long
        long microsecondsSinceEpoch = Long.parseLong(timestamp.ToString());
        long nanoAdjustment = (microsecondsSinceEpoch % 1_000_000) * 1000;
        return Instant.ofEpochSecond(microsecondsSinceEpoch / 1_000_000, nanoAdjustment);
    }
}
