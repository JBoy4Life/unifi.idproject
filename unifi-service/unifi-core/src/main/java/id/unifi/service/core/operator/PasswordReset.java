package id.unifi.service.core.operator;

import com.statemachinesystems.envy.Default;
import id.unifi.service.common.api.annotations.ApiConfigPrefix;
import id.unifi.service.common.db.Database;
import static id.unifi.service.core.db.Tables.OPERATOR_PASSWORD_RESET;
import static id.unifi.service.core.operator.SecretHashing.SCRYPT_FORMAT_NAME;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.currentTimestamp;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.timestampAdd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class PasswordReset {
    private static Logger log = LoggerFactory.getLogger(PasswordReset.class);

    private final Database db;
    private final SecretHashing tokenHashing;
    private final Field<LocalDateTime> sqlExpiryDate;
    private final SecureRandom random;

    public interface Config {
        @Default("864000")
        long validitySeconds();

        SecretHashing.ScryptConfig hashing();
    }

    public PasswordReset(@ApiConfigPrefix("operator.password.reset") Config config,
                         Database db) {
        this.db = db;
        this.tokenHashing = new SecretHashing(config.hashing());
        this.sqlExpiryDate = timestampAdd(currentTimestamp(), config.validitySeconds(), DatePart.SECOND)
                .cast(LocalDateTime.class);

        try {
            this.random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class TimestampedTokenHash {
        public final byte[] hash;
        public final Instant since;

        public TimestampedTokenHash(byte[] hash, Instant since) {
            this.hash = hash;
            this.since = since;
        }
    }

    public TimestampedToken generateResetToken(DSLContext sql, String clientId, String username) {
        byte[] token = new byte[TimestampedToken.TOKEN_LENGTH];
        random.nextBytes(token);
        byte[] tokenHash = tokenHashing.hash(token);
            sql.insertInto(OPERATOR_PASSWORD_RESET)
                    .set(OPERATOR_PASSWORD_RESET.CLIENT_ID, clientId)
                    .set(OPERATOR_PASSWORD_RESET.USERNAME, username)
                    .set(OPERATOR_PASSWORD_RESET.TOKEN_HASH, tokenHash)
                    .set(OPERATOR_PASSWORD_RESET.ALGORITHM, SCRYPT_FORMAT_NAME)
                    .set(OPERATOR_PASSWORD_RESET.EXPIRY_DATE, sqlExpiryDate)
                    .returning(OPERATOR_PASSWORD_RESET.EXPIRY_DATE)
                    .execute();
        LocalDateTime now = sql.fetchOne(select(currentLocalDateTime())).value1();
        return new TimestampedToken(now.toInstant(ZoneOffset.UTC), token);
    }

    public boolean preparePasswordReset(DSLContext sql, String clientId, String username, TimestampedToken token) {
        Optional<TimestampedTokenHash> tokenHash = findValidTokenHash(sql, clientId, username, token);
        tokenHash.ifPresent(t -> sql.deleteFrom(OPERATOR_PASSWORD_RESET) // TODO: archive
                .where(OPERATOR_PASSWORD_RESET.CLIENT_ID.eq(clientId))
                .and(OPERATOR_PASSWORD_RESET.USERNAME.eq(username))
                .execute());
        return tokenHash.isPresent();

    }

    public void cancel(String clientId, String username, TimestampedToken token) {
        db.execute(sql -> {
            Optional<TimestampedTokenHash> tokenHash = findValidTokenHash(sql, clientId, username, token);
            tokenHash.ifPresent(t -> sql.deleteFrom(OPERATOR_PASSWORD_RESET) // TODO: archive
                    .where(OPERATOR_PASSWORD_RESET.CLIENT_ID.eq(clientId))
                    .and(OPERATOR_PASSWORD_RESET.USERNAME.eq(username))
                    .and(OPERATOR_PASSWORD_RESET.TOKEN_HASH.eq(t.hash)));
            return null;
        });
    }

    public Optional<TimestampedTokenHash> findValidTokenHash(DSLContext sql,
                                                             String clientId,
                                                             String username,
                                                             TimestampedToken token) {
        return sql.select(OPERATOR_PASSWORD_RESET.TOKEN_HASH, OPERATOR_PASSWORD_RESET.SINCE)
                .from(OPERATOR_PASSWORD_RESET)
                .where(OPERATOR_PASSWORD_RESET.CLIENT_ID.eq(clientId))
                .and(OPERATOR_PASSWORD_RESET.USERNAME.eq(username))
                .and(OPERATOR_PASSWORD_RESET.EXPIRY_DATE.gt(currentLocalDateTime()))
                .fetchOptional()
                .filter(p -> SecretHashing.check(token.token, p.value1()))
                .map(p -> new TimestampedTokenHash(p.value1(), p.value2().toInstant(ZoneOffset.UTC)));
    }
}
