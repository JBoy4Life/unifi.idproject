package id.unifi.service.common.api.access;

public enum Access {
    /**
     *  Permissioned operation;
     *  requests approved by access control only if the user has been granted the corresponding permission.
     */
    PERMISSIONED,

    /**
     * Permissioned operation;
     * requests not checked by access control, they can be checked programmatically in the operation implementation.
     */
    PERMISSIONED_NOT_CHECKED,

    /**
     * Public operation;
     * requests not checked by access control, a corresponding permission doesn't exist.
     */
    PUBLIC,
}
