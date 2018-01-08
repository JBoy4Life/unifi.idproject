package id.unifi.service.common;

import java.util.Objects;

public class FullSiteId {
    public final String clientId;
    public final String siteId;

    public FullSiteId(String clientId, String siteId) {
        this.clientId = clientId;
        this.siteId = siteId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullSiteId that = (FullSiteId) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(siteId, that.siteId);
    }

    public int hashCode() {
        return Objects.hash(clientId, siteId);
    }

    public String toString() {
        return "FullSiteId{" +
                "clientId='" + clientId + '\'' +
                ", siteId='" + siteId + '\'' +
                '}';
    }
}
