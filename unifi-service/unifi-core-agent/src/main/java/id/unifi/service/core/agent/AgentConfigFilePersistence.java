package id.unifi.service.core.agent;

import id.unifi.service.core.agent.config.AgentConfig;
import id.unifi.service.core.agent.config.ConfigSerialization;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class AgentConfigFilePersistence implements AgentConfigPersistence {
    private static final Logger log = LoggerFactory.getLogger(AgentConfigFilePersistence.class);

    private static final Path AGENT_CONFIG_PATH =
            Paths.get(System.getProperty("user.home"), ".unifi", "agent-config.json");

    public Optional<AgentConfig> readConfig() {
        try (BufferedReader reader = Files.newBufferedReader(AGENT_CONFIG_PATH, UTF_8)) {
            return Optional.ofNullable(ConfigSerialization.getConfigObjectMapper().readValue(reader, AgentConfig.class));
        } catch (NoSuchFileException e) { // TODO: factor out IOException handling
            log.warn("Local config file not found: " + AGENT_CONFIG_PATH);
            return Optional.empty();
        } catch (IOException e) {
            log.warn("Failed to read agent config from file: {}", AGENT_CONFIG_PATH, e);
            return Optional.empty();
        }
    }

    public void writeConfig(AgentConfig config) {
        try {
            Files.createDirectories(AGENT_CONFIG_PATH.getParent());
            Path tempPath = Files.createTempFile("agent-config", null);
            try (BufferedWriter writer = Files.newBufferedWriter(tempPath, UTF_8, WRITE, CREATE)) {
                ConfigSerialization.getConfigObjectMapper().writeValue(writer, config);
            }

            Files.move(tempPath, AGENT_CONFIG_PATH, REPLACE_EXISTING);
            log.info("Agent config saved to {}", AGENT_CONFIG_PATH);
        } catch (IOException e) {
            log.warn("Failed to write agent config to a file", e);
        }
    }
}
