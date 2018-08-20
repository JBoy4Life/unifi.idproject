package id.unifi.service.common.api.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HttpMarshallableError extends MarshallableError {
    @JsonIgnore
    int getHttpStatusCode();
}
