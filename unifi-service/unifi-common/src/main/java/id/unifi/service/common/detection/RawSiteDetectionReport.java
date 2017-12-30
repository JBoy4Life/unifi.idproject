package id.unifi.service.common.detection;

public class RawSiteDetectionReport {
    public final String clientId;
    public final String siteId;
    public final RawDetectionReport report;

    public RawSiteDetectionReport(String clientId, String siteId, RawDetectionReport report) {
        this.clientId = clientId;
        this.siteId = siteId;
        this.report = report;
    }

    public String toString() {
        return "RawSiteDetectionReport{" +
                "clientId='" + clientId + '\'' +
                ", siteId='" + siteId + '\'' +
                ", report=" + report +
                '}';
    }
}
