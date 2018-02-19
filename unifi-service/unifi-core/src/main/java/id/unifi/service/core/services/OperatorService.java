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
import id.unifi.service.common.operator.AuthInfo;
import id.unifi.service.common.types.OperatorPK;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.security.Token;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.core.VerticalConfigManager;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.OPERATOR;
import static id.unifi.service.core.db.Tables.OPERATOR_PASSWORD;
import static id.unifi.service.core.db.Tables.OPERATOR_LOGIN_ATTEMPT;
import id.unifi.service.core.db.tables.records.OperatorRecord;
import id.unifi.service.core.operator.OperatorInfo;
import id.unifi.service.core.operator.PasswordReset;
import id.unifi.service.common.security.SecretHashing;
import id.unifi.service.common.security.TimestampedToken;
import id.unifi.service.core.operator.email.OperatorEmailRenderer;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApiService("operator")
public class OperatorService {
    private static final Logger log = LoggerFactory.getLogger(OperatorService.class);

    private final Database db;
    private final PasswordReset passwordReset;
    private final SecretHashing passwordHashing;
    private final OperatorEmailRenderer emailRenderer;
    private final EmailSenderProvider emailSender;
    private final SessionTokenStore sessionTokenStore;
    private final VerticalConfigManager verticalConfigManager;
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
                           SessionTokenStore sessionTokenStore,
                           VerticalConfigManager verticalConfigManager) {
        this.config = config;
        this.db = dbProvider.bySchema(CORE);
        this.passwordReset = passwordReset;
        this.passwordHashing = new SecretHashing(hashingConfig);
        this.emailRenderer = emailRenderer;
        this.emailSender = emailSender;
        this.sessionTokenStore = sessionTokenStore;
        this.verticalConfigManager = verticalConfigManager;
    }

    @ApiOperation
    public void registerOperator(OperatorSessionData session,
                                 String clientId,
                                 String username,
                                 String name,
                                 String email,
                                 boolean invite) {
        authorize(session, clientId);
        validateAll(
                v("username", shortId(username)),
                v("name", shortString(name)),
                v("email", email(email))
        );
        OperatorPK onboarder = session.getOperator();
        db.execute(sql -> {
            try {
                sql.insertInto(OPERATOR)
                        .set(OPERATOR.CLIENT_ID, clientId)
                        .set(OPERATOR.USERNAME, username)
                        .set(OPERATOR.NAME, name)
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
    public AuthInfo authPassword(OperatorSessionData session, String clientId, String username, String password) {
        validateAll(
                v(shortId(clientId), AuthenticationFailed::new),
                v(shortId(username), AuthenticationFailed::new),
                v(shortString(password), AuthenticationFailed::new)
        );
        if (passwordMatches(clientId, username, password)) {
            OperatorPK operator = new OperatorPK(clientId, username);
            return approveAuthAttempt(session, operator);
        } else {
            // TODO recordLoginAttempt(clientId, username, false);
            session.setAuth(null, null);
            throw new AuthenticationFailed();
        }
    }

    @ApiOperation
    public AuthInfo authToken(OperatorSessionData session, Token sessionToken) {
        Optional<OperatorPK> operator = sessionTokenStore.get(sessionToken);
        if (operator.isPresent()) {
            session.setAuth(sessionToken, operator.get());
            return new AuthInfo(operator.get(),
                    sessionToken,
                    Instant.now().plusSeconds(config.sessionTokenValiditySeconds()),
                    verticalConfigManager.getClientSideConfig(operator.get().clientId));
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
        authorize(session, clientId);
        return db.execute(sql -> sql.selectFrom(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .fetch(OperatorService::operatorFromRecord));
    }

    @ApiOperation
    public OperatorInfo getOperator(OperatorSessionData session, String clientId, String username) {
        authorize(session, clientId);
        return getOperatorInfo(clientId, username);
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
                OperatorInfo operator = findOperator(sql, clientId, username).orElseThrow(AssertionError::new);
                return new PasswordResetInfo(tokenHash.get().since, operator);
            } else {
                return null;
            }
        });
    }

    @ApiOperation
    public AuthInfo setPassword(OperatorSessionData session,
                            String clientId,
                            String username,
                            String password,
                            TimestampedToken token) {
        return db.execute(sql -> {
            boolean isResetValid = passwordReset.preparePasswordReset(sql, clientId, username, token);
            if (isResetValid) {
                setPassword(sql, clientId, username, password);
                return approveAuthAttempt(session, new OperatorPK(clientId, username));
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

    private AuthInfo approveAuthAttempt(OperatorSessionData session, OperatorPK operator) {
        Token sessionToken = new Token();
        sessionTokenStore.put(sessionToken, operator);
        session.setAuth(sessionToken, operator);
        recordAuthAttempt(operator, true);
        return new AuthInfo(operator,
                sessionToken,
                Instant.now().plusSeconds(config.sessionTokenValiditySeconds()),
                verticalConfigManager.getClientSideConfig(operator.clientId));
    }

    private OperatorInfo getOperatorInfo(String clientId, String username) {
        return db.execute(sql -> sql.selectFrom(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOne(OperatorService::operatorFromRecord));
    }

    private static OperatorInfo operatorFromRecord(OperatorRecord r) {
        return new OperatorInfo(r.getClientId(), r.getUsername(), r.getName(), r.getEmail(), r.getActive());
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
                findOperator(sql, clientId, username).map(o -> o.email))
                .orElseThrow(() -> new NotFound("operator"));

        EmailSenderProvider.EmailMessage message;
        if (onboarder.isPresent()) {
            message = emailRenderer.renderInvitation(clientId, username, token, onboarder.get());
        } else {
            message = emailRenderer.renderPasswordResetInstructions(clientId, username, token);
        }

        emailSender.send(actualEmailAddress, message);
    }

    private void recordAuthAttempt(OperatorPK operator, boolean successful) {
        db.execute(sql -> sql.insertInto(OPERATOR_LOGIN_ATTEMPT)
                .set(OPERATOR_LOGIN_ATTEMPT.CLIENT_ID, operator.clientId)
                .set(OPERATOR_LOGIN_ATTEMPT.USERNAME, operator.username)
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

    private static Optional<OperatorInfo> findOperator(DSLContext sql, String clientId, String username) {
        return sql.selectFrom(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOptional(OperatorService::operatorFromRecord);
    }

    private boolean passwordMatches(String clientId, String username, String password) {
        Optional<byte[]> encodedHash = db.execute(sql -> sqlPasswordHash(sql, clientId, username));
        return encodedHash.map(hash -> SecretHashing.check(password, hash)).orElse(false);
    }

    public class PasswordResetInfo {
        public final Instant expiryDate;
        public final OperatorInfo operator;

        public PasswordResetInfo(Instant expiryDate, OperatorInfo operator) {
            this.expiryDate = expiryDate;
            this.operator = operator;
        }
    }
}
