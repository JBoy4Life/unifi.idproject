package id.unifi.service.core.agent;

import id.unifi.service.common.rfid.RfidReader;
import id.unifi.service.common.util.TimeUtils;
import id.unifi.service.core.agent.config.AgentConfig;
import id.unifi.service.core.agent.config.ConfigSerialization;
import id.unifi.service.provider.rfid.LlrpReaderDiscovery;
import id.unifi.service.provider.rfid.config.ReaderConfig;
import id.unifi.service.provider.rfid.config.ReaderFullConfig;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class GenerateSetupMode {
    private static final Logger log = LoggerFactory.getLogger(GenerateSetupMode.class);
    private static final String SETUP_FILE_NAME_FORMAT = "generated-agent-setup_%s.yaml";

    static void run() throws IOException {
        List<RfidReader> readers = LlrpReaderDiscovery.discoverReaders();

        if (readers.isEmpty()) {
            log.error("No readers found");
            return;
        }

        readers.forEach(r -> log.info("Found reader: {}", r));

        List<ReaderFullConfig> configuredReaders = readers.stream().map(reader -> {
            List<Integer> enabledPortNumbers = reader.getStatus().getAntennaeConnected().entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(toList());
            ReaderConfig cfg = ReaderConfig.fromPortNumbers(enabledPortNumbers);
            return new ReaderFullConfig(Optional.ofNullable(reader.getSn()),
                    Optional.of(reader.getStatus().getEndpoint()),
                    Optional.of(cfg));
        }).collect(toList());

        AgentConfig agentConfig = new AgentConfig(configuredReaders);
        String serializedAgentConfig = ConfigSerialization.getSetupObjectMapper().writeValueAsString(agentConfig);

        Path setupFilePath = Paths.get(String.format(SETUP_FILE_NAME_FORMAT, TimeUtils.getFormattedLocalDateTimeNow()));
        Files.write(setupFilePath, List.of(serializedAgentConfig), UTF_8, WRITE, CREATE);
        log.info("Setup saved to {}", setupFilePath);
    }
}
