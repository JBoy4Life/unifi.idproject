package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class Unauthorized extends CoreMarshallableError implements HttpMarshallableError {
    public final UnauthorizedReason reason;

    public Unauthorized() {
        this(UnauthorizedReason.SESSION);
    }

    public Unauthorized(UnauthorizedReason reason) {
        super("unauthorized", "Unauthorized: " + reason);
        this.reason = reason;
    }

    public int getHttpStatusCode() {
        return SC_UNAUTHORIZED;
    }
}
