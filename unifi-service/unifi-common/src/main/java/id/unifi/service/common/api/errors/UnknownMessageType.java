package id.unifi.service.common.api.errors;

public class UnknownMessageType extends CoreMarshallableError {
    public final String messageType;

    public UnknownMessageType(String messageType) {
        super("unknown-message-type", "Unknown message type: " + messageType);
        this.messageType = messageType;
    }
}
