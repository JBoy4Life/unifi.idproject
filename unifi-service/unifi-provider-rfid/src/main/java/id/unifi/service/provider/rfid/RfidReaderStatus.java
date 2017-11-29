package id.unifi.service.provider.rfid;

import com.google.common.net.HostAndPort;

import java.util.Collections;
import java.util.Map;

public class RfidReaderStatus {
    private final HostAndPort endpoint;
    private final String firmwareVersion;
    private final Map<Integer, Boolean> antennaeConnected;

    RfidReaderStatus(HostAndPort endpoint, String firmwareVersion, Map<Integer, Boolean> antennaeConnected) {
        this.endpoint = endpoint;
        this.firmwareVersion = firmwareVersion;
        this.antennaeConnected = Collections.unmodifiableMap(antennaeConnected);
    }

    public HostAndPort getEndpoint() {
        return endpoint;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public Map<Integer, Boolean> getAntennaeConnected() {
        return antennaeConnected;
    }

    public String toString() {
        return "ReaderStatus{" +
                "endpoint=" + endpoint +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", antennaeConnected=" + antennaeConnected +
                '}';
    }
}
