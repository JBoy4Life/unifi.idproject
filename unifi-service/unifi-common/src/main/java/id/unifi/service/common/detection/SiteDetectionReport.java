package id.unifi.service.common.detection;

import static java.util.Collections.unmodifiableList;

import java.util.List;

public class SiteDetectionReport {
    public final String readerSn;
    public final List<SiteRfidDetection> detections;

    public SiteDetectionReport(String readerSn, List<SiteRfidDetection> detections) {
        this.readerSn = readerSn;
        this.detections = unmodifiableList(detections);
    }

    public String toString() {
        return "RfidReaderDetectionReport{" +
                "readerSn='" + readerSn + '\'' +
                ", detections=" + detections +
                '}';
    }
}
