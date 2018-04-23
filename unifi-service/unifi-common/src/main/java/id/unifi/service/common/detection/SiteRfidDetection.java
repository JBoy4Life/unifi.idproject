package id.unifi.service.common.detection;

import id.unifi.service.common.types.client.ClientDetectable;

import java.math.BigDecimal;
import java.time.Instant;

public class SiteRfidDetection {
    public final Instant timestamp;
    public final int portNumber;
    public final ClientDetectable detectable;
    public final BigDecimal rssi;
    public final int count;

    public SiteRfidDetection(Instant timestamp,
                             int portNumber,
                             ClientDetectable detectable,
                             BigDecimal rssi,
                             int count) {
        this.timestamp = timestamp;
        this.portNumber = portNumber;
        this.detectable = detectable;
        this.rssi = rssi;
        this.count = count;
    }

    public String toString() {
        return "SiteRfidDetection{" +
                "timestamp=" + timestamp +
                ", portNumber=" + portNumber +
                ", detectable='" + detectable + '\'' +
                ", rssi=" + rssi +
                ", count=" + count +
                '}';
    }
}
