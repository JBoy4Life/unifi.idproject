package id.unifi.service.dbcommon;

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
