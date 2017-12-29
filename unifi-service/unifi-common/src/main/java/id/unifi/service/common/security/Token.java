package id.unifi.service.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class Token {
    public final static int TOKEN_LENGTH = 18;
    private final static Random random = new SecureRandom();

    @JsonValue
    public final byte[] raw;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Token(byte[] raw) {
        if (raw.length != TOKEN_LENGTH)
            throw new IllegalArgumentException("Invalid token length: " + raw.length);

        this.raw = raw;
    }

    public Token() {
        this.raw = new byte[TOKEN_LENGTH];
        random.nextBytes(this.raw);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Arrays.equals(raw, token.raw);
    }

    public int hashCode() {
        return Arrays.hashCode(raw);
    }
}
