package id.unifi.service.common.rfid;

import java.time.Instant;

public class RfidDetection {
    public final Instant timestamp;
    public final int portNumber;
    public final String epc;
    public final double rssi;

    public RfidDetection(Instant timestamp, int portNumber, String epc, double rssi) {
        this.timestamp = timestamp;
        this.portNumber = portNumber;
        this.epc = epc;
        this.rssi = rssi;
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
