package id.unifi.service.core;

import com.codahale.metrics.MetricRegistry;
import com.google.common.net.HostAndPort;
import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import com.statemachinesystems.envy.Nullable;
import com.statemachinesystems.envy.Prefix;
import id.unifi.service.attendance.AttendanceMatcher;
import id.unifi.service.attendance.AttendanceProcessor;
import static id.unifi.service.attendance.db.Attendance.ATTENDANCE;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.HttpServer;
import static id.unifi.service.common.api.Protocol.JSON;
import static id.unifi.service.common.api.Protocol.MSGPACK;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.http.HttpUtils;
import id.unifi.service.common.config.HostAndPortValueParser;
import id.unifi.service.common.config.MqConfig;
import id.unifi.service.common.config.UnifiConfigSource;
import id.unifi.service.common.operator.InMemorySessionTokenStore;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.provider.LoggingEmailSender;
import id.unifi.service.common.subscriptions.InMemorySubscriptionManager;
import id.unifi.service.common.subscriptions.SubscriptionManager;
import id.unifi.service.common.util.MetricUtils;
import id.unifi.service.common.version.VersionInfo;
import id.unifi.service.core.agents.IdentityService;
import static id.unifi.service.core.db.Core.CORE;
import id.unifi.service.core.email.SmtpEmailSenderProvider;
import id.unifi.service.core.processing.DetectionMatcher;
import id.unifi.service.core.processing.DetectionProcessor;
import id.unifi.service.core.processing.consumer.DetectionPersistence;
import id.unifi.service.core.processing.listener.DetectionSubscriber;
import id.unifi.service.core.sms.AwsSmsSenderProvider;
import id.unifi.service.core.sms.LoggingSmsSenderProvider;
import id.unifi.service.core.sms.SmsSenderProvider;
import id.unifi.service.core.util.RegionsValueParser;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.net.InetSocketAddress.createUnresolved;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class CoreService {
    static {
        // Prefer IPv4, otherwise 0.0.0.0 gets interpreted as IPv6 broadcast
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Redirect j.u.l logs to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

    }

    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    @Prefix("unifi")
    private interface Config {
        @Default("0.0.0.0:8000")
        HostAndPort apiServiceListenEndpoint();

        @Default("0.0.0.0:8001")
        HostAndPort agentServiceListenEndpoint();

        MqConfig mq();

        @Nullable
        String smtpServer();

        @Default("false")
        boolean smsEnabled();
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
        
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Core");
        VersionInfo.log();

        var config = Envy.configure(Config.class, UnifiConfigSource.get(),
                HostAndPortValueParser.instance, RegionsValueParser.instance);

        var registry = new MetricRegistry();
        var jmxReporter = MetricUtils.createJmxReporter(registry);
        jmxReporter.start();

        var dbProvider = new DatabaseProvider();
        dbProvider.bySchema(CORE, ATTENDANCE); // TODO: Migrate in a more normal way

        var subscriptionManager = new InMemorySubscriptionManager();
        var detectionSubscriber = new DetectionSubscriber(subscriptionManager);
        var detectionPersistence = new DetectionPersistence(dbProvider);

        var attendanceMatcher = new AttendanceMatcher(dbProvider);
        var attendanceProcessor = new AttendanceProcessor(dbProvider, attendanceMatcher);

        var detectionMatcher = new DetectionMatcher(dbProvider);
        var detectionProcessor = new DetectionProcessor(config.mq(), detectionMatcher,
                Set.of(detectionPersistence, attendanceProcessor),
                Set.of(detectionSubscriber));

        var emailSenderProvider = config.smtpServer() != null
                ? new SmtpEmailSenderProvider(config.mq())
                : new LoggingEmailSender();

        var smsSenderProvider = config.smsEnabled()
                ? new AwsSmsSenderProvider(config.mq())
                : new LoggingSmsSenderProvider();

        var componentHolder = new ComponentHolder(Map.of(
                MetricRegistry.class, registry,
                DatabaseProvider.class, dbProvider,
                MqConfig.class, config.mq(),
                SubscriptionManager.class, subscriptionManager,
                DetectionSubscriber.class, detectionSubscriber,
                DetectionProcessor.class, detectionProcessor,
                SessionTokenStore.class, new InMemorySessionTokenStore(864000),
                EmailSenderProvider.class, emailSenderProvider,
                SmsSenderProvider.class, smsSenderProvider));

        startApiService(config.apiServiceListenEndpoint(), componentHolder, subscriptionManager);
        startAgentService(componentHolder, config.agentServiceListenEndpoint());
    }

    private static void startAgentService(ComponentHolder componentHolder, HostAndPort agentEndpoint) throws Exception {
        var agentRegistry = new ServiceRegistry(Map.of("core", "id.unifi.service.core.agents"), componentHolder);
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
        var agentServer = new HttpServer(agentServerSocket, "/agents", agentDispatcher, List.of(MSGPACK));
        agentServer.start(false);
    }

    private static void startApiService(HostAndPort apiEndpoint,
                                        ComponentHolder componentHolder,
                                        SubscriptionManager subscriptionManager) throws Exception {
        var registry = new ServiceRegistry(
                Map.of(
                        "core", "id.unifi.service.core.services",
                        "attendance", "id.unifi.service.attendance.services"),
                componentHolder);
        var sessionTokenStore = componentHolder.get(SessionTokenStore.class);
        var dispatcher = new Dispatcher<>(
                registry, OperatorSessionData.class, s -> new OperatorSessionData(), subscriptionManager,
                request -> Optional.ofNullable(request.getHeader("authorization"))
                        .flatMap(HttpUtils::extractAuthToken)
                        .flatMap(t -> sessionTokenStore.get(t).map(op -> new OperatorSessionData(op, t)))
                        .orElseGet(OperatorSessionData::new));
        var apiServerSocket = createUnresolved(apiEndpoint.getHost(), apiEndpoint.getPort());
        var apiServer = new HttpServer(apiServerSocket, "/service", dispatcher, List.of(JSON, MSGPACK));
        apiServer.start(true);
    }
}
