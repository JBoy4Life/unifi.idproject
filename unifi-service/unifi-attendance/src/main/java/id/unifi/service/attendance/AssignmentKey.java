package id.unifi.service.attendance;

import java.util.Objects;

public final class AssignmentKey {
    public final String clientId;
    public final String clientReference;
    public final String scheduleId;

    public AssignmentKey(String clientId, String clientReference, String scheduleId) {
        this.clientId = clientId;
        this.clientReference = clientReference;
        this.scheduleId = scheduleId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentKey that = (AssignmentKey) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientReference, that.clientReference) &&
                Objects.equals(scheduleId, that.scheduleId);
    }

    public int hashCode() {
        return Objects.hash(clientId, clientReference, scheduleId);
    }
}
