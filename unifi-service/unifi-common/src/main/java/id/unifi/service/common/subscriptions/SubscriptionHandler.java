package id.unifi.service.common.subscriptions;

import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

public class SubscriptionHandler<T> {
    private final Session session;
    private final ByteBuffer correlationId;
    private final Consumer<T> consumer;

    public SubscriptionHandler(Session session, byte[] correlationId, Consumer<T> consumer) {
        this.session = session;
        this.correlationId = ByteBuffer.wrap(correlationId).asReadOnlyBuffer();
        this.consumer = consumer;
    }

    public ByteBuffer getCorrelationId() {
        return correlationId;
    }

    public Session getSession() {
        return session;
    }

    public void accept(T message) {
        consumer.accept(message);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionHandler<?> that = (SubscriptionHandler<?>) o;
        return Objects.equals(session, that.session) &&
                Objects.equals(correlationId, that.correlationId);
    }

    public int hashCode() {
        return Objects.hash(session, correlationId);
    }
}
