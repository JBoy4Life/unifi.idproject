package id.unifi.service.core;

import com.google.common.collect.Iterables;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.HttpServer;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.operator.InMemorySessionTokenStore;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.provider.LoggingEmailSender;
import id.unifi.service.common.rfid.RfidDetection;
import id.unifi.service.common.rfid.RfidDetectionReport;
import id.unifi.service.common.version.VersionInfo;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import id.unifi.service.core.db.tables.records.DetectableRecord;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class CoreService {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    private interface Config {
        @Default("8000")
        int apiServiceHttpPort();

        @Default("8001")
        int agentServiceHttpPort();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Core");
        VersionInfo.log();

        Config config = Envy.configure(Config.class, UnifiConfigSource.get());

        startApiService(config.apiServiceHttpPort());
        //startAgentService(config.agentServiceHttpPort());

    }

    private static void startAgentService(int agentServiceHttpPort) throws Exception {
        ServiceRegistry agentRegistry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agentservices"),
                Map.of());
        Dispatcher<AgentSessionData> agentDispatcher =
                new Dispatcher<>(agentRegistry, AgentSessionData.class, AgentSessionData::new);

        //AgentHandler agentHandler = new AgentHandler(dbProvider, agentDispatcher, detectionProcessor);
        //agentDispatcher.addSessionListener(agentHandler);

        InetSocketAddress agentServerSocket = createUnresolved("0.0.0.0", agentServiceHttpPort);
        HttpServer agentServer = new HttpServer(
                agentServerSocket,
                "/agent-service",
                agentDispatcher,
                Set.of(Protocol.MSGPACK));
        agentServer.start();
    }

    private static void startApiService(int apiServiceHttpPort) throws Exception {
        DatabaseProvider dbProvider = new DatabaseProvider();
        DetectionProcessor detectionProcessor = new DefaultDetectionProcessor(dbProvider);
        ServiceRegistry registry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.services"),
                Map.of(
                        DetectionProcessor.class, detectionProcessor,
                        SessionTokenStore.class, new InMemorySessionTokenStore(864000),
                        EmailSenderProvider.class, new LoggingEmailSender()));
        Dispatcher<?> dispatcher =
                new Dispatcher<>(registry, OperatorSessionData.class, s -> new OperatorSessionData());
        InetSocketAddress apiServerSocket = createUnresolved("0.0.0.0", apiServiceHttpPort);
        HttpServer apiServer = new HttpServer(
                apiServerSocket,
                "/service",
                dispatcher,
                Set.of(Protocol.JSON, Protocol.MSGPACK));
        apiServer.start();

        mockDetections(dbProvider, detectionProcessor);

    }

    private static void mockDetections(DatabaseProvider dbProvider,
                                       DetectionProcessor detectionProcessor) throws Exception {
        Database db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
        AntennaRecord[] antennae;
        DetectableRecord[] foundDetectables;
        do {
            Thread.sleep(5000);
            log.info("Looking for antennae and detectables...");
            antennae = db.execute(sql -> sql.selectFrom(ANTENNA).fetchArray());
            foundDetectables = db.execute(sql -> sql.selectFrom(DETECTABLE).fetchArray());
        } while (antennae.length == 0 || foundDetectables.length == 0);
        log.info("Found antennae and detectables");
        final DetectableRecord[] detectables = foundDetectables;
        Map<String, List<AntennaRecord>> readerAntennae =
                Arrays.stream(antennae).collect(groupingBy(AntennaRecord::getReaderSn));
        String clientId = antennae[0].getClientId();
        String siteId = antennae[0].getSiteId();
        log.info("clientId: {}, siteId: {}", clientId, siteId);
        Random random = new Random();
        int readerCount = readerAntennae.keySet().size();

        while (true) {
            for (int i = 0; i < 100; i++) {
                String readerSn = Iterables.get(readerAntennae.keySet(), random.nextInt(readerCount));
                List<AntennaRecord> portNumbers = readerAntennae.get(readerSn);
                List<RfidDetection> rawDetections = IntStream.range(0, random.nextInt(2) + 1)
                        .mapToObj(j -> {
                            int portNumber = portNumbers.get(random.nextInt(portNumbers.size())).getPortNumber();
                            String detectableId = detectables[random.nextInt(detectables.length)].getDetectableId();
                            Instant timestamp = Instant.now().minusMillis(random.nextInt(200));
                            return new RfidDetection(timestamp, portNumber, detectableId, 0d);
                        }).collect(toList());
                log.trace("Processing mock raw detections: {}", rawDetections);
                detectionProcessor.process(clientId, siteId, new RfidDetectionReport(readerSn, rawDetections));
                sleepUninterruptibly((long) (300 * Math.abs(random.nextGaussian())), TimeUnit.MILLISECONDS);
            }
        }
    }
}
