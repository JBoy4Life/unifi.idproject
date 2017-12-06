package id.unifi.service.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import id.unifi.service.core.api.annotations.ApiService;
import id.unifi.service.core.db.Database;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private final Map<String, Class<?>> handlers;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper messagePackMapper;
    private final Database db;

    public Dispatcher(Database db, String... packageNames) {
        this.db = db;
        this.handlers = new HashMap<>();

        jsonMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());

        messagePackMapper = new ObjectMapper(new MessagePackFactory())
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());

        ClassPath classPath;
        try {
            classPath = ClassPath.from(Dispatcher.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String packageName : packageNames) {
            ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(packageName);
            for (ClassPath.ClassInfo classInfo : classes) {
                Class<?> cls = classInfo.load();
                log.info("Found {}", cls);
                log.info("Annotations: {}", Arrays.toString(cls.getDeclaredAnnotations()));
                String key = cls.getDeclaredAnnotation(ApiService.class).value();
                handlers.put(key, cls);
            }
        }
    }

    public void dispatch(InputStream stream, Protocol protocol, ReturnChannel returnChannel) {
        ObjectMapper om = protocol == Protocol.JSON ? jsonMapper : messagePackMapper;
        try {
            log.info("Parsing request");
            JsonNode requestMessage = om.readTree(stream);
            log.info("Request parsed: " + requestMessage);
            Message request = om.treeToValue(requestMessage, Message.class);
            log.info("Request unmarshalled: " + request);
            String[] parts = request.messageType.split("\\.");

            String serviceName = parts[1];
            Class<?> handlerClass = handlers.get(serviceName);
            String operationName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, parts[2]);
            Method method = Arrays.stream(handlerClass.getDeclaredMethods()).filter(m -> m.getName().equals(operationName)).findFirst().get();
            Object[] params = Arrays.stream(method.getParameters())
                    .map(p -> {
                        try {
                            JsonNode param = request.payload.get(p.getName());
                            if (param == null) {
                                throw new RuntimeException("Missing param: " + p.getName());
                            }
                            return om.readValue(om.treeAsTokens(param), TypeFactory.defaultInstance().constructType(p.getParameterizedType()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray();

            Object handler = handlerClass.getConstructor(Database.class).newInstance(db);

            log.info("Invoking method " + method + " with: " + Arrays.toString(params));
            Object result = method.invoke(handler, params);
            log.info("Method returned result: " + result);

            JsonNode payload = method.getReturnType().equals(Void.TYPE) ? null : om.valueToTree(result);
            Message response = new Message(request.protocolVersion, request.releaseVersion, request.correlationId, request.messageType + "-result", payload);
            log.info("Response message: " + response);
            byte[] bytes = om.writeValueAsBytes(response);
            log.info("Sending marshalled response: " + hex(bytes));
            returnChannel.send(ByteBuffer.wrap(bytes));
        } catch (IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array)
            sb.append(String.format("%02x ", b));
        return sb.toString();
    }
}
