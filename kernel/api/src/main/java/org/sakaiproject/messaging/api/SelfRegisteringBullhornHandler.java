package org.sakaiproject.messaging.api;

/**
 * Marker interface for a handler that will register and unregister itself.
 *
 * This is to accommodate any unknown implementations of the BullhornHandler, which were previously autowired into the
 * {@link MessagingService}. The {@link org.sakaiproject.messaging.api.bullhornhandlers.AbstractBullhornHandler} base
 * class handles this registration for convenience in subclasses. However, there may be bare implementations of the
 * interface that need to be scooped up by a post-processor and implicitly registered to preserve the previous behavior.
 */
public interface SelfRegisteringBullhornHandler extends BullhornHandler {
}
