package id.unifi.service.core.agent.setup;

import com.opencsv.CSVWriter;
import id.unifi.service.common.detection.RawDetectionReport;
import static id.unifi.service.common.util.TimeUtils.getFormattedLocalDateTimeNow;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CsvDetectionLogger implements DetectionLogger {
    private static final Logger log = LoggerFactory.getLogger(CsvDetectionLogger.class);

    @Nullable
    private final CSVWriter writer;

    public CsvDetectionLogger() {
        writer = createWriter(Paths.get(String.format("detections_%s.csv", getFormattedLocalDateTimeNow())));
    }

    public void log(RawDetectionReport report) {
        if (writer == null) return;
        try {
            report.detections.forEach(d -> writer.writeNext(new String[]{
                    d.timestamp.toString(),
                    report.readerSn,
                    Integer.toString(d.portNumber),
                    d.detectableId,
                    d.detectableType.toString(),
                    Double.toString(d.rssi)
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
            log.info("Appending detections to {}", logFilePath);
            return new CSVWriter(Files.newBufferedWriter(logFilePath, UTF_8, WRITE, APPEND, CREATE));
        } catch (IOException e) {
            log.warn("Error opening {}, can't log detections.", logFilePath, e);
            return null;
        }
    }
}
