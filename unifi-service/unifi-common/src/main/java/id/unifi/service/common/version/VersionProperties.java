package id.unifi.service.common.version;

import java.io.IOException;
import java.util.Properties;

public class VersionProperties {
    public final String projectVersion;
    public final String buildNumber;

    public static VersionProperties read() {
        var props = new Properties();
        try (var stream = VersionProperties.class.getResourceAsStream("/unifi-version.properties")) {
            props.load(stream);
            return new VersionProperties(props.getProperty("project.version"), props.getProperty("build.number"));
        } catch (IOException e) {
            throw new IllegalStateException("Version properties file can't be read");
        }
    }

    private VersionProperties(String projectVersion, String buildNumber) {
        this.projectVersion = projectVersion;
        this.buildNumber = buildNumber;
    }
}
