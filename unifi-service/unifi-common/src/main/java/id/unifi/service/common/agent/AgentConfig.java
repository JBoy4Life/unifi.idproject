package id.unifi.service.common.agent;

import java.util.List;

/**
 * Agent config data object that hides provider-specific properties behind a JSON object to avoid core dependency
 * on providers.
 */
public class AgentConfig {
    public final List<AgentReaderFullConfig> readers;

    public AgentConfig(List<AgentReaderFullConfig> readers) {
        this.readers = readers;
    }
}
