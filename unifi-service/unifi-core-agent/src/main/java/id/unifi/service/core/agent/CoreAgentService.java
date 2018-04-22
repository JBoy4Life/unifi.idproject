package id.unifi.service.core.agent;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Nullable;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.util.MetricUtils;
import static id.unifi.service.core.agent.DefaultReaderManager.getDetectableTypes;
import id.unifi.service.core.agent.config.AgentFullConfig;
import static id.unifi.service.core.agent.config.ConfigSerialization.getConfigObjectMapper;
import static id.unifi.service.core.agent.config.ConfigSerialization.getSetupObjectMapper;
import id.unifi.service.core.agent.parsing.HexByteArrayValueParser;
import id.unifi.service.core.agent.setup.CsvDetectionLogger;
import id.unifi.service.core.agent.setup.DetectionLogger;
import id.unifi.service.provider.rfid.RfidProvider;
import id.unifi.service.provider.rfid.config.ReaderConfig;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
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
        Optional<DetectionLogger> detectionLogger;
        if (mode == AgentMode.PRODUCTION) {
            persistence = new AgentConfigFilePersistence();
            detectionLogger = Optional.empty();
        } else { // AgentMode.TEST_SETUP
            Path setupFilePath = Paths.get(args[0]);
            AgentFullConfig setupAgentConfig;
            try (BufferedReader reader = Files.newBufferedReader(setupFilePath, UTF_8)) {
                setupAgentConfig = getSetupObjectMapper().readValue(reader, AgentFullConfig.class);
            }
            logServiceConfig(setupAgentConfig);

            persistence = new AgentConfigNoopPersistence(setupAgentConfig);
            detectionLogger = Optional.of(new CsvDetectionLogger());
        }

        MetricRegistry registry = new MetricRegistry();
        JmxReporter jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

        AtomicReference<CoreClient> client = new AtomicReference<>();
        Consumer<RawDetectionReport> detectionConsumer = report -> {
            RawDetectionReport filteredReport = new RawDetectionReport(report.readerSn,
                    report.detections.stream()
                            .filter(d -> getDetectableTypes().contains(d.detectableType))
                            .collect(toList()));
            CoreClient coreClient = client.get();
            if (coreClient != null) coreClient.sendRawDetections(filteredReport);
            detectionLogger.ifPresent(w -> w.log(filteredReport));
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

    private static void logServiceConfig(AgentFullConfig setupAgentConfig) throws JsonProcessingException {
        AgentFullConfig serviceConfig = setupAgentConfig.compactForService();
        String serializedServiceAgentConfig =
                getConfigObjectMapper().writeValueAsString(serviceConfig.agent.get());
        log.info("Serialized agent config for service use: \n{}", serializedServiceAgentConfig);

        for (ReaderFullConfig<ReaderConfig> r : serviceConfig.readers) {
            String serializedServiceReaderConfig = getConfigObjectMapper().writeValueAsString(r.config.get());
            log.info("Serialized reader configuration for {}/{}: \n{}",
                    r.readerSn.orElse("?"), r.endpoint.orElse(null), String.join("\n", serializedServiceReaderConfig));
        }
    }
}
