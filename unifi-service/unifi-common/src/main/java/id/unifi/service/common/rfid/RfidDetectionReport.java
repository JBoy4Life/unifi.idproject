package id.unifi.service.common.rfid;

import java.util.List;

public class RfidDetectionReport {
    public final String readerSn;
    public final List<RfidDetection> detections;

    public RfidDetectionReport(String readerSn, List<RfidDetection> detections) {
        this.readerSn = readerSn;
        this.detections = detections;
    }
}
