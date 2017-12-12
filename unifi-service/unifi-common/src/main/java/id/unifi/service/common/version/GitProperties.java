package id.unifi.service.common.version;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public class GitProperties {
    public final String commitSha1;
    public final String commitTime;
    public final String buildTime;
    public final String branch;
    public final String buildUserName;

    public static Optional<GitProperties> read() {
        Properties props = new Properties();
        InputStream stream = GitProperties.class.getResourceAsStream("/unifi-git.properties");
        if (stream == null) return Optional.empty();

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            props.load(reader);
            return Optional.of(new GitProperties(
                    props.getProperty("git.commit.id"),
                    props.getProperty("git.commit.time"),
                    props.getProperty("git.branch"),
                    props.getProperty("git.build.time"),
                    props.getProperty("git.build.user.name")));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private GitProperties(String commitSha1,
                          String commitTime,
                          String branch,
                          String buildTime,
                          String buildUserName) {
        this.commitSha1 = commitSha1;
        this.commitTime = commitTime;
        this.buildTime = buildTime;
        this.branch = branch;
        this.buildUserName = buildUserName;
    }
}