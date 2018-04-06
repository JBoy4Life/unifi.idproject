package id.unifi.service.common.detection;

import java.util.List;

public class RawSiteDetectionReports {
    public final String clientId;
    public final List<RawDetectionReport> reports;

    public RawSiteDetectionReports(String clientId, List<RawDetectionReport> reports) {
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
