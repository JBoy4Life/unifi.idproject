package id.unifi.service.common.config;

import com.google.common.net.HostAndPort;
import com.statemachinesystems.envy.Default;

public interface MqConfig {
    @Default("127.0.0.1:5672")
    HostAndPort endpoint();
}
