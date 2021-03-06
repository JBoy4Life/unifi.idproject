package id.unifi.service.dbcommon;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Function;

public class Database {
    private static final Settings jooqSettings = new Settings();

    private final String schemaName;
    private final DataSource dataSource;
    private final SQLDialect dialect;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate txTemplate;

    Database(String schemaName, DataSource dataSource, SQLDialect dialect) {
        this.schemaName = schemaName;
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.txTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    /**
     * Migrates the DB's schema to the latest version.
     */
    public void migrate() {
        getMigrator().migrate();
    }

    /**
     * Drops all tables.
     */
    public void clean() {
        getMigrator().clean();
    }

    public <T> T execute(Function<DSLContext, T> callback) {
        return txTemplate.execute(status ->
                jdbcTemplate.execute((ConnectionCallback<T>) connection ->
                        callback.apply(createDSLContext(connection))));
    }

    private Flyway getMigrator() {
        var migrator = new Flyway();
        migrator.setDataSource(dataSource);
        migrator.setSchemas(schemaName);

        var dialectPathName = dialect.toString().toLowerCase();
        migrator.setLocations("classpath:migrations/" + schemaName + "/" + dialectPathName);

        return migrator;
    }

    private DSLContext createDSLContext(Connection connection) {
        return DSL.using(new DefaultConfiguration()
                .set(dialect)
                .set(jooqSettings)
                .set(new DefaultConnectionProvider(connection))
                .set(new DefaultExecuteListenerProvider(ExceptionTranslator.getInstance())));
    }
}
