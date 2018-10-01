package id.unifi.service.common.types;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import com.google.common.base.Converter;

public class EnumUtils {
    private static final Converter<String, String> inputConverter = LOWER_HYPHEN.converterTo(UPPER_UNDERSCORE);
    private static final Converter<String, String> outputConverter = getInputConverter().reverse();

    public static Converter<String, String> getInputConverter() {
        return inputConverter;
    }

    public static Converter<String, String> getOutputConverter() {
        return outputConverter;
    }
}
