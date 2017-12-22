package id.unifi.service.common.api.errors;

public class Unauthorized extends CoreMarshallableError {
    public Unauthorized() {
        super("unauthorized", "Unauthorized");
    }
}
