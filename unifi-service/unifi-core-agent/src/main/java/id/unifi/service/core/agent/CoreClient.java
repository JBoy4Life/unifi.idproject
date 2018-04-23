package id.unifi.service.core.agent;

import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import id.unifi.service.common.api.errors.AuthenticationFailed;
import id.unifi.service.common.detection.SiteDetectionReport;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CoreClient {
    private static final Logger log = LoggerFactory.getLogger(CoreClient.class);

    private final Dispatcher<Boolean> dispatcher;
    private final BlockingQueue<SiteDetectionReport> pendingReports;
    private final Thread sendThread;
    private final URI serviceUri;
    private final String clientId;
    private final String agentId;
    private final AtomicReference<Session> sessionRef;
    private CountDownLatch authenticated;
    private final Thread connectThread;
    private final byte[] password;

    CoreClient(URI serviceUri, String clientId, String agentId, byte[] password, ComponentHolder componentHolder) {
        this.serviceUri = serviceUri;
        this.clientId = clientId;
        this.agentId = agentId;
        this.password = password;

        pendingReports = new ArrayBlockingQueue<>(100_000);
        sessionRef = new AtomicReference<>();

        var registry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agent.services"),
                componentHolder);
        dispatcher = new Dispatcher<>(registry, Boolean.class, t -> true);
        dispatcher.putMessageListener("core.detection.process-raw-detections-result", Void.class,
                (s, o) -> log.trace("Confirmed detection"));

        dispatcher.putMessageListener("core.identity.auth-password-result", Void.class, (s, o) -> {
            sessionRef.set(s);
            authenticated.countDown();
        });

        dispatcher.putMessageListener("core.error.authentication-failed", AuthenticationFailed.class, (s, e) -> {
            sessionRef.set(null);
            authenticated.countDown();
        });

        connectThread = new Thread(this::maintainConnection);
        connectThread.start();

        sendThread = new Thread(this::takeAndSend);
        sendThread.setDaemon(true);
        sendThread.start();
    }

    private void takeAndSend() {
        while (true) {
            List<SiteDetectionReport> reports;
            try {
                if (pendingReports.isEmpty()) {
                    reports = List.of(pendingReports.take());
                } else {
                    reports = new ArrayList<>(200);
                    pendingReports.drainTo(reports, 200);
                }
            } catch (InterruptedException e) {
                continue;
            }
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
                    break;
                } catch (WebSocketException | IOException e) {
                    log.info("Couldn't send detection report to server, retrying soon...");
                    try {
                        Thread.sleep(5_000);
                    } catch (InterruptedException e1) {
                        return;
                    }
                }
            }
        }
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

    public void sendRawDetections(SiteDetectionReport report) {
        try {
            pendingReports.put(report);
        } catch (InterruptedException ignored) {}
        var reportsSize = pendingReports.size();
        if (reportsSize % 500 == 0 && reportsSize != 0) {
            log.info("Pending reports: {}", reportsSize);
        }
    }
}
