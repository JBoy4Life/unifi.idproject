package id.unifi.service.core.agent.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.net.HostAndPort;

import java.io.IOException;

public class ConfigSerialization {
    private static final SimpleModule customSerializationModule;
    private static final ObjectMapper yamlObjectMapper;
    private static final ObjectMapper jsonObjectMapper;

    static {
        customSerializationModule = new SimpleModule();
        customSerializationModule.addSerializer(HostAndPort.class, new JsonSerializer<>() {
            public void serialize(HostAndPort value, JsonGenerator gen, SerializerProvider ss) throws IOException {
                gen.writeString(value.toString());
            }
        });

        yamlObjectMapper = configure(new ObjectMapper(new YAMLFactory()));
        jsonObjectMapper = configure(new ObjectMapper(new MappingJsonFactory()))
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    public static ObjectMapper getSetupObjectMapper() {
        return yamlObjectMapper;
    }

    public static ObjectMapper getConfigObjectMapper() {
        return jsonObjectMapper;
    }

    private static ObjectMapper configure(ObjectMapper mapper) {
        return mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(customSerializationModule)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }
}
