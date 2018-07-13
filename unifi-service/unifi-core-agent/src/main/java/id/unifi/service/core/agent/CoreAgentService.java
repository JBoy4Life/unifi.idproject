package id.unifi.service.core.agent;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Nullable;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.HexByteArrayValueParser;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.core.agent.config.AgentFullConfig;
import id.unifi.service.core.agent.config.ConfigAdapter;
import static id.unifi.service.core.agent.config.ConfigSerialization.getConfigObjectMapper;
import static id.unifi.service.core.agent.config.ConfigSerialization.getSetupObjectMapper;
import id.unifi.service.core.agent.config.ProductionConfigWrapper;
import id.unifi.service.core.agent.logger.DetectionLogger;
import id.unifi.service.core.agent.logger.NullDetectionLogger;
import id.unifi.service.core.agent.setup.CsvDetectionLogger;
import id.unifi.service.core.agent.setup.GenerateSetupMode;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

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
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1)
            throw new IllegalArgumentException(
                    "Too many arguments. Expected either none to discover readers and generate a setup file " +
                            "or a setup file name to test setup and generate service config.");

        var config = Envy.configure(Config.class, UnifiConfigSource.get(),
                HostAndPortValueParser.instance, HexByteArrayValueParser.instance);

        var mode = !config.production() && args.length > 0
                ? AgentMode.TEST_SETUP
                : (config.production() || config.clientId() != null ? AgentMode.PRODUCTION : AgentMode.GENERATE_SETUP);

        log.info("Starting unifi.id core agent service in {} mode", mode);

        if (mode == AgentMode.GENERATE_SETUP) {
            GenerateSetupMode.run();
            return;
        }

        var productionMode = mode == AgentMode.PRODUCTION;

        var registry = new MetricRegistry();
        var jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

        Optional<Function<ConfigAdapter, CoreClient>> coreClientFactory;
        if (productionMode) {
            coreClientFactory = Optional.of(configAdapter -> {
                var configWrapper = new ProductionConfigWrapper(
                        new AgentConfigFilePersistence(), Duration.ofSeconds(10), configAdapter);
                return new CoreClient(
                        config.serviceUri(), config.clientId(), config.agentId(), config.agentPassword(),
                        configWrapper);
            });
        } else {
            log.info("Running in {} mode. Not connecting to a server.", mode);
            coreClientFactory = Optional.empty();
        }

        DetectionLogger detectionLogger = productionMode ? new NullDetectionLogger() : new CsvDetectionLogger();

        var agent = CoreAgent.create(coreClientFactory, detectionLogger, registry);

        if (!productionMode) configureFromSetupFile(agent, Paths.get(args[0]));
    }

    private static void configureFromSetupFile(CoreAgent agent, Path setupFilePath) throws IOException {
        AgentFullConfig setupAgentConfig;
        try (var reader = Files.newBufferedReader(setupFilePath, UTF_8)) {
            setupAgentConfig = getSetupObjectMapper().readValue(reader, AgentFullConfig.class);
        }
        logServiceConfig(setupAgentConfig);
        agent.configure(setupAgentConfig, true);
    }

    private static void logServiceConfig(AgentFullConfig setupAgentConfig) throws JsonProcessingException {
        var serviceConfig = setupAgentConfig.compactForService();
        var serializedServiceAgentConfig =
                getConfigObjectMapper().writeValueAsString(serviceConfig.agent.get());
        log.info("Serialized agent config for service use: \n{}", serializedServiceAgentConfig);

        for (var r : serviceConfig.readers) {
            var serializedServiceReaderConfig = getConfigObjectMapper().writeValueAsString(r.config.get());
            log.info("Serialized reader configuration for {}/{}: \n{}",
                    r.readerSn.orElse("?"), r.endpoint.orElse(null), String.join("\n", serializedServiceReaderConfig));
        }
    }
}
