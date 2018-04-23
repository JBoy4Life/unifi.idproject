package id.unifi.service.common.detection;

import java.math.BigDecimal;
import java.time.Instant;

public class RawDetection {
    public final Instant timestamp;
    public final int portNumber;
    public final String detectableId;
    public final DetectableType detectableType;
    public final BigDecimal rssi;
    public final int count;

    public RawDetection(Instant timestamp,
                        int portNumber,
                        String detectableId,
                        DetectableType detectableType,
                        BigDecimal rssi,
                        int count) {
        this.timestamp = timestamp;
        this.portNumber = portNumber;
        this.detectableId = detectableId;
        this.detectableType = detectableType;
        this.rssi = rssi;
        this.count = count;
    }

    public String toString() {
        return "RawDetection{" +
                "timestamp=" + timestamp +
                ", portNumber=" + portNumber +
                ", detectableId='" + detectableId + '\'' +
                ", detectableType=" + detectableType +
                ", rssi=" + rssi +
                ", count=" + count +
                '}';
    }
}
