package id.unifi.service.core;

import static java.util.stream.Collectors.toMap;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.TableField;
import static org.jooq.impl.DSL.trueCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class QueryUtils {
    private static final Logger log = LoggerFactory.getLogger(QueryUtils.class);

    public static <T> Condition filterCondition(Optional<T> filter, Function<T, Condition> condition) {
        return filter.map(condition).orElse(trueCondition());
    }

    public static <T> Optional<T> fieldValueOpt(Record r, TableField<?, T> tableField) {
        return r.field(tableField) == null ? Optional.empty() : Optional.ofNullable(r.get(tableField));
    }

    public static Optional<ImageWithType> imageWithType(byte[] data) {
        String mimeType;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
        } catch (IOException ignored) {
            return Optional.empty();
        }

        if ("application/xml".equals(mimeType)) mimeType = "image/svg+xml";

        if (mimeType == null || !mimeType.startsWith("image/")) {
            log.warn("Ignoring image of unrecognizable type: {}", mimeType);
            return Optional.empty();
        }

        return Optional.of(new ImageWithType(mimeType, data));
    }

    public static <R extends Record, C> Map<? extends TableField<R, ?>, ?> getUpdateQueryFieldMap(
            Map<? extends TableField<R, ?>, Function<C, ?>> editables,
            C changes) {
        return editables.entrySet().stream()
                .flatMap(e -> Stream.ofNullable(e.getValue().apply(changes)).map(v -> Map.entry(e.getKey(), v)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static class ImageWithType {
        public final String mimeType;
        public final byte[] data;

        public ImageWithType(String mimeType, byte[] data) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }
}
