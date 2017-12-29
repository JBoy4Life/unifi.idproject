package id.unifi.service.provider.rfid;

import java.time.Instant;

public class RfidDetection {
    private final Instant timestamp;
    private final int portNumber;
    private final String epc;
    private final double rssi;

    RfidDetection(Instant timestamp, int portNumber, String epc, double rssi) {
        this.timestamp = timestamp;
        this.portNumber = portNumber;
        this.epc = epc;
        this.rssi = rssi;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getEpc() {
        return epc;
    }

    public double getRssi() {
        return rssi;
    }

    public String toString() {
        return "RfidDetection{" +
                "timestamp=" + timestamp +
                ", portNumber=" + portNumber +
                ", epc='" + epc + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
