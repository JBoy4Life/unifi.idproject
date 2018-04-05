package id.unifi.service.core.agent;

import id.unifi.service.common.detection.ReaderConfig;

import java.util.List;

public class ReaderConfigNoopPersistence implements ReaderConfigPersistence {
    private final List<ReaderConfig> config;

    public ReaderConfigNoopPersistence(List<ReaderConfig> config) {
        this.config = config;
    }

    public List<ReaderConfig> readConfig() {
        return config;
    }

    public void writeConfig(List<ReaderConfig> config) {}
}
