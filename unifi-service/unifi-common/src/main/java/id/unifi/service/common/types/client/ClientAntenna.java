package id.unifi.service.common.types.client;

import id.unifi.service.common.types.pk.AntennaPK;

import java.util.Objects;

public class ClientAntenna {
    public final String readerSn;
    public final int portNumber;

    public ClientAntenna(String readerSn, int portNumber) {
        this.readerSn = readerSn;
        this.portNumber = portNumber;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ClientAntenna) o;
        return portNumber == that.portNumber &&
                Objects.equals(readerSn, that.readerSn);
    }

    public int hashCode() {
        return Objects.hash(readerSn, portNumber);
    }

    public String toString() {
        return "ClientAntenna{" +
                "readerSn='" + readerSn + '\'' +
                ", portNumber=" + portNumber +
                '}';
    }

    public AntennaPK withClientId(String clientId) {
        return new AntennaPK(clientId, readerSn, portNumber);
    }
}
