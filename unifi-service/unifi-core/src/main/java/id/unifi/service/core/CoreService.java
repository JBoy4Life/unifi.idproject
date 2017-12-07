package id.unifi.service.core;

import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.core.api.Dispatcher;
import id.unifi.service.core.api.HttpServer;
import id.unifi.service.core.api.ServiceRegistry;
import id.unifi.service.core.config.FileConfigSource;
import id.unifi.service.core.db.Database;
import id.unifi.service.core.db.DatabaseConfig;
import id.unifi.service.core.db.DatabaseUtils;
import id.unifi.service.core.version.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CoreService {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    private interface Config {
        @Default("8000")
        int httpPort();

        DatabaseConfig core();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Core");
        VersionInfo.log();

        Config config = Envy.configure(Config.class, FileConfigSource.get());
        Database db = DatabaseUtils.prepareSqlDatabase(DatabaseUtils.CORE_DB_NAME, config.core());

        ServiceRegistry registry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.services"),
                Map.of(Database.class, db));
        Dispatcher dispatcher = new Dispatcher(registry);
        HttpServer server = new HttpServer(config.httpPort(), dispatcher);
        server.start();
    }
}
