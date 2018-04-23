package id.unifi.service.core;

import com.codahale.metrics.MetricRegistry;
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
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.attendance.AttendanceProcessor;
import static id.unifi.service.attendance.db.Attendance.ATTENDANCE;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.HttpServer;
import id.unifi.service.common.api.Protocol;
import static id.unifi.service.common.api.SerializationUtils.getObjectMapper;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.ClientDetectable;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.common.detection.RawSiteDetectionReports;
import id.unifi.service.common.operator.InMemorySessionTokenStore;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.provider.LoggingEmailSender;
import id.unifi.service.common.util.MetricUtils;
import static id.unifi.service.common.util.TimeUtils.utcLocalFromInstant;
import id.unifi.service.common.version.VersionInfo;
import id.unifi.service.core.agents.IdentityService;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.DETECTABLE;
import static id.unifi.service.core.db.Tables.RFID_DETECTION;
import static java.net.InetSocketAddress.createUnresolved;
import static java.util.stream.Collectors.toList;
import org.eclipse.jetty.websocket.api.Session;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.function.IntFunction;

public class CoreService {
    static {
        // Prefer IPv4, otherwise 0.0.0.0 gets interpreted as IPv6 broadcast
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    public static final String PENDING_RAW_DETECTIONS_QUEUE_NAME = "core.detection.pending-raw-detections";

    private static final BlockingQueue<TaggedDetectionReport> detectionQueue = new ArrayBlockingQueue<>(10_000);

    public interface MqConfig {
        @Default("127.0.0.1:5672")
        HostAndPort endpoint();
    }

    @Prefix("unifi")
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

        var config = Envy.configure(Config.class, UnifiConfigSource.get(), HostAndPortValueParser.instance);

        var registry = new MetricRegistry();
        var jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

        var dbProvider = new DatabaseProvider();
        dbProvider.bySchema(CORE, ATTENDANCE); // TODO: Migrate in a more normal way
        var detectionProcessor = new DefaultDetectionProcessor(dbProvider);

        var componentHolder = new ComponentHolder(Map.of(
                MetricRegistry.class, registry,
                DatabaseProvider.class, dbProvider,
                MqConfig.class, config.mq(),
                DetectionProcessor.class, detectionProcessor,
                SessionTokenStore.class, new InMemorySessionTokenStore(864000),
                EmailSenderProvider.class, new LoggingEmailSender()));

        startApiService(config.apiServiceListenEndpoint(), componentHolder);
        var mapper = startAgentService(componentHolder, config.agentServiceListenEndpoint());
        var channel = startRawDetectionConsumer(mapper, config.mq());
        var attendanceProcessor = new AttendanceProcessor(dbProvider);
        processQueue(channel, dbProvider.bySchema(CORE), detectionProcessor, attendanceProcessor);
    }

    private static void processQueue(Channel channel,
                                     Database db,
                                     DetectionProcessor detectionProcessor,
                                     AttendanceProcessor attendanceProcessor) {
        var thread = new Thread(() -> {
            var insertQuery = insertInto(RFID_DETECTION,
                    RFID_DETECTION.CLIENT_ID,
                    RFID_DETECTION.DETECTABLE_ID,
                    RFID_DETECTION.DETECTABLE_TYPE,
                    RFID_DETECTION.READER_SN,
                    RFID_DETECTION.PORT_NUMBER,
                    RFID_DETECTION.DETECTION_TIME,
                    RFID_DETECTION.RSSI,
                    RFID_DETECTION.COUNT)
                    .values((String) null, null, null, null, null, null, null, null)
                    .onConflictDoNothing();

            while (true) {
                if (detectionQueue.size() < 200) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        log.info("Detection processing thread interrupted, stopping");
                        return;
                    }
                    if (detectionQueue.isEmpty()) continue;
                }

                ArrayList<TaggedDetectionReport> allTagged = new ArrayList<>(1000);
                detectionQueue.drainTo(allTagged, 1000);

                log.debug("Processing {} detection reports", allTagged.stream().mapToInt(t -> t.reports.size()).sum());

                var detections = allTagged.stream()
                        .flatMap(t -> t.reports.stream().flatMap(r -> r.detections.stream().map(d ->
                                new Detection(new ClientDetectable(t.clientId, d.detectableId, d.detectableType),
                                        r.readerSn, d.portNumber, d.timestamp, d.rssi, d.count))))
                        .collect(toList());

                var detectableIdRows = detections.stream()
                        .map(d -> d.detectable)
                        .distinct()
                        .map(cd -> DSL.row(cd.clientId, cd.detectableId))
                        .toArray((IntFunction<Row2<String, String>[]>) Row2[]::new);

                db.execute(sql -> {
                    var vDetectableId = field(name("v", "detectable_id"), String.class);
                    var vClientId = field(name("v", "client_id"), String.class);
                    var unknownDetectableIds = sql
                            .select(vDetectableId)
                            .from(values(detectableIdRows).asTable("v", "client_id", "detectable_id"))
                            .leftJoin(DETECTABLE).using(vClientId, vDetectableId)
                            .where(DETECTABLE.DETECTABLE_ID.isNull())
                            .fetch(Record1::value1);

                    log.trace("Unknown: {}", unknownDetectableIds);

                    var batch = sql.batch(insertQuery);
                    detections.forEach(detection -> {
                        if (!unknownDetectableIds.contains(detection.detectable.detectableId)) {
                            batch.bind(
                                    detection.detectable.clientId,
                                    detection.detectable.detectableId,
                                    detection.detectable.detectableType.toString(),
                                    detection.readerSn,
                                    detection.portNumber,
                                    utcLocalFromInstant(detection.detectionTime),
                                    detection.rssi,
                                    detection.count);
                        }
                    });
                    var batchSize = batch.size();
                    if (batchSize > 0) {
                        batch.execute();
                    }
                    return null;
                });

                detections.forEach(detectionProcessor::process);
                attendanceProcessor.processDetections(detections);

                if (!allTagged.isEmpty()) {
                    var deliveryTag = allTagged.get(allTagged.size() - 1).deliveryTag;
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
        var factory = new ConnectionFactory();
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
                var report = mapper.readValue(body, RawSiteDetectionReports.class);
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
        var agentRegistry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agents"), componentHolder);
        var agentDispatcher = new Dispatcher<>(agentRegistry, AgentSessionData.class, s -> new AgentSessionData());
        componentHolder.get(IdentityService.class).setAgentDispatcher(agentDispatcher); // FIXME: break circular dependency
        agentDispatcher.addSessionListener(new Dispatcher.SessionListener<>() {
            public void onSessionCreated(Session session, AgentSessionData data) {
                log.info("New agent session: {}", session);
            }

            public void onSessionDropped(Session session) {
                log.info("Session dropped: {}", session);
            }
        });

        var agentServerSocket = createUnresolved(agentEndpoint.getHost(), agentEndpoint.getPort());
        var agentServer = new HttpServer(
                agentServerSocket,
                "/agents",
                agentDispatcher,
                Set.of(Protocol.MSGPACK));
        agentServer.start();
        return getObjectMapper(Protocol.MSGPACK);
    }

    private static void startApiService(HostAndPort apiEndpoint, ComponentHolder componentHolder) throws Exception {
        var registry = new ServiceRegistry(
                Map.of(
                        "core", "id.unifi.service.core.services",
                        "attendance", "id.unifi.service.attendance.services"),
                componentHolder);
        var dispatcher = new Dispatcher<>(registry, OperatorSessionData.class, s -> new OperatorSessionData());
        var apiServerSocket = createUnresolved(apiEndpoint.getHost(), apiEndpoint.getPort());
        var apiServer = new HttpServer(
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
