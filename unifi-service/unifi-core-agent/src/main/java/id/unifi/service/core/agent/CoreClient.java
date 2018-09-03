package id.unifi.service.core.agent;

import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import static id.unifi.service.common.api.client.ClientUtils.awaitResponse;
import static id.unifi.service.common.api.client.ClientUtils.oneOffWireMessageListener;
import id.unifi.service.common.api.client.UnmarshalledError;
import id.unifi.service.common.detection.SiteDetectionReport;
import static id.unifi.service.core.agent.Common.METRIC_NAME_PREFIX;
import id.unifi.service.core.agent.config.ConfigAdapter;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class CoreClient {
    private static final Logger log = LoggerFactory.getLogger(CoreClient.class);
    private static final int AUTH_TIMEOUT_SECONDS = 30;
    private static final int CONNECT_TIMEOUT_SECONDS = 30;

    private final Dispatcher<Boolean> dispatcher;
    private final URI serviceUri;
    private final String clientId;
    private final String agentId;
    private final AtomicReference<Session> sessionRef;
    private volatile CountDownLatch authenticated;
    private final Thread connectThread;
    private final byte[] password;
    private final Map<List<SiteDetectionReport>, Runnable> unackedReports;

    CoreClient(URI serviceUri,
               String clientId,
               String agentId,
               byte[] password,
               ConfigAdapter configAdapter,
               MetricRegistry registry) {
        this.serviceUri = serviceUri;
        this.clientId = clientId;
        this.agentId = agentId;
        this.password = password;

        sessionRef = new AtomicReference<>();
        authenticated = new CountDownLatch(1);

        var componentHolder = new ComponentHolder(Map.of(ConfigAdapter.class, configAdapter));
        var serviceRegistry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agent.services"),
                componentHolder);
        dispatcher = new Dispatcher<>(serviceRegistry, Boolean.class, t -> true);
        dispatcher.putMessageListener("core.detection.process-raw-detections-result",
                (om, session, msg) -> log.trace("Confirmed detection"));

        connectThread = new Thread(this::maintainConnection);
        connectThread.start();

        unackedReports = new ConcurrentHashMap<>();
        registry.gauge(name(METRIC_NAME_PREFIX, "unacked-reports"), () -> unackedReports::size);
    }

    private void maintainConnection() {
        while (true) {
            var client = new WebSocketClient();
            try {
                client.start();
                var request = new ClientUpgradeRequest();
                var delegate = new WebSocketDelegate(dispatcher, Protocol.MSGPACK);
                var sessionFuture = client.connect(delegate, serviceUri, request);
                log.info("Waiting for connection to service");

                // Need to add explicit timeout until https://github.com/eclipse/jetty.project/issues/2875 is fixed
                var session = sessionFuture.get(CONNECT_TIMEOUT_SECONDS, SECONDS);

                log.info("Connection to service established ({}), authenticating as clientId: {}, agentId: {}, password {}",
                        serviceUri, clientId, agentId, password.length > 0 ? "non-empty" : "empty");
                var authFuture = awaitResponse("core.identity.auth-password-result", listener ->
                        dispatcher.request(session, Protocol.MSGPACK, "core.identity.auth-password",
                                Map.of("clientId", clientId, "agentId", agentId, "password", password), listener));

                try {
                    authFuture.get(AUTH_TIMEOUT_SECONDS, SECONDS);
                    sessionRef.set(session);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof UnmarshalledError) {
                        var cause = (UnmarshalledError) e.getCause();
                        sessionRef.set(null);
                        if (cause.getProtocolMessageType().equals("core.error.authentication-failed")) {
                            log.error("Agent authentication failed: {}", cause.getMessage());
                            session.close();
                        } else {
                            log.error("Error authenticating agent", cause);
                        }
                    }
                    log.error("Error authenticating agent", e.getCause());
                } catch (TimeoutException e) {
                    log.error("Agent authentication timed out");
                    session.close();
                }

                if (sessionRef.get() != null) {
                    log.info("Sending {} unacked reports from previous session", unackedReports.size());
                    unackedReports.forEach(this::dispatchReports);
                }

                authenticated.countDown();

                var closeCode = delegate.awaitClose();
                log.info("Connection closed (WebSocket code {})", closeCode);
            } catch (Exception e) {
                log.error("Can't establish connection to server ({})", serviceUri, e);
            } finally {
                if (authenticated.getCount() == 0)
                    authenticated = new CountDownLatch(1);
            }

            try {
                client.stop();
                log.info("Reconnecting in 10 seconds");
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                log.info("Connection thread interrupted, stopping");
                break;
            } catch (Exception e) {
                log.error("Failed to stop client", e);
            }

        }
    }

    /**
     * Sends detections reports to the service and runs `ackCallback`, retrying if necessary.
     * May block.
     * @param reports list of detection reports
     * @param ackCallback runnable to call when the service has acknowledged the receipt
     */
    public void sendDetectionReports(List<SiteDetectionReport> reports, Runnable ackCallback) {
        try {
            authenticated.await();
            unackedReports.put(reports, ackCallback);
            dispatchReports(reports, ackCallback);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Couldn't send detection report to server, disconnecting", e);
            var session = sessionRef.get();
            if (session != null) session.close();
        }
    }

    private void dispatchReports(List<SiteDetectionReport> reports, Runnable ackCallback) {
        var session = requireNonNull(sessionRef.get());
        Map<String, Object> params = Map.of("reports", reports);
        dispatcher.request(session, Protocol.MSGPACK, "core.detection.process-raw-detections", params,
                oneOffWireMessageListener((om, s, message) -> {
                    if (message.messageType.equals("core.detection.process-raw-detections-result")) {
                        ackCallback.run();
                        unackedReports.remove(reports);
                    } else {
                        log.error("Unexpected response to report, disconnecting: {}", message);
                        session.close();
                    }
                }));
    }
}
