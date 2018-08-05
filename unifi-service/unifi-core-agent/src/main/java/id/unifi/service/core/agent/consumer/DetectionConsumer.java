package id.unifi.service.core.agent.consumer;

import id.unifi.service.common.detection.SiteDetectionReport;

public interface DetectionConsumer {
    void accept(SiteDetectionReport report);
}
