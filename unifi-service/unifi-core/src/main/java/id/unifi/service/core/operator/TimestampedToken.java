package id.unifi.service.core.operator;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.nio.ByteBuffer;
import java.time.Instant;

public class TimestampedToken {
    public final static int TOKEN_LENGTH = 18;
    private final static int TIMESTAMPED_TOKEN_LENGTH = Long.BYTES + Integer.BYTES + TOKEN_LENGTH;

    public final Instant timestamp;
    public final byte[] token;

    public TimestampedToken(Instant timestamp, byte[] token) {
        if (token.length != TOKEN_LENGTH)
            throw new IllegalArgumentException("Invalid token length: " + token.length);

        this.timestamp = timestamp;
        this.token = token;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public TimestampedToken(byte[] encoded) {
        ByteBuffer bb = ByteBuffer.wrap(encoded);
        long epochSecond = bb.getLong();
        int epochNano = bb.getInt();

        this.timestamp = Instant.ofEpochSecond(epochSecond, epochNano);
        this.token = new byte[TOKEN_LENGTH];
        bb.get(token);
    }

    public byte[] encoded() {
        ByteBuffer bb = ByteBuffer.allocate(TIMESTAMPED_TOKEN_LENGTH);
        bb.putLong(timestamp.getEpochSecond());
        bb.putInt(timestamp.getNano());
        bb.put(token);
        return bb.array();
    }
}
