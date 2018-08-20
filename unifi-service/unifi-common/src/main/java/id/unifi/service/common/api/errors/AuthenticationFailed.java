package id.unifi.service.common.api.errors;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class AuthenticationFailed extends CoreMarshallableError implements HttpMarshallableError {
    public AuthenticationFailed() {
        this("Authentication failed");
    }

    public AuthenticationFailed(String message) {
        super("authentication-failed", message);
    }

    public int getHttpStatusCode() {
        return SC_UNAUTHORIZED;
    }
}
