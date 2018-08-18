package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class NotFound extends CoreMarshallableError implements HttpMarshallableError {
    public final String type;

    public NotFound(String type) {
        super("not-found", type + " not found");
        this.type = type;
    }

    public int getHttpStatusCode() {
        return SC_NOT_FOUND;
    }
}
