package id.unifi.service.provider.rfid;

import com.codahale.metrics.MetricRegistry;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.detection.ReaderConfig;
import id.unifi.service.common.provider.DetectionProvider;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RfidProvider implements DetectionProvider {
    private static final Logger log = LoggerFactory.getLogger(RfidProvider.class);
    private final Consumer<RawDetectionReport> detectionConsumer;
    private Map<String, ImpinjReaderController> controllers;
    private MetricRegistry registry;

    public RfidProvider(Consumer<RawDetectionReport> detectionConsumer, MetricRegistry registry) {
        this.detectionConsumer = detectionConsumer;
        this.registry = registry;
        this.controllers = Map.of();
    }

    public synchronized void configure(List<ReaderConfig> readers) {
        controllers.forEach((sn, controller) -> controller.close());
        controllers = readers.stream()
                .collect(toMap(r -> r.readerSn, r -> new ImpinjReaderController(r, detectionConsumer, registry)));
    }
}
