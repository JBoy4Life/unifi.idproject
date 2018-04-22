package id.unifi.service.core.agent;

import id.unifi.service.core.agent.config.AgentFullConfig;

import java.util.Optional;

public interface AgentConfigPersistence {
    Optional<AgentFullConfig> readConfig();
    void writeConfig(AgentFullConfig config);
}
