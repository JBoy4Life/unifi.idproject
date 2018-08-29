package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class MissingParameter extends CoreMarshallableError implements HttpMarshallableError {
    public final String name;
    public final String type;

    public MissingParameter(String name, String type) {
        super("missing-parameter", "Missing parameter " + name + " of type " + type);
        this.name = name;
        this.type = type;
    }

    public int getHttpStatusCode() {
        return SC_BAD_REQUEST;
    }
}
