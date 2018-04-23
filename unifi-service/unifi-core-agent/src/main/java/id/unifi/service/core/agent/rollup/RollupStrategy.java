package id.unifi.service.core.agent.rollup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public enum RollupStrategy {
    NONE, TIME_SLOT;

    private static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);

    private final String stringName;

    RollupStrategy() {
        this.stringName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name());
    }

    @JsonCreator
    public static RollupStrategy fromString(String type) {
        return valueOf(inputConverter.convert(type));
    }

    @JsonValue
    public String toString() {
        return stringName;
    }
}
