package id.unifi.service.attendance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public enum OverriddenStatus {
    PRESENT, ABSENT, AUTH_ABSENT;

    public static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);
    private final String stringName;

    OverriddenStatus() {
        this.stringName = UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }

    @JsonCreator
    public static OverriddenStatus fromString(String status) {
        return valueOf(inputConverter.convert(status));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }
}
