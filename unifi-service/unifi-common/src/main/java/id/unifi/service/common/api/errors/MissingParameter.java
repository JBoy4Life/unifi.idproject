package id.unifi.service.common.api.errors;

public class MissingParameter extends CoreMarshallableError {
    public final String name;
    public final String type;

    public MissingParameter(String name, String type) {
        super("missing-parameter", "Missing parameter " + name + " of type " + type);
        this.name = name;
        this.type = type;
    }
}
