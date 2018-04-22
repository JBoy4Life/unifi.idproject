package id.unifi.service.core.agent.config;

import id.unifi.service.common.agent.ReaderFullConfig;
import id.unifi.service.provider.rfid.config.AntennaConfig;
import id.unifi.service.provider.rfid.config.ReaderConfig;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AgentFullConfig extends id.unifi.service.common.agent.AgentFullConfig<AgentConfig, ReaderConfig> {
    public AgentFullConfig(Optional<AgentConfig> agent, List<ReaderFullConfig<ReaderConfig>> readers) {
        super(agent, readers);
    }

    /**
     * @return compact representation for core service DB use omitting empty antenna configs
     */
    public AgentFullConfig compactForService() {
        Optional<AgentConfig> compactAgentConfig = Optional.of(agent.orElse(AgentConfig.empty));
        List<ReaderFullConfig<ReaderConfig>> compactReaderFullConfigs = new ArrayList<>();
        for (ReaderFullConfig<ReaderConfig> reader : readers) {
            ReaderConfig config = reader.config.orElse(ReaderConfig.empty);
            Map<Integer, AntennaConfig> compactPorts = config.ports.orElse(Map.of()).entrySet().stream()
                    .filter(r -> !r.getValue().equals(AntennaConfig.empty))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            ReaderConfig compactReaderConfig =
                    config.copyWithPorts(Optional.of(compactPorts).filter(m -> !m.isEmpty()));

            ReaderFullConfig<ReaderConfig> compactReaderFullConfig =
                    new ReaderFullConfig<>(reader.readerSn, reader.endpoint, Optional.of(compactReaderConfig));
            compactReaderFullConfigs.add(compactReaderFullConfig);
        }

        return new AgentFullConfig(compactAgentConfig, unmodifiableList(compactReaderFullConfigs));
    }
}
