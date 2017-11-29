package id.unifi.service.core;

import com.statemachinesystems.envy.Envy;
import id.unifi.service.core.config.FileConfigSource;
import id.unifi.service.core.db.Tables;
import id.unifi.service.core.db.Database;
import id.unifi.service.core.db.DatabaseConfig;
import id.unifi.service.core.db.DatabaseUtils;
import static id.unifi.service.core.db.Tables.CLIENT;
import id.unifi.service.core.version.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

public class CoreService {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    private interface Config {
        DatabaseConfig core();
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Core");
        VersionInfo.log();

        Config config = Envy.configure(Config.class, FileConfigSource.get());
        Database db = DatabaseUtils.prepareSqlDatabase(DatabaseUtils.CORE_DB_NAME, config.core());
        try {
            db.execute(sql -> {
                sql.insertInto(CLIENT)
                        .columns(CLIENT.CLIENT_ID, CLIENT.DISPLAY_NAME, CLIENT.LOGO)
                        .values("acme", "Acme Ltd", new byte[0])
                        .execute();
                return null;
            });
        } catch (DuplicateKeyException ignored) {}
    }
}
