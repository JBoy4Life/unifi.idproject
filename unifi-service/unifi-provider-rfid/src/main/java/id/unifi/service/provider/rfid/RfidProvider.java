package id.unifi.service.provider.rfid;

import com.codahale.metrics.MetricRegistry;
import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.provider.DetectionProvider;
import id.unifi.service.provider.rfid.config.ReaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RfidProvider implements DetectionProvider {
    private static final Logger log = LoggerFactory.getLogger(RfidProvider.class);
    private final Consumer<RawDetectionReport> detectionConsumer;
    private List<ImpinjReaderController> controllers;
    private MetricRegistry registry;

    public RfidProvider(Consumer<RawDetectionReport> detectionConsumer, MetricRegistry registry) {
        this.detectionConsumer = detectionConsumer;
        this.registry = registry;
        this.controllers = List.of();
    }

    public synchronized void configure(List<ReaderFullConfig<ReaderConfig>> readers) {
        controllers.forEach(ImpinjReaderController::close);
        controllers = readers.stream()
                .map(r -> new ImpinjReaderController(r, detectionConsumer, registry))
                .collect(Collectors.toList());
    }
}
