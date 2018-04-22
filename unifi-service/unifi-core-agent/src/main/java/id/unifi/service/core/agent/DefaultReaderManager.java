package id.unifi.service.core.agent;

import id.unifi.service.common.detection.DetectableType;
import static id.unifi.service.common.detection.DetectableType.UHF_EPC;
import static id.unifi.service.common.detection.DetectableType.UHF_TID;
import id.unifi.service.core.agent.config.AgentFullConfig;
import id.unifi.service.provider.rfid.RfidProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultReaderManager implements ReaderManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultReaderManager.class);

    private static final Set<DetectableType> DEFAULT_DETECTABLE_TYPES = Set.of(UHF_EPC, UHF_TID);
    private static volatile Set<DetectableType> detectableTypes = DEFAULT_DETECTABLE_TYPES; // FIXME: factor out

    private final RfidProvider rfidProvider;
    private final AgentConfigPersistence persistence;
    private volatile boolean configured;
    private final ScheduledFuture<?> alternativeConfigFuture;

    public DefaultReaderManager(AgentConfigPersistence persistence,
                                RfidProvider rfidProvider,
                                Duration serverConfigReceiveTimeout) {
        this.persistence = persistence;
        this.rfidProvider = rfidProvider;
        ScheduledExecutorService backupConfigScheduler = Executors.newSingleThreadScheduledExecutor();
        alternativeConfigFuture = backupConfigScheduler.schedule(() ->
                        persistence.readConfig().ifPresentOrElse(
                                config -> configure(config, false),
                                () -> log.info("No locally persisted config found.")),
                serverConfigReceiveTimeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void configure(AgentFullConfig config) {
        configure(config, true);
    }

    public static Set<DetectableType> getDetectableTypes() {
        return detectableTypes;
    }

    private synchronized void configure(AgentFullConfig config, boolean authoritative) {
        if (config.readers.isEmpty()) {
            log.info("Ignoring empty reader config (authoritative: {})", authoritative);
            return;
        }

        // Apply config to RFID provider only if it's from the server (i.e. authoritative) or we timed out
        // and are applying previously persisted backup config.
        if (authoritative || !configured) {
            alternativeConfigFuture.cancel(false);
            log.info("Agent config received (authoritative: {}): {}", authoritative, config);
            configured = true;
            if (authoritative) persistence.writeConfig(config); // TODO: factor out
            rfidProvider.configure(config.readers);
            detectableTypes = config.agent.flatMap(a -> a.detectableTypes).orElse(DEFAULT_DETECTABLE_TYPES);
        }
    }
}
