package id.unifi.service.common.db;

import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.core.db.Core;
import static id.unifi.service.core.db.Core.CORE;
import org.jooq.Schema;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseProvider {
    private final Map<Schema, Database> databases;

    public DatabaseProvider() {
        this.databases = new ConcurrentHashMap<>();
    }

    public Database bySchema(Schema mainSchema, Schema... otherRequiredSchemas) {
        return databases.computeIfAbsent(mainSchema, name -> {
            DatabaseConfig config =
                    Envy.configure(DatabaseConfig.class, UnifiConfigSource.getForPrefix(mainSchema.getName()));
            Database database = prepareSqlDatabase(mainSchema.getName(), config);
            for (Schema schema : otherRequiredSchemas) {
                prepareSqlDatabase(schema.getName(), config);
            }
            return database;
        });
    }

    private static Database prepareSqlDatabase(String schemaName, DatabaseConfig config) {
        Database db = getSqlDatabase(schemaName, config);
        db.migrate();
        return db;
    }

    private static Database getSqlDatabase(String schemaName, DatabaseConfig config) {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPass());
        dataSource.setDriverClassName(config.getJdbcDriver().getName());
        Properties props = new Properties();
        props.setProperty("stringtype", "unspecified"); // to support non-standard stringy types like CITEXT in Postgres
        dataSource.setConnectionProperties(props);
        return new Database(schemaName, dataSource, config.getJooqDialect());
    }
}
