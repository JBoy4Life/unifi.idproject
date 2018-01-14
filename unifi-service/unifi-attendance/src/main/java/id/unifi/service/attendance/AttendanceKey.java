package id.unifi.service.attendance;

import java.util.Objects;

public final class AttendanceKey {
    public final String clientId;
    public final String clientReference;
    public final String scheduleId;
    public final String blockId;

    public AttendanceKey(String clientId, String clientReference, String scheduleId, String blockId) {
        this.clientId = clientId;
        this.clientReference = clientReference;
        this.scheduleId = scheduleId;
        this.blockId = blockId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceKey that = (AttendanceKey) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientReference, that.clientReference) &&
                Objects.equals(scheduleId, that.scheduleId) &&
                Objects.equals(blockId, that.blockId);
    }

    public int hashCode() {
        return Objects.hash(clientId, clientReference, scheduleId, blockId);
    }

    public String toString() {
        return "AttendanceKey{" +
                "clientId='" + clientId + '\'' +
                ", clientReference='" + clientReference + '\'' +
                ", scheduleId='" + scheduleId + '\'' +
                ", blockId='" + blockId + '\'' +
                '}';
    }
}
