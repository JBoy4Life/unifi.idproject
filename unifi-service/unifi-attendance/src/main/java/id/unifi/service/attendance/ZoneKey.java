package id.unifi.service.attendance;

import java.util.Objects;

public final class ZoneKey {
    public final String clientId;
    public final String siteId;
    public final String zoneId;

    public ZoneKey(String clientId, String siteId, String zoneId) {
        this.clientId = clientId;
        this.siteId = siteId;
        this.zoneId = zoneId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZoneKey zoneKey = (ZoneKey) o;
        return Objects.equals(clientId, zoneKey.clientId) &&
                Objects.equals(siteId, zoneKey.siteId) &&
                Objects.equals(zoneId, zoneKey.zoneId);
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
