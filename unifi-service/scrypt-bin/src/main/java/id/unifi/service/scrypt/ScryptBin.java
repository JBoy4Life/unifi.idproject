package id.unifi.service.scrypt;

import com.google.common.io.BaseEncoding;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.security.ScryptConfig;
import id.unifi.service.common.security.SecretHashing;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Hashes (and generates) a binary agent password using scrypt and our binary format.
 */
public class ScryptBin {
    private static final int PASSWORD_LENGTH = 32;
    private static final Random random = new SecureRandom();
    private static final BaseEncoding hex = BaseEncoding.base16().lowerCase();

    public static void main(String[] args) {
        byte[] input = args.length > 0 ? hexDecode(args[0]) : randomBytes();

        if (input == null) {
            System.err.println("Usage: [SCRYPT_LOG_N=...] [SCRYPT_R=...] [SCRYPT_P=...] scrypt-bin [hex-password]");
            return;
        }

        ScryptConfig config = Envy.configure(ScryptConfig.class);
        System.out.println("Config: " + config);
        SecretHashing hashing = new SecretHashing(config);
        byte[] hash = hashing.hash(input);

        System.out.println("Input (hex): " + hex.encode(input));
        System.out.println("Hash (hex): " + hex.encode(hash));
        System.out.println("Algorithm: scrypt");
    }

    private static byte[] hexDecode(String encoded) {
        try {
            return hex.decode(encoded);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static byte[] randomBytes() {
        byte[] bytes = new byte[PASSWORD_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }
}
