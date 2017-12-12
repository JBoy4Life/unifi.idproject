package id.unifi.service.common.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.io.BaseEncoding;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static final Message.Version CURRENT_PROTOCOL_VERSION = new Message.Version(1, 0, 0);

    private final ObjectMapper jsonMapper;
    private final ObjectMapper messagePackMapper;
    private final ServiceRegistry serviceRegistry;
    private final Class<?> sessionDataType;

    public Dispatcher(ServiceRegistry serviceRegistry, Class<?> sessionDataType) {
        this.serviceRegistry = serviceRegistry;
        this.sessionDataType = sessionDataType;

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
    }

    public void dispatch(Object sessionData, InputStream stream, Protocol protocol, ReturnChannel returnChannel) {
        ObjectMapper om = protocol == Protocol.JSON ? jsonMapper : messagePackMapper;
        try {
            log.trace("Parsing request");
            JsonNode requestMessage = om.readTree(stream);
            log.trace("Request parsed: {}", requestMessage);
            Message request = om.treeToValue(requestMessage, Message.class);
            log.trace("Request unmarshalled: {}", request);

            ServiceRegistry.Operation operation = serviceRegistry.getOperation(request.messageType);
            Object[] params = operation.params.entrySet().stream().map(entry -> {
                if (entry.getValue() == sessionDataType) {
                    return sessionData;
                }
                try {
                    JsonNode paramNode = request.payload.get(entry.getKey());
                    if (paramNode == null) {
                        throw new RuntimeException(
                                "Missing parameter: " + entry.getKey() + " of type " + entry.getValue());
                    }
                    return om.readValue(om.treeAsTokens(paramNode), om.constructType(entry.getValue()));
                } catch (IOException e) {
                    throw new RuntimeException("Error marshalling parameter for:" + entry.getKey(), e);
                }
            }).toArray();

            Object result = serviceRegistry.invokeRpc(operation, params);

            JsonNode payload = operation.resultType.equals(Void.TYPE) ? null : om.valueToTree(result);
            Message response = new Message(CURRENT_PROTOCOL_VERSION, request.releaseVersion, request.correlationId, operation.resultMessageType, payload);
            log.trace("Response message: {}", response);
            byte[] bytes = om.writeValueAsBytes(response);
            if (log.isTraceEnabled()) {
                log.trace("Sending marshalled response: " + BaseEncoding.base16().lowerCase().encode(bytes));
            }
            returnChannel.send(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object createSessionData() {
        try {
            return sessionDataType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
