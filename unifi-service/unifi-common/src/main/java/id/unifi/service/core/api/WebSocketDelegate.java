package id.unifi.service.core.api;

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
import java.nio.charset.StandardCharsets;

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
        private final String basePath;

        Creator(Dispatcher dispatcher, String basePath) {
            this.dispatcher = dispatcher;
            this.basePath = basePath;
        }

        public WebSocketDelegate createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse res) {
            if ((basePath + "/json").equals(req.getHttpServletRequest().getPathInfo())) {
                return new WebSocketDelegate(dispatcher, Protocol.JSON);
            } else if ((basePath + "/msgpack").equals(req.getHttpServletRequest().getPathInfo())) {
                return new WebSocketDelegate(dispatcher, Protocol.MSGPACK);
            } else {
                res.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        log.debug("Connected: {}", session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int closeCode, String closeReason) {
        log.debug("Closed: {} {} {}", session, closeCode, closeReason);
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
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
                        session.getRemote().sendString(new String(payload.array(), StandardCharsets.UTF_8), writeCallback);
                    } else {
                        session.getRemote().sendBytes(payload, writeCallback);
                    }
                } catch (Exception e) {
                    log.error("Websocket error", e);
                }
            }
        };
        dispatcher.dispatch(stream, protocol, returnChannel);
    }
}
