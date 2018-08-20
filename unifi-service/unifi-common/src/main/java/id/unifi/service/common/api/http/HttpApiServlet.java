package id.unifi.service.common.api.http;

import id.unifi.service.common.api.Dispatcher;
import id.unifi.service.common.api.Protocol;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class HttpApiServlet extends HttpServlet {
    private final Dispatcher<?> dispatcher;
    private final List<Protocol> protocols;

    public HttpApiServlet(Dispatcher<?> dispatcher, List<Protocol> protocols) {
        this.dispatcher = dispatcher;
        this.protocols = protocols;
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dispatcher.dispatch(protocols, request.startAsync());
    }
}
