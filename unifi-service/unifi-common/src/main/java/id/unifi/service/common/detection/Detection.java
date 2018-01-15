package id.unifi.service.common.detection;

import java.time.Instant;
import java.util.Objects;

public final class Detection {
    public final ClientDetectable detectable;
    public final String readerSn;
    public final int portNumber;
    public final Instant detectionTime;

    public Detection(ClientDetectable detectable, String readerSn, int portNumber, Instant detectionTime) {
        this.detectable = detectable;
        this.readerSn = readerSn;
        this.portNumber = portNumber;
        this.detectionTime = detectionTime;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Detection detection = (Detection) o;
        return portNumber == detection.portNumber &&
                Objects.equals(detectable, detection.detectable) &&
                Objects.equals(readerSn, detection.readerSn) &&
                Objects.equals(detectionTime, detection.detectionTime);
    }

    public int hashCode() {
        return Objects.hash(detectable, readerSn, portNumber, detectionTime);
    }

    public String toString() {
        return "Detection{" +
                "detectable=" + detectable +
                ", readerSn='" + readerSn + '\'' +
                ", portNumber=" + portNumber +
                ", detectionTime=" + detectionTime +
                '}';
    }
}
