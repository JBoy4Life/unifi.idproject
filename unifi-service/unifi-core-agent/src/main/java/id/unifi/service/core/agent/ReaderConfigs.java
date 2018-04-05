package id.unifi.service.core.agent;

import com.google.common.net.HostAndPort;
import id.unifi.service.common.detection.ReaderConfig;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaderConfigs {
    private static final Pattern p = Pattern.compile("([a-zA-Z0-9-]+),([^,]+:[0-9]+),\\[((?:[0-9]+,)*[0-9]+)]");

    public final List<ReaderConfig> readers;

    public ReaderConfigs(List<ReaderConfig> readers) {
        this.readers = readers;
    }

    /**
     * Parses reader config from a command-line-friendly string.
     * The string must contain a semicolon-separated list of reader configs, each of the form
     * "reader-serial-number:hostname:port,[antenna-port-1,antenna-port-2,...]", e.g.
     * "37017090614,192.168.42.167:5084,[1];37017090615,192.168.42.168:5084,[1,2,3,4]"
     * @param value semicolon-separated reader configs
     * @return wrapped list of ReaderConfig
     */
    public static ReaderConfigs fromString(String value) {
        List<ReaderConfig> readers = Arrays.stream(value.split(";")).filter(s -> !s.isEmpty()).map(readerString -> {
            Matcher matcher = p.matcher(readerString);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Can't parse reader string: '" + readerString + "'");
            }

            String readerSn = matcher.group(1).replaceAll("-", "");
            HostAndPort endpoint = HostAndPort.fromString(matcher.group(2));
            int[] enabledAntennae = Arrays.stream(matcher.group(3).split(",")).mapToInt(Integer::parseInt).toArray();
            return new ReaderConfig(readerSn, endpoint, enabledAntennae);
        }).collect(toList());
        return new ReaderConfigs(readers);
    }
}
