package id.unifi.service.common.api.client;

import id.unifi.service.common.api.errors.AbstractMarshallableError;

public class UnmarshalledError extends AbstractMarshallableError {
    private final String protocolMessageType;

    public UnmarshalledError(String protocolMessageType, String message) {
        super(message);
        this.protocolMessageType = protocolMessageType;
    }

    public String getProtocolMessageType() {
        return protocolMessageType;
    }
}
