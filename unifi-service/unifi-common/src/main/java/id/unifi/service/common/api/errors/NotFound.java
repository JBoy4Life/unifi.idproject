package id.unifi.service.common.api.errors;

public class NotFound extends CoreMarshallableError {
    private final String type;

    public NotFound(String type) {
        super("not-found", "Not found");
        this.type = type;
    }
}
