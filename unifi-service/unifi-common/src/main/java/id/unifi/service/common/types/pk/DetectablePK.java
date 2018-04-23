package id.unifi.service.common.types.pk;

import id.unifi.service.common.detection.DetectableType;

import java.util.Objects;

public final class DetectablePK {
    public final String clientId;
    public final String detectableId;
    public final DetectableType detectableType;

    public DetectablePK(String clientId, String detectableId, DetectableType detectableType) {
        this.clientId = clientId;
        this.detectableId = detectableId;
        this.detectableType = detectableType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DetectablePK) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(detectableId, that.detectableId) &&
                detectableType == that.detectableType;
    }

    public int hashCode() {
        return Objects.hash(clientId, detectableId, detectableType);
    }

    public String toString() {
        return "DetectablePK{" +
                "clientId='" + clientId + '\'' +
                ", detectableId='" + detectableId + '\'' +
                ", detectableType=" + detectableType +
                '}';
    }
}
