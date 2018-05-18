package id.unifi.service.core;

import id.unifi.service.common.api.VerticalConfigForApi;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.CLIENT_CONFIG;
import static id.unifi.service.core.db.Tables.CLIENT_VERTICAL;
import id.unifi.service.core.db.tables.records.ClientConfigRecord;
import id.unifi.service.core.db.tables.records.ClientVerticalRecord;
import id.unifi.service.core.verticalconfig.CoreVerticalConfigForApi;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VerticalConfigManager {
    private static final Logger log = LoggerFactory.getLogger(VerticalConfigManager.class);

    private static final Duration ASSIGNMENT_REFRESH_RATE = Duration.ofMinutes(1);

    private final Database db;
    private final ScheduledExecutorService refreshScheduler;
    private AllConfig config;

    public VerticalConfigManager(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
        refresh();
        this.refreshScheduler = Executors.newSingleThreadScheduledExecutor();
        this.refreshScheduler.scheduleAtFixedRate(this::refresh,
                ASSIGNMENT_REFRESH_RATE.toSeconds(), ASSIGNMENT_REFRESH_RATE.toSeconds(), TimeUnit.SECONDS);
    }

    public Map<String, VerticalConfigForApi> getClientSideConfig(String clientId) {
        var config = this.config;
        var enabledVerticals = Optional.ofNullable(config.clientEnabledVerticals.get(clientId)).orElse(Set.of());
        var coreConfig = Optional.ofNullable(config.coreConfig.get(clientId)).orElse(new ClientConfigRecord());
        fillDefaults(coreConfig);

        Map<String, VerticalConfigForApi> clientSideConfig =
                enabledVerticals.stream().collect(toMap(identity(), v -> new VerticalConfigForApi() {}));
        clientSideConfig.put("core", new CoreVerticalConfigForApi(coreConfig));

        return clientSideConfig;
    }

    private void refresh() {
        log.info("Refreshing vertical config");
        this.config = db.execute(sql -> {
            var clientEnabledVerticals = sql.selectFrom(CLIENT_VERTICAL).stream()
                    .collect(groupingBy(ClientVerticalRecord::getClientId,
                            mapping(ClientVerticalRecord::getVerticalId, toSet())));
            var clientConfig = sql.selectFrom(CLIENT_CONFIG).stream()
                    .collect(toUnmodifiableMap(ClientConfigRecord::getClientId, identity()));
            return new AllConfig(clientEnabledVerticals, clientConfig);
        });
    }

    private static void fillDefaults(ClientConfigRecord config) {
        if (config.getLiveViewEnabled() == null) config.setLiveViewEnabled(true);
    }

    private static class AllConfig {
        final Map<String, Set<String>> clientEnabledVerticals;
        final Map<String, ClientConfigRecord> coreConfig;

        AllConfig(Map<String, Set<String>> clientEnabledVerticals,
                         Map<String, ClientConfigRecord> coreConfig) {
            this.clientEnabledVerticals = clientEnabledVerticals;
            this.coreConfig = coreConfig;
        }
    }
}
