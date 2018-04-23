package id.unifi.service.common.detection;

import java.util.List;

public class DetectionReports {
    public final String clientId;
    public final List<SiteDetectionReport> reports;

    public DetectionReports(String clientId, List<SiteDetectionReport> reports) {
        this.clientId = clientId;
        this.reports = reports;
    }

    public String toString() {
        return "RawSiteDetectionReport{" +
                "clientId='" + clientId + '\'' +
                ", reports=" + reports +
                '}';
    }
}
