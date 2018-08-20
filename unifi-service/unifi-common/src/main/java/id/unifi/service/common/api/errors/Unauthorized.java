package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class Unauthorized extends CoreMarshallableError implements HttpMarshallableError {
    public Unauthorized() {
        super("unauthorized", "Unauthorized");
    }

    public int getHttpStatusCode() {
        return SC_UNAUTHORIZED;
    }
}
