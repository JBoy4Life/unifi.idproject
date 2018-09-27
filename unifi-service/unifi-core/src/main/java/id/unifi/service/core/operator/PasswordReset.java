package id.unifi.service.core.operator;

import com.statemachinesystems.envy.Default;
import id.unifi.service.common.api.annotations.ApiConfigPrefix;
import id.unifi.service.common.security.ScryptConfig;
import id.unifi.service.common.security.SecretHashing;
import static id.unifi.service.common.security.SecretHashing.SCRYPT_FORMAT_NAME;
import id.unifi.service.common.security.TimestampedToken;
import id.unifi.service.common.security.Token;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.OPERATOR_PASSWORD_RESET;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.time.ZoneOffset.UTC;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.currentTimestamp;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.timestampAdd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class PasswordReset {
    private static Logger log = LoggerFactory.getLogger(PasswordReset.class);

    private final Database db;
    private final SecretHashing tokenHashing;
    private final Field<LocalDateTime> sqlExpiryDate;

    public interface Config {
        @Default("864000")
        long validitySeconds();

        ScryptConfig hashing();
    }

    public PasswordReset(@ApiConfigPrefix("unifi.operator.password.reset") Config config,
                         DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
        this.tokenHashing = new SecretHashing(config.hashing());
        this.sqlExpiryDate = timestampAdd(currentTimestamp(), config.validitySeconds(), DatePart.SECOND)
                .cast(LocalDateTime.class);
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
        var token = new Token();
        var tokenHash = tokenHashing.hash(token.raw);
            sql.insertInto(OPERATOR_PASSWORD_RESET)
                    .set(OPERATOR_PASSWORD_RESET.CLIENT_ID, clientId)
                    .set(OPERATOR_PASSWORD_RESET.USERNAME, username)
                    .set(OPERATOR_PASSWORD_RESET.TOKEN_HASH, tokenHash)
                    .set(OPERATOR_PASSWORD_RESET.ALGORITHM, SCRYPT_FORMAT_NAME)
                    .set(OPERATOR_PASSWORD_RESET.EXPIRY_DATE, sqlExpiryDate)
                    .returning(OPERATOR_PASSWORD_RESET.EXPIRY_DATE)
                    .execute();
        var now = sql.fetchOne(select(currentLocalDateTime())).value1();
        return new TimestampedToken(now.toInstant(ZoneOffset.UTC), token);
    }

    public boolean preparePasswordReset(DSLContext sql, String clientId, String username, TimestampedToken token) {
        var tokenHash = findValidTokenHash(sql, clientId, username, token);
        tokenHash.ifPresent(t -> sql.deleteFrom(OPERATOR_PASSWORD_RESET) // TODO: archive
                .where(OPERATOR_PASSWORD_RESET.CLIENT_ID.eq(clientId))
                .and(OPERATOR_PASSWORD_RESET.USERNAME.eq(username))
                .execute());
        return tokenHash.isPresent();

    }

    public void cancel(String clientId, String username, TimestampedToken token) {
        db.execute(sql -> {
            var tokenHash = findValidTokenHash(sql, clientId, username, token);
            tokenHash.ifPresent(t -> sql.deleteFrom(OPERATOR_PASSWORD_RESET) // TODO: archive
                    .where(OPERATOR_PASSWORD_RESET.CLIENT_ID.eq(clientId))
                    .and(OPERATOR_PASSWORD_RESET.USERNAME.eq(username))
                    .execute());
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
                .and(OPERATOR_PASSWORD_RESET.SINCE.eq(token.timestamp.atOffset(UTC)))
                .and(OPERATOR_PASSWORD_RESET.EXPIRY_DATE.gt(currentLocalDateTime()))
                .fetchOptional()
                .filter(p -> SecretHashing.check(token.token.raw, p.value1()))
                .map(p -> new TimestampedTokenHash(p.value1(), p.value2().toInstant()));
    }
}
