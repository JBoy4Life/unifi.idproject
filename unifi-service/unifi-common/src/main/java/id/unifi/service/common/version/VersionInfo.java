package id.unifi.service.common.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionInfo {
    private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);

    public static void log() {
        var gitProperties = GitProperties.read();
        if (gitProperties.isPresent()) {
            var props = gitProperties.get();
            log.info("Running commit {} ({}) from branch '{}' built at {} by {}",
                    props.commitSha1, props.commitTime,
                    props.branch, props.buildTime, props.buildUserName);
        } else {
            log.info("No Git version information found");
        }

        var version = VersionProperties.read();
        log.info("Project version {} build number {}", version.projectVersion, version.buildNumber);
    }
}
