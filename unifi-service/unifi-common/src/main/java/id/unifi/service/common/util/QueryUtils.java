package id.unifi.service.common.util;

import id.unifi.service.common.api.errors.ValidationFailure;
import static java.util.stream.Collectors.toMap;
import org.jooq.Condition;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.TableField;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.trueCondition;
import org.jooq.impl.DefaultDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class QueryUtils {
    private static final Logger log = LoggerFactory.getLogger(QueryUtils.class);

    public static final DataType<String> CITEXT = new DefaultDataType<>(SQLDialect.POSTGRES, String.class, "citext");

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

        return Optional.of(new ImageWithType(data, mimeType));
    }

    public static Optional<ImageWithType> validateImageFormat(Optional<byte[]> image) {
        return image.map(im -> QueryUtils.imageWithType(im).orElseThrow(() -> new ValidationFailure(
                List.of(new ValidationFailure.ValidationError("image", ValidationFailure.Issue.BAD_FORMAT)))));
    }

    public static <R extends Record, C> Map<? extends TableField<R, ?>, ?> getUpdateQueryFieldMap(
            Map<? extends TableField<R, ?>, Function<C, ?>> editables,
            C changes) {
        return editables.entrySet().stream()
                .flatMap(e -> Stream.ofNullable(e.getValue().apply(changes)).map(v -> Map.entry(e.getKey(), v)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <R extends Record, T> Field<T> unqualified(TableField<R, T> field) {
        return field(name(field.getUnqualifiedName()), field.getType());
    }

    public static class ImageWithType {
        public final String mimeType;
        public final byte[] data;

        public ImageWithType(byte[] data, String mimeType) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }
}
