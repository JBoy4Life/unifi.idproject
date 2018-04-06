package id.unifi.service.common.security;

import com.statemachinesystems.envy.Default;

public interface ScryptConfig {
    @Default("15")
    byte scryptLogN();

    @Default("8")
    short scryptR();

    @Default("1")
    short scryptP();
}
