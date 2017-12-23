package id.unifi.service.core.services;

import com.statemachinesystems.envy.Default;
import static id.unifi.service.common.api.Validation.*;
import id.unifi.service.common.api.annotations.ApiConfigPrefix;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.AlreadyExists;
import id.unifi.service.common.api.errors.AuthenticationFailed;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.db.Database;
import id.unifi.service.common.db.DatabaseProvider;
import static id.unifi.service.common.db.DatabaseProvider.CORE_SCHEMA_NAME;
import id.unifi.service.common.operator.ExpiringToken;
import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.security.Token;
import id.unifi.service.core.OperatorSessionData;
import static id.unifi.service.core.db.Tables.OPERATOR;
import static id.unifi.service.core.db.Tables.OPERATOR_PASSWORD;
import static id.unifi.service.core.db.Tables.OPERATOR_LOGIN_ATTEMPT;
import id.unifi.service.core.operator.OperatorInfo;
import id.unifi.service.core.operator.PasswordReset;
import id.unifi.service.common.security.SecretHashing;
import id.unifi.service.common.security.TimestampedToken;
import id.unifi.service.core.operator.email.OperatorEmailRenderer;
import static java.util.stream.Collectors.toList;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApiService("operator")
public class OperatorService {
    private final Database db;
    private final PasswordReset passwordReset;
    private final SecretHashing passwordHashing;
    private final OperatorEmailRenderer emailRenderer;
    private final EmailSenderProvider emailSender;
    private final SessionTokenStore sessionTokenStore;
    private final Config config;

    private interface Config {
        @Default("864000")
        int sessionTokenValiditySeconds();
    }

    public OperatorService(@ApiConfigPrefix("operator") Config config,
                           @ApiConfigPrefix("operator.password.hashing") SecretHashing.ScryptConfig hashingConfig,
                           DatabaseProvider dbProvider,
                           PasswordReset passwordReset,
                           OperatorEmailRenderer emailRenderer,
                           EmailSenderProvider emailSender,
                           SessionTokenStore sessionTokenStore) {
        this.config = config;
        this.db = dbProvider.bySchemaName(CORE_SCHEMA_NAME);
        this.passwordReset = passwordReset;
        this.passwordHashing = new SecretHashing(hashingConfig);
        this.emailRenderer = emailRenderer;
        this.emailSender = emailSender;
        this.sessionTokenStore = sessionTokenStore;
    }

    @ApiOperation
    public void registerOperator(OperatorSessionData session,
                                 String clientId,
                                 String username,
                                 String email,
                                 boolean invite) {
        validateAll(
                v(shortId(clientId), Unauthorized::new),
                v("username", shortId(username)),
                v("email", email(email))
        );
        OperatorPK onboarder = session.getOperator() != null ? session.getOperator() : new OperatorPK(clientId, "???");
        db.execute(sql -> {
            try {
                sql.insertInto(OPERATOR)
                        .set(OPERATOR.CLIENT_ID, clientId)
                        .set(OPERATOR.USERNAME, username)
                        .set(OPERATOR.EMAIL, email)
                        .set(OPERATOR.ACTIVE, true)
                        .execute();
            } catch (DuplicateKeyException e) {
                throw new AlreadyExists("operator");
            }

            if (invite) {
                requestPasswordSet(sql, clientId, username, Optional.of(email), Optional.of(onboarder));
            }
            return null;
        });
    }

    @ApiOperation
    public ExpiringToken authPassword(OperatorSessionData session, String clientId, String username, String password) {
        validateAll(
                v(shortId(clientId), AuthenticationFailed::new),
                v(shortId(username), AuthenticationFailed::new),
                v(shortString(password), AuthenticationFailed::new)
        );
        if (passwordMatches(clientId, username, password)) {
            OperatorPK operator = new OperatorPK(clientId, username);
            Token sessionToken = new Token();
            sessionTokenStore.put(sessionToken, operator);
            session.setAuth(sessionToken, operator);
            recordAuthAttempt(clientId, username, true);
            return new ExpiringToken(sessionToken, Instant.now().plusSeconds(config.sessionTokenValiditySeconds()));
        } else {
            // TODO recordLoginAttempt(clientId, username, false);
            session.setAuth(null, null);
            throw new AuthenticationFailed();
        }
    }

    @ApiOperation
    public ExpiringToken authToken(OperatorSessionData session, Token sessionToken) {
        Optional<OperatorPK> operator = sessionTokenStore.get(sessionToken);
        if (operator.isPresent()) {
            session.setAuth(sessionToken, operator.get());
            return new ExpiringToken(sessionToken, Instant.now().plusSeconds(config.sessionTokenValiditySeconds()));
        } else {
            session.setAuth(null, null);
            throw new AuthenticationFailed();
        }
    }

    @ApiOperation
    public void invalidateAuthToken(OperatorSessionData session) {
        Token sessionToken = session.getSessionToken();
        if (sessionToken != null) {
            sessionTokenStore.remove(sessionToken);
            session.setAuth(null, null);
        }
    }

    @ApiOperation
    public List<OperatorInfo> listOperators(OperatorSessionData session, String clientId) {
        authorize(session);
        return db.execute(sql -> sql.selectFrom(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .stream()
                .map(r -> new OperatorInfo(r.getClientId(), r.getUsername(), r.getEmail(), r.getActive())))
                .collect(toList());
    }

    @ApiOperation
    public OperatorInfo getOperator(OperatorSessionData session, String clientId, String username) {
        authorize(session);
        return db.execute(sql -> sql.selectFrom(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOptional()
                .map(r -> new OperatorInfo(r.getClientId(), r.getUsername(), r.getEmail(), r.getActive())))
                .orElse(null);
    }

    @ApiOperation
    public void inviteOperator(OperatorSessionData session, String clientId, String username) {
        OperatorPK operator = authorize(session);
        db.execute(sql -> {
            requestPasswordSet(sql, clientId, username, Optional.empty(), Optional.of(operator));
            return null;
        });
    }

    @ApiOperation
    public void requestPasswordReset(String clientId, String username) {
        db.execute(sql -> {
            requestPasswordSet(sql, clientId, username, Optional.empty(), Optional.empty());
            return null;
        });
    }

    @ApiOperation
    public PasswordResetInfo getPasswordReset(String clientId, String username, TimestampedToken token) {
        return db.execute(sql -> {
            Optional<PasswordReset.TimestampedTokenHash> tokenHash =
                    passwordReset.findValidTokenHash(sql, clientId, username, token);
            if (tokenHash.isPresent()) {
                String email = findEmail(sql, clientId, username).orElseThrow(AssertionError::new);
                return new PasswordResetInfo(tokenHash.get().since, email);
            } else {
                return null;
            }
        });
    }

    @ApiOperation
    public void setPassword(String clientId, String username, String password, TimestampedToken token) {
        db.execute(sql -> {
            boolean isResetValid = passwordReset.preparePasswordReset(sql, clientId, username, token);
            if (isResetValid) {
                setPassword(sql, clientId, username, password);
                return null;
            } else {
                throw new AuthenticationFailed();
            }
        });
    }

    @ApiOperation
    public void changePassword(OperatorSessionData session, String currentPassword, String password) {
        OperatorPK operator = authorize(session);
        if (passwordMatches(operator.clientId, operator.username, currentPassword)) {
            db.execute(sql -> {
                setPassword(sql, operator.clientId, operator.username, password);
                return null;
            });
        } else {
            throw new AuthenticationFailed();
        }
    }

    private void setPassword(DSLContext sql, String clientId, String username, String password) {
        byte[] hash = passwordHashing.hash(password);
        sql.insertInto(OPERATOR_PASSWORD)
                .set(OPERATOR_PASSWORD.CLIENT_ID, clientId)
                .set(OPERATOR_PASSWORD.USERNAME, username)
                .set(OPERATOR_PASSWORD.PASSWORD_HASH, hash)
                .set(OPERATOR_PASSWORD.ALGORITHM, SecretHashing.SCRYPT_FORMAT_NAME)
                .onConflict()
                .doUpdate()
                .set(OPERATOR_PASSWORD.PASSWORD_HASH, hash)
                .set(OPERATOR_PASSWORD.ALGORITHM, SecretHashing.SCRYPT_FORMAT_NAME)
                .set(OPERATOR_PASSWORD.SINCE, DSL.currentLocalDateTime())
                .execute();
    }

    private void requestPasswordSet(DSLContext sql,
                                    String clientId,
                                    String username,
                                    Optional<String> emailAddress,
                                    Optional<OperatorPK> onboarder) {
        TimestampedToken token = passwordReset.generateResetToken(sql, clientId, username);

        String actualEmailAddress = emailAddress.or(() ->
                findEmail(sql, clientId, username))
                .orElseThrow(() -> new NotFound("operator"));

        EmailSenderProvider.EmailMessage message;
        if (onboarder.isPresent()) {
            message = emailRenderer.renderInvitation(clientId, username, token, onboarder.get());
        } else {
            message = emailRenderer.renderPasswordResetInstructions(clientId, username, token);
        }

        emailSender.send(actualEmailAddress, message);
    }

    private void recordAuthAttempt(String clientId, String username, boolean successful) {
        db.execute(sql -> sql.insertInto(OPERATOR_LOGIN_ATTEMPT)
                .set(OPERATOR_LOGIN_ATTEMPT.CLIENT_ID, clientId)
                .set(OPERATOR_LOGIN_ATTEMPT.USERNAME, username)
                .set(OPERATOR_LOGIN_ATTEMPT.SUCCESSFUL, successful)
                .execute());
    }

    private static OperatorPK authorize(OperatorSessionData sessionData) {
        return Optional.ofNullable(sessionData.getOperator()).orElseThrow(Unauthorized::new);
    }

    private static OperatorPK authorize(OperatorSessionData sessionData, String clientId) {
        return Optional.ofNullable(sessionData.getOperator())
                .filter(op -> op.clientId.equals(clientId))
                .orElseThrow(Unauthorized::new);
    }

    private static Optional<byte[]> sqlPasswordHash(DSLContext sql, String clientId, String username) {
        return sql.select(OPERATOR_PASSWORD.PASSWORD_HASH)
                .from(OPERATOR_PASSWORD)
                .where(OPERATOR_PASSWORD.CLIENT_ID.eq(clientId))
                .and(OPERATOR_PASSWORD.USERNAME.eq(username))
                .fetchOptional(OPERATOR_PASSWORD.PASSWORD_HASH);
    }

    private static Optional<String> findEmail(DSLContext sql, String clientId, String username) {
        return sql.select(OPERATOR.EMAIL)
                .from(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOptional(OPERATOR.EMAIL);
    }

    private boolean passwordMatches(String clientId, String username, String password) {
        Optional<byte[]> encodedHash = db.execute(sql -> sqlPasswordHash(sql, clientId, username));
        return encodedHash.map(hash -> SecretHashing.check(password, hash)).orElse(false);
    }

    public class PasswordResetInfo {
        public final Instant expiryDate;
        public final String email;

        public PasswordResetInfo(Instant expiryDate, String email) {
            this.expiryDate = expiryDate;
            this.email = email;
        }
    }
}
