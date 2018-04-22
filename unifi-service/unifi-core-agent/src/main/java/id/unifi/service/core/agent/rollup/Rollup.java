package id.unifi.service.core.agent.rollup;

import id.unifi.service.common.detection.RawDetectionReport;

import java.util.List;

public interface Rollup {
    List<RawDetectionReport> process(RawDetectionReport report);
}
