package id.unifi.service.core.operator;

public class OperatorInfo {
    public final String clientId;
    public final String username;
    public final String email;
    public final boolean active;

    public OperatorInfo(String clientId, String username, String email, boolean active) {
        this.clientId = clientId;
        this.username = username;
        this.email = email;
        this.active = active;
    }
}
