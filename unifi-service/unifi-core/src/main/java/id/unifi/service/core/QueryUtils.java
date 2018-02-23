package id.unifi.service.core;

import org.jooq.Condition;
import static org.jooq.impl.DSL.trueCondition;

import java.util.Optional;
import java.util.function.Function;

public class QueryUtils {
    public static <T> Condition filterCondition(Optional<T> filter, Function<T, Condition> condition) {
        return filter.map(condition).orElse(trueCondition());
    }
}
