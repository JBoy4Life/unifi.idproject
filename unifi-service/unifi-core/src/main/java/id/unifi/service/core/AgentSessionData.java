package id.unifi.service.core;

import org.eclipse.jetty.websocket.api.Session;

public class AgentSessionData {
    private String clientId;

    public AgentSessionData(Session session) {
        clientId = session.getUpgradeRequest().getHeader("x-client-id");
        if (clientId == null) {
            throw new RuntimeException("No client ID specified in agent");
        }
    }

    public String getClientId() {
        return clientId;
    }
}
