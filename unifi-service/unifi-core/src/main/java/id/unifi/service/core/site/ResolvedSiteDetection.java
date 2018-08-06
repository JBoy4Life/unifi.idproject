package id.unifi.service.core.site;

import java.time.Instant;

public class ResolvedSiteDetection {
    public final Instant detectionTime;
    public final String clientReference;
    public final String zoneId;

    public ResolvedSiteDetection(Instant detectionTime, String clientReference, String zoneId) {
        this.detectionTime = detectionTime;
        this.clientReference = clientReference;
        this.zoneId = zoneId;
    }
}
