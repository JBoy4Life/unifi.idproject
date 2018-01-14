package id.unifi.service.attendance;

import java.util.Objects;

public final class AntennaKey {
    public final String clientId;
    public final String readerSn;
    public final int portNumber;

    public AntennaKey(String clientId, String readerSn, int portNumber) {
        this.clientId = clientId;
        this.readerSn = readerSn;
        this.portNumber = portNumber;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AntennaKey that = (AntennaKey) o;
        return portNumber == that.portNumber &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(readerSn, that.readerSn);
    }

    public int hashCode() {
        return Objects.hash(clientId, readerSn, portNumber);
    }

    public String toString() {
        return "AntennaKey{" +
                "clientId='" + clientId + '\'' +
                ", readerSn='" + readerSn + '\'' +
                ", portNumber=" + portNumber +
                '}';
    }
}
