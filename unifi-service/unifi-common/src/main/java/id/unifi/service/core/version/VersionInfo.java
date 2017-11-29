package id.unifi.service.core.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Properties;

public class VersionInfo {
    private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);

    public static void log() {
        Optional<GitProperties> gitProperties = GitProperties.read();
        if (gitProperties.isPresent()) {
            GitProperties props = gitProperties.get();
            log.info("Running commit {} ({}) from branch '{}' built at {} by {}",
                    props.commitSha1, props.commitTime,
                    props.branch, props.buildTime, props.buildUserName);
        } else {
            log.info("No Git version information found");
        }

        VersionProperties version = VersionProperties.read();
        log.info("Project version {} build number {}", version.projectVersion, version.buildNumber);
    }
}
