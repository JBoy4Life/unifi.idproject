package id.unifi.service.common.db;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Properties;

public class DatabaseUtils {
    public static final String CORE_DB_NAME = "core";

    public static Database prepareSqlDatabase(String dbName, DatabaseConfig config) {
        Database db = getSqlDatabase(dbName, config);
        db.execute(sql -> sql.execute("CREATE EXTENSION IF NOT EXISTS citext"));
        db.migrate();
        return db;
    }

    public static Database getSqlDatabase(String dbName, DatabaseConfig config) {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPass());
        dataSource.setDriverClassName(config.getJdbcDriver().getName());
        Properties props = new Properties();
        props.setProperty("stringtype", "unspecified"); // to support non-standard stringy types like CITEXT in Postgres
        dataSource.setConnectionProperties(props);
        return new Database(dbName, dataSource, config.getJooqDialect());
    }
}
