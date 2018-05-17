package id.unifi.service.common.agent;

import java.time.Instant;
import java.util.Map;

public class AgentHealth {
    public final Instant timestamp;
    public final Map<String, ReaderHealth> readers;

    public AgentHealth(Instant timestamp, Map<String, ReaderHealth> readers) {
        this.timestamp = timestamp;
        this.readers = readers;
    }

    public static class ReaderHealth {
        public final boolean healthy;
        public final Map<Integer, Boolean> antennaHealth;

        public ReaderHealth(boolean healthy, Map<Integer, Boolean> antennaHealth) {
            this.healthy = healthy;
            this.antennaHealth = antennaHealth;
        }

        public String toString() {
            return "ReaderHealth{" +
                    "healthy=" + healthy +
                    ", antennaHealth=" + antennaHealth +
                    '}';
        }
    }

    public String toString() {
        return "AgentHealth{" +
                "timestamp=" + timestamp +
                ", readers=" + readers +
                '}';
    }
}
