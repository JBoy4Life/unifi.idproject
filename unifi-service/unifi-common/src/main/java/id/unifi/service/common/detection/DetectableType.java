package id.unifi.service.common.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public enum DetectableType {
    UHF_EPC, UHF_TID;

    private static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);

    private final String stringName;

    DetectableType() {
        this.stringName = UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }

    @JsonCreator
    public static DetectableType fromString(String status) {
        return valueOf(inputConverter.convert(status));
    }

    public String toString() {
        return stringName;
    }
}
