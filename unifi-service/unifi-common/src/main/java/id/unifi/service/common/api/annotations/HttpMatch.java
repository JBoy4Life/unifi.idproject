package id.unifi.service.common.api.annotations;

import org.eclipse.jetty.http.HttpMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines an HTTP endpoint for an API operation. `path` should have the format `[segment-1]/[segment-2]/...`, where
 * each segment of the form ':[param-name]' is interpreted as a parameter. All other segments are constant values.
 * Paths are URL-encoded, so it's possible to escape the leading ':' to force a value segment.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMatch {
    String path();
    HttpMethod method() default HttpMethod.GET;
}
