package id.unifi.service.core.agent;

import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Nullable;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.provider.rfid.RfidProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CoreAgentService {
    private static final Logger log = LoggerFactory.getLogger(CoreAgentService.class);

    @Prefix("unifi")
    interface Config {
        String clientId();

        String siteId();

        @Default("ws://localhost:8001/agents/msgpack")
        URI serviceUri();

        @Default("false")
        boolean mockDetections();

        @Nullable
        String detectionLogFilePath();
    }

    public static void main(String[] args) {
        Config config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);

        Optional<BufferedWriter> logWriter = Optional.ofNullable(config.detectionLogFilePath()).flatMap(filePath -> {
            try {
                Path path = Paths.get(filePath);
                log.info("Appending detections to {}", path);
                return Optional.of(Files.newBufferedWriter(
                        path,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND,
                        StandardOpenOption.CREATE));
            } catch (IOException e) {
                log.error("Error opening detection log file", e);
                return Optional.empty();
            }
        });

        AtomicReference<CoreClient> client = new AtomicReference<>();
        Consumer<RawDetectionReport> detectionConsumer = report -> {
            client.get().sendRawDetections(report);
            logWriter.ifPresent(w -> {
                try {
                    w.write(report.toString());
                    w.newLine();
                    w.flush();
                } catch (IOException ignored) {}
            });
        };
        ReaderManager readerManager = config.mockDetections()
                ? new MockReaderManager(new DatabaseProvider(), config.clientId(), config.siteId(), detectionConsumer)
                : new DefaultReaderManager(new DatabaseProvider(), new RfidProvider(detectionConsumer));
        ComponentHolder componentHolder = new ComponentHolder(Map.of(ReaderManager.class, readerManager));
        client.set(new CoreClient(config.serviceUri(), config.clientId(), config.siteId(), componentHolder));
    }
}
