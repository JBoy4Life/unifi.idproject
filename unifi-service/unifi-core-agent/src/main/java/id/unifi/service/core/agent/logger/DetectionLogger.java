package id.unifi.service.core.agent.logger;

import id.unifi.service.common.detection.SiteDetectionReport;

import java.io.Closeable;

public interface DetectionLogger extends Closeable {
    void log(SiteDetectionReport report);
}
