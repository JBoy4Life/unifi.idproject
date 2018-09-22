package id.unifi.service.common.api.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public enum UnauthorizedReason {
    SESSION, PERMISSION;

    private static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);
    private final String stringName;

    UnauthorizedReason() {
        this.stringName = UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }

    @JsonCreator
    public static UnauthorizedReason fromString(String status) {
        return status == null ? null : valueOf(inputConverter.convert(status));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }
}
