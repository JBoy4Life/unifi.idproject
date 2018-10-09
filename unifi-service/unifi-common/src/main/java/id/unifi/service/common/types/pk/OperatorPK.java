package id.unifi.service.common.types.pk;

import java.util.Objects;

public class OperatorPK {
    public final String clientId;
    public final String username;

    public OperatorPK(String clientId, String username) {
        this.clientId = clientId;
        this.username = username;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (OperatorPK) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(username, that.username);
    }

    public int hashCode() {
        return Objects.hash(clientId, username);
    }

    public String toString() {
        return "OperatorPK{" +
                "clientId='" + clientId + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
