package id.unifi.service.core.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
    }
}
