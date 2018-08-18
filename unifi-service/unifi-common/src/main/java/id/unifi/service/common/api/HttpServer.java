package id.unifi.service.common.api;

import id.unifi.service.common.api.http.HttpApiServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private static final long DEFAULT_WEB_SOCKET_IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(500);

    private final Server server;
    private final String basePath;
    private final Dispatcher dispatcher;
    private final List<Protocol> protocols;
    private final long webSocketIdleTimeoutMillis;

    public HttpServer(InetSocketAddress socketAddress,
                      String basePath,
                      Dispatcher dispatcher,
                      List<Protocol> protocols) {
        this(socketAddress, basePath, dispatcher, protocols, DEFAULT_WEB_SOCKET_IDLE_TIMEOUT_MILLIS);
    }

    public HttpServer(InetSocketAddress socketAddress,
                      String basePath,
                      Dispatcher dispatcher,
                      List<Protocol> protocols,
                      long webSocketIdleTimeoutMillis) {
        this.basePath = basePath;
        this.dispatcher = dispatcher;
        this.protocols = protocols;
        this.webSocketIdleTimeoutMillis = webSocketIdleTimeoutMillis;
        this.server = new Server(socketAddress);
    }

    public void start(boolean withHttpSupport) throws Exception {
        var webSocketHandler = new WebSocketHandler() {
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(webSocketIdleTimeoutMillis);
                factory.getPolicy().setMaxBinaryMessageSize(10_000_000);
                factory.getPolicy().setMaxTextMessageSize(10_000_000);
                var extensionFactory = factory.getExtensionFactory();
                extensionFactory.unregister("permessage-deflate");
                extensionFactory.unregister("x-webkit-deflate-frame");
                extensionFactory.unregister("deflate-frame");
                factory.setCreator(new WebSocketDelegate.Creator(dispatcher, basePath, protocols));
                setHandler(withHttpSupport ? createHttpHandler(dispatcher, protocols) : null);
            }
        };
        server.setHandler(webSocketHandler);
        server.setStopAtShutdown(true);
        server.start();
    }

    private static Handler createHttpHandler(Dispatcher<?> dispatcher, List<Protocol> protocols) {
        var handler = new ServletHandler();
        var servletHolder = new ServletHolder(new HttpApiServlet(dispatcher, protocols));
        handler.addServletWithMapping(servletHolder, "/api/v1/*");
        return handler;
    }

    public void stop() throws Exception {
        server.stop();
    }
}
