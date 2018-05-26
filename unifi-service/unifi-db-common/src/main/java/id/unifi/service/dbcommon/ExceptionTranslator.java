package id.unifi.service.dbcommon;

import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import java.util.Map;

class ExceptionTranslator extends DefaultExecuteListener {
    private ExceptionTranslator() {}

    // see Spring's sql-error-codes.xml for the list of supported products
    private static final Map<SQLDialect, String> productNames = Map.of(
            SQLDialect.POSTGRES, "PostgreSQL"
    );

    private static final ExceptionTranslator instance = new ExceptionTranslator();

    private static String productName(ExecuteContext ctx) {
        return productNames.get(ctx.configuration().dialect());
    }

    public static ExceptionTranslator getInstance() {
        return instance;
    }

    @Override
    public void exception(ExecuteContext ctx) {
        SQLExceptionTranslator exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(productName(ctx));
        ctx.exception(exceptionTranslator.translate("jOOQ", ctx.sql(), ctx.sqlException()));
    }
}
