package id.unifi.service.common.api;

import java.nio.ByteBuffer;

public interface ReturnChannel {
    void send(ByteBuffer payload);
}
