package id.unifi.service.provider.rfid;

import java.util.List;

public class RfidDetectionReport {
    private final String readerSerialNumber;

    public String getReaderSerialNumber() {
        return readerSerialNumber;
    }

    public List<RfidDetection> getDetections() {
        return detections;
    }

    private final List<RfidDetection> detections;

    RfidDetectionReport(String readerSerialNumber, List<RfidDetection> detections) {
        this.readerSerialNumber = readerSerialNumber;
        this.detections = detections;
    }
}
