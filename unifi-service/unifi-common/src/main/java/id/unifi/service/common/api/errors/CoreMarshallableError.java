package id.unifi.service.common.api.errors;

public class CoreMarshallableError extends AbstractMarshallableError {
    private final String code;

    CoreMarshallableError(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getProtocolMessageType() {
        return "core.error." + code;
    }
}
