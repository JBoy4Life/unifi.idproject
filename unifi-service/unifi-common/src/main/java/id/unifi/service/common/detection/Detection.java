package id.unifi.service.common.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import id.unifi.service.common.types.pk.DetectablePK;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class Detection {
    public final DetectablePK detectable;
    public final String readerSn;
    public final int portNumber;
    public final Instant detectionTime;
    public final BigDecimal rssi;
    public final int count;

    @JsonCreator
    public Detection(DetectablePK detectable,
                     String readerSn,
                     int portNumber,
                     Instant detectionTime,
                     BigDecimal rssi,
                     int count) {
        this.detectable = detectable;
        this.readerSn = readerSn;
        this.portNumber = portNumber;
        this.detectionTime = detectionTime;
        this.rssi = rssi;
        this.count = count;
    }

    public Detection(String clientId, String readerSn, SiteRfidDetection detection) {
        this.detectable = detection.detectable.withClientId(clientId);
        this.readerSn = readerSn;
        this.portNumber = detection.portNumber;
        this.detectionTime = detection.detectionTime;
        this.rssi = detection.rssi;
        this.count = detection.count;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var detection = (Detection) o;
        return portNumber == detection.portNumber &&
                count == detection.count &&
                Objects.equals(detectable, detection.detectable) &&
                Objects.equals(readerSn, detection.readerSn) &&
                Objects.equals(detectionTime, detection.detectionTime) &&
                Objects.equals(rssi, detection.rssi);
    }

    public int hashCode() {
        return Objects.hash(detectable, readerSn, portNumber, detectionTime, rssi, count);
    }

    public String toString() {
        return "Detection{" +
                "detectable=" + detectable +
                ", readerSn='" + readerSn + '\'' +
                ", portNumber=" + portNumber +
                ", detectionTime=" + detectionTime +
                ", rssi=" + rssi +
                ", count=" + count +
                '}';
    }
}
