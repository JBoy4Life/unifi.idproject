package id.unifi.service.core.agent;

import id.unifi.service.core.agent.config.AgentConfig;

import java.util.Optional;

public interface AgentConfigPersistence {
    Optional<AgentConfig> readConfig();
    void writeConfig(AgentConfig config);
}
