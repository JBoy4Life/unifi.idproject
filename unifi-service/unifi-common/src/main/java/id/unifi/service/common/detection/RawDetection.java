package id.unifi.service.common.detection;

import java.time.Instant;

public class RawDetection {
    public final Instant timestamp;
    public final int portNumber;
    public final String detectableId;
    public final DetectableType detectableType;
    public final double rssi;

    public RawDetection(Instant timestamp,
                        int portNumber,
                        String detectableId,
                        DetectableType detectableType,
                        double rssi) {
        this.timestamp = timestamp;
        this.portNumber = portNumber;
        this.detectableId = detectableId;
        this.detectableType = detectableType;
        this.rssi = rssi;
    }
}
