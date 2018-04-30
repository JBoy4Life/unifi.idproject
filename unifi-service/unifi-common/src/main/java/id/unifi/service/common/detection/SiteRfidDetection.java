package id.unifi.service.common.detection;

import id.unifi.service.common.types.client.ClientDetectable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class SiteRfidDetection {
    public final Instant detectionTime;
    public final int portNumber;
    public final ClientDetectable detectable;
    public final Optional<BigDecimal> rssi;
    public final int count;

    public SiteRfidDetection(Instant detectionTime,
                             int portNumber,
                             ClientDetectable detectable,
                             Optional<BigDecimal> rssi,
                             int count) {
        this.detectionTime = detectionTime;
        this.portNumber = portNumber;
        this.detectable = detectable;
        this.rssi = rssi;
        this.count = count;
    }

    public String toString() {
        return "SiteRfidDetection{" +
                "detectionTime=" + detectionTime +
                ", portNumber=" + portNumber +
                ", detectable='" + detectable + '\'' +
                ", rssi=" + rssi +
                ", count=" + count +
                '}';
    }
}
