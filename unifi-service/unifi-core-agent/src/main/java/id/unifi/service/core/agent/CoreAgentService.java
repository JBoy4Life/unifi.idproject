package id.unifi.service.core.agent;

import com.google.common.collect.Iterables;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
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
import java.util.concurrent.TimeUnit;
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
    }

    public static void main(String[] args) throws Exception {
        Config config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);

        AtomicReference<CoreClient> client = new AtomicReference<>();
        Consumer<RawDetectionReport> detectionConsumer = report -> client.get().sendRawDetections(report);
        ReaderManager readerManager = new ReaderManager(new DatabaseProvider(), new RfidProvider(detectionConsumer));
        ComponentHolder componentHolder = new ComponentHolder(Map.of(ReaderManager.class, readerManager));
        client.set(new CoreClient(config.serviceUri(), config.clientId(), config.siteId(), componentHolder));
    }

    private static void mockDetections(DatabaseProvider dbProvider,
                                       RawDetectionProcessor detectionProcessor) throws Exception {
        Database serviceDb = dbProvider.bySchema(CORE);
        AntennaRecord[] antennae;
        DetectableRecord[] foundDetectables;
        do {
            Thread.sleep(5000);
            log.info("Looking for antennae and detectables...");
            antennae = serviceDb.execute(sql -> sql.selectFrom(ANTENNA).fetchArray());
            foundDetectables = serviceDb.execute(sql -> sql.selectFrom(DETECTABLE).fetchArray());
        } while (antennae.length == 0 || foundDetectables.length == 0);
        log.info("Found antennae and detectables");
        final DetectableRecord[] detectables = foundDetectables;
        Map<String, List<AntennaRecord>> readerAntennae =
                Arrays.stream(antennae).collect(groupingBy(AntennaRecord::getReaderSn));
        Random random = new Random();
        int readerCount = readerAntennae.keySet().size();

        while (true) {
            for (int i = 0; i < 100; i++) {
                String readerSn = Iterables.get(readerAntennae.keySet(), random.nextInt(readerCount));
                List<AntennaRecord> portNumbers = readerAntennae.get(readerSn);
                List<RawDetection> rawDetections = IntStream.range(0, random.nextInt(2) + 1)
                        .mapToObj(j -> {
                            int portNumber = portNumbers.get(random.nextInt(portNumbers.size())).getPortNumber();
                            String detectableId = detectables[random.nextInt(detectables.length)].getDetectableId();
                            Instant timestamp = Instant.now().minusMillis(random.nextInt(200));
                            return new RawDetection(timestamp, portNumber, detectableId, DetectableType.UHF_EPC, 0d);
                        }).collect(toList());
                log.trace("Processing mock raw detections: {}", rawDetections);
                detectionProcessor.process(new RawDetectionReport(readerSn, rawDetections));
                sleepUninterruptibly((long) (300 * Math.abs(random.nextGaussian())), TimeUnit.MILLISECONDS);
            }
        }
    }
}
