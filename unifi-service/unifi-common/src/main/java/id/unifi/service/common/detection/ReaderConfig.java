package id.unifi.service.common.detection;

import com.google.common.net.HostAndPort;

import java.util.Arrays;

public class ReaderConfig {
    public final String readerSn;
    public final HostAndPort endpoint;
    public final int[] enabledAntennae;

    public ReaderConfig(String readerSn, HostAndPort endpoint, int[] enabledAntennae) {
        this.readerSn = readerSn;
        this.endpoint = endpoint;
        this.enabledAntennae = enabledAntennae;
    }

    public String toString() {
        return "ReaderConfig{" +
                "readerSn='" + readerSn + '\'' +
                ", endpoint=" + endpoint +
                ", enabledAntennae=" + Arrays.toString(enabledAntennae) +
                '}';
    }
}
