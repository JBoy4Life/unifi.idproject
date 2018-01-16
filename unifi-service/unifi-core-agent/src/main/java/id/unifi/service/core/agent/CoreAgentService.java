package id.unifi.service.core.agent;

import com.google.common.collect.Iterables;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.DetectableType;
import id.unifi.service.common.detection.RawDetection;
import id.unifi.service.common.detection.RawDetectionReport;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import id.unifi.service.core.db.tables.records.DetectableRecord;
import id.unifi.service.provider.rfid.RfidProvider;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class CoreAgentService {
    private static final Logger log = LoggerFactory.getLogger(CoreAgentService.class);

    @Prefix("unifi")
    interface Config {
        @Default("ucl-som")
        String clientId();

        @Default("level38")
        String siteId();

        @Default("ws://localhost:8001/agents/msgpack")
        URI serviceUri();

        @Default("false")
        boolean mockDetections();
    }

    public static void main(String[] args) throws Exception {
        Config config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);

        AtomicReference<CoreClient> client = new AtomicReference<>();
        Consumer<RawDetectionReport> detectionConsumer = report -> client.get().sendRawDetections(report);
        ReaderManager readerManager = config.mockDetections()
                ? new MockReaderManager(new DatabaseProvider(), config.clientId(), config.siteId(), detectionConsumer)
                : new DefaultReaderManager(new DatabaseProvider(), new RfidProvider(detectionConsumer));
        ComponentHolder componentHolder = new ComponentHolder(Map.of(ReaderManager.class, readerManager));
        client.set(new CoreClient(config.serviceUri(), config.clientId(), config.siteId(), componentHolder));
    }
}
