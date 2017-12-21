package id.unifi.service.common.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.io.BaseEncoding;
import org.eclipse.jetty.websocket.api.Session;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Dispatcher<S> {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static final Message.Version CURRENT_PROTOCOL_VERSION = new Message.Version(1, 0, 0);

    private final ObjectMapper jsonMapper;
    private final ObjectMapper messagePackMapper;
    private final ServiceRegistry serviceRegistry;
    private final Class<S> sessionDataType;
    private final Function<Session, S> sessionDataCreator;
    private final ConcurrentMap<Session, S> sessionData;
    private final Set<SessionListener<S>> sessionListeners;
    private final Map<String, PayloadConsumer> messageListeners;

    public interface PayloadConsumer {
        void accept(ObjectMapper om, Session session, JsonNode node);
    }

    public interface SessionListener<S> {
        void onSessionCreated(Session session, S sessionData);
        void onSessionDropped(Session session);
    }

    public Dispatcher(ServiceRegistry serviceRegistry,
                      Class<S> sessionDataType,
                      Function<Session, S> sessionDataCreator) {
        this.serviceRegistry = serviceRegistry;
        this.sessionDataType = sessionDataType;
        this.sessionDataCreator = sessionDataCreator;
        this.sessionData = new ConcurrentHashMap<>();

        jsonMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        messagePackMapper = new ObjectMapper(new MessagePackFactory())
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        sessionListeners = new CopyOnWriteArraySet<>();
        messageListeners = new ConcurrentHashMap<>();
    }

    public void dispatch(Session session, InputStream stream, Protocol protocol, ReturnChannel returnChannel) {
        ObjectMapper om = protocol == Protocol.JSON ? jsonMapper : messagePackMapper;
        try {
            Message message = parseMessage(stream, om);

            PayloadConsumer messageListener = messageListeners.get(message.messageType);
            if (messageListener != null) {
                messageListener.accept(om, session, message.payload);
                return;
            }

            ServiceRegistry.Operation operation = serviceRegistry.getOperation(message.messageType);
            processRequest(session, returnChannel, om, message, operation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processRequest(Session session,
                                ReturnChannel returnChannel,
                                ObjectMapper om,
                                Message message,
                                ServiceRegistry.Operation operation) throws JsonProcessingException {
        Object[] params = operation.params.entrySet().stream().map(entry -> {
            if (entry.getValue() == Session.class)
                return session;
            if (entry.getValue() == sessionDataType)
                return sessionData.get(session);

            String name = entry.getKey();
            Type type = entry.getValue();
            try {
                JsonNode paramNode = message.payload.get(name);
                if (paramNode == null) {
                    throw new RuntimeException("Missing parameter: " + name + " of type " + type);
                }
                return om.readValue(om.treeAsTokens(paramNode), om.constructType(type));
            } catch (IOException e) {
                throw new RuntimeException("Error marshalling parameter for:" + name, e);
            }
        }).toArray();

        switch (operation.invocationType) {
            case RPC:
                Object result = serviceRegistry.invokeRpc(operation, params);

                JsonNode payload = operation.resultType.equals(Void.TYPE) ? null : om.valueToTree(result);
                Message rpcResponse = new Message(
                        CURRENT_PROTOCOL_VERSION,
                        message.releaseVersion,
                        message.correlationId,
                        operation.resultMessageType,
                        payload);
                log.trace("Response message: {}", rpcResponse);
                byte[] bytes = om.writeValueAsBytes(rpcResponse);
                if (log.isTraceEnabled()) {
                    log.trace("Sending marshalled response: " + BaseEncoding.base16().lowerCase().encode(bytes));
                }
                returnChannel.send(ByteBuffer.wrap(bytes));
                break;

            case MULTI:
                MessageListener<?> listenerParam = (MessageListener<Object>) (messageType, payloadObj) -> {
                    JsonNode payloadNode = om.valueToTree(payloadObj);
                    Message response = new Message(
                            CURRENT_PROTOCOL_VERSION,
                            message.releaseVersion,
                            message.correlationId,
                            messageType,
                            payloadNode);
                    log.trace("Multi-response message: {}", response);
                    byte[] responseBytes = new byte[0];
                    try {
                        responseBytes = om.writeValueAsBytes(response);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("Sending marshalled response: " + BaseEncoding.base16().lowerCase().encode(responseBytes));
                    }

                    returnChannel.send(ByteBuffer.wrap(responseBytes));
                };
                serviceRegistry.invokeMulti(operation, params, listenerParam);
                break;
        }

    }

    public <T> void putMessageListener(String messageType, Type type, BiConsumer<Session, T> listener) {
        messageListeners.put(messageType, (om, session, node) -> {
            try {
                T payload = om.readValue(om.treeAsTokens(node), om.constructType(type));
                listener.accept(session, payload);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void createSession(Session session) {
        S sessionData = sessionDataCreator.apply(session);
        this.sessionData.put(session, sessionData);
        sessionListeners.forEach(l -> l.onSessionCreated(session, sessionData));
    }

    public void dropSession(Session session) {
        sessionData.remove(session);
        sessionListeners.forEach(l -> l.onSessionDropped(session));
    }

    public void addSessionListener(SessionListener<S> listener) {
        sessionListeners.add(listener);
    }

    private static Message parseMessage(InputStream stream, ObjectMapper om) throws IOException {
        log.trace("Parsing request");
        JsonNode requestMessage = om.readTree(stream);
        log.trace("Request parsed: {}", requestMessage);
        Message request = om.treeToValue(requestMessage, Message.class);
        log.trace("Request unmarshalled: {}", request);
        return request;
    }
}
