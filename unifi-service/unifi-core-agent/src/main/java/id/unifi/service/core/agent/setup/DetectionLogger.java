package id.unifi.service.core.agent.setup;

import id.unifi.service.common.detection.RawDetectionReport;

import java.io.Closeable;

public interface DetectionLogger extends Closeable {
    void log(RawDetectionReport report);
}
