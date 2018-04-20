package id.unifi.service.core.agent;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.opencsv.CSVWriter;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Nullable;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.core.agent.config.AgentConfig;
import id.unifi.service.core.agent.config.ConfigSerialization;
import id.unifi.service.core.agent.parsing.HexByteArrayValueParser;
import id.unifi.service.provider.rfid.RfidProvider;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CoreAgentService {
    private static final Logger log = LoggerFactory.getLogger(CoreAgentService.class);

    private static final Path DETECTION_LOG_FILE_PATH = Paths.get("detections.log");

    private enum AgentMode {
        PRODUCTION,
        TEST_SETUP,
        GENERATE_SETUP
    }

    @Prefix("unifi")
    interface Config {
        @Default("false")
        boolean production(); // Force production mode

        @Nullable
        String clientId();

        @Default("default")
        String agentId();

        @Default("")
        byte[] agentPassword();

        @Default("ws://localhost:8001/agents/msgpack")
        URI serviceUri();

        @Default("false")
        boolean mockDetections();
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1)
            throw new IllegalArgumentException(
                    "Too many arguments. Expected either none to discover readers and generate a setup file " +
                            "or a setup file name to test setup and generate service config.");

        Config config = Envy.configure(Config.class, UnifiConfigSource.get(),
                HostAndPortValueParser.instance, HexByteArrayValueParser.instance);

        AgentMode mode = !config.production() && args.length > 0
                ? AgentMode.TEST_SETUP
                : (config.production() || config.clientId() != null ? AgentMode.PRODUCTION : AgentMode.GENERATE_SETUP);

        log.info("Starting unifi.id core agent service in {} mode", mode);

        if (mode == AgentMode.GENERATE_SETUP) {
            GenerateSetupMode.run();
            return;
        }

        AgentConfigPersistence persistence;
        if (mode == AgentMode.PRODUCTION) {
            persistence = new AgentConfigFilePersistence();
        } else { // AgentMode.TEST_SETUP
            Path setupFilePath = Paths.get(args[0]);
            AgentConfig setupAgentConfig;
            try (BufferedReader reader = Files.newBufferedReader(setupFilePath, UTF_8)) {
                setupAgentConfig =
                        ConfigSerialization.getSetupObjectMapper().readValue(reader, AgentConfig.class);
            }
            persistence = new AgentConfigNoopPersistence(setupAgentConfig);
        }

        MetricRegistry registry = new MetricRegistry();
        JmxReporter jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

        Optional<CSVWriter> logWriter = getDetectionLogWriter(mode);

        AtomicReference<CoreClient> client = new AtomicReference<>();
        Consumer<RawDetectionReport> detectionConsumer = report -> {
            CoreClient coreClient = client.get();
            if (coreClient != null) coreClient.sendRawDetections(report);
            logWriter.ifPresent(w -> {
                try {
                    report.detections.forEach(d -> w.writeNext(new String[]{
                            d.timestamp.toString(),
                            report.readerSn,
                            Integer.toString(d.portNumber),
                            d.detectableId,
                            d.detectableType.toString(),
                            Double.toString(d.rssi)
                    }));
                    w.flush();
                } catch (IOException e) {
                    log.error("Failed to write detection to file.", e);
                }
            });
        };

        ReaderManager readerManager = config.mockDetections()
                ? new MockReaderManager(persistence, config.clientId(), detectionConsumer)
                : new DefaultReaderManager(persistence, new RfidProvider(detectionConsumer, registry),
                mode == AgentMode.PRODUCTION ? Duration.ofSeconds(10) : Duration.ZERO);

        if (mode == AgentMode.PRODUCTION) {
            ComponentHolder componentHolder = new ComponentHolder(Map.of(ReaderManager.class, readerManager));
            client.set(new CoreClient(config.serviceUri(), config.clientId(), config.agentId(), config.agentPassword(), componentHolder));
        } else {
            log.info("Running in {} mode. Not connecting to a server.", mode);
        }
    }

    private static Optional<CSVWriter> getDetectionLogWriter(AgentMode mode) {
        if (mode == AgentMode.PRODUCTION) {
            return Optional.empty();
        } else {
            try {
                log.info("Appending detections to {}", DETECTION_LOG_FILE_PATH);
                CSVWriter writer = new CSVWriter(
                        Files.newBufferedWriter(DETECTION_LOG_FILE_PATH, UTF_8, WRITE, APPEND, CREATE));
                return Optional.of(writer);
            } catch (IOException e) {
                log.warn("Error opening {}, can't log detections.", DETECTION_LOG_FILE_PATH, e);
                return Optional.empty();
            }
        }
    }
}
