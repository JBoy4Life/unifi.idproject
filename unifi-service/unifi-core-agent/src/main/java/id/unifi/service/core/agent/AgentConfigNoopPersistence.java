package id.unifi.service.core.agent;

import id.unifi.service.core.agent.config.AgentFullConfig;

import javax.annotation.Nullable;
import java.util.Optional;

public class AgentConfigNoopPersistence implements AgentConfigPersistence {
    @Nullable
    private final AgentFullConfig config;

    public AgentConfigNoopPersistence(AgentFullConfig config) {
        this.config = config;
    }

    public Optional<AgentFullConfig> readConfig() {
        return Optional.ofNullable(config);
    }

    public void writeConfig(AgentFullConfig config) {}
}
