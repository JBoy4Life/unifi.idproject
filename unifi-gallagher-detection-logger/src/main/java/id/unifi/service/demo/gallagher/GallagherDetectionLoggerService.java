package id.unifi.service.demo.gallagher;

import com.codahale.metrics.MetricRegistry;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.mq.MqUtils;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.common.version.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GallagherDetectionLoggerService {
    static {
        // Prefer IPv4, otherwise 0.0.0.0 gets interpreted as IPv6 broadcast
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static final Logger log = LoggerFactory.getLogger(GallagherDetectionLoggerService.class);

    @Prefix("unifi")
    private interface Config {
        MqConfig mq();
    }

    public static void main(String[] args) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Gallagher detection logger");
        VersionInfo.log();

        var config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);

        var registry = new MetricRegistry();
        var jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

        var mqConnection = MqUtils.connect(config.mq());
        var adapter = GallagherAdapter.create();
        DetectionMqForwarder.create(mqConnection.createChannel(), (match, onSuccess) -> {
            try {
                adapter.process(match, onSuccess);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
