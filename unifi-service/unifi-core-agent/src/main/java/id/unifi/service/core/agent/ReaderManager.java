package id.unifi.service.core.agent;

import id.unifi.service.common.detection.ReaderConfig;

import java.util.List;

public interface ReaderManager {
    void configure(List<ReaderConfig> readers);
}
