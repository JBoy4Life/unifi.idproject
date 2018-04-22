package id.unifi.service.core.agent.config;

import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.provider.rfid.config.ReaderConfig;

import java.util.List;
import java.util.Optional;

public class AgentFullConfig extends id.unifi.service.common.agent.AgentFullConfig<AgentConfig, ReaderConfig> {
    public AgentFullConfig(Optional<AgentConfig> agent, List<ReaderFullConfig<ReaderConfig>> readers) {
        super(agent, readers);
    }
}
