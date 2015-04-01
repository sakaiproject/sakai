package org.sakaiproject.tool.api;

/**
 * Exception to indicate that we shouldn't allow new sessions to be created as the
 * node is closing.
 *
 * In the future this shouldn't be a RuntimeException and callers should explicitly catch this
 * exception.
 */
public class ClosingException extends RuntimeException {
}
