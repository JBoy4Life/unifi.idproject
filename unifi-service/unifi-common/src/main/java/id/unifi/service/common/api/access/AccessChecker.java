package id.unifi.service.common.api.access;

/**
 * Can be injected into API operation implementations as a parameter to check access permissions.
 */
@FunctionalInterface
public interface AccessChecker {
    void authorize();
}
