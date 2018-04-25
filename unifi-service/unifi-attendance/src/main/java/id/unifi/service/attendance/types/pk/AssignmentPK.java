package id.unifi.service.attendance.types.pk;

import java.util.Objects;

public final class AssignmentPK {
    public final String clientId;
    public final String clientReference;
    public final String scheduleId;

    public AssignmentPK(String clientId, String clientReference, String scheduleId) {
        this.clientId = clientId;
        this.clientReference = clientReference;
        this.scheduleId = scheduleId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (AssignmentPK) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientReference, that.clientReference) &&
                Objects.equals(scheduleId, that.scheduleId);
    }

    public int hashCode() {
        return Objects.hash(clientId, clientReference, scheduleId);
    }

    public String toString() {
        return "AssignmentPK{" +
                "clientId='" + clientId + '\'' +
                ", clientReference='" + clientReference + '\'' +
                ", scheduleId='" + scheduleId + '\'' +
                '}';
    }
}
