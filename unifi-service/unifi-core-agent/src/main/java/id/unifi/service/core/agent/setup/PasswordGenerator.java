package id.unifi.service.core.agent.setup;

import com.google.common.io.BaseEncoding;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.security.ScryptConfig;
import id.unifi.service.common.security.SecretHashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;

class PasswordGenerator {
    private static final Logger log = LoggerFactory.getLogger(PasswordGenerator.class);
    private static final int PASSWORD_LENGTH = 32;
    private static final Random random = new SecureRandom();
    private static final BaseEncoding hex = BaseEncoding.base16().lowerCase();
    private static final ScryptConfig config = Envy.configure(ScryptConfig.class);

    static {
        log.info("scrypt config: {}", config);
    }

    static String generateHexPassword() {
        return hex.encode(randomBytes());
    }

    static String hashHexPassword(String hexPassword) {
        SecretHashing hashing = new SecretHashing(config);
        byte[] hash = hashing.hash(hex.decode(hexPassword));
        return hex.encode(hash);
    }

    private static byte[] randomBytes() {
        byte[] bytes = new byte[PASSWORD_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }

}
