package id.unifi.service.common.api.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MarshallableError extends RuntimeException {
    public MarshallableError(String message) {
        super(message);
    }

    // TODO: write a custom serializer instead of ignoring fields
    @JsonIgnore
    public abstract String getProtocolMessageType();

    @JsonIgnore
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    @JsonIgnore
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @JsonIgnore
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }
}
