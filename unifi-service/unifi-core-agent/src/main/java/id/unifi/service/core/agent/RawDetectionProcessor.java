package id.unifi.service.core.agent;

import id.unifi.service.common.detection.SiteDetectionReport;

public interface RawDetectionProcessor {
    void process(SiteDetectionReport rfidReaderDetectionGroup);
}
