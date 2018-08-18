package id.unifi.service.common.api;

import id.unifi.service.common.api.errors.AbstractMarshallableError;
import id.unifi.service.common.api.errors.ValidationFailure;
import id.unifi.service.common.api.errors.ValidationFailure.Issue;
import id.unifi.service.common.api.errors.ValidationFailure.ValidationError;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Validation {
    private static final int SHORT_STRING_MAX_LENGTH = 64;
    private static final Pattern shortIdPattern = Pattern.compile("^[a-zA-Z0-9._-]+$");

    public static void validateAll(ValidationDef... validationDefs) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (var def : validationDefs) {
            if (def.issue != null) {
                if (def.immediateError != null) {
                    throw def.immediateError.get();
                } else {
                    validationErrors.add(new ValidationError(def.field, def.issue));
                }
            }
        }

        if (!validationErrors.isEmpty())
            throw new ValidationFailure(validationErrors);
    }

    public static ValidationDef v(String field, @Nullable Issue result) {
        return new ValidationDef(field, result, null);
    }

    public static ValidationDef v(String field, @Nullable String value, Function<String, Issue> validator) {
        return v(field, value == null ? null : validator.apply(value));
    }

    public static ValidationDef v(@Nullable Issue result, Supplier<AbstractMarshallableError> immediateError) {
        return new ValidationDef(null, result, immediateError);
    }

    public static @Nullable Issue shortId(String value) {
        if (value.isEmpty()) return Issue.TOO_SHORT;
        if (value.length() > SHORT_STRING_MAX_LENGTH) return Issue.TOO_LONG;
        if (!shortIdPattern.matcher(value).matches()) return Issue.BAD_FORMAT;
        return null;
    }

    public static @Nullable Issue shortString(String value) {
        if (value.length() > SHORT_STRING_MAX_LENGTH) return Issue.TOO_LONG;
        return null;
    }

    public static @Nullable Issue email(String value) {
        if (value.length() > SHORT_STRING_MAX_LENGTH) return Issue.TOO_LONG;
        if (!value.contains("@")) return Issue.BAD_FORMAT;
        return null;
    }

    public static @Nullable Issue atLeastOneNonNull(Object... values) {
        return Arrays.stream(values).noneMatch(Objects::nonNull) ? Issue.MISSING : null;
    }

    public static class ValidationDef {
        private final String field;
        private final Issue issue;
        private final Supplier<AbstractMarshallableError> immediateError;

        private ValidationDef(@Nullable String field,
                              Issue issue,
                              @Nullable Supplier<AbstractMarshallableError> immediateError) {
            this.field = field;
            this.issue = issue;
            this.immediateError = immediateError;
        }
    }
}
