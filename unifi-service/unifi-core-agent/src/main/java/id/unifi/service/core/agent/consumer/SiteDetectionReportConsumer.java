package id.unifi.service.core.agent.consumer;

import id.unifi.service.common.detection.SiteDetectionReport;

import java.util.List;
import java.util.function.BiConsumer;

public interface SiteDetectionReportConsumer extends BiConsumer<List<SiteDetectionReport>, Runnable> {
    void accept(List<SiteDetectionReport> reports, Runnable ackCallback);
}
