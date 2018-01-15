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
import id.unifi.service.attendance.AttendanceProcessor;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.HttpServer;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.ClientDetectable;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.detection.RawSiteDetectionReports;
import id.unifi.service.common.detection.ReaderConfig;
import id.unifi.service.common.operator.InMemorySessionTokenStore;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.provider.LoggingEmailSender;
import static id.unifi.service.common.util.TimeUtils.utcLocalFromInstant;
import id.unifi.service.common.version.VersionInfo;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.ANTENNA;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import static id.unifi.service.core.db.Tables.READER;
import static id.unifi.service.core.db.Tables.UHF_DETECTION;
import id.unifi.service.core.db.tables.records.AntennaRecord;
import id.unifi.service.core.db.tables.records.UhfDetectionRecord;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import org.eclipse.jetty.websocket.api.Session;
import org.jooq.BatchBindStep;
import org.jooq.Field;
import org.jooq.InsertReturningStep;
import org.jooq.Record1;
import org.jooq.Row2;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.insertInto;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.function.IntFunction;

public class CoreService {
    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    public static final String PENDING_RAW_DETECTIONS_QUEUE_NAME = "core.detection.pending-raw-detections";

    private static final BlockingQueue<TaggedDetectionReport> detectionQueue = new ArrayBlockingQueue<>(10_000);

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
                DatabaseProvider.class, dbProvider,
                MqConfig.class, config.mq(),
                DetectionProcessor.class, detectionProcessor,
                SessionTokenStore.class, new InMemorySessionTokenStore(864000),
                EmailSenderProvider.class, new LoggingEmailSender()));

        startApiService(config.apiServiceListenEndpoint(), componentHolder);
        ObjectMapper mapper = startAgentService(componentHolder, config.agentServiceListenEndpoint());
        Channel channel = startRawDetectionConsumer(mapper, config.mq());
        AttendanceProcessor attendanceProcessor = new AttendanceProcessor(dbProvider);
        processQueue(channel, dbProvider.bySchema(CORE), attendanceProcessor);
    }

    private static void processQueue(Channel channel, Database db, AttendanceProcessor attendanceProcessor) {
        Thread thread = new Thread(() -> {
            InsertReturningStep<UhfDetectionRecord> insertQuery = insertInto(UHF_DETECTION,
                    UHF_DETECTION.CLIENT_ID,
                    UHF_DETECTION.DETECTABLE_ID,
                    UHF_DETECTION.DETECTABLE_TYPE,
                    UHF_DETECTION.READER_SN,
                    UHF_DETECTION.PORT_NUMBER,
                    UHF_DETECTION.DETECTION_TIME)
                    .values(null, null, null, null, (Integer) null, null)
                    .onConflictDoNothing();

            while (true) {
                if (detectionQueue.size() < 200) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.info("Detection processing thread interrupted, stopping");
                        return;
                    }
                    if (detectionQueue.isEmpty()) continue;
                }

                ArrayList<TaggedDetectionReport> allTagged = new ArrayList<>(1000);
                detectionQueue.drainTo(allTagged, 1000);

                log.debug("Processing {} detection reports", allTagged.stream().mapToInt(t -> t.reports.size()).sum());

                List<Detection> detections = allTagged.stream()
                        .flatMap(t -> t.reports.stream().flatMap(r -> r.detections.stream().map(d ->
                                new Detection(new ClientDetectable(t.clientId, d.detectableId, d.detectableType), r.readerSn, d.portNumber, d.timestamp))))
                        .collect(toList());

                Row2<String, String>[] detectableIdRows = detections.stream()
                        .map(d -> d.detectable)
                        .distinct()
                        .map(cd -> DSL.row(cd.clientId, cd.detectableId))
                        .toArray((IntFunction<Row2<String, String>[]>) Row2[]::new);

                db.execute(sql -> {
                    Field<String> vDetectableId = field(name("v", "detectable_id"), String.class);
                    Field<String> vClientId = field(name("v", "client_id"), String.class);
                    List<String> unknownDetectableIds = sql
                            .select(vDetectableId)
                            .from(values(detectableIdRows).asTable("v", "client_id", "detectable_id"))
                            .leftJoin(DETECTABLE).using(vClientId, vDetectableId)
                            .where(DETECTABLE.DETECTABLE_ID.isNull())
                            .fetch(Record1::value1);

                    log.trace("Unknown: {}", unknownDetectableIds);

                    BatchBindStep batch = sql.batch(insertQuery);
                    detections.forEach(detection -> {
                        if (!unknownDetectableIds.contains(detection.detectable.detectableId)) {
                            batch.bind(
                                    detection.detectable.clientId,
                                    detection.detectable.detectableId,
                                    detection.detectable.detectableType.toString(),
                                    detection.readerSn,
                                    detection.portNumber,
                                    utcLocalFromInstant(detection.detectionTime));
                        }
                    });
                    int batchSize = batch.size();
                    if (batchSize > 0) {
                        batch.execute();
                    }
                    return null;
                });

                attendanceProcessor.processDetections(detections);

                if (!allTagged.isEmpty()) {
                    long deliveryTag = allTagged.get(allTagged.size() - 1).deliveryTag;
                    try {
                        channel.basicAck(deliveryTag, true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }

    private static Channel startRawDetectionConsumer(ObjectMapper mapper, MqConfig mqConfig) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mqConfig.endpoint().getHost());
        factory.setPort(mqConfig.endpoint().getPort());
        Connection connection;
        Channel channel;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.basicQos(0);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                RawSiteDetectionReports report = mapper.readValue(body, RawSiteDetectionReports.class);
                try {
                    detectionQueue.put(new TaggedDetectionReport(report.clientId, report.reports, envelope.getDeliveryTag()));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // TODO fix and re-enable for live view: detectionProcessor.process(report.clientId, report.siteId, report.report);
            }
        };
        try {
            channel.basicConsume(PENDING_RAW_DETECTIONS_QUEUE_NAME, false, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return channel;
    }

    private static ObjectMapper startAgentService(ComponentHolder componentHolder,
                                                  HostAndPort agentEndpoint) throws Exception {
        Database db = componentHolder.get(DatabaseProvider.class).bySchema(CORE);
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

    private static void startApiService(HostAndPort apiEndpoint, ComponentHolder componentHolder) throws Exception {
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

    private static class TaggedDetectionReport {
        final String clientId;
        final List<RawDetectionReport> reports;
        final long deliveryTag;

        TaggedDetectionReport(String clientId, List<RawDetectionReport> reports, long deliveryTag) {
            this.clientId = clientId;
            this.reports = reports;
            this.deliveryTag = deliveryTag;
        }
    }
}
