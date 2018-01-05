package id.unifi.service.core.agent;

import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.WebSocketDelegate;
import id.unifi.service.common.detection.RawDetectionReport;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CoreClient {
    private static final Logger log = LoggerFactory.getLogger(CoreClient.class);

    private final Dispatcher<Boolean> dispatcher;
    private final BlockingQueue<RawDetectionReport> pendingReports;
    private final Thread sendThread;

    CoreClient(URI serviceUri, String clientId, String siteId, ReaderManager readerManager) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("x-client-id", clientId);
        request.setHeader("x-site-id", siteId);
        ServiceRegistry registry = new ServiceRegistry(
                Map.of("core", "id.unifi.service.core.agent.services"),
                Map.of(ReaderManager.class, readerManager));
        dispatcher = new Dispatcher<>(registry, Boolean.class, t -> true);
        dispatcher.putMessageListener("core.detection.process-raw-detections-result", Void.class,
                (s, o) -> log.debug("Confirmed detection"));
        WebSocketDelegate delegate = new WebSocketDelegate(dispatcher, Protocol.MSGPACK);
        Future<Session> sessionFuture = client.connect(delegate, serviceUri, request);
        pendingReports = new ArrayBlockingQueue<>(10_000);

        sendThread = new Thread(() -> {
            log.info("Waiting for connection to service");
            Session session;
            try {
                session = sessionFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            log.info("Connection to service established");

            while (true) {
                RawDetectionReport report;
                try {
                    report = pendingReports.take();
                } catch (InterruptedException e) {
                    continue;
                }
                Map<String, Object> params = Map.of("report", report);
                dispatcher.request(session, Protocol.MSGPACK, "core.detection.process-raw-detections", params);
            }
        });
        sendThread.start();
    }

    public void sendRawDetections(RawDetectionReport report) {
        pendingReports.add(report);
    }
}
