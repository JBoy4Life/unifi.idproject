package id.unifi.service.core.agent.rollup;

import id.unifi.service.common.detection.RawDetectionReport;

import java.util.stream.Stream;

public interface Rollup {
    Stream<RawDetectionReport> process(RawDetectionReport report);
}
