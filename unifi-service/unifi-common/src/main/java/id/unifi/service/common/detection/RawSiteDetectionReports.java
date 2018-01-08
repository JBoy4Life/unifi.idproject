package id.unifi.service.common.detection;

import java.util.List;

public class RawSiteDetectionReports {
    public final String clientId;
    public final String siteId;
    public final List<RawDetectionReport> reports;

    public RawSiteDetectionReports(String clientId, String siteId, List<RawDetectionReport> reports) {
        this.clientId = clientId;
        this.siteId = siteId;
        this.reports = reports;
    }

    public String toString() {
        return "RawSiteDetectionReport{" +
                "clientId='" + clientId + '\'' +
                ", siteId='" + siteId + '\'' +
                ", reports=" + reports +
                '}';
    }
}
