package id.unifi.service.common.api.client;

import id.unifi.service.common.api.errors.MarshallableError;

public class UnmarshalledError extends MarshallableError {
    private final String protocolMessageType;

    public UnmarshalledError(String protocolMessageType, String message) {
        super(message);
        this.protocolMessageType = protocolMessageType;
    }

    public String getProtocolMessageType() {
        return protocolMessageType;
    }
}
