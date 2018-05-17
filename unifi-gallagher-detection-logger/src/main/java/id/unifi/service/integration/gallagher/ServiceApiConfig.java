package id.unifi.service.integration.gallagher;

import com.statemachinesystems.envy.Default;

import java.net.URI;

public interface ServiceApiConfig {
    @Default("ws://127.0.0.1:8000/service/msgpack")
    URI uri();

    String clientId();

    String username();

    String password();
}
