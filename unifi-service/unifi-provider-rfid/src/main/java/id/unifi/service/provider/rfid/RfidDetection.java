package id.unifi.service.provider.rfid;

public class RfidDetection {
    private final long timestamp;
    private final int portNumber;
    private final String epc;
    private final double rssi;

    RfidDetection(long timestamp, int portNumber, String epc, double rssi) {
        this.timestamp = timestamp;
        this.portNumber = portNumber;
        this.epc = epc;
        this.rssi = rssi;
    }

    public long getTimestamp() {
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
