package id.unifi.service.core.agent;

import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import id.unifi.service.common.detection.SiteDetectionReport;
import id.unifi.service.core.agent.config.ConfigAdapter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CoreClient {
    private static final Logger log = LoggerFactory.getLogger(CoreClient.class);

    private final Dispatcher<Boolean> dispatcher;
    private final URI serviceUri;
    private final String clientId;
    private final String agentId;
    private final AtomicReference<Session> sessionRef;
    private CountDownLatch authenticated;
    private final Thread connectThread;
    private final byte[] password;

    CoreClient(URI serviceUri,
               String clientId,
               String agentId,
               byte[] password,
               ConfigAdapter configAdapter) {
        this.serviceUri = serviceUri;
        this.clientId = clientId;
        this.agentId = agentId;
        this.password = password;

        sessionRef = new AtomicReference<>();

        var componentHolder = new ComponentHolder(Map.of(ConfigAdapter.class, configAdapter));
        var registry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agent.services"),
                componentHolder);
        dispatcher = new Dispatcher<>(registry, Boolean.class, t -> true);
        dispatcher.putMessageListener("core.detection.process-raw-detections-result",
                (om, session, msg) -> log.trace("Confirmed detection"));

        dispatcher.putMessageListener("core.identity.auth-password-result", (om, session, msg) -> {
            sessionRef.set(session);
            authenticated.countDown();
        });

        dispatcher.putMessageListener("core.error.authentication-failed", (om, session, msg) -> {
            sessionRef.set(null);
            authenticated.countDown();
        });

        connectThread = new Thread(this::maintainConnection);
        connectThread.start();
    }

    private void maintainConnection() {
        while (true) {
            try {
                var client = new WebSocketClient();
                client.start();
                var request = new ClientUpgradeRequest();
                var delegate = new WebSocketDelegate(dispatcher, Protocol.MSGPACK);
                var sessionFuture = client.connect(delegate, serviceUri, request);
                log.info("Waiting for connection to service");
                Session session;
                session = sessionFuture.get();

                log.info("Connection to service established ({}), authenticating", serviceUri);
                authenticated = new CountDownLatch(1);
                dispatcher.request(session, Protocol.MSGPACK, "core.identity.auth-password",
                        Map.of("clientId", clientId, "agentId", agentId, "password", password));

                authenticated.await();
                if (sessionRef.get() == null) {
                    // Authentication failed; FIXME: build a proper client
                    log.error("Agent authentication failed");
                    session.close();
                }
                var closeCode = delegate.awaitClose();
                log.info("Connection closed (WebSocket code {})", closeCode);
            } catch (Exception e) {
                log.error("Can't establish connection to server ({})", serviceUri);
            }

            try {
                log.info("Reconnecting in 10 seconds");
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                log.info("Connection thread interrupted, stopping");
                break;
            }
        }
    }

    public void sendDetectionReports(List<SiteDetectionReport> reports, Runnable ackCallback) {
        Map<String, Object> params = Map.of("reports", reports);
        while (true) {
            try {
                var session = sessionRef.get();
                if (session == null) throw new IOException("No session");
                dispatcher.request(
                        session,
                        Protocol.MSGPACK,
                        "core.detection.process-raw-detections",
                        params);
                ackCallback.run(); // TODO: wait for response
                break;
            } catch (WebSocketException | IOException e) {
                log.info("Couldn't send detection report to server, retrying soon...");
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException e1) {
                    log.info("Interrupted while waiting to send detection reports");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
