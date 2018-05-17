package id.unifi.service.integration.gallagher;

import ch.qos.logback.classic.Level;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.mq.MqUtils;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.common.version.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GallagherDetectionLoggerService {
    static {
        // Prefer IPv4, otherwise 0.0.0.0 gets interpreted as IPv6 broadcast
        System.setProperty("java.net.preferIPv4Stack", "true");

        // j-Interop uses java.util.logging, let's redirect to SLF4J
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static final Logger log = LoggerFactory.getLogger(GallagherDetectionLoggerService.class);
    private static final Logger metricsLog =
            LoggerFactory.getLogger(GallagherDetectionLoggerService.class.getName() + ":metrics");

    public static void main(String[] args) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Gallagher detection logger");
        VersionInfo.log();

        var config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);
        config.logLevel().ifPresent(level ->
                ((ch.qos.logback.classic.Logger)
                        LoggerFactory.getLogger(GallagherDetectionLoggerService.class.getPackageName()))
                        .setLevel(Level.toLevel(level, null)));

        var registry = new MetricRegistry();
        var jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();
        var loggingReporter = Slf4jReporter.forRegistry(registry)
                .outputTo(metricsLog)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .build();
        loggingReporter.start(1, 1, TimeUnit.MINUTES);

        var mqConnection = MqUtils.connect(config.mq());
        var adapter = GallagherAdapter.create(registry, config.ftcApi());
        DetectionMqForwarder.create(mqConnection.createChannel(), adapter::process);
        if (config.serviceApi().isPresent()) {
            HealthReporter.create(config.serviceApi().get(), adapter::reportHealth);
        } else {
            log.warn("No service API config present. Won't send health reports.");
        }
    }
}
