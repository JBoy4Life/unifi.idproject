package id.unifi.service.core.agent;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Nullable;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.core.agent.config.HexByteArrayValueParser;
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
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CoreAgentService {
    private static final Logger log = LoggerFactory.getLogger(CoreAgentService.class);

    @Prefix("unifi")
    interface Config {
        String clientId();

        @Default("default")
        String agentId();

        @Default("")
        byte[] agentPassword();

        @Default("ws://localhost:8001/agents/msgpack")
        URI serviceUri();

        @Default("false")
        boolean mockDetections();

        @Default("false")
        boolean standaloneMode();

        @Default("")
        ReaderConfigs readers();

        @Nullable
        String detectionLogFilePath();
    }

    public static void main(String[] args) {
        log.info("Starting unifi.id core agent service");

        Config config = getConfig();

        MetricRegistry registry = new MetricRegistry();
        JmxReporter jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

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
            CoreClient coreClient = client.get();
            if (coreClient != null) coreClient.sendRawDetections(report);
            logWriter.ifPresent(w -> {
                try {
                    w.write(report.toString());
                    w.newLine();
                    w.flush();
                } catch (IOException ignored) {}
            });
        };

        ReaderConfigPersistence persistence = config.standaloneMode()
                ? new ReaderConfigNoopPersistence(config.readers().readers)
                : new ReaderConfigDatabasePersistence(new DatabaseProvider(), config.readers().readers);

        ReaderManager readerManager = config.mockDetections()
                ? new MockReaderManager(persistence, config.clientId(), detectionConsumer)
                : new DefaultReaderManager(persistence, new RfidProvider(detectionConsumer, registry),
                config.standaloneMode() ? Duration.ZERO : Duration.ofSeconds(10));

        if (!config.standaloneMode()) {
            ComponentHolder componentHolder = new ComponentHolder(Map.of(ReaderManager.class, readerManager));
            client.set(new CoreClient(config.serviceUri(), config.clientId(), config.agentId(), config.agentPassword(), componentHolder));
        } else {
            log.info("Running in standalone mode as requested. Not connecting to a server.");
        }
    }

    private static Config getConfig() {
        Config config = Envy.configure(Config.class, UnifiConfigSource.get(),
                HostAndPortValueParser.instance, ReaderConfigsValueParser.instance, HexByteArrayValueParser.instance);

        if (config.standaloneMode() && config.readers() == null)
            throw new IllegalArgumentException("UNIFI_READERS must be specified in standalone mode.");

        return config;
    }
}
