package id.unifi.service.core.agent;

import id.unifi.service.common.detection.ReaderConfig;
import id.unifi.service.provider.rfid.RfidProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultReaderManager implements ReaderManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultReaderManager.class);

    private final RfidProvider rfidProvider;
    private final ReaderConfigPersistence persistence;
    private volatile boolean configured;
    private final ScheduledFuture<?> alternativeConfigFuture;

    public DefaultReaderManager(ReaderConfigPersistence persistence,
                                RfidProvider rfidProvider,
                                Duration serverConfigReceiveTimeout) {
        this.persistence = persistence;
        this.rfidProvider = rfidProvider;
        ScheduledExecutorService backupConfigScheduler = Executors.newSingleThreadScheduledExecutor();
        alternativeConfigFuture = backupConfigScheduler.schedule(() -> configure(persistence.readConfig(), false),
                serverConfigReceiveTimeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void configure(List<ReaderConfig> readers) {
        configure(readers, true);
    }

    private synchronized void configure(List<ReaderConfig> readers, boolean authoritative) {
        if (readers.isEmpty()) {
            log.info("Ignoring empty reader config (authoritative: {})", authoritative);
            return;
        }

        // Apply config to RFID provider only if it's from the server (i.e. authoritative) or we timed out
        // and are applying previously persisted backup config.
        if (authoritative || !configured) {
            alternativeConfigFuture.cancel(false);
            log.info("Reader config received (authoritative: {}): {}", authoritative, readers);
            configured = true;
            if (authoritative) persistence.writeConfig(readers);
            rfidProvider.configure(readers);
        }
    }
}
