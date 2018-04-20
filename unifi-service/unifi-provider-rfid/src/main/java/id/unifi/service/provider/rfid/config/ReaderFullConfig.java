package id.unifi.service.provider.rfid.config;

import com.google.common.net.HostAndPort;

import java.util.Objects;
import java.util.Optional;

/**
 * Protocol-compatible with {@link id.unifi.service.common.agent.AgentReaderFullConfig}
 */
public class ReaderFullConfig {
    public final Optional<String> readerSn;
    public final Optional<HostAndPort> endpoint;
    public final Optional<ReaderConfig> config;

    public ReaderFullConfig(Optional<String> readerSn,
                            Optional<HostAndPort> endpoint,
                            Optional<ReaderConfig> config) {
        this.readerSn = readerSn;
        this.endpoint = endpoint;
        this.config = config;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReaderFullConfig that = (ReaderFullConfig) o;
        return Objects.equals(readerSn, that.readerSn) &&
                Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(config, that.config);
    }

    public int hashCode() {
        return Objects.hash(readerSn, endpoint, config);
    }

    public String toString() {
        return "ReaderFullConfig{" +
                "readerSn=" + readerSn +
                ", endpoint=" + endpoint +
                ", config=" + config +
                '}';
    }
}
