package id.unifi.service.core.agent.config;

import id.unifi.service.common.detection.DetectableType;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AgentConfig {
    public static final AgentConfig empty = new AgentConfig(Optional.empty(), Optional.empty());

    public final Optional<Set<DetectableType>> detectableTypes;
    public final Optional<RollupConfig> rollup;

    public AgentConfig(Optional<Set<DetectableType>> detectableTypes, Optional<RollupConfig> rollup) {
        this.detectableTypes = detectableTypes;
        this.rollup = rollup;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (AgentConfig) o;
        return Objects.equals(detectableTypes, that.detectableTypes) &&
                Objects.equals(rollup, that.rollup);
    }

    public int hashCode() {
        return Objects.hash(detectableTypes, rollup);
    }

    public String toString() {
        return "AgentConfig{" +
                "detectableTypes=" + detectableTypes +
                ", rollup=" + rollup +
                '}';
    }
}
