package id.unifi.service.common.db;

import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.config.UnifiConfigSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseProvider {
    public static final String CORE_SCHEMA_NAME = "core";
    
    private final Map<String, Database> databases;

    public DatabaseProvider() {
        this.databases = new ConcurrentHashMap<>();
    }

    public Database bySchemaName(String schemaName) {
        return databases.computeIfAbsent(schemaName, name -> {
            DatabaseConfig config = Envy.configure(DatabaseConfig.class, UnifiConfigSource.getForPrefix(name));
            return prepareSqlDatabase(name, config);
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
