package id.unifi.service.common.config;

import com.statemachinesystems.envy.ConfigSource;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Parameter;
import com.statemachinesystems.envy.sources.DelegatingConfigSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UnifiConfigSource {
    private static final String PROPERTIES_RESOURCE_NAME = "/unifi.properties";
    private static Properties properties = null;
    private static final ConfigSource unprefixedSource = new DelegatingConfigSource(
            Envy.defaultConfigSource(),
            p -> properties.getProperty(p.asSystemPropertyName()));
    private static final Map<String, ConfigSource> sourceByPrefix = new HashMap<>();

    public static synchronized ConfigSource get() {
        loadProperties();
        return unprefixedSource;
    }

    public static synchronized ConfigSource getForPrefix(String prefix) {
        if (prefix == null) return get();

        return sourceByPrefix.computeIfAbsent(prefix, pre -> {
            loadProperties();
            return new DelegatingConfigSource(
                    p -> System.getProperty(new Parameter(pre).join(p).asSystemPropertyName()),
                    p -> System.getenv(new Parameter(pre).join(p).asEnvironmentVariableName()),
                    p -> properties.getProperty(new Parameter(pre).join(p).asSystemPropertyName()));
        });
    }

    private static synchronized void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (var stream = UnifiConfigSource.class.getResourceAsStream(PROPERTIES_RESOURCE_NAME)) {
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
