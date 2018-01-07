package id.unifi.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.HttpServer;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.detection.RawDetection;
import id.unifi.service.common.detection.RawSiteDetectionReport;
import id.unifi.service.common.detection.ReaderConfig;
import id.unifi.service.common.operator.InMemorySessionTokenStore;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.provider.LoggingEmailSender;
import static id.unifi.service.common.util.TimeUtils.utcLocalFromInstant;
import id.unifi.service.common.version.VersionInfo;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.READER;
import static id.unifi.service.core.db.Tables.UHF_DETECTION;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class CoreService {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    public static final String PENDING_RAW_DETECTIONS_QUEUE_NAME = "core.detection.pending-raw-detections";

    public interface MqConfig {
        @Default("127.0.0.1:5672")
        HostAndPort endpoint();
    }

    private interface Config {
        @Default("0.0.0.0:8000")
        HostAndPort apiServiceListenEndpoint();

        @Default("0.0.0.0:8001")
        HostAndPort agentServiceListenEndpoint();

        MqConfig mq();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Core");
        VersionInfo.log();

        Config config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);

        DatabaseProvider dbProvider = new DatabaseProvider();
        DetectionProcessor detectionProcessor = new DefaultDetectionProcessor(dbProvider);

        ComponentHolder componentHolder = new ComponentHolder(Map.of(
                MqConfig.class, config.mq(),
                DetectionProcessor.class, detectionProcessor,
                SessionTokenStore.class, new InMemorySessionTokenStore(864000),
                EmailSenderProvider.class, new LoggingEmailSender()));
        startApiService(config.apiServiceListenEndpoint(), config.mq(), detectionProcessor, componentHolder);
        ObjectMapper mapper = startAgentService(componentHolder, config.agentServiceListenEndpoint());
        startRawDetectionConsumer(dbProvider.bySchemaName(CORE_SCHEMA_NAME), mapper, config.mq());
    }

    private static void startRawDetectionConsumer(Database db,
                                                  ObjectMapper mapper,
                                                  MqConfig mqConfig) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mqConfig.endpoint().getHost());
        factory.setPort(mqConfig.endpoint().getPort());
        Connection connection;
        Channel channel;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.basicQos(1);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                try {
                    RawSiteDetectionReport report = mapper.readValue(body, RawSiteDetectionReport.class);
                    storeDetections(db, report);
                    // TODO fix and re-enable for live view: detectionProcessor.process(report.clientId, report.siteId, report.report);
                } finally {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        try {
            channel.basicConsume(PENDING_RAW_DETECTIONS_QUEUE_NAME, false, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void storeDetections(Database db, RawSiteDetectionReport report) {
        db.execute(sql -> {
            for (RawDetection detection : report.report.detections) {
                try {
                    sql.insertInto(UHF_DETECTION,
                            UHF_DETECTION.CLIENT_ID,
                            UHF_DETECTION.DETECTABLE_ID,
                            UHF_DETECTION.DETECTABLE_TYPE,
                            UHF_DETECTION.READER_SN,
                            UHF_DETECTION.PORT_NUMBER,
                            UHF_DETECTION.DETECTION_TIME)
                            .values(
                                    report.clientId,
                                    detection.detectableId,
                                    detection.detectableType.toString(),
                                    report.report.readerSn,
                                    detection.portNumber,
                                    utcLocalFromInstant(detection.timestamp))
                            .execute();
                } catch (DataIntegrityViolationException e) {
                    log.trace("Ignoring unknown detectable {}", detection, e);
                }
            }
            return null;
        });
    }

    private static ObjectMapper startAgentService(ComponentHolder componentHolder,
                                                  HostAndPort agentEndpoint) throws Exception {
        Database db = componentHolder.get(DatabaseProvider.class).bySchemaName(CORE_SCHEMA_NAME);
        ServiceRegistry agentRegistry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agents"), componentHolder);
        Dispatcher<AgentSessionData> agentDispatcher =
                new Dispatcher<>(agentRegistry, AgentSessionData.class, AgentSessionData::new);
        agentDispatcher.addSessionListener(new Dispatcher.SessionListener<>() {
            public void onSessionCreated(Session session, AgentSessionData sessionData) {
                log.info("Agent session created for {}:{}", sessionData.getClientId(), sessionData.getSiteId());
                List<ReaderConfig> readerConfigs = db.execute(sql -> {
                    Map<String, List<AntennaRecord>> antennae = sql.selectFrom(ANTENNA)
                            .where(ANTENNA.CLIENT_ID.eq(sessionData.getClientId()))
                            .and(ANTENNA.SITE_ID.eq(sessionData.getSiteId()))
                            .stream()
                            .collect(groupingBy(AntennaRecord::getReaderSn));
                    return sql.selectFrom(READER)
                            .where(READER.CLIENT_ID.eq(sessionData.getClientId()))
                            .and(READER.SITE_ID.eq(sessionData.getSiteId()))
                            .stream()
                            .map(r -> new ReaderConfig(
                                    r.getReaderSn(),
                                    HostAndPort.fromString(r.getEndpoint()),
                                    antennae.get(r.getReaderSn()).stream().mapToInt(AntennaRecord::getPortNumber).toArray()))
                            .collect(toList());
                });
                try {
                    agentDispatcher.request(
                            session,
                            Protocol.MSGPACK,
                            "core.config.set-reader-config",
                            Map.of("readers", readerConfigs));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public void onSessionDropped(Session session) {
                log.info("Session dropped: {}", session);
            }
        });

        //AgentHandler agentHandler = new AgentHandler(dbProvider, agentDispatcher, detectionProcessor);
        //agentDispatcher.addSessionListener(agentHandler);

        InetSocketAddress agentServerSocket = createUnresolved(agentEndpoint.getHost(), agentEndpoint.getPort());
        HttpServer agentServer = new HttpServer(
                agentServerSocket,
                "/agents",
                agentDispatcher,
                Set.of(Protocol.MSGPACK));
        agentServer.start();
        return agentDispatcher.getObjectMapper(Protocol.MSGPACK);
    }

    private static void startApiService(HostAndPort apiEndpoint,
                                        MqConfig mqConfig,
                                        Object detectionProcessor,
                                        ComponentHolder componentHolder) throws Exception {
        ServiceRegistry registry = new ServiceRegistry(
                Map.of(
                        "core", "id.unifi.service.core.services",
                        "attendance", "id.unifi.service.attendance.services"),
                componentHolder);
        Dispatcher<?> dispatcher =
                new Dispatcher<>(registry, OperatorSessionData.class, s -> new OperatorSessionData());
        InetSocketAddress apiServerSocket = createUnresolved(apiEndpoint.getHost(), apiEndpoint.getPort());
        HttpServer apiServer = new HttpServer(
                apiServerSocket,
                "/service",
                dispatcher,
                Set.of(Protocol.JSON, Protocol.MSGPACK));
        apiServer.start();
    }
}
