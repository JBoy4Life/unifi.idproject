package id.unifi.service.core.config;

import com.statemachinesystems.envy.ConfigSource;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.sources.DelegatingConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileConfigSource {
    private static final String PROPERTIES_RESOURCE_NAME = "/unifi.properties";
    private static ConfigSource source;

    public static synchronized ConfigSource get() {
        if (source == null) {
            Properties properties = new Properties();
            InputStream stream = FileConfigSource.class.getResourceAsStream(PROPERTIES_RESOURCE_NAME);
            if (stream != null) {
                try {
                    properties.load(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            source = new DelegatingConfigSource(
                    Envy.defaultConfigSource(),
                    p -> properties.getProperty(p.asSystemPropertyName()));
        }

        return source;
    }
}
