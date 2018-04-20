package id.unifi.service.core.agent;

import id.unifi.service.core.agent.config.AgentConfig;

import javax.annotation.Nullable;
import java.util.Optional;

public class AgentConfigNoopPersistence implements AgentConfigPersistence {
    @Nullable
    private final AgentConfig config;

    public AgentConfigNoopPersistence(AgentConfig config) {
        this.config = config;
    }

    public Optional<AgentConfig> readConfig() {
        return Optional.ofNullable(config);
    }

    public void writeConfig(AgentConfig config) {}
}
