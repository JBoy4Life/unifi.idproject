package id.unifi.service.common.subscriptions;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import id.unifi.service.common.api.errors.AlreadyExists;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InMemorySubscriptionManager implements SubscriptionManager {
    private static final Logger log = LoggerFactory.getLogger(InMemorySubscriptionManager.class);

    private final Map<Session, Set<SubscriptionHandler<?>>> handlersBySession;
    private final Map<ByteBuffer, SubscriptionHandler<?>> handlersByCorrelationId;
    private final SetMultimap<Topic<?>, SubscriptionHandler<?>> handlersByTopic;
    private final Map<SubscriptionHandler<?>, Topic<?>> topicsByHandler;
    private final Object assignmentsMonitor;

    public InMemorySubscriptionManager() {
        this.assignmentsMonitor = new Object();
        this.handlersBySession = new HashMap<>();
        this.handlersByCorrelationId = new HashMap<>();
        this.handlersByTopic = MultimapBuilder.hashKeys().hashSetValues().build();
        this.topicsByHandler = new HashMap<>();
    }

    public <T> void addSubscription(Topic<T> topic, SubscriptionHandler<T> handler) {
        synchronized (assignmentsMonitor) {
            var assignedTopic = topicsByHandler.get(handler);
            if (assignedTopic != null) {
                if (assignedTopic.equals(topic)) {
                    return;
                } else {
                    throw new AlreadyExists("subscription");
                }
            }

            var sessionHandlers = handlersBySession.get(handler.getSession());
            if (sessionHandlers == null) {
                log.debug("Skipping dead session {}", handler.getSession());
                return;
            }

            sessionHandlers.add(handler);
            handlersByCorrelationId.put(handler.getCorrelationId(), handler);
            handlersByTopic.put(topic, handler);
            topicsByHandler.put(handler, topic);
        }
    }

    public void removeSubscription(Session session, byte[] correlationId) {
        synchronized (assignmentsMonitor) {
            var correlationIdBytes = bytesWrapper(correlationId);
            SubscriptionHandler<?> handler = handlersByCorrelationId.get(correlationIdBytes);
            if (handler == null) return;
            if (!handler.getSession().equals(session)) {
                // Trying to unsubscribe on someone else's correlation ID
                log.debug("Rejecting unsubscribe request, correlation ID not owned by current session ({})",
                        session.getRemoteAddress());
                return;
            }

            var sessionHandlers = handlersBySession.get(handler.getSession());
            if (sessionHandlers != null) sessionHandlers.remove(handler);

            handlersByCorrelationId.remove(correlationIdBytes);
            Topic<?> removedTopic = topicsByHandler.remove(handler);
            if (removedTopic != null) handlersByTopic.remove(removedTopic, handler);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void distributeMessage(Topic<T> topic, T message) {
        handlersByTopic.get(topic).forEach(h -> ((SubscriptionHandler<T>) h).accept(message));
    }

    public void addSession(Session session) {
        log.debug("New session: {}", session.getRemoteAddress());
        synchronized (assignmentsMonitor) {
            if (handlersBySession.get(session) != null) throw new AssertionError();
            handlersBySession.put(session, new HashSet<>());
        }
    }

    public void removeSession(Session session) {
        log.debug("Removing session: {}", session);

        synchronized (assignmentsMonitor) {
            var removedHandlers = handlersBySession.remove(session);
            if (removedHandlers == null)
                throw new AssertionError("Handlers entry expected to exist for session: " + session.getRemoteAddress());

            removedHandlers.forEach(handler -> {
                handlersByCorrelationId.remove(handler.getCorrelationId());
                Topic<?> topic = topicsByHandler.remove(handler);
                handlersByTopic.remove(topic, handler);
            });
        }
    }

    private static ByteBuffer bytesWrapper(byte[] bytes) {
        return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
    }
}
