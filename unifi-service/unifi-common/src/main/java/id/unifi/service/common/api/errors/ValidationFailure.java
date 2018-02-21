package id.unifi.service.common.api.errors;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;

import java.util.List;

public class ValidationFailure extends CoreMarshallableError {
    public final List<ValidationError> errors;

    public ValidationFailure(List<ValidationError> errors) {
        super("validation-failure", "Field validation failed");
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failure errors must not be empty");
        }
        this.errors = errors;
    }

    public enum Issue {
        MISSING, TOO_SHORT, TOO_LONG, BAD_FORMAT;

        private final String stringName;

        Issue() {
            this.stringName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name());
        }

        @JsonValue
        public String toString() {
            return stringName;
        }
    }

    public static class ValidationError {
        public final String field;
        public final Issue issue;

        public ValidationError(String field, Issue issue) {
            this.field = field;
            this.issue = issue;
        }
    }
}
