package id.unifi.service.dbcommon;

import static java.util.stream.Collectors.toUnmodifiableMap;
import org.jooq.Condition;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import static org.jooq.SQLDialect.POSTGRES;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.trueCondition;
import org.jooq.impl.DefaultDataType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class DatabaseUtils {
    public static final DataType<String> CITEXT = new DefaultDataType<>(POSTGRES, String.class, "public.citext");

    public static final Field<Instant> CURRENT_INSTANT = DSL.currentTimestamp().cast(Instant.class);

    public static <T> Condition filterCondition(Optional<T> filter, Function<T, Condition> condition) {
        return filter.map(condition).orElse(trueCondition());
    }

    public static <T> Optional<T> fieldValueOpt(Record r, TableField<?, T> tableField) {
        return r.field(tableField) == null ? Optional.empty() : Optional.ofNullable(r.get(tableField));
    }

    public static <R extends Record, C> Map<? extends TableField<R, ?>, ?> getUpdateQueryFieldMap(
            Map<? extends TableField<R, ?>, Function<C, ?>> editables,
            C changes) {
        return editables.entrySet().stream()
                .flatMap(e -> Stream.ofNullable(e.getValue().apply(changes)).map(v -> Map.entry(e.getKey(), v)))
                .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <R extends Record, T> Field<T> unqualified(TableField<R, T> field) {
        return field(name(field.getUnqualifiedName()), field.getType());
    }
}
