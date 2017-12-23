package id.unifi.service.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import static id.unifi.service.common.security.Token.TOKEN_LENGTH;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Objects;

public class TimestampedToken {
    private final static int TIMESTAMPED_TOKEN_LENGTH = Long.BYTES + Integer.BYTES + TOKEN_LENGTH;

    public final Instant timestamp;
    public final Token token;

    public TimestampedToken(Instant timestamp, Token token) {
        this.timestamp = timestamp;
        this.token = token;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public TimestampedToken(byte[] encoded) {
        ByteBuffer bb = ByteBuffer.wrap(encoded);
        long epochSecond = bb.getLong();
        int epochNano = bb.getInt();

        this.timestamp = Instant.ofEpochSecond(epochSecond, epochNano);
        byte[] rawToken = new byte[TOKEN_LENGTH];
        bb.get(rawToken);
        this.token = new Token(rawToken);
    }

    public byte[] encoded() {
        ByteBuffer bb = ByteBuffer.allocate(TIMESTAMPED_TOKEN_LENGTH);
        bb.putLong(timestamp.getEpochSecond());
        bb.putInt(timestamp.getNano());
        bb.put(token.raw);
        return bb.array();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimestampedToken that = (TimestampedToken) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(token, that.token);
    }

    public int hashCode() {
        return Objects.hash(timestamp, token);
    }
}
