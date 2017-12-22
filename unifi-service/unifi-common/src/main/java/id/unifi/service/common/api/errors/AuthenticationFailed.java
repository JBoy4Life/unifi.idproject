package id.unifi.service.common.api.errors;

public class AuthenticationFailed extends CoreMarshallableError {
    public AuthenticationFailed() {
        super("authentication-failed", "Authentication failed");
    }
}
