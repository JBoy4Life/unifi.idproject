package id.unifi.service.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.concurrent.TimeUnit;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private static final long DEFAULT_WEB_SOCKET_IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(500);
    private static final String BASE_PATH = "/service";

    private final Server server;
    private final Dispatcher dispatcher;
    private final long webSocketIdleTimeoutMillis;

    public HttpServer(int port, Dispatcher<?> dispatcher) {
        this(port, dispatcher, DEFAULT_WEB_SOCKET_IDLE_TIMEOUT_MILLIS);
    }

    public HttpServer(int port, Dispatcher dispatcher, long webSocketIdleTimeoutMillis) {
        this.dispatcher = dispatcher;
        this.webSocketIdleTimeoutMillis = webSocketIdleTimeoutMillis;
        this.server = new Server(port);
    }

    public void start() throws Exception {
        WebSocketHandler webSocketHandler = new WebSocketHandler() {
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(webSocketIdleTimeoutMillis);
                factory.setCreator(new WebSocketDelegate.Creator(dispatcher, BASE_PATH));
            }
        };
        server.setHandler(webSocketHandler);
        server.setStopAtShutdown(true);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
