package id.unifi.service.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private static final long DEFAULT_WEB_SOCKET_IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(500);

    private final Server server;
    private final String basePath;
    private final Dispatcher dispatcher;
    private final Set<Protocol> protocols;
    private final long webSocketIdleTimeoutMillis;

    public HttpServer(InetSocketAddress socketAddress,
                      String basePath,
                      Dispatcher dispatcher,
                      Set<Protocol> protocols) {
        this(socketAddress, basePath, dispatcher, protocols, DEFAULT_WEB_SOCKET_IDLE_TIMEOUT_MILLIS);
    }

    public HttpServer(InetSocketAddress socketAddress,
                      String basePath,
                      Dispatcher dispatcher,
                      Set<Protocol> protocols,
                      long webSocketIdleTimeoutMillis) {
        this.basePath = basePath;
        this.dispatcher = dispatcher;
        this.protocols = protocols;
        this.webSocketIdleTimeoutMillis = webSocketIdleTimeoutMillis;
        this.server = new Server(socketAddress);
    }

    public void start() throws Exception {
        WebSocketHandler webSocketHandler = new WebSocketHandler() {
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(webSocketIdleTimeoutMillis);
                factory.getExtensionFactory().unregister("permessage-deflate");
                factory.setCreator(new WebSocketDelegate.Creator(dispatcher, basePath, protocols));
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
