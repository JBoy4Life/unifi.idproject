package id.unifi.service.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public enum HolderType {
    CONTACT;

    private static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);

    private final String stringName;

    HolderType() {
        this.stringName = UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }

    @JsonCreator
    public static HolderType fromString(String type) {
        return valueOf(inputConverter.convert(type));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }

}
