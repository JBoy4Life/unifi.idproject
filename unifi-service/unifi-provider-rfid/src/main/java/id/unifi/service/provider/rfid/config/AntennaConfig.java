package id.unifi.service.provider.rfid.config;

import java.util.Objects;
import java.util.OptionalDouble;

public class AntennaConfig {
    public final OptionalDouble txPower;
    public final OptionalDouble rxSensitivity;

    public static final AntennaConfig empty = new AntennaConfig(OptionalDouble.empty(), OptionalDouble.empty());

    public AntennaConfig(OptionalDouble txPower, OptionalDouble rxSensitivity) {
        this.txPower = txPower;
        this.rxSensitivity = rxSensitivity;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AntennaConfig that = (AntennaConfig) o;
        return Objects.equals(txPower, that.txPower) &&
                Objects.equals(rxSensitivity, that.rxSensitivity);
    }

    public int hashCode() {
        return Objects.hash(txPower, rxSensitivity);
    }

    public String toString() {
        return "AntennaConfig{" +
                "txPower=" + txPower +
                ", rxSensitivity=" + rxSensitivity +
                '}';
    }
}
