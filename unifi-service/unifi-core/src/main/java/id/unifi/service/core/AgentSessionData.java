package id.unifi.service.core;

public class AgentSessionData {
    private volatile AgentPK agent;

    AgentSessionData() {}

    public AgentPK getAgent() {
        return agent;
    }

    public void setAgent(String clientId, String agentId) {
        this.agent = new AgentPK(clientId, agentId);
    }
}
