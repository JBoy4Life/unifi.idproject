package id.unifi.service.provider.rfid.config;

import java.util.OptionalDouble;

public class AntennaConfig {
    public final OptionalDouble txPower;
    public final OptionalDouble rxSensitivity;

    public static final AntennaConfig empty = new AntennaConfig(OptionalDouble.empty(), OptionalDouble.empty());

    public AntennaConfig(OptionalDouble txPower, OptionalDouble rxSensitivity) {
        this.txPower = txPower;
        this.rxSensitivity = rxSensitivity;
    }
}
