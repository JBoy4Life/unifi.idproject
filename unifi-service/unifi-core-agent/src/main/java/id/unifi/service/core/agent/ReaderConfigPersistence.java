package id.unifi.service.core.agent;

import id.unifi.service.common.detection.ReaderConfig;

import java.util.List;

public interface ReaderConfigPersistence {
    List<ReaderConfig> readConfig();
    void writeConfig(List<ReaderConfig> config);
}
