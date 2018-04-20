package id.unifi.service.core.agent.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import id.unifi.service.provider.rfid.config.ReaderFullConfig;

import java.util.List;
import java.util.Objects;

/**
 * Protocol-compatible with {@link id.unifi.service.common.agent.AgentConfig},
 * which hides provider-specific properties behind a JSON object to avoid core dependency on providers.
 */
public class AgentConfig {
    public final List<ReaderFullConfig> readers;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AgentConfig(List<ReaderFullConfig> readers) {
        this.readers = readers;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentConfig that = (AgentConfig) o;
        return Objects.equals(readers, that.readers);
    }

    public int hashCode() {
        return Objects.hash(readers);
    }

    public String toString() {
        return "AgentConfig{" +
                "readers=" + readers +
                '}';
    }
}
