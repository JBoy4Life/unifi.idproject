package id.unifi.service.core.agent.rollup;

import id.unifi.service.common.detection.SiteDetectionReport;

import java.util.stream.Stream;

public interface Rollup {
    Stream<SiteDetectionReport> process(SiteDetectionReport report);
}
