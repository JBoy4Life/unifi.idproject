package id.unifi.service.common.detection;

import id.unifi.service.common.types.pk.ZonePK;

import java.util.Objects;
import java.util.Optional;

// FIXME: Break away unifi-attendance and move this class to unifi-core
public class DetectionMatch {
    public final Detection detection;
    public final ZonePK zone;
    public final Optional<String> clientReference;

    public DetectionMatch(Detection detection, ZonePK zone, Optional<String> clientReference) {
        this.detection = detection;
        this.zone = zone;
        this.clientReference = clientReference;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DetectionMatch) o;
        return Objects.equals(detection, that.detection) &&
                Objects.equals(zone, that.zone) &&
                Objects.equals(clientReference, that.clientReference);
    }

    public int hashCode() {
        return Objects.hash(detection, zone, clientReference);
    }

    public String toString() {
        return "DetectionMatch{" +
                "detection=" + detection +
                ", zone=" + zone +
                ", clientReference=" + clientReference +
                '}';
    }
}
