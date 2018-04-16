package id.unifi.service.common.util;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.DefaultObjectNameFactory;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jmx.ObjectNameFactory;
import com.google.common.base.CharMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class MetricUtils {
    private MetricUtils() {}

    private static final Logger log = LoggerFactory.getLogger(MetricUtils.class);
    private static final Pattern unifiMetricName =
            Pattern.compile("id\\.unifi\\.service\\.([^.]+)\\.([^.]+)\\.([^.]+)\\.([^.]+)");
    private static final ObjectNameFactory defaultNameFactory = new DefaultObjectNameFactory();
    private static final CharMatcher invalidPropValueChars = CharMatcher.anyOf(",=:\"?*");

    public static JmxReporter createJmxReporter(MetricRegistry registry) {
        return JmxReporter.forRegistry(registry)
                .createsObjectNamesWith(MetricUtils::deriveObjectName)
                .build();
    }

    private static ObjectName deriveObjectName(String type, String domain, String name) {
        log.trace("Deriving a JMX object name for domain {}, metric name {}", domain, name);

        Matcher m = unifiMetricName.matcher(name);
        try {
            if (m.matches()) {
                Object[] escaped = IntStream.rangeClosed(1, 4)
                        .mapToObj(i -> quotePropValue(m.group(i)))
                        .toArray();
                return new ObjectName(String.format("id.unifi.service.%s:type=%s,name=%s,metric=%s", escaped));
            }
        } catch (MalformedObjectNameException e) {
            log.warn("Unable to derive a JMX object name for domain {}, metric name {}", domain, name, e);
            throw new RuntimeException(e);
        }
        return defaultNameFactory.createName(type, domain, name);
    }

    private static String quotePropValue(String value) {
        // Quoting wraps string in visible quotes. Check whether we need to do this first.
        return invalidPropValueChars.matchesAnyOf(value)
                ? "\"" + value.replaceAll("[\\\\\"?*]", "\\\\$0") + "\""
                : value;
    }
}
