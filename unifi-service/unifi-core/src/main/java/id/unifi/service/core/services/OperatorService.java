package id.unifi.service.core.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Sets;
import com.statemachinesystems.envy.Default;
import id.unifi.service.common.api.Validation;
import static id.unifi.service.common.api.Validation.*;
import id.unifi.service.common.api.access.Access;
import id.unifi.service.common.api.access.AccessChecker;
import id.unifi.service.common.api.access.AccessManager;
import static id.unifi.service.common.api.access.AccessUtils.subsumesOperation;
import id.unifi.service.common.api.annotations.ApiConfigPrefix;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.annotations.HttpMatch;
import id.unifi.service.common.api.errors.AlreadyExists;
import id.unifi.service.common.api.errors.AuthenticationFailed;
import id.unifi.service.common.api.errors.NotFound;
import id.unifi.service.common.api.errors.Unauthorized;
import id.unifi.service.common.operator.AuthInfo;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.operator.SessionTokenStore;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.security.ScryptConfig;
import id.unifi.service.common.security.SecretHashing;
import id.unifi.service.common.security.TimestampedToken;
import id.unifi.service.common.security.Token;
import id.unifi.service.common.types.OperatorInfo;
import id.unifi.service.common.types.pk.OperatorPK;
import id.unifi.service.core.VerticalConfigManager;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.*;
import static id.unifi.service.core.db.tables.Operator.OPERATOR;
import static id.unifi.service.core.db.tables.Permission.PERMISSION;
import id.unifi.service.core.db.tables.records.OperatorRecord;
import id.unifi.service.core.operator.PasswordReset;
import id.unifi.service.core.operator.email.OperatorEmailRenderer;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static id.unifi.service.dbcommon.DatabaseUtils.filterCondition;
import static id.unifi.service.dbcommon.DatabaseUtils.getUpdateQueryFieldMap;
import static org.eclipse.jetty.http.HttpMethod.POST;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.TableField;
import static org.jooq.impl.DSL.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final AccessManager<OperatorSessionData> accessManager;
    private final Config config;

    private interface Config {
        @Default("864000")
        int sessionTokenValiditySeconds();
    }

    public OperatorService(@ApiConfigPrefix("unifi.operator") Config config,
                           @ApiConfigPrefix("unifi.operator.password.hashing") ScryptConfig hashingConfig,
                           DatabaseProvider dbProvider,
                           PasswordReset passwordReset,
                           OperatorEmailRenderer emailRenderer,
                           EmailSenderProvider emailSender,
                           SessionTokenStore sessionTokenStore,
                           VerticalConfigManager verticalConfigManager,
                           AccessManager<OperatorSessionData> accessManager) {
        this.config = config;
        this.db = dbProvider.bySchema(CORE);
        this.passwordReset = passwordReset;
        this.passwordHashing = new SecretHashing(hashingConfig);
        this.emailRenderer = emailRenderer;
        this.emailSender = emailSender;
        this.sessionTokenStore = sessionTokenStore;
        this.verticalConfigManager = verticalConfigManager;
        this.accessManager = accessManager;
    }

    @ApiOperation
    public void registerOperator(OperatorSessionData session,
                                 String clientId,
                                 String username,
                                 String name,
                                 String email,
                                 boolean invite) {
        var onboarder = authorize(session, clientId);
        validateAll(
                v("username", shortId(username)),
                v("name", shortString(name)),
                v("email", email(email))
        );
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

        var fieldMap = getUpdateQueryFieldMap(editables, changes);

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

    @ApiOperation(access = Access.PUBLIC)
    @HttpMatch(path = "operators/auth-password", method = POST)
    public AuthInfo authPassword(OperatorSessionData session, String clientId, String username, String password) {
        validateAll(
                v(shortId(clientId), AuthenticationFailed::new),
                v(shortId(username), AuthenticationFailed::new),
                v(shortString(password), AuthenticationFailed::new)
        );
        if (passwordMatches(clientId, username, password)) {
            var operator = new OperatorPK(clientId, username);
            return approveAuthAttempt(session, operator);
        } else {
            // TODO recordLoginAttempt(clientId, username, false);
            session.setAuth(null, null);
            throw new AuthenticationFailed();
        }
    }

    @ApiOperation(access = Access.PUBLIC)
    public AuthInfo authToken(OperatorSessionData session, Token sessionToken) {
        var operator = sessionTokenStore.get(sessionToken);
        if (operator.isPresent()) {
            var pk = operator.get();
            session.setAuth(pk, sessionToken);
            return new AuthInfo(getOperatorInfo(pk.clientId, pk.username),
                    sessionToken,
                    Instant.now().plusSeconds(config.sessionTokenValiditySeconds()),
                    accessManager.getPermissions(pk),
                    verticalConfigManager.getClientSideConfig(pk.clientId));
        } else {
            session.setAuth(null, null);
            throw new AuthenticationFailed();
        }
    }

    @ApiOperation(access = Access.PUBLIC)
    public AuthInfo getAuthInfo(OperatorSessionData session) {
        var sessionToken = session.getSessionToken();
        var operator = Optional.ofNullable(sessionToken).flatMap(sessionTokenStore::get);

        return operator.map(op -> new AuthInfo(
                getOperatorInfo(op.clientId, op.username),
                sessionToken,
                Instant.now().plusSeconds(config.sessionTokenValiditySeconds()),
                accessManager.getPermissions(op),
                verticalConfigManager.getClientSideConfig(op.clientId))
        ).orElseThrow(() -> new NotFound("session"));
    }

    @ApiOperation(access = Access.PUBLIC)
    public void invalidateAuthToken(OperatorSessionData session) {
        var sessionToken = session.getSessionToken();
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
        var filterCondition = filterCondition(filter.active, OPERATOR.ACTIVE::eq);
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

    @ApiOperation(access = Access.PERMISSIONED_NOT_CHECKED)
    public void requestPasswordReset(OperatorSessionData session,
                                     String clientId,
                                     String username,
                                     AccessChecker accessChecker) {
        var invitee = new OperatorPK(clientId, username);
        var operator = session.getOperator();

        Optional<OperatorPK> onboarder;
        if (operator == null || operator.equals(invitee)) {
            // Request resetting my own password; allow without access restrictions
            onboarder = Optional.empty();
        } else {
            // Requesting reset on behalf of another operator, check access
            accessChecker.authorize();

            // Now check we're under the expected `clientId` as usual
            authorize(session, clientId);

            onboarder = Optional.of(operator);
        }

        // TODO: Should return an error if inviting an inactive operator
        db.execute(sql -> {
            if (isOperatorActive(sql, clientId, username))
                requestPasswordSet(sql, clientId, username, onboarder);
            return null;
        });
    }

    @ApiOperation(access = Access.PUBLIC)
    public PasswordResetInfo getPasswordReset(String clientId, String username, TimestampedToken token) {
        return db.execute(sql -> {
            var tokenHash =
                    passwordReset.findValidTokenHash(sql, clientId, username, token);
            if (tokenHash.isPresent()) {
                var operator = findOperator(sql, clientId, username).orElseThrow(AssertionError::new);
                return new PasswordResetInfo(tokenHash.get().since, operator);
            } else {
                return null;
            }
        });
    }

    @ApiOperation(access = Access.PUBLIC)
    public AuthInfo setPassword(OperatorSessionData session,
                                String clientId,
                                String username,
                                String password,
                                TimestampedToken token) {
        return db.execute(sql -> {
            var isResetValid = passwordReset.preparePasswordReset(sql, clientId, username, token);
            if (isResetValid) {
                setPassword(sql, clientId, username, password);
                return approveAuthAttempt(session, new OperatorPK(clientId, username));
            } else {
                throw new AuthenticationFailed();
            }
        });
    }

    @ApiOperation(access = Access.PUBLIC)
    public void cancelPasswordReset(String clientId,
                                    String username,
                                    TimestampedToken token) {
        passwordReset.cancel(clientId, username, token);
    }

    @ApiOperation
    public void changePassword(OperatorSessionData session, String currentPassword, String password) {
        var operator = session.getOperator();
        if (passwordMatches(operator.clientId, operator.username, currentPassword)) {
            db.execute(sql -> {
                setPassword(sql, operator.clientId, operator.username, password);
                return null;
            });
        } else {
            throw new AuthenticationFailed("Current password isn't valid");
        }
    }

    @ApiOperation(access = Access.PERMISSIONED_NOT_CHECKED, description = "Lists permissions for an operator")
    public Set<String> listPermissions(OperatorSessionData session,
                                       String clientId,
                                       String username,
                                       AccessChecker accessChecker) {
        var operator = authorize(session, clientId);
        if (!username.equals(operator.username)) accessChecker.authorize();
        return accessManager.getPermissions(new OperatorPK(clientId, username));
    }

    @ApiOperation(description = "Grants and revokes specified permissions")
    public void editPermissions(OperatorSessionData session,
                                String clientId,
                                String username,
                                @Nullable Set<String> grant,
                                @Nullable Set<String> revoke) {
        var operator = authorize(session, clientId);

        validateAll(
                v("grant|revoke", atLeastOneNonNull(grant, revoke)),
                v("grant|revoke", disjoint(grant, revoke))
        );

        Set<String> operationsToGrant = grant != null ? grant : Set.of();
        Set<String> operationsToRevoke = revoke != null ? revoke : Set.of();

        var subjectOperator = new OperatorPK(clientId, username);
        var operatorPermissions = accessManager.getPermissions(operator);

        var operations = Sets.union(operationsToGrant, operationsToRevoke);

        // An operator can only grant/revoke permissions they themselves have
        if (!subsumesAllOperations(operatorPermissions, operations)) throw new Unauthorized();

        db.execute(sql -> {
            grantPermissions(sql, clientId, username, operator.username, operationsToGrant);
            revokePermissions(sql, clientId, username, operator.username, operationsToRevoke);
            accessManager.invalidatePermissionsCache(subjectOperator);
            return null;
        });
    }

    @ApiOperation(description = "Grants permissions on all operations from a role")
    public void applyRole(OperatorSessionData session, String clientId, String username, String role) {
        var operator = authorize(session, clientId);
        var subjectOperator = new OperatorPK(clientId, username);
        var operatorPermissions = accessManager.getPermissions(operator);

        db.execute(sql -> {
            if (!sql.fetchExists(ROLE, ROLE.ROLE_.eq(role)))
                throw new NotFound("role");

            var operations = sql.select(ROLE_OPERATION.OPERATION)
                    .from(ROLE_OPERATION)
                    .where(ROLE_OPERATION.ROLE.eq(role))
                    .fetch(ROLE_OPERATION.OPERATION);

            if (!subsumesAllOperations(operatorPermissions, operations)) throw new Unauthorized();

            grantPermissions(sql, clientId, username, operator.username, operations);
            accessManager.invalidatePermissionsCache(subjectOperator);

            return null;
        });
    }

    @ApiOperation(description = "Lists permissioned operations")
    public List<String> listPermissionValues(OperatorSessionData session) {
        authorize(session);

        return db.execute(sql -> sql.select(OPERATION.OPERATION_)
                .from(OPERATION)
                .where(OPERATION.OPERATION_.notLike("%*")) // Ignore wildcards
                .fetch(OPERATION.OPERATION_));
    }

    private static void revokePermissions(DSLContext sql,
                                          String clientId,
                                          String username,
                                          String revokedBy,
                                          Collection<String> operations) {
        // jOOQ doesn't support DML CTEs yet https://github.com/jOOQ/jOOQ/issues/4474
        var query = sql.query("WITH d as ({0}) {1}",
                deleteFrom(PERMISSION)
                        .where(PERMISSION.CLIENT_ID.eq(clientId))
                        .and(PERMISSION.USERNAME.eq(username))
                        .and(PERMISSION.OPERATION.in(operations))
                        .returning(),
                insertInto(PERMISSION_HISTORY).select(select(
                        field("client_id"),
                        field("username"),
                        field("operation"),
                        field("granted_by"),
                        val(revokedBy),
                        field("since"),
                        currentLocalDateTime()
                ).from(table("d"))));
        query.execute();
    }

    private AuthInfo approveAuthAttempt(OperatorSessionData session, OperatorPK operator) {
        var sessionToken = new Token();
        sessionTokenStore.put(sessionToken, operator);
        session.setAuth(operator, sessionToken);
        recordAuthAttempt(operator, true);
        accessManager.invalidatePermissionsCache(operator);

        return new AuthInfo(getOperatorInfo(operator.clientId, operator.username),
                sessionToken,
                Instant.now().plusSeconds(config.sessionTokenValiditySeconds()),
                accessManager.getPermissions(operator),
                verticalConfigManager.getClientSideConfig(operator.clientId));
    }

    private OperatorInfo getOperatorInfo(String clientId, String username) {
        return db.execute(sql -> sql.select(OPERATOR_FIELDS)
                .from(OPERATOR)
                .where(OPERATOR.CLIENT_ID.eq(clientId))
                .and(OPERATOR.USERNAME.eq(username))
                .fetchOne(OperatorService::operatorFromRecord));
    }

    private static void grantPermissions(DSLContext sql,
                                         String clientId,
                                         String username,
                                         String grantedBy,
                                         Collection<String> operations) {
        var q = sql.insertInto(PERMISSION,
                PERMISSION.CLIENT_ID, PERMISSION.USERNAME, PERMISSION.OPERATION, PERMISSION.GRANTED_BY);
        for (var operation : operations) q = q.values(clientId, username, operation, grantedBy);
        q.onConflictDoNothing().execute();
    }

    private static boolean subsumesAllOperations(Set<String> operatorPermissions, Collection<String> operations) {
        return operations.stream().allMatch(o -> subsumesOperation(operatorPermissions, o));
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
        var hash = passwordHashing.hash(password);
        sql.insertInto(OPERATOR_PASSWORD)
                .set(OPERATOR_PASSWORD.CLIENT_ID, clientId)
                .set(OPERATOR_PASSWORD.USERNAME, username)
                .set(OPERATOR_PASSWORD.PASSWORD_HASH, hash)
                .set(OPERATOR_PASSWORD.ALGORITHM, SecretHashing.SCRYPT_FORMAT_NAME)
                .onConflict()
                .doUpdate()
                .set(OPERATOR_PASSWORD.PASSWORD_HASH, hash)
                .set(OPERATOR_PASSWORD.ALGORITHM, SecretHashing.SCRYPT_FORMAT_NAME)
                .set(OPERATOR_PASSWORD.SINCE, currentLocalDateTime())
                .execute();
    }

    private void requestPasswordSet(DSLContext sql,
                                    String clientId,
                                    String username,
                                    Optional<OperatorPK> onboarder) {
        var token = passwordReset.generateResetToken(sql, clientId, username);

        var operatorInfo = getOperatorInfo(clientId, username);
        if (operatorInfo == null) throw new NotFound("operator");

        EmailSenderProvider.EmailMessage message;
        if (onboarder.isPresent()) {
            var onboarderInfo = getOperatorInfo(onboarder.get().clientId, onboarder.get().username);
            message = emailRenderer.renderInvitation(operatorInfo, token, onboarderInfo);
        } else {
            message = emailRenderer.renderPasswordResetInstructions(operatorInfo, token);
        }

        emailSender.queue(operatorInfo.name, operatorInfo.email, message);
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
        var encodedHash = db.execute(sql -> sqlPasswordHash(sql, clientId, username));
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
        final Optional<Boolean> active;

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
