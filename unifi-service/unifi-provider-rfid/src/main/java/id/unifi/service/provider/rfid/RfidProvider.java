package id.unifi.service.provider.rfid;

import com.codahale.metrics.MetricRegistry;
import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.common.provider.DetectionProvider;
import id.unifi.service.provider.rfid.config.ReaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RfidProvider implements DetectionProvider {
    private static final Logger log = LoggerFactory.getLogger(RfidProvider.class);
    private List<ImpinjReaderController> controllers;

    public RfidProvider(List<ReaderFullConfig<ReaderConfig>> readers,
                        Consumer<SiteDetectionReport> detectionConsumer,
                        MetricRegistry registry) {
        controllers = readers.stream()
                .map(r -> new ImpinjReaderController(r, detectionConsumer, registry))
                .collect(Collectors.toList());
    }

    public synchronized void close() {
        controllers.forEach(ImpinjReaderController::close);
    }
}
