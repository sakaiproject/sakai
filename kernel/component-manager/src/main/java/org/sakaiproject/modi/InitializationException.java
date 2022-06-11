package org.sakaiproject.modi;

/**
 * Thrown when there is an unrecoverable problem during {@link Environment} initialization.
 */
public class InitializationException extends RuntimeException {
    public InitializationException(String msg) {
        super(msg);
    }
}