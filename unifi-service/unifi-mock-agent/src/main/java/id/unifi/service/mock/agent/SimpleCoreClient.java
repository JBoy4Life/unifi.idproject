package id.unifi.service.mock.agent;

import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import static id.unifi.service.common.api.Protocol.MSGPACK;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.core.agent.ReaderManager;
import id.unifi.service.core.agent.config.AgentFullConfig;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleCoreClient {
    private static final Logger log = LoggerFactory.getLogger(SimpleCoreClient.class);

    private final Dispatcher<Boolean> dispatcher;
    private final CountDownLatch configured;
    private final BlockingQueue<Boolean> authenticationQueue;
    private final URI serviceUri;
    private final String clientId;
    private final String agentId;
    private final byte[] password;
    private volatile AgentFullConfig config;
    private volatile Session session;

    SimpleCoreClient(URI serviceUri, String clientId, String agentId, byte[] password) {
        this.serviceUri = serviceUri;
        this.clientId = clientId;
        this.agentId = agentId;
        this.password = password;
        this.configured = new CountDownLatch(1);
        this.authenticationQueue = new ArrayBlockingQueue<>(1);
        var componentHolder = new ComponentHolder(Map.of(ReaderManager.class, (ReaderManager) config -> {
            this.config = config;
            configured.countDown();
        }));
        var registry = new ServiceRegistry(Map.of("core", "id.unifi.service.core.agent.services"), componentHolder);
        this.dispatcher = new Dispatcher<>(registry, Boolean.class, t -> true);
        dispatcher.putMessageListener("core.detection.process-raw-detections-result", (om, session, msg) -> {});

        dispatcher.putMessageListener("core.identity.auth-password-result",
                (om, session, msg) -> authenticationQueue.offer(true));
        dispatcher.putMessageListener("core.error.authentication-failed",
                (om, session, msg) -> authenticationQueue.offer(false));
    }

    public AgentFullConfig connect() {
        try {
            var client = new WebSocketClient();
            client.start();
            var request = new ClientUpgradeRequest();
            var delegate = new WebSocketDelegate(dispatcher, MSGPACK);
            log.info("Waiting for connection to service");
            var session = client.connect(delegate, serviceUri, request).get();

            log.info("Connection to service established ({}), authenticating", serviceUri);
            dispatcher.request(session, MSGPACK, "core.identity.auth-password",
                    Map.of("clientId", clientId, "agentId", agentId, "password", password));

            var authResult = authenticationQueue.poll(10, TimeUnit.SECONDS);
            if (authResult == null) throw new RuntimeException("Authentication timeout");
            if (!authResult) throw new RuntimeException("Authentication failed");
            configured.await();
            this.session = session;
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDetectionReports(List<SiteDetectionReport> reports) {
        if (session == null) throw new IllegalStateException();
        try {
            dispatcher.request(session, MSGPACK, "core.detection.process-raw-detections", Map.of("reports", reports));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
