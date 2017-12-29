package id.unifi.service.common.security;

import com.lambdaworks.crypto.SCrypt;
import com.statemachinesystems.envy.Default;
import id.unifi.service.common.util.HexEncoded;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

public class SecretHashing {
    private static final Logger log = LoggerFactory.getLogger(SecretHashing.class);
    private static final SecureRandom random = new SecureRandom();

    public static final String SCRYPT_FORMAT_NAME = "scrypt";
    private static final byte[] SCRYPT_FORMAT_HEADER = {'s', 'c', 'r'};
    private static final int DERIVED_KEY_LENGTH = 32;

    private final byte scryptLogN;
    private final int scryptN;
    private final short scryptR;
    private final short scryptP;

    public SecretHashing(ScryptConfig config) {
        if (config.scryptLogN() < 1 || config.scryptLogN() > 24)
            throw new IllegalArgumentException("scryptLogN must be between 1 and 24 inclusive");
        if (config.scryptR() <= 0)
            throw new IllegalArgumentException("scryptR must be positive");
        if (config.scryptP() <= 0)
            throw new IllegalArgumentException("scryptP must be positive");

        this.scryptLogN = config.scryptLogN();
        this.scryptN = 1 << config.scryptLogN();
        this.scryptR = config.scryptR();
        this.scryptP = config.scryptP();

        if (scryptN > Integer.MAX_VALUE / 128 / scryptR)
            throw new IllegalArgumentException("scryptN too large in proportion to scryptR");
        if (scryptR > Integer.MAX_VALUE / 128 / scryptP)
            throw new IllegalArgumentException("scryptR too large in proportion to scryptP");
    }

    private static class ScryptHash {
        final byte logN;
        final short r;
        final short p;
        final byte[] salt;
        final byte[] derivedKey;

        ScryptHash(byte logN, short r, short p, byte[] salt, byte[] derivedKey) {
            this.logN = logN;
            this.r = r;
            this.p = p;
            this.salt = salt;
            this.derivedKey = derivedKey;
        }

        byte[] encoded() {
            ByteBuffer output = ByteBuffer.allocate(56);
            output.put(SCRYPT_FORMAT_HEADER);
            output.put(logN);
            output.putShort(r);
            output.putShort(p);
            output.put(salt);
            output.put(derivedKey);
            return output.array();
        }
        public String toString() {
            return String.format("<scrypt: log_N=%d,r=%d,p=%d salt=%s dk=%s>",
                    logN, r, p, new HexEncoded(salt), new HexEncoded(derivedKey));
        }

    }

    private static Optional<ScryptHash> parse(byte[] encoded) {
        ByteBuffer input = ByteBuffer.wrap(encoded);

        byte[] format = new byte[3];
        byte[] salt = new byte[16];
        byte[] hash = new byte[32];

        input.get(format);
        byte logN = input.get();
        short r = input.getShort();
        short p = input.getShort();

        log.trace("Extracted hash params: log_N={}, r={}, p={}", logN, r, p);
        input.get(salt);
        input.get(hash);

        if (Arrays.equals(format, SCRYPT_FORMAT_HEADER)) {
            return Optional.of(new ScryptHash(logN, r, p, salt, hash));
        } else {
            log.debug("Bad hash format, expected 'scr' for scrypt");
            return Optional.empty();
        }
    }

    public static boolean check(String password, byte[] encodedHash) {
        return check(password.getBytes(UTF_8), encodedHash);
    }

    public static boolean check(byte[] password, byte[] encodedHash) {
        ScryptHash hash = parse(encodedHash)
                .orElseThrow(() -> new RuntimeException("Bad hash format: " + new HexEncoded(encodedHash)));
        log.trace("Checking password with hash: {}", new HexEncoded(encodedHash));

        byte[] rederived;
        try {
            rederived = SCrypt.scrypt(password, hash.salt, 1 << hash.logN, hash.r, hash.p, DERIVED_KEY_LENGTH);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        boolean result = MessageDigest.isEqual(hash.derivedKey, rederived);
        log.trace("Password check result: {} for {}", rederived, hash);
        return result;
    }

    public static String toString(byte[] encodedHash) {
        return parse(encodedHash).map(ScryptHash::toString).orElse("<none>");
    }

    public interface ScryptConfig {
        @Default("12")
        byte scryptLogN();

        @Default("4")
        short scryptR();

        @Default("1")
        short scryptP();
    }

    public byte[] hash(String password) {
        return hash(password.getBytes(UTF_8));
    }

    public byte[] hash(byte[] password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] derivedKey;
        try {
            derivedKey = SCrypt.scrypt(password, salt, scryptN, scryptR, scryptP, DERIVED_KEY_LENGTH);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return new ScryptHash(scryptLogN, scryptR, scryptP, salt, derivedKey).encoded();
    }
}
