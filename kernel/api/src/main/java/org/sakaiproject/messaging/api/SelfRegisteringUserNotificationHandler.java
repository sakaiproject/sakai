package org.sakaiproject.messaging.api;

/**
 * Marker interface for a handler that will register and unregister itself.
 *
 * This is to accommodate any unknown implementations of the UserNotificationHandler, which were previously autowired into the
 * {@link UserMessagingService}. The {@link org.sakaiproject.messaging.api.AbstractUserNotificationHandler} base
 * class handles this registration for convenience in subclasses. However, there may be bare implementations of the
 * interface that need to be scooped up by a post-processor and implicitly registered to preserve the previous behavior.
 */
public interface SelfRegisteringUserNotificationHandler extends UserNotificationHandler {
}
