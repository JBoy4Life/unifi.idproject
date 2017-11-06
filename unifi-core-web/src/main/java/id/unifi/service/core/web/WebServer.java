package id.unifi.service.core.web;

import com.statemachinesystems.envy.Default;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.core.config.FileConfigSource;
import id.unifi.service.core.version.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private interface Config {
        @Default("8000")
        int httpPort();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
                    .error("Uncaught exception in thread '" + t.getName() + "'", e);
            System.exit(1);
        });

        log.info("Starting unifi.id Web Server");
        VersionInfo.log();

        Config config = Envy.configure(Config.class, FileConfigSource.get());

        Server server = new Server(config.httpPort());
        WebSocketHandler wsHandler = new WebSocketHandler() {
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(50_000);
                // TODO: set up delegate
            }
        };

        wsHandler.setHandler(new AbstractHandler() {
            public void handle(String target,
                               Request baseReq,
                               HttpServletRequest req,
                               HttpServletResponse res) throws IOException, ServletException {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("Hello, unifi.id!");
                res.flushBuffer();
            }
        });

        server.setHandler(wsHandler);
        server.setStopAtShutdown(true);
        server.start();
    }
}
