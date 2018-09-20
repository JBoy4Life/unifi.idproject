package id.unifi.service.common.api.access;

import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.api.errors.Unauthorized;
import static id.unifi.service.common.api.errors.UnauthorizedReason.PERMISSION;
import id.unifi.service.common.types.pk.OperatorPK;

import java.util.Collection;
import java.util.Set;

public interface AccessManager<S> {
    void updateOperationList(Collection<ServiceRegistry.Operation> operations);

    Set<String> getPermissions(OperatorPK operator);

    boolean isAuthorized(String operation, S sessionData, boolean skipAccessTypeCheck);

    default void ensureAuthorized(String operation, S sessionData, boolean skipAccessTypeCheck) {
        if (!isAuthorized(operation, sessionData, skipAccessTypeCheck)) throw new Unauthorized(PERMISSION);
    }

    default void ensureAuthorized(String operation, S sessionData) {
        ensureAuthorized(operation, sessionData, false);
    }

    void invalidatePermissionsCache(OperatorPK operator);
}
