package id.unifi.service.common.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.net.HostAndPort;

public class AgentReaderFullConfig {
    public final String readerSn;
    public final HostAndPort endpoint;
    public final JsonNode config;

    public AgentReaderFullConfig(String readerSn, HostAndPort endpoint, JsonNode config) {
        this.readerSn = readerSn;
        this.endpoint = endpoint;
        this.config = config;
    }
}
