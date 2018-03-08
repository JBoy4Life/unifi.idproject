package id.unifi.service.common.api.errors;

public class NotFound extends CoreMarshallableError {
    public final String type;

    public NotFound(String type) {
        super("not-found", type + " not found");
        this.type = type;
    }
}
