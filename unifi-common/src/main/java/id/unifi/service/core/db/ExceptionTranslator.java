package id.unifi.service.core.db;

import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import java.util.HashMap;
import java.util.Map;

class ExceptionTranslator extends DefaultExecuteListener {
    private static final Map<SQLDialect, String> productNames = new HashMap<>();

    static {
        // see Spring's sql-error-codes.xml for the list of supported products
        productNames.put(SQLDialect.POSTGRES, "PostgreSQL");
    }

    private static String productName(ExecuteContext ctx) {
        return productNames.get(ctx.configuration().dialect());
    }

    @Override
    public void exception(ExecuteContext ctx) {
        SQLExceptionTranslator exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(productName(ctx));
        ctx.exception(exceptionTranslator.translate("jOOQ", ctx.sql(), ctx.sqlException()));
    }
}
