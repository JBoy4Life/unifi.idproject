package id.unifi.service.integration.gallagher;

import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.MqConfig;

import java.util.Optional;

@Prefix("unifi")
interface Config {
    MqConfig mq();

    FtcApiConfig ftcApi();

    Optional<ServiceApiConfig> serviceApi();

    Optional<String> logLevel();
}
