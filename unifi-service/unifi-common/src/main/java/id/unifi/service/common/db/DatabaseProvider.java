package id.unifi.service.common.db;

import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.config.UnifiConfigSource;
import org.jooq.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseProvider {
    private static final Logger log = LoggerFactory.getLogger(DatabaseProvider.class);

    private final Map<Schema, Database> databases;

    public DatabaseProvider() {
        this.databases = new ConcurrentHashMap<>();
    }

    public Database bySchema(Schema mainSchema, Schema... otherRequiredSchemas) {
        return databases.computeIfAbsent(mainSchema, name -> {
            var config =
                    Envy.configure(DatabaseConfig.class, UnifiConfigSource.getForPrefix("unifi." + mainSchema.getName()));
            var database = prepareSqlDatabase(mainSchema.getName(), config);
            for (var schema : otherRequiredSchemas) {
                prepareSqlDatabase(schema.getName(), config);
            }
            return database;
        });
    }

    private static Database prepareSqlDatabase(String schemaName, DatabaseConfig config) {
        var db = getSqlDatabase(schemaName, config);
        log.info("Migrating schema {}", schemaName);
        db.migrate();
        return db;
    }

    private static Database getSqlDatabase(String schemaName, DatabaseConfig config) {
        var dataSource = new DriverManagerDataSource(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPass());
        dataSource.setDriverClassName(config.getJdbcDriver().getName());
        var props = new Properties();
        props.setProperty("stringtype", "unspecified"); // to support non-standard stringy types like CITEXT in Postgres
        dataSource.setConnectionProperties(props);
        return new Database(schemaName, dataSource, config.getJooqDialect());
    }
}
