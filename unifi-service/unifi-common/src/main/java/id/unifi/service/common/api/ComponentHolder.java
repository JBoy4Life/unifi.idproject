package id.unifi.service.common.api;

import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.api.annotations.ApiConfigPrefix;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ComponentHolder {
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

    private final Map<Class<?>, Object> components;

    public ComponentHolder(Map<Class<?>, Object> components) {
        this.components = new HashMap<>(components);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> cls) {
        return (T) components.computeIfAbsent(cls, ignored -> {
            Constructor<?>[] constructors = cls.getConstructors();
            if (constructors.length != 1) {
                throw new RuntimeException("Expected one constructor, got " + constructors.length + " for " + cls);
            }
            Constructor<?> constructor = constructors[0];
            Object[] componentObjects = Arrays.stream(constructor.getParameters())
                    .map(this::getFromParameter)
                    .toArray();
            try {
                Object instance = constructor.newInstance(componentObjects);
                components.put(cls, instance);
                return (T) instance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Object getFromParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        Object instance = components.get(type);
        if (instance == null) {
            if (type.isInterface()) {
                ApiConfigPrefix prefixAnnotation = parameter.getDeclaredAnnotation(ApiConfigPrefix.class);
                String prefix = prefixAnnotation == null ? null : prefixAnnotation.value();
                log.info("Getting configuration for {} with prefix {}", type, prefix);
                instance =
                        Envy.configure(type, UnifiConfigSource.getForPrefix(prefix), HostAndPortValueParser.instance);
            } else {
                instance = get(type);
            }
        }
        return instance;
    }
}
