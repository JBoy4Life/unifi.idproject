package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class InvalidParameterFormat extends CoreMarshallableError implements HttpMarshallableError {
    public final String name;

    public InvalidParameterFormat(String name, String message) {
        super("invalid-parameter-format", message);
        this.name = name;
    }

    public int getHttpStatusCode() {
        return SC_BAD_REQUEST;
    }
}
