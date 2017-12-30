package id.unifi.service.core.agent;

import id.unifi.service.common.detection.RawDetectionReport;

public interface RawDetectionProcessor {
    void process(RawDetectionReport rawDetectionReport);
}
