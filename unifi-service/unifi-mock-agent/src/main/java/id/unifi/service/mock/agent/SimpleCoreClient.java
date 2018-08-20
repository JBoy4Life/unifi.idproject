package id.unifi.service.mock.agent;

import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import static id.unifi.service.common.api.Protocol.MSGPACK;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import static id.unifi.service.common.api.client.ClientUtils.awaitResponse;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.core.agent.config.AgentFullConfig;
import id.unifi.service.core.agent.config.ConfigAdapter;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class SimpleCoreClient {
    private static final Logger log = LoggerFactory.getLogger(SimpleCoreClient.class);

    private final Dispatcher<Boolean> dispatcher;
    private final BlockingQueue<AgentFullConfig> configQueue;
    private final URI serviceUri;
    private final String clientId;
    private final String agentId;
    private final byte[] password;
    private volatile Session session;

    SimpleCoreClient(URI serviceUri, String clientId, String agentId, byte[] password) {
        this.serviceUri = serviceUri;
        this.clientId = clientId;
        this.agentId = agentId;
        this.password = password;
        this.configQueue = new SynchronousQueue<>();
        var componentHolder = new ComponentHolder(
                Map.of(ConfigAdapter.class, (ConfigAdapter) (config, authoritative) -> configQueue.offer(config)));
        var registry = new ServiceRegistry(Map.of("core", "id.unifi.service.core.agent.services"), componentHolder);
        this.dispatcher = new Dispatcher<>(registry, Boolean.class, t -> true);
        dispatcher.putMessageListener("core.detection.process-raw-detections-result", (om, session, msg) -> {});
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
            var authFuture = awaitResponse("core.identity.auth-password-result", listener ->
                    dispatcher.request(session, MSGPACK, "core.identity.auth-password",
                            Map.of("clientId", clientId, "agentId", agentId, "password", password), listener));
            authFuture.get(10, SECONDS);
            log.info("Configured, awaiting config from server");

            this.session = session;
            return configQueue.take();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDetectionReports(List<SiteDetectionReport> reports) {
        if (session == null) throw new IllegalStateException();
        dispatcher.request(session, MSGPACK, "core.detection.process-raw-detections", Map.of("reports", reports));
    }
}
