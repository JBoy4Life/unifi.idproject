package id.unifi.service.core;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;

import java.util.Objects;

public class AgentSessionData {
    private String clientId;
    private String siteId;

    public AgentSessionData(Session session) {
        UpgradeRequest upgradeRequest = session.getUpgradeRequest();
        clientId = Objects.requireNonNull(upgradeRequest.getHeader("x-client-id"),
                "No client ID specified in agent");
        siteId = Objects.requireNonNull(upgradeRequest.getHeader("x-site-id"),
                "No site ID specified in agent");
    }

    public String getClientId() {
        return clientId;
    }

    public String getSiteId() {
        return siteId;
    }
}
