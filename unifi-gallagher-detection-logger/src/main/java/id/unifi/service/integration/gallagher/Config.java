package id.unifi.service.integration.gallagher;

import com.statemachinesystems.envy.Prefix;
import id.unifi.service.common.config.MqConfig;

@Prefix("unifi")
interface Config {
    MqConfig mq();

    FtcApiConfig ftcApi();


}
