package id.unifi.service.core.site;

import java.time.Instant;

public class ResolvedDetection {
    public final Instant detectionTime;
    public final String clientReference;
    public final String zoneId;

    public ResolvedDetection(Instant detectionTime, String clientReference, String zoneId) {
        this.detectionTime = detectionTime;
        this.clientReference = clientReference;
        this.zoneId = zoneId;
    }
}
