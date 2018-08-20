package id.unifi.service.core.agent.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.core.agent.config.AgentFullConfig;
import id.unifi.service.core.agent.config.ConfigAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiService("config")
public class ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final ConfigAdapter configAdapter;

    public ConfigService(ConfigAdapter configAdapter) {
        this.configAdapter = configAdapter;
    }

    @ApiOperation
    public void setAgentConfig(AgentFullConfig config) {
        log.info("Received agent config from server: {}", config);
        configAdapter.configure(config, true);
    }
}
