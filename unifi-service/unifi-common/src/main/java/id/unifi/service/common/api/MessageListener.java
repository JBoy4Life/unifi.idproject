package id.unifi.service.common.api;

import org.eclipse.jetty.websocket.api.Session;

import java.util.function.BiConsumer;

public interface MessageListener<T> extends BiConsumer<String, T> {
    void accept(String messageType, T payload);

    Session getSession();

    byte[] getCorrelationId();
}
