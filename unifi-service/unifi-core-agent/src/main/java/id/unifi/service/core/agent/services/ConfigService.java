package id.unifi.service.core.agent.services;

import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.detection.ReaderConfig;
import id.unifi.service.core.agent.ReaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApiService("config")
public class ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final ReaderManager readerManager;

    public ConfigService(ReaderManager readerManager) {
        this.readerManager = readerManager;
    }

    @ApiOperation
    public void setReaderConfig(List<ReaderConfig> readers) {
        log.info("Received reader config from server: {}", readers);
        readerManager.configure(readers);
    }
}
