package id.unifi.service.core.agent.logger;

import id.unifi.service.common.detection.SiteDetectionReport;

public class NullDetectionLogger implements DetectionLogger {
    public void log(SiteDetectionReport report) {}

    public void close() {}
}
