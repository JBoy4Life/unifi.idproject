package id.unifi.service.common.api.access;

import id.unifi.service.common.api.ServiceRegistry;
import id.unifi.service.common.types.pk.OperatorPK;

import java.util.Collection;
import java.util.Set;

public interface AccessManager<S> {
    void updateOperationList(Collection<ServiceRegistry.Operation> operations);

    Set<String> getPermissions(OperatorPK operator);

    boolean authorize(String messageType, S sessionData);

    boolean isAllowed(String operationName, OperatorPK operator);
}
