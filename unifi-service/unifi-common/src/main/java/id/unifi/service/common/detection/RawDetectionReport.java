package id.unifi.service.common.detection;

import static java.util.Collections.unmodifiableList;

import java.util.List;

public class RawDetectionReport {
    public final String readerSn;
    public final List<RawDetection> detections;

    public RawDetectionReport(String readerSn, List<RawDetection> detections) {
        this.readerSn = readerSn;
        this.detections = unmodifiableList(detections);
    }

    public String toString() {
        return "RawDetectionReport{" +
                "readerSn='" + readerSn + '\'' +
                ", detections=" + detections +
                '}';
    }
}
