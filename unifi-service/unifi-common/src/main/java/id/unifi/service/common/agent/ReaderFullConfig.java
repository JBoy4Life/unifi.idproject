package id.unifi.service.common.agent;

import com.google.common.net.HostAndPort;

import java.util.Optional;

public class ReaderFullConfig<R> {
    public final Optional<String> readerSn;
    public final Optional<HostAndPort> endpoint;
    public final Optional<R> config;

    public ReaderFullConfig(Optional<String> readerSn, Optional<HostAndPort> endpoint, Optional<R> config) {
        if (!readerSn.isPresent() && !endpoint.isPresent())
            throw new IllegalArgumentException(
                    "At least one of 'readerSn' and 'endpoint' and must be present for a reader.");

        this.readerSn = readerSn;
        this.endpoint = endpoint;
        this.config = config;
    }
}
