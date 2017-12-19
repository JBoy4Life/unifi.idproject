package id.unifi.service.common.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@WebSocket
public class WebSocketDelegate {
    private static final Logger log = LoggerFactory.getLogger(WebSocketDelegate.class);

    private final Dispatcher dispatcher;
    private final Protocol protocol;

    private WebSocketDelegate(Dispatcher dispatcher, Protocol protocol) {
        this.dispatcher = dispatcher;
        this.protocol = protocol;
    }

    public static class Creator implements WebSocketCreator {
        private final Dispatcher dispatcher;
        private final Map<String, Protocol> protocolByPath;

        Creator(Dispatcher dispatcher, String basePath, Set<Protocol> protocols) {
            this.dispatcher = dispatcher;
            this.protocolByPath = protocols.stream().collect(toMap(p -> basePath + "/" + p, identity()));
        }

        public WebSocketDelegate createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse res) {
            String path = req.getHttpServletRequest().getPathInfo();
            Protocol protocol = protocolByPath.get(path);
            if (protocol != null) {
                return new WebSocketDelegate(dispatcher, protocol);
            } else {
                res.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        log.debug("Connected: {}", session);
        dispatcher.createSession(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int closeCode, String closeReason) {
        log.debug("Closed: {} {} {}", session, closeCode, closeReason);
        dispatcher.dropSession(session);
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String message) {
        log.info("Received text message, converting to binary");
        byte[] bytes = message.getBytes(UTF_8);
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            onBinaryMessage(session, stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnWebSocketMessage
    public void onBinaryMessage(Session session, InputStream stream) {
        ReturnChannel returnChannel = payload -> {
            WriteCallback writeCallback = new WriteCallback() {
                public void writeFailed(Throwable x) {
                    log.error("Write failed", x);
                }

                public void writeSuccess() {
                    log.debug("Write OK");
                }
            };
            if (session.isOpen()) {
                try {
                    if (protocol == Protocol.JSON) {
                        log.info("Sending JSON response");
                        session.getRemote().sendString(new String(payload.array(), UTF_8), writeCallback);
                    } else {
                        log.info("Sending MessagePack response");
                        session.getRemote().sendBytes(payload, writeCallback);
                    }
                } catch (Exception e) {
                    log.error("Websocket error", e);
                }
            }
        };
        log.info("Dispatching call with " + protocol);
        dispatcher.dispatch(session, stream, protocol, returnChannel);
    }
}
