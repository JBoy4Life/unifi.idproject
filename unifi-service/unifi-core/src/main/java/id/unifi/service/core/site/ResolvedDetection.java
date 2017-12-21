package id.unifi.service.core.site;

import java.time.Instant;

public class ResolvedDetection {
    public final Instant timestamp;
    public final String clientReference;
    public final String zoneId;

    public ResolvedDetection(Instant timestamp, String clientReference, String zoneId) {
        this.timestamp = timestamp;
        this.clientReference = clientReference;
        this.zoneId = zoneId;
    }
}
