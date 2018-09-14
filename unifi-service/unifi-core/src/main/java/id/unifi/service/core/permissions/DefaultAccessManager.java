package id.unifi.service.core.permissions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.access.Access;
import id.unifi.service.common.api.access.AccessManager;
import static id.unifi.service.common.api.access.AccessUtils.getAllSubsumingOperations;
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

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                )));;
    }

    public void updateOperationList(Collection<ServiceRegistry.Operation> permissionedOperations) {
        this.permissionedOperations = permissionedOperations.stream().collect(toMap(o -> o.messageType, identity()));

        var operationNames = this.permissionedOperations.keySet().stream()
                .flatMap(o -> Stream.of(getAllSubsumingOperations(o)))
                .collect(Collectors.toUnmodifiableSet());

        var operationsInserted = db.execute(sql -> {
            var inserted = 0;
            for (var messageType : operationNames) {
                inserted += sql.insertInto(OPERATION)
                        .set(OPERATION.OPERATION_, messageType)
                        .set(OPERATION.DESCRIPTION, "") // TODO: fill in
                        .onConflictDoNothing()
                        .execute();
            }
            return inserted;
        });

        log.info("Inserted {} new operations", operationsInserted);
    }

    public boolean authorize(String operationName, OperatorSessionData session) {
        if (permissionedOperations == null)
            throw new IllegalStateException("Cannot authorize without list of permissioned operations");

        var operator = session.getOperator();
        var operation = permissionedOperations.get(operationName);
        return operation == null || operation.access == Access.PERMISSIONED_NOT_CHECKED
                || (operator != null && subsumesOperation(getPermissions(operator), operationName));
    }

    public boolean isAllowed(String operationName, OperatorPK operator) {
        if (permissionedOperations == null)
            throw new IllegalStateException("Cannot authorize without list of permissioned operations");

        return subsumesOperation(getPermissions(operator), operationName);
    }

    public Set<String> getPermissions(OperatorPK operator) {
        return permissionCache.getUnchecked(operator);
    }

    public void invalidatePermissionsCache(OperatorPK operator) {
        permissionCache.invalidate(operator);
    }
}
