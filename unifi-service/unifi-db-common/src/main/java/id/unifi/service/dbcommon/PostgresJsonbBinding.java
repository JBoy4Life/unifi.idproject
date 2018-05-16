package id.unifi.service.dbcommon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.impl.DSL;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

public class PostgresJsonbBinding implements Binding<Object, JsonNode> {
    private static final ObjectMapper mapper = new ObjectMapper(new MappingJsonFactory());

    private static final String JSONB_TYPE = "jsonb";

    public Converter<Object, JsonNode> converter() {
        return new Converter<>() {
            public JsonNode from(Object obj) {
                if (obj == null) return null;

                if (!(obj instanceof PGobject))
                    throw new IllegalArgumentException("Expected PGobject, got " + obj.getClass());

                var pgObj = (PGobject) obj;
                if (!JSONB_TYPE.equals(pgObj.getType()))
                    throw new IllegalArgumentException("Unexpected PGobject type: " + pgObj.getType());

                try {
                    return mapper.readTree(pgObj.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public Object to(JsonNode node) {
                if (node == null) return null;

                try {
                    return mapper.writeValueAsString(node);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            public Class<Object> fromType() {
                return Object.class;
            }

            public Class<JsonNode> toType() {
                return JsonNode.class;
            }
        };
    }

    public void sql(BindingSQLContext<JsonNode> ctx) {
        ctx.render().visit(DSL.val(ctx.convert(converter()).value())).sql("::jsonb");
    }

    public void register(BindingRegisterContext<JsonNode> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    public void set(BindingSetStatementContext<JsonNode> ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    public void get(BindingGetResultSetContext<JsonNode> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getObject(ctx.index()));
    }

    public void get(BindingGetStatementContext<JsonNode> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getObject(ctx.index()));
    }

    public void get(BindingGetSQLInputContext<JsonNode> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public void set(BindingSetSQLOutputContext<JsonNode> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
