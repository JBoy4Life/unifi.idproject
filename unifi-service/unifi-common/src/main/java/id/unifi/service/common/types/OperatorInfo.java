package id.unifi.service.common.types;

public class OperatorInfo {
    public final String clientId;
    public final String username;
    public final String name;
    public final String email;
    public final boolean active;
    public final boolean hasPassword;

    public OperatorInfo(String clientId,
                        String username,
                        String name,
                        String email,
                        boolean active,
                        boolean hasPassword) {
        this.clientId = clientId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.active = active;
        this.hasPassword = hasPassword;
    }
}
