package id.unifi.service.core.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.statemachinesystems.envy.Default;
import id.unifi.service.common.api.Validation;
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
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.security.SecretHashing;
import id.unifi.service.common.security.TimestampedToken;
import id.unifi.service.common.security.Token;
import id.unifi.service.common.types.OperatorInfo;
import id.unifi.service.common.types.OperatorPK;
import static id.unifi.service.common.util.QueryUtils.filterCondition;
import static id.unifi.service.common.util.QueryUtils.getUpdateQueryFieldMap;
import id.unifi.service.core.VerticalConfigManager;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.OPERATOR;
import static id.unifi.service.core.db.Tables.OPERATOR_LOGIN_ATTEMPT;
import static id.unifi.service.core.db.Tables.OPERATOR_PASSWORD;
import id.unifi.service.core.db.tables.records.OperatorRecord;
import id.unifi.service.core.operator.PasswordReset;
import id.unifi.service.core.operator.email.OperatorEmailRenderer;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.selectFrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ApiService("operator")
public class OperatorService {
    private static final Logger log = LoggerFactory.getLogger(OperatorService.class);
    private static final Field<Boolean> OPERATOR_HAS_PASSWORD = field(exists(
            selectFrom(OPERATOR_PASSWORD)
                    .where(OPERATOR_PASSWORD.CLIENT_ID.eq(OPERATOR.CLIENT_ID))
                    .and(OPERATOR_PASSWORD.USERNAME.eq(OPERATOR.USERNAME))))
            .as("operator_has_password");
    private static final SelectField<?>[] OPERATOR_FIELDS = {
            OPERATOR.CLIENT_ID, OPERATOR.USERNAME, OPERATOR.NAME, OPERATOR.EMAIL, OPERATOR.ACTIVE, OPERATOR_HAS_PASSWORD
    };

    private static final Map<? extends TableField<OperatorRecord, ?>, Function<FieldChanges, ?>> editables = Map.of(
            OPERATOR.NAME, c -> c.name,
            OPERATOR.EMAIL, c -> c.email,
            OPERATOR.ACTIVE, c -> c.active);

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
                requestPasswordSet(sql, clientId, username, Optional.of(onboarder));
            }
            return null;
        });
    }

    @ApiOperation
    public void editOperator(OperatorSessionData session,
                             String clientId,
                             String username,
                             FieldChanges changes) {
        authorize(session, clientId);
        changes.validate();

        Map<? extends TableField<OperatorRecord, ?>, ?> fieldMap = getUpdateQueryFieldMap(editables, changes);

        int rowsUpdated = db.execute(sql -> sql
                .update(OPERATOR)
                .set(fieldMap)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .execute());

        if (rowsUpdated == 0) {
            throw new NotFound("operator");
        }
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
            return new AuthInfo(getOperatorInfo(operator.get().clientId, operator.get().username),
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
    public List<OperatorInfo> listOperators(OperatorSessionData session,
                                            String clientId,
                                            @Nullable ListFilter filter) {
        authorize(session, clientId);
        if (filter == null) filter = ListFilter.empty();
        Condition filterCondition = filterCondition(filter.active, OPERATOR.ACTIVE::eq);
        return db.execute((DSLContext sql) -> sql.select(OPERATOR_FIELDS)
                .from(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(filterCondition)
                .fetch(OperatorService::operatorFromRecord));
    }

    @ApiOperation
    public OperatorInfo getOperator(OperatorSessionData session, String clientId, String username) {
        authorize(session, clientId);
        return getOperatorInfo(clientId, username);
    }

    @ApiOperation
    public void requestPasswordReset(OperatorSessionData session, String clientId, String username) {
        Optional<OperatorPK> onboarder;
        if (session.getOperator() != null) { // Authorized invitation or reset request for someone password
            OperatorPK operator = authorize(session, clientId);
            onboarder = Optional.of(operator);
        } else { // Reset request for my own password
            onboarder = Optional.empty();
        }

        db.execute(sql -> {
            if (isOperatorActive(sql, clientId, username))
                requestPasswordSet(sql, clientId, username, onboarder);
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
    public void cancelPasswordReset(String clientId,
                                    String username,
                                    TimestampedToken token) {
        passwordReset.cancel(clientId, username, token);
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
            throw new AuthenticationFailed("Current password isn't valid");
        }
    }

    private AuthInfo approveAuthAttempt(OperatorSessionData session, OperatorPK operator) {
        Token sessionToken = new Token();
        sessionTokenStore.put(sessionToken, operator);
        session.setAuth(sessionToken, operator);
        recordAuthAttempt(operator, true);
        return new AuthInfo(getOperatorInfo(operator.clientId, operator.username),
                sessionToken,
                Instant.now().plusSeconds(config.sessionTokenValiditySeconds()),
                verticalConfigManager.getClientSideConfig(operator.clientId));
    }

    private OperatorInfo getOperatorInfo(String clientId, String username) {
        return db.execute(sql -> sql.select(OPERATOR_FIELDS)
                .from(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOne(OperatorService::operatorFromRecord));
    }

    private static boolean isOperatorActive(DSLContext sql, String clientId, String username) {
        return sql.fetchExists(selectFrom(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .and(OPERATOR.ACTIVE));
    }

    private static OperatorInfo operatorFromRecord(Record r) {
        return new OperatorInfo(
                r.get(OPERATOR.CLIENT_ID),
                r.get(OPERATOR.USERNAME),
                r.get(OPERATOR.NAME),
                r.get(OPERATOR.EMAIL),
                r.get(OPERATOR.ACTIVE),
                r.get(OPERATOR_HAS_PASSWORD));
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
                                    Optional<OperatorPK> onboarder) {
        TimestampedToken token = passwordReset.generateResetToken(sql, clientId, username);

        OperatorInfo operatorInfo = getOperatorInfo(clientId, username);
        if (operatorInfo == null) throw new NotFound("operator");

        EmailSenderProvider.EmailMessage message;
        if (onboarder.isPresent()) {
            OperatorInfo onboarderInfo = getOperatorInfo(onboarder.get().clientId, onboarder.get().username);
            message = emailRenderer.renderInvitation(operatorInfo, token, onboarderInfo);
        } else {
            message = emailRenderer.renderPasswordResetInstructions(operatorInfo, token);
        }

        emailSender.send(operatorInfo.email, message);
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
        return sql.select(OPERATOR_FIELDS)
                .from(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOptional(OperatorService::operatorFromRecord);
    }

    private boolean passwordMatches(String clientId, String username, String password) {
        Optional<byte[]> encodedHash = db.execute(sql -> sqlPasswordHash(sql, clientId, username));
        return encodedHash.map(hash -> SecretHashing.check(password, hash)).orElse(false);
    }

    public static class PasswordResetInfo {
        public final Instant expiryDate;
        public final OperatorInfo operator;

        public PasswordResetInfo(Instant expiryDate, OperatorInfo operator) {
            this.expiryDate = expiryDate;
            this.operator = operator;
        }
    }

    public static class ListFilter {
        Optional<Boolean> active;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) // TODO: shouldn't have to specify mode(?)
        public ListFilter(Optional<Boolean> active) {
            this.active = active;
        }

        static ListFilter empty() {
            return new ListFilter(Optional.empty());
        }
    }

    // TODO: Figure out a way to make this immutable
    public static class FieldChanges {
        public String name;
        public String email;
        public Boolean active;

        public FieldChanges() {}

        // Need to validate outside the ctor, otherwise Jackson will rewrap as JsonMappingException
        void validate() {
            validateAll(
                    v("name|email|active", atLeastOneNonNull(name, email, active)),
                    v("name", name, Validation::shortString),
                    v("email", email, Validation::email)
            );
        }

        public String toString() {
            return "FieldChanges{" +
                    "name=" + name +
                    ", email=" + email +
                    ", active=" + active +
                    '}';
        }
    }
}
