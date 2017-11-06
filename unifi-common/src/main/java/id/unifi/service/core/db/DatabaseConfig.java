package id.unifi.service.core.db;

import com.statemachinesystems.envy.Default;
import org.jooq.SQLDialect;

public interface DatabaseConfig {
    String getJdbcUrl();

    String getJdbcUser();

    @Default("")
    String getJdbcPass();

    @Default("org.postgresql.Driver")
    Class<?> getJdbcDriver();

    @Default("POSTGRES")
    SQLDialect getJooqDialect();
}
