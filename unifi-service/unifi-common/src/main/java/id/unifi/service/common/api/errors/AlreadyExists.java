package id.unifi.service.common.api.errors;

public class AlreadyExists extends CoreMarshallableError {
    public final String type;

    public AlreadyExists(String type) {
        super("already-exists", type + " already exists");
        this.type = type;
    }
}
