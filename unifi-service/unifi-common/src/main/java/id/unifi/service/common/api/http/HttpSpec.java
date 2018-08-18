package id.unifi.service.common.api.http;

import org.eclipse.jetty.http.HttpMethod;

import java.util.List;

public class HttpSpec {
    public final HttpMethod method;
    public final List<PathSegment> segments;

    public HttpSpec(HttpMethod method, List<PathSegment> segments) {
        this.method = method;
        this.segments = segments;
    }
}
