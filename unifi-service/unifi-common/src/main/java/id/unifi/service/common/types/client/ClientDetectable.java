package id.unifi.service.common.types.client;

import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.types.pk.DetectablePK;

import java.util.Objects;

public final class ClientDetectable {
    public final String detectableId;
    public final DetectableType detectableType;

    public ClientDetectable(String detectableId, DetectableType detectableType) {
        this.detectableId = detectableId;
        this.detectableType = detectableType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ClientDetectable) o;
        return Objects.equals(detectableId, that.detectableId) &&
                detectableType == that.detectableType;
    }

    public int hashCode() {
        return Objects.hash(detectableId, detectableType);
    }

    public String toString() {
        return "ClientDetectable{" +
                "detectableId='" + detectableId + '\'' +
                ", detectableType=" + detectableType +
                '}';
    }

    public DetectablePK withClientId(String clientId) {
        return new DetectablePK(clientId, detectableId, detectableType);
    }
}
