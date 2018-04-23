package id.unifi.service.core.agent.setup;

import id.unifi.service.common.detection.SiteDetectionReport;

import java.io.Closeable;

public interface DetectionLogger extends Closeable {
    void log(SiteDetectionReport report);
}
