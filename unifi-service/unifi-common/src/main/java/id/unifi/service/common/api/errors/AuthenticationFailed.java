package id.unifi.service.common.api.errors;

public class AuthenticationFailed extends CoreMarshallableError {
    public AuthenticationFailed() {
        this("Authentication failed");
    }

    public AuthenticationFailed(String message) {
        super("authentication-failed", message);
    }
}
