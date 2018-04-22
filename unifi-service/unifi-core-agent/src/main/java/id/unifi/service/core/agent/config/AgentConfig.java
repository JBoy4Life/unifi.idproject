package id.unifi.service.core.agent.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import id.unifi.service.common.detection.DetectableType;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AgentConfig {
    public static final AgentConfig empty = new AgentConfig(Optional.empty());

    public final Optional<Set<DetectableType>> detectableTypes;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AgentConfig(Optional<Set<DetectableType>> detectableTypes) {
        this.detectableTypes = detectableTypes;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentConfig that = (AgentConfig) o;
        return Objects.equals(detectableTypes, that.detectableTypes);
    }

    public int hashCode() {
        return Objects.hash(detectableTypes);
    }

    public String toString() {
        return "AgentConfig{" +
                "detectableTypes=" + detectableTypes +
                '}';
    }
}
