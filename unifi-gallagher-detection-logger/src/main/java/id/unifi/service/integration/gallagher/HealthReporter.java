package id.unifi.service.integration.gallagher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import id.unifi.service.common.agent.ReaderHealth;
import id.unifi.service.common.api.ComponentHolder;
import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import id.unifi.service.common.api.errors.AuthenticationFailed;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HealthReporter { // TODO: reuse client code, simplify
    private static final Type READER_HEALTH_RESULT_TYPE = new TypeReference<List<ReaderHealth>>() {}.getType();

    private static final Logger log = LoggerFactory.getLogger(HealthReporter.class);

    private final ServiceApiConfig config;
    private final Consumer<List<ReaderHealth>> consumer;
    private final Thread connectThread;
    private final AtomicReference<Session> sessionRef;
    private final ScheduledExecutorService reportExecutor;
    private final Dispatcher<Boolean> dispatcher;

    private CountDownLatch authenticated;

    static HealthReporter create(ServiceApiConfig config, Consumer<List<ReaderHealth>> consumer) {
        var client = new HealthReporter(config, consumer);
        client.start();
        return client;
    }

    private HealthReporter(ServiceApiConfig config, Consumer<List<ReaderHealth>> consumer) {
        this.config = config;
        this.dispatcher = new Dispatcher<>(
                new ServiceRegistry(Map.of(), new ComponentHolder(Map.of())), Boolean.class, s -> true);
        this.consumer = consumer;
        this.sessionRef = new AtomicReference<>();
        this.connectThread = new Thread(this::maintainConnection);
        this.reportExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    private void start() {
        connectThread.start();
        reportExecutor.scheduleWithFixedDelay(this::reportHealth, 10, 10, TimeUnit.SECONDS);
    }

    private void maintainConnection() {
        dispatcher.putMessageListener("core.operator.auth-password-result", JsonNode.class, (s, o) -> {
            sessionRef.set(s);
            authenticated.countDown();
        });

        dispatcher.putMessageListener("core.error.authentication-failed", AuthenticationFailed.class, (s, e) -> {
            sessionRef.set(null);
            authenticated.countDown();
        });

        while (true) {
            try {
                sessionRef.set(null);
                var client = new WebSocketClient();
                client.start();
                var request = new ClientUpgradeRequest();
                var delegate = new WebSocketDelegate(dispatcher, Protocol.MSGPACK);
                var sessionFuture = client.connect(delegate, config.uri(), request);
                log.info("Waiting for connection to service");
                Session session;
                session = sessionFuture.get();

                log.info("Connection to service established ({}), authenticating", config.uri());
                authenticated = new CountDownLatch(1);
                dispatcher.request(session, Protocol.MSGPACK, "core.operator.auth-password", Map.of(
                        "clientId", config.clientId(),
                        "username", config.username(),
                        "password", config.password()));

                authenticated.await();
                if (sessionRef.get() == null) {
                    log.error("Authentication failed");
                    session.close();
                } else {
                    log.info("Authenticated");
                }
                var closeCode = delegate.awaitClose();
                log.info("Connection closed (WebSocket code {})", closeCode);
            } catch (Exception e) {
                log.error("Can't establish connection to server ({})", config.uri());
            }

            try {
                log.info("Reconnecting to service in 10 seconds");
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                log.info("Connection thread interrupted, stopping");
                break;
            }
        }
    }

    private void reportHealth() {
        // Skip reporting this time if we know we're not connected and authenticated
        var session = sessionRef.get();
        if (session == null || !session.isOpen()) return;
        if (authenticated.getCount() > 0) return;

        var healthQueue = new ArrayBlockingQueue<List<ReaderHealth>>(1);

        // TODO: Correlate properly
        try {
            dispatcher.<List<ReaderHealth>>putMessageListener(
                    "core.detection.get-reader-health-result",
                    READER_HEALTH_RESULT_TYPE,
                    (s, health) -> healthQueue.offer(health));
            dispatcher.request(session, Protocol.MSGPACK, "core.detection.get-reader-health",
                    Map.of("clientId", config.clientId()));
            var health = healthQueue.poll(10, TimeUnit.SECONDS);
            if (health == null) throw new RuntimeException("Fetching reader health timed out");
            consumer.accept(health);
        } catch (IOException | RuntimeException e) {
            log.warn("Failed to report health to Gallagher", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
