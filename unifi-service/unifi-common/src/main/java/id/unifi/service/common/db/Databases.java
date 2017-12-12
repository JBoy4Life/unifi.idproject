package id.unifi.service.common.db;

import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.config.UnifiConfigSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Databases {
    private final Map<String, Database> databases;

    public Databases() {
        this.databases = new HashMap<>();
    }

    Database named(String dbName) {
        return databases.computeIfAbsent(dbName, name -> {
            DatabaseConfig config = Envy.configure(DatabaseConfig.class, UnifiConfigSource.getForPrefix(dbName));
            return prepareSqlDatabase(name, config);
        });
    }

    private static Database prepareSqlDatabase(String dbName, DatabaseConfig config) {
        Database db = getSqlDatabase(dbName, config);
        db.execute(sql -> sql.execute("CREATE EXTENSION IF NOT EXISTS citext"));
        db.migrate();
        return db;
    }

    private static Database getSqlDatabase(String dbName, DatabaseConfig config) {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPass());
        dataSource.setDriverClassName(config.getJdbcDriver().getName());
        Properties props = new Properties();
        props.setProperty("stringtype", "unspecified"); // to support non-standard stringy types like CITEXT in Postgres
        dataSource.setConnectionProperties(props);
        return new Database(dbName, dataSource, config.getJooqDialect());
    }
}
