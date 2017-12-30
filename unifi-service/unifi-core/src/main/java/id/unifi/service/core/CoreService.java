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
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.HttpServer;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.db.DatabaseProvider;
import id.unifi.service.common.detection.RawSiteDetectionReport;
import id.unifi.service.common.operator.InMemorySessionTokenStore;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.provider.LoggingEmailSender;
import id.unifi.service.common.version.VersionInfo;
import static java.net.InetSocketAddress.createUnresolved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
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

        startApiService(config.apiServiceListenEndpoint(), config.mq(), detectionProcessor);
        ObjectMapper mapper = startAgentService(config.agentServiceListenEndpoint());
        startRawDetectionConsumer(detectionProcessor, mapper, config.mq());
    }

    private static void startRawDetectionConsumer(DetectionProcessor detectionProcessor,
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
                    detectionProcessor.process(report.clientId, report.siteId, report.report);
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

    private static ObjectMapper startAgentService(HostAndPort agentEndpoint) throws Exception {
        ServiceRegistry agentRegistry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agents"),
                Map.of());
        Dispatcher<AgentSessionData> agentDispatcher =
                new Dispatcher<>(agentRegistry, AgentSessionData.class, AgentSessionData::new);

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

    private static void startApiService(HostAndPort apiEndpoint, MqConfig mqConfig, Object detectionProcessor) throws Exception {
        DatabaseProvider dbProvider = new DatabaseProvider();
        ServiceRegistry registry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.services"),
                Map.of(
                        MqConfig.class, mqConfig,
                        DetectionProcessor.class, detectionProcessor,
                        SessionTokenStore.class, new InMemorySessionTokenStore(864000),
                        EmailSenderProvider.class, new LoggingEmailSender()));
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
