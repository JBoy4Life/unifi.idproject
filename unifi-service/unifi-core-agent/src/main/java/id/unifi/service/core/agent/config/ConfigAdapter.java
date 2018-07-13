package id.unifi.service.core.agent.config;

public interface ConfigAdapter {
    void configure(AgentFullConfig config, boolean authoritative);
}
