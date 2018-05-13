package id.unifi.service.integration.gallagher;

import com.statemachinesystems.envy.Default;

public interface FtcApiConfig {
    String server();

    String domain();

    String username();

    String password();

    @Default("2")
    int eventType();

    int facilityCode();

    @Default("unifi.id")
    String systemId();
}
