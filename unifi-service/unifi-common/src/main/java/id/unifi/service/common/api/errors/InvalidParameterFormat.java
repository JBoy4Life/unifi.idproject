package id.unifi.service.common.api.errors;

public class InvalidParameterFormat extends CoreMarshallableError {
    public final String name;

    public InvalidParameterFormat(String name, String message) {
        super("invalid-parameter-format", message);
        this.name = name;
    }
}
