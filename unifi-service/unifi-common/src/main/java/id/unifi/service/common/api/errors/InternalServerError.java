package id.unifi.service.common.api.errors;

public class InternalServerError extends CoreMarshallableError {
    public InternalServerError() {
        super("internal-server-error", "Internal server error");
    }

    public InternalServerError(String message) {
        super("internal-server-error", message);
    }
}
