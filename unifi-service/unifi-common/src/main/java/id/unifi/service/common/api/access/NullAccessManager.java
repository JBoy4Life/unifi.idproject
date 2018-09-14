package id.unifi.service.common.api.access;

import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.types.pk.OperatorPK;

import java.util.Collection;
import java.util.Set;

public class NullAccessManager<S> implements AccessManager<S> {
    public void updateOperationList(Collection<ServiceRegistry.Operation> operations) {}

    public Set<String> getPermissions(OperatorPK operator) {
        return Set.of();
    }

    public boolean isAuthorized(String operation, S sessionData, boolean accessTypeAlreadyChecked) {
        return true;
    }

    public void invalidatePermissionsCache(OperatorPK operator) {}
}
