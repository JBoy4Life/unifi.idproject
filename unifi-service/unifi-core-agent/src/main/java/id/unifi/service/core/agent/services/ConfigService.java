package id.unifi.service.core.agent.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.core.agent.ReaderManager;
import id.unifi.service.core.agent.config.AgentFullConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiService("config")
public class ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final ReaderManager readerManager;

    public ConfigService(ReaderManager readerManager) {
        this.readerManager = readerManager;
    }

    @ApiOperation
    public void setAgentConfig(AgentFullConfig config) {
        log.info("Received agent config from server: {}", config);
        readerManager.configure(config);
    }
}
