package id.unifi.service.common.types.pk;

import java.util.Objects;

public final class SitePK {
    public final String clientId;
    public final String siteId;

    public SitePK(String clientId, String siteId) {
        this.clientId = clientId;
        this.siteId = siteId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (SitePK) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(siteId, that.siteId);
    }

    public int hashCode() {
        return Objects.hash(clientId, siteId);
    }

    public String toString() {
        return "SitePK{" +
                "clientId='" + clientId + '\'' +
                ", siteId='" + siteId + '\'' +
                '}';
    }
}
