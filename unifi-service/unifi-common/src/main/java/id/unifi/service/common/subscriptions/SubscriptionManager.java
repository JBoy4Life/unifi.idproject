package id.unifi.service.common.subscriptions;

import org.eclipse.jetty.websocket.api.Session;

public interface SubscriptionManager {
    <T> void addSubscription(Topic<T> topic, SubscriptionHandler<T> handler);

    void removeSubscription(Session session, byte[] correlationId);

    <T> void distributeMessage(Topic<T> topic, T message);

    void addSession(Session session);

    void removeSession(Session session);
}
