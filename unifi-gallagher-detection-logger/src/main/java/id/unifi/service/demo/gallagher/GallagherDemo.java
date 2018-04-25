package id.unifi.service.demo.gallagher;

import com.codahale.metrics.MetricRegistry;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.types.pk.DetectablePK;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.common.version.VersionInfo;
import id.unifi.service.provider.security.gallagher.FtcApi;
import id.unifi.service.provider.security.gallagher.IFTMiddleware2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;

public class GallagherDemo implements IFTMiddleware2 {
    static {
        // Prefer IPv4, otherwise 0.0.0.0 gets interpreted as IPv6 broadcast
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static final Logger log = LoggerFactory.getLogger(GallagherDemo.class);
    public static final ZoneId ZONE = ZoneId.systemDefault();

    public final FtcApi ftcApi;
    public static final CountDownLatch registerLatch = new CountDownLatch(1);
    private static final CountDownLatch quitLatch = new CountDownLatch(1);

    public GallagherDemo(FtcApi ftcApi) {
        this.ftcApi = ftcApi;
    }

    @Prefix("unifi")
    private interface Config {
        MqConfig mq();
    }

    public static void main(String[] args) throws Exception {
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

        log.info("Zone: {}", ZONE);

        GallagherDemo demo = setUp();
        Detection detection =
                new Detection(new DetectablePK("test-club", "E28011606000020497CB0065", DetectableType.UHF_EPC),
                        "37017090614", 1, Instant.now(), BigDecimal.ZERO, 1);
        demo.processDetection(detection);
        quitLatch.await();
    }

    public static GallagherDemo setUp() {
        System.out.println("********* GALLAGHER DEMO *********");

        FtcApi ftcApi = new FtcApi(
                "10.0.99.3",
                "localhost",
                "Administrator",
                "TestPass123");
        GallagherDemo demo = new GallagherDemo(ftcApi);
        ftcApi.registerMiddleware(demo);

        try {
            registerLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return demo;
    }

    public void processDetection(Detection detection) {
        int eventId = 0; // corr ID; 0 for none
        log.info("Logging " + detection.detectable.detectableId);
        ftcApi.logLongCardEvent2(2, eventId, detection.detectionTime.atZone(ZONE), false,
                2, detection.detectable.detectableId, 12345, "unifi.id", "unifi.id.zone.reception",
                "Card detected: Zone [Reception], #12345", "No details.");
        log.info("Logged " + detection.detectable.detectableId);
    }

    @Override
    public void notifyItemRegistered(String systemId, String itemId, String config) {
        ftcApi.notifyStatus("unifi.id", "unifi.id.zone.reception",
                1, false, false, "Unifi.id: Zone [Reception] is online.");
        ftcApi.notifyStatus("unifi.id", "unifi.id.zone.f1",
                1, false, true, "Unifi.id: Zone [Floor 1] is offline.");
        registerLatch.countDown();
    }

    @Override
    public void notifyItemDeregistered(String systemId, String itemId) {

    }

    @Override
    public void notifySystemRegistered(String systemId, String typeId, String config) {

    }

    @Override
    public void notifySystemDeregistered(String systemId) {

    }

    @Override
    public void notifyAlarmAcknowledged(String systemId, int eventId) {
    }

}
