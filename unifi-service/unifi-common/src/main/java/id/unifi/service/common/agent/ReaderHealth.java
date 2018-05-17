package id.unifi.service.common.agent;

import java.time.Instant;
import java.util.Map;

public class ReaderHealth {
    public final String readerSn;
    public final boolean healthy;
    public final Map<Integer, Boolean> antennaHealth;
    public final Instant timestamp;

    public ReaderHealth(String readerSn, boolean healthy, Map<Integer, Boolean> antennaHealth, Instant timestamp) {
        this.readerSn = readerSn;
        this.healthy = healthy;
        this.antennaHealth = antennaHealth;
        this.timestamp = timestamp;
    }
}
