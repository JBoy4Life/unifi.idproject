package id.unifi.service.core.permissions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.access.Access;
import id.unifi.service.common.api.access.AccessManager;
import id.unifi.service.common.api.access.AccessUtils;
import static id.unifi.service.common.api.access.AccessUtils.subsumesOperation;
import id.unifi.service.common.operator.OperatorSessionData;
import id.unifi.service.common.types.pk.OperatorPK;
import static id.unifi.service.core.db.Core.CORE;
import static id.unifi.service.core.db.Tables.OPERATION;
import static id.unifi.service.core.db.Tables.PERMISSION;
import id.unifi.service.dbcommon.Database;
import id.unifi.service.dbcommon.DatabaseProvider;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DefaultAccessManager implements AccessManager<OperatorSessionData> {
    private static final Logger log = LoggerFactory.getLogger(DefaultAccessManager.class);

    private static final Duration PERMISSION_CACHE_EXPIRY_TIME = Duration.ofMinutes(5);

    private final Database db;
    private final LoadingCache<OperatorPK, Set<String>> permissionCache;
    private volatile Map<String, ServiceRegistry.Operation> permissionedOperations;

    public DefaultAccessManager(DatabaseProvider dbProvider) {
        this.db = dbProvider.bySchema(CORE);
        this.permissionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(PERMISSION_CACHE_EXPIRY_TIME.toMillis(), TimeUnit.MILLISECONDS)
                .build(CacheLoader.from(operator -> db.execute(sql ->
                        sql.select(PERMISSION.OPERATION).from(PERMISSION)
                                .where(PERMISSION.CLIENT_ID.eq(operator.clientId))
                                .and(PERMISSION.USERNAME.eq(operator.username))
                                .stream()
                                .map(Record1::value1)
                                .collect(toSet())
                )));
    }

    public void updateOperationList(Collection<ServiceRegistry.Operation> permissionedOperations) {
        this.permissionedOperations = permissionedOperations.stream().collect(toMap(o -> o.messageType, identity()));

        var operationNames = this.permissionedOperations.keySet().stream()
                .flatMap(AccessUtils::getAllSubsumingOperations)
                .collect(Collectors.toUnmodifiableSet());

        var operationsInserted = db.execute(sql -> {
            var inserted = 0;
            for (var operationName : operationNames) {
                var description = getDescription(operationName);
                inserted += sql.insertInto(OPERATION)
                        .set(OPERATION.OPERATION_, operationName)
                        .set(OPERATION.DESCRIPTION, description)
                        .onConflict()
                        .doUpdate()
                        .set(OPERATION.DESCRIPTION, description)
                        .execute();
            }
            return inserted;
        });

        log.info("Inserted/updated {} operations", operationsInserted);
    }

    private String getDescription(String operationName) {
        return Optional.ofNullable(this.permissionedOperations.get(operationName)).map(o -> o.description).orElse("");
    }

    public boolean isAuthorized(String operationName, OperatorSessionData session, boolean skipAccessTypeCheck) {
        if (permissionedOperations == null)
            throw new IllegalStateException("Cannot authorize without list of permissioned operations");

        return allowedByAccessType(operationName, skipAccessTypeCheck)
                || subsumesOperation(getPermissions(session.getOperator()), operationName);
    }

    public Set<String> getPermissions(@Nullable OperatorPK operator) {
        return operator == null ? Set.of() : permissionCache.getUnchecked(operator);
    }

    public void invalidatePermissionsCache(OperatorPK operator) {
        permissionCache.invalidate(operator);
    }

    private boolean allowedByAccessType(String operationName, boolean skipAccessTypeCheck) {
        if (skipAccessTypeCheck) return false;
        var operation = permissionedOperations.get(operationName); // Missing entry means operation is public
        return operation == null || operation.access == Access.PERMISSIONED_NOT_CHECKED;
    }
}
