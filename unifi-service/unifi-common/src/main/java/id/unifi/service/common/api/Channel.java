package id.unifi.service.common.api;

import java.nio.ByteBuffer;

public interface Channel {
    void send(ByteBuffer payload);
    void send(String payload);
}
