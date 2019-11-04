package org.sakaiproject.authz.api;

public class AuthzRealmLockException extends Exception {

    private String reference;
    private AuthzGroup.RealmLockMode lockMode;

    public AuthzRealmLockException(String message) {
        super(message);
    }

    public AuthzRealmLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthzRealmLockException(String message, String reference, AuthzGroup.RealmLockMode lockMode, Throwable cause) {
        super(message, cause);
        this.reference = reference;
        this.lockMode = lockMode;
    }

    @Override
    public String getMessage() {
        String lockMessage = "";
        if (reference != null) {
            lockMessage = ", caused by reference: " + reference;
        }
        if (lockMode != null) {
            lockMessage = ", with lock: " + lockMode;
        }
        return super.getMessage() + lockMessage;
    }
}
