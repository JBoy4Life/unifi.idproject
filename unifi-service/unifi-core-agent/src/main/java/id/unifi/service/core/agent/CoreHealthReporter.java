package id.unifi.service.core.agent;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import id.unifi.service.common.agent.AgentHealth;
import static java.util.Map.entry;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CoreHealthReporter extends ScheduledReporter {
    private final static Logger log = LoggerFactory.getLogger(CoreHealthReporter.class);
    private static final Pattern readerMetricName =
            Pattern.compile(quote("id.unifi.service.rfid-provider.reader.") + "([^.]+)\\.health");
    private static final Pattern antennaMetricName =
            Pattern.compile(quote("id.unifi.service.rfid-provider.antenna.") + "([^._]+)_([0-9]+)\\.health");

    private final CoreClient client;

    public CoreHealthReporter(MetricRegistry registry, CoreClient client) {
        super(
                registry,
                "unifi-core-health",
                MetricFilter.startsWith("id.unifi.service."),
                TimeUnit.SECONDS,
                TimeUnit.MILLISECONDS);
        this.client = client;
    }

    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        var timestamp = Instant.now();
        var readerHealth = new HashMap<String, Boolean>();
        var antennaHealth = new HashMap<String, Map<Integer, Boolean>>();

        for (var e : gauges.entrySet()) {
            var metricName = e.getKey();
            var gauge = e.getValue();
            var readerMatcher = readerMetricName.matcher(metricName);
            var antennaMatcher = antennaMetricName.matcher(metricName);

            if (readerMatcher.matches()) {
                var readerSn = readerMatcher.group(1);
                readerHealth.put(readerSn, healthFromGauge(gauge));
            } else if (antennaMatcher.matches()) {
                var readerSn = antennaMatcher.group(1);
                var portNumber = Integer.parseInt(antennaMatcher.group(2));
                antennaHealth.computeIfAbsent(readerSn, sn -> new HashMap<>())
                        .put(portNumber, healthFromGauge(gauge));
            }
        }

        var readers = readerHealth.entrySet().stream()
                .filter(e -> antennaHealth.containsKey(e.getKey())) // Drop if we know metrics are being created
                .map(e -> entry(e.getKey(), new AgentHealth.ReaderHealth(e.getValue(), antennaHealth.get(e.getKey()))))
                .collect(toMap(Entry::getKey, Entry::getValue));

        client.reportAgentHealth(new AgentHealth(timestamp, readers));
    }

    private static boolean healthFromGauge(Gauge gauge) {
        return ((Gauge<Integer>) gauge).getValue() > 0;
    }
}
