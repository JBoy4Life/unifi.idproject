package id.unifi.service.core.agent.config;

import id.unifi.service.core.agent.AgentConfigPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Decorates an existing ConfigAdapter with persistence including an interim config source in case the server
 * doesn't send agent configuration in time.
 */
public class ProductionConfigWrapper implements ConfigAdapter {
    private static final Logger log = LoggerFactory.getLogger(ProductionConfigWrapper.class);

    private final Future<?> alternativeConfigFuture;
    private final AgentConfigPersistence persistence;
    private final ConfigAdapter delegate;

    public ProductionConfigWrapper(AgentConfigPersistence persistence,
                                   Duration serverConfigReceiveTimeout,
                                   ConfigAdapter delegate) {
        var backupConfigScheduler = Executors.newSingleThreadScheduledExecutor();

        this.delegate = delegate;
        this.persistence = persistence;
        this.alternativeConfigFuture = backupConfigScheduler.schedule(() ->
                        persistence.readConfig().ifPresentOrElse(
                                config -> configure(config, false),
                                () -> log.info("No locally persisted config found.")),
                serverConfigReceiveTimeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void configure(AgentFullConfig config, boolean authoritative) {
        if (authoritative) alternativeConfigFuture.cancel(false);
        log.info("Agent config received (authoritative: {}): {}", authoritative, config);
        if (authoritative) persistence.writeConfig(config);
        delegate.configure(config, authoritative);
    }
}
