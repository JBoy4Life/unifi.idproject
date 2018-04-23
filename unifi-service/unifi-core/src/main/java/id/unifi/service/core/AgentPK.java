package id.unifi.service.core;

import java.util.Objects;

public class AgentPK {
    public final String clientId;
    public final String agentId;

    public AgentPK(String clientId, String agentId) {
        this.clientId = clientId;
        this.agentId = agentId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var agentPK = (AgentPK) o;
        return Objects.equals(clientId, agentPK.clientId) &&
                Objects.equals(agentId, agentPK.agentId);
    }

    public int hashCode() {

        return Objects.hash(clientId, agentId);
    }

    public String toString() {
        return "AgentPK{" +
                "clientId='" + clientId + '\'' +
                ", agentId='" + agentId + '\'' +
                '}';
    }
}
