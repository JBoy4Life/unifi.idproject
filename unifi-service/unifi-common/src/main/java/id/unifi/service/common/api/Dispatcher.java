package id.unifi.service.common.api;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.TextNode;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import id.unifi.service.common.api.errors.AbstractMarshallableError;
import id.unifi.service.common.api.access.AccessChecker;
import id.unifi.service.common.api.access.AccessManager;
import id.unifi.service.common.api.access.NullAccessManager;
import id.unifi.service.common.api.errors.InternalServerError;
import id.unifi.service.common.api.errors.InvalidParameterFormat;
import id.unifi.service.common.api.errors.MissingParameter;
import id.unifi.service.common.api.errors.NotFound;
import static id.unifi.service.common.api.http.HttpUtils.*;
import id.unifi.service.common.security.Token;
import id.unifi.service.common.subscriptions.SubscriptionManager;
import id.unifi.service.common.util.HexEncoded;
import static java.util.Arrays.stream;
import static javax.servlet.AsyncContext.ASYNC_REQUEST_URI;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

public class Dispatcher<S> {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static final Message.Version CURRENT_PROTOCOL_VERSION = new Message.Version(1, 0, 0);

    private final ServiceRegistry serviceRegistry;
    private final Class<S> sessionDataType;
    private final Function<Session, S> sessionDataCreator;
    private final Function<HttpServletRequest, S> httpSessionDataCreator;
    private final ConcurrentMap<Session, S> sessionDataStore;
    private final Set<SessionListener<S>> sessionListeners;
    private final Map<String, WireMessageListener> messageListeners;
    private final Map<ByteBuffer, CancellableWireMessageListener> responseListeners;
    private final AccessManager<S> accessManager;

    public interface WireMessageListener {
        void accept(ObjectMapper om, Session session, Message message) throws JsonProcessingException;
    }

    public interface CancellableWireMessageListener {
        // return false to unsubscribe
        boolean accept(ObjectMapper om, Session session, Message message) throws JsonProcessingException;
    }

    public interface SessionListener<S> {
        void onSessionCreated(Session session, S sessionData);
        void onSessionDropped(Session session);
    }

    public Dispatcher(ServiceRegistry serviceRegistry,
                      Class<S> sessionDataType,
                      Function<Session, S> sessionDataCreator,
                      SubscriptionManager subscriptionManager,
                      AccessManager<S> accessManager,
                      Function<HttpServletRequest, S> httpSessionDataCreator) {
        this.serviceRegistry = serviceRegistry;
        this.sessionDataType = sessionDataType;
        this.sessionDataCreator = sessionDataCreator;
        this.accessManager = accessManager != null ? accessManager : new NullAccessManager<>();
        this.httpSessionDataCreator = httpSessionDataCreator;
        this.sessionDataStore = new ConcurrentHashMap<>();

        this.sessionListeners = new CopyOnWriteArraySet<>();
        this.messageListeners = new ConcurrentHashMap<>();

        if (subscriptionManager != null) {
            messageListeners.put("core.protocol.unsubscribe", (om, session, msg) ->
                    subscriptionManager.removeSubscription(session, msg.correlationId));

            sessionListeners.add(new SessionListener<>() {
                public void onSessionCreated(Session session, S sessionData) {
                    subscriptionManager.addSession(session);
                }

                public void onSessionDropped(Session session) {
                    subscriptionManager.removeSession(session);
                }
            });
        }
        this.responseListeners = new HashMap<>();
    }

    public Dispatcher(ServiceRegistry serviceRegistry,
                      Class<S> sessionDataType,
                      Function<Session, S> sessionDataCreator,
                      SubscriptionManager subscriptionManager) {
        this(serviceRegistry, sessionDataType, sessionDataCreator, subscriptionManager, null, null);
    }

    public Dispatcher(ServiceRegistry serviceRegistry,
                      Class<S> sessionDataType,
                      Function<Session, S> sessionDataCreator) {
        this(serviceRegistry, sessionDataType, sessionDataCreator, null);
    }

    public void dispatch(Session session, MessageStream stream, Protocol protocol, Channel returnChannel) {
        log.trace("Dispatching {} request in {}", protocol, session);
        var mapper = getObjectMapper(protocol);
        Message message = null;
        try {
            message = parseMessage(stream, mapper);

            var correlationIdBytes = ByteBuffer.wrap(message.correlationId);
            var responseListener = responseListeners.get(correlationIdBytes);
            if (responseListener != null) {
                var moreMessagesExpected = responseListener.accept(mapper, session, message);
                if (!moreMessagesExpected) responseListeners.remove(correlationIdBytes);
                return;
            }

            var messageTypeListener = messageListeners.get(message.messageType);
            if (messageTypeListener != null) {
                messageTypeListener.accept(mapper, session, message);
                return;
            }

            var operation = serviceRegistry.getOperation(message.messageType);
            processRequest(session, returnChannel, mapper, protocol, message, operation);
        } catch (AbstractMarshallableError e) {
            if (message != null) {
                sendPayload(returnChannel, mapper, protocol, errorMessage(mapper, message, e));
            } else {
                session.close(StatusCode.BAD_PAYLOAD, "Couldn't process payload");
            }
        } catch (JsonProcessingException e) {
            var errMessage = "Couldn't process " + protocol + " payload";
            log.debug(errMessage + " in {}", session, e);
            session.close(StatusCode.BAD_PAYLOAD, errMessage);
        } catch (RuntimeException | IOException e) {
            log.error("Error while dispatching request", e);
            if (message != null) {
                sendPayload(returnChannel, mapper, protocol, errorMessage(mapper, message, new InternalServerError()));
            } else {
                session.close(StatusCode.BAD_PAYLOAD, "Couldn't process payload");
            }
        }
    }

    public void dispatch(List<Protocol> protocols, AsyncContext context) throws IOException {
        var request = (HttpServletRequest) context.getRequest();
        var response = (HttpServletResponse) context.getResponse();
        Protocol protocol = null;
        ObjectMapper mapper = null;

        try {
            var requestPath = (String) request.getAttribute(ASYNC_REQUEST_URI);
            var servletPrefix = request.getServletPath() + "/";
            if (!requestPath.startsWith(servletPrefix))
                throw new AssertionError("Expected path prefix '" + servletPrefix + "', got: " + requestPath);
            var path = requestPath.substring(servletPrefix.length());


            HttpMethod method;
            try {
                method = HttpMethod.valueOf(request.getMethod());
            } catch (IllegalArgumentException e) {
                throw new NotFound("operation");
            }

            var operationMatch = serviceRegistry.getOperationFromUrlPath(method, path);
            if (operationMatch == null) throw new NotFound("operation");

            log.trace("Dispatching {} /{}, as {}", request.getMethod(), path, operationMatch.operation);

            protocol = determineProtocol(protocols, request);
            mapper = getObjectMapper(protocol);
            var channel = createChannel(protocol, context);

            var sessionData = httpSessionDataCreator.apply(request);

            var queryParams = getQueryParameters(request.getQueryString());
            var bodyJson = protocol.isBinary()
                    ? mapper.readTree(request.getInputStream())
                    : mapper.readTree(request.getReader());

            var paramGetter = paramGetter(mapper, operationMatch.pathParams, queryParams, bodyJson);
            processRequest(sessionData, context, channel, mapper, protocol, paramGetter, operationMatch.operation);
        } catch (Exception e) {
            try {
                respondWithThrowable(protocol, mapper, response, e);
            } finally {
                context.complete();
            }
        }
    }

    public void request(Session session,
                        Protocol protocol,
                        String messageType,
                        Map<String, Object> params) {
        request(session, protocol, messageType, params, null);
    }

    public void request(Session session,
                        Protocol protocol,
                        String messageType,
                        Map<String, Object> params,
                        @Nullable CancellableWireMessageListener listener) {
        log.debug("Requesting using {} in {}", protocol, session);
        var mapper = getObjectMapper(protocol);

        var correlationId = new Token().raw;
        var payload = mapper.valueToTree(params);
        var message = new Message(
                CURRENT_PROTOCOL_VERSION,
                CURRENT_PROTOCOL_VERSION,
                correlationId,
                messageType,
                payload);
        byte[] byteMessage;
        try {
            byteMessage = mapper.writeValueAsBytes(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var correlationIdBytes = ByteBuffer.wrap(correlationId);
        if (listener != null) responseListeners.put(correlationIdBytes, listener);

        try {
            session.getRemote().sendBytes(ByteBuffer.wrap(byteMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void putMessageListener(String messageType, WireMessageListener consumer) {
        messageListeners.put(messageType, consumer);
    }

    public void createSession(Session session) {
        var sessionData = sessionDataCreator.apply(session);
        sessionDataStore.put(session, sessionData);
        sessionListeners.forEach(l -> l.onSessionCreated(session, sessionData));
    }

    public void dropSession(Session session) {
        sessionDataStore.remove(session);
        sessionListeners.forEach(l -> l.onSessionDropped(session));
    }

    public void addSessionListener(SessionListener<S> listener) {
        sessionListeners.add(listener);
    }

    private void processRequest(Session session,
                                Channel returnChannel,
                                ObjectMapper mapper,
                                Protocol protocol,
                                Message message,
                                ServiceRegistry.Operation operation) {
        var sessionData = sessionDataStore.get(session);
        if (sessionData == null) return; // Ignore dead sessions

        accessManager.ensureAuthorized(message.messageType, sessionData);

        var params = getParams(mapper, operation, session, null, sessionData, message.payload::get);

        switch (operation.invocationType) {
            case RPC:
                Message rpcResponse;
                try {
                    var result = serviceRegistry.invokeRpc(operation, params);

                    var payload = mapper.valueToTree(result);
                    rpcResponse = new Message(
                            CURRENT_PROTOCOL_VERSION,
                            message.releaseVersion,
                            message.correlationId,
                            operation.resultMessageType,
                            payload);
                    log.trace("Response message: {}", rpcResponse);
                } catch (AbstractMarshallableError e) {
                    rpcResponse = errorMessage(mapper, message, e);
                }

                sendPayload(returnChannel, mapper, protocol, rpcResponse);
                break;

            case MULTI:
                var listenerParam = new MessageListener<>() {
                    public void accept(String messageType, Object payload) {
                        var payloadNode = mapper.valueToTree(payload);
                        var response = new Message(
                                CURRENT_PROTOCOL_VERSION,
                                message.releaseVersion,
                                message.correlationId,
                                messageType,
                                payloadNode);
                        log.trace("Multi-response message: {}", response);
                        sendPayload(returnChannel, mapper, protocol, response);
                    }

                    public Session getSession() {
                        return session;
                    }

                    public byte[] getCorrelationId() {
                        return message.correlationId;
                    }
                };

                try {
                    serviceRegistry.invokeMulti(operation, params, listenerParam);
                } catch (AbstractMarshallableError e) {
                    sendPayload(returnChannel, mapper, protocol, errorMessage(mapper, message, e));
                }
                break;
        }
    }

    private void processRequest(S sessionData,
                                AsyncContext asyncContext,
                                Channel channel,
                                ObjectMapper mapper,
                                Protocol protocol,
                                Function<String, JsonNode> getParam,
                                ServiceRegistry.Operation operation) {
        accessManager.ensureAuthorized(operation.messageType, sessionData);
        var params = getParams(mapper, operation, null, asyncContext, sessionData, getParam);
        var result = serviceRegistry.invokeRpc(operation, params);
        if (stream(params).noneMatch(p -> p instanceof AsyncContext)) {
            // Process response unless a non-null context was passed in, in which case application logic handles this
            var payload = mapper.valueToTree(result);
            log.trace("Response payload: {}", payload);
            sendPayload(channel, mapper, protocol, payload);
        }
    }

    private Object[] getParams(ObjectMapper mapper,
                               ServiceRegistry.Operation operation,
                               @Nullable Session session,
                               @Nullable AsyncContext asyncContext,
                               S sessionData,
                               Function<String, JsonNode> getParam) {
        return operation.params.entrySet().stream().map(entry -> {
            var type = entry.getValue().type;
            var nullable = entry.getValue().nullable;

            if (type == Session.class) {
                if (session != null) return session;
                throw new AssertionError("Unexpected null session. Trying to expose a subscription call via HTTP?");
            }

            if (type == AsyncContext.class) {
                if (asyncContext == null && !nullable) throw new NotFound("operation");
                return asyncContext;
            }

            if (type == AccessChecker.class) {
                // TODO: Capture operator in session; reading mutable session data several times (here and in
                // service impl) may yield different operators -> potential vulnerability
                return (AccessChecker) () -> accessManager.ensureAuthorized(operation.messageType, sessionData, true);
            }

            if (type == ObjectMapper.class) return mapper;
            if (type == sessionDataType) return sessionData;

            var name = entry.getKey();
            try {
                var paramNode = getParam.apply(name);
                if (paramNode == null || paramNode.isNull()) {
                    if (nullable) {
                        return null;
                    } else {
                        throw new MissingParameter(name, type.getTypeName());
                    }
                }
                return readValue(mapper, type, paramNode);
            } catch (JsonProcessingException e) {
                throw new InvalidParameterFormat(name, e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toArray();
    }

    private static <T> T readValue(ObjectMapper mapper, Type type, JsonNode paramNode) throws IOException {
        var javaType = mapper.constructType(type);
        try {
            return mapper.readValue(mapper.treeAsTokens(paramNode), javaType);
        } catch (InvalidFormatException e) {
            // For failed base-64 decoding try again with URL-safe Base64 variant
            // This is a hack that won't work on nested structures
            if (e.getTargetType() == byte[].class && paramNode.isTextual()) {
                var binValue = ((TextNode) paramNode).getBinaryValue(Base64Variants.MODIFIED_FOR_URL);
                return mapper.readValue(mapper.treeAsTokens(new BinaryNode(binValue)), javaType);
            }
            throw e;
        }
    }

    private static Message errorMessage(ObjectMapper mapper, Message message, AbstractMarshallableError e) {
        return new Message(
                CURRENT_PROTOCOL_VERSION,
                message.releaseVersion,
                message.correlationId,
                e.getProtocolMessageType(),
                mapper.valueToTree(e));
    }

    private static Message parseMessage(MessageStream stream, ObjectMapper mapper) throws IOException {
        var message = stream.isBinary() ? mapper.readTree(stream.inputStream) : mapper.readTree(stream.reader);
        log.trace("Request parsed: {}", message);
        var request = mapper.treeToValue(message, Message.class);
        log.trace("Request unmarshalled: {}", request);
        return request;
    }

    private static void sendPayload(Channel channel,
                                    ObjectMapper mapper,
                                    Protocol protocol,
                                    Object payload) {
        if (protocol.isBinary()) {
            byte[] binaryPayload;
            try {
                binaryPayload = mapper.writeValueAsBytes(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if (log.isTraceEnabled()) {
                log.trace("Sending marshalled message: {}", new HexEncoded(binaryPayload));
            }
            channel.send(ByteBuffer.wrap(binaryPayload));
        } else {
            String stringPayload;
            try {
                stringPayload = mapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.trace("Sending marshalled message: {}", stringPayload);
            channel.send(stringPayload);
        }
    }
}
