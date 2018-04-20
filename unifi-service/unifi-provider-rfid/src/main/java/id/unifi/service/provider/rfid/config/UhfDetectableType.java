package id.unifi.service.provider.rfid.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public enum UhfDetectableType {
    UHF_EPC, UHF_TID;

    private static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);

    private final String stringName;

    UhfDetectableType() {
        this.stringName = UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }

    @JsonCreator
    public static UhfDetectableType fromString(String type) {
        return valueOf(inputConverter.convert(type));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }

}
