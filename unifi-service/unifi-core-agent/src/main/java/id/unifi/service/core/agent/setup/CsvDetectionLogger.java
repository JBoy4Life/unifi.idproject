package id.unifi.service.core.agent.setup;

import com.opencsv.CSVWriter;
import id.unifi.service.common.detection.SiteDetectionReport;
import static id.unifi.service.common.util.TimeUtils.UNIX_TIMESTAMP;
import static id.unifi.service.common.util.TimeUtils.filenameFormattedLocalDateTimeNow;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class CsvDetectionLogger implements DetectionLogger {
    private static final Logger log = LoggerFactory.getLogger(CsvDetectionLogger.class);

    private static final DecimalFormat microDecimalFormat = new DecimalFormat("#.######");
    private static final String[] HEADERS = {
            "iso_time", "unix_time", "reader_sn", "port_number", "detectable_id", "detectable_type", "rssi", "count"
    };

    @Nullable
    private final CSVWriter writer;

    public CsvDetectionLogger() {
        writer = createWriter(Paths.get(String.format("detections_%s.csv", filenameFormattedLocalDateTimeNow())));
    }

    public void log(SiteDetectionReport report) {
        if (writer == null) return;
        try {
            report.detections.forEach(d -> writer.writeNext(new String[]{
                    d.detectionTime.toString(),
                    microDecimalFormat.format(d.detectionTime.query(UNIX_TIMESTAMP)),
                    report.readerSn,
                    Integer.toString(d.portNumber),
                    d.detectable.detectableId,
                    d.detectable.detectableType.toString(),
                    d.rssi.toString(),
                    Integer.toString(d.count)
            }));
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to write detections to file.", e);
        }
    }

    public void close() throws IOException {
        if (writer != null) writer.close();
    }

    @Nullable
    private CSVWriter createWriter(Path logFilePath) {
        try {
            log.info("Writing detections to {}", logFilePath);
            var writer = new CSVWriter(Files.newBufferedWriter(logFilePath, UTF_8, WRITE, CREATE));
            writer.writeNext(HEADERS);
            return writer;
        } catch (IOException e) {
            log.warn("Error opening {}, can't log detections.", logFilePath, e);
            return null;
        }
    }
}
