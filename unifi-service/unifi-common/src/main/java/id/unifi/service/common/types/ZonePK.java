package id.unifi.service.common.types;

import java.util.Objects;

public final class ZonePK {
    public final String clientId;
    public final String siteId;
    public final String zoneId;

    public ZonePK(String clientId, String siteId, String zoneId) {
        this.clientId = clientId;
        this.siteId = siteId;
        this.zoneId = zoneId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var zonePK = (ZonePK) o;
        return Objects.equals(clientId, zonePK.clientId) &&
                Objects.equals(siteId, zonePK.siteId) &&
                Objects.equals(zoneId, zonePK.zoneId);
    }

    public int hashCode() {
        return Objects.hash(clientId, siteId, zoneId);
    }

    public String toString() {
        return "ZoneKey{" +
                "clientId='" + clientId + '\'' +
                ", siteId='" + siteId + '\'' +
                ", zoneId='" + zoneId + '\'' +
                '}';
    }
}
