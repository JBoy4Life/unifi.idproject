package id.unifi.service.common.api;

import id.unifi.service.common.api.errors.ValidationFailure;
import id.unifi.service.common.api.errors.ValidationFailure.Issue;
import id.unifi.service.common.api.errors.ValidationFailure.ValidationError;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class Validation {
    private static final int SHORT_STRING_MAX_LENGTH = 64;
    private static final Pattern shortIdPattern = Pattern.compile("^[a-zA-Z0-9._-]+$");

    public static void validateAll(Map<String, Optional<Issue>> fieldIssues) {
        List<ValidationError> errors = fieldIssues.entrySet().stream()
                .flatMap(e -> e.getValue().map(issue -> new ValidationError(e.getKey(), issue)).stream())
                .collect(toList());
        if (!errors.isEmpty())
            throw new ValidationFailure(errors);
    }

    public static Optional<Issue> shortId(String value) {
        if (value.isEmpty()) return Optional.of(Issue.TOO_SHORT);
        if (value.length() > SHORT_STRING_MAX_LENGTH) return Optional.of(Issue.TOO_LONG);
        if (!shortIdPattern.matcher(value).matches()) return Optional.of(Issue.BAD_FORMAT);
        return Optional.empty();
    }

    public static Optional<Issue> shortString(String value) {
        if (value.length() > SHORT_STRING_MAX_LENGTH) return Optional.of(Issue.TOO_LONG);
        return Optional.empty();
    }

    public static Optional<Issue> email(String value) {
        if (value.length() > SHORT_STRING_MAX_LENGTH) return Optional.of(Issue.TOO_LONG);
        if (!value.contains("@")) return Optional.of(Issue.BAD_FORMAT);
        return Optional.empty();
    }
}
