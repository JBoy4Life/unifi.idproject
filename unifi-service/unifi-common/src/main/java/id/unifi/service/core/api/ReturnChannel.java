package id.unifi.service.core.api;

import java.nio.ByteBuffer;

public interface ReturnChannel {
    void send(ByteBuffer payload);
}
