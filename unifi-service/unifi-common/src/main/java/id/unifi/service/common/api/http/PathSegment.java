package id.unifi.service.common.api.http;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public class PathSegment {
    private final String paramName;
    private final String value;

    public static PathSegment param(String paramName) {
        return new PathSegment(paramName, null);
    }

    public static PathSegment value(String value) {
        return new PathSegment(null, value);
    }

    public String getParamName() {
        if (paramName == null) throw new NoSuchElementException("Not a parameter segment");
        return paramName;
    }

    public String getValue() {
        if (value == null) throw new NoSuchElementException("Not a value segment");
        return value;
    }

    private PathSegment(@Nullable String paramName, @Nullable String value) {
        this.paramName = paramName;
        this.value = value;
    }

    public boolean isParam() {
        return paramName != null;
    }
}
