package id.unifi.service.common.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.net.HostAndPort;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.util.Map;

public class SerializationUtils {
    private static final SimpleModule customSerializationModule;
    private static final Map<Protocol, ObjectMapper> objectMappers;

    static {
        customSerializationModule = new SimpleModule();
        customSerializationModule.addSerializer(HostAndPort.class, new JsonSerializer<>() {
            public void serialize(HostAndPort value, JsonGenerator gen, SerializerProvider ss) throws IOException {
                gen.writeString(value.toString());
            }
        });

        objectMappers = Map.of(
                Protocol.JSON, configureObjectMapper(new ObjectMapper(new MappingJsonFactory())),
                Protocol.MSGPACK, configureObjectMapper(new ObjectMapper(new MessagePackFactory()))
        );
    }

    public static ObjectMapper getObjectMapper(Protocol protocol) {
        return objectMappers.get(protocol);
    }

    private static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        return mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(customSerializationModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
}
