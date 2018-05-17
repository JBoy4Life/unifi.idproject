package id.unifi.service.core.detection;

import id.unifi.service.common.agent.AgentHealth;
import id.unifi.service.common.agent.ReaderHealth;
import id.unifi.service.common.types.pk.AgentPK;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReaderHealthContainer {
    private final Map<String, Map<String, AgentHealth>> healthByAgent;

    public ReaderHealthContainer() {
        this.healthByAgent = new ConcurrentHashMap<>();
    }

    public List<ReaderHealth> getHealth(String clientId) {
        return healthByAgent.getOrDefault(clientId, Map.of()).values().stream()
                .flatMap(health -> health.readers.entrySet().stream().map(
                        e -> new ReaderHealth(e.getKey(), e.getValue().healthy, e.getValue().antennaHealth, health.timestamp)))
                .collect(toList());
    }

    public void putHealth(AgentPK agent, AgentHealth health) {
        healthByAgent.computeIfAbsent(agent.clientId, ag -> new ConcurrentHashMap<>()).put(agent.agentId, health);
    }
}
