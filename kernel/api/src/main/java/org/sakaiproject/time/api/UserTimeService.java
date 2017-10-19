package org.sakaiproject.time.api;

import java.util.TimeZone;

/**
 * This is an extraction out the user timezone specific parts of the TimeService. Refactorings can then be
 * done to bind to this service when the rest of TimeService isn't needed.
 *
 * @see TimeService
 */
public interface UserTimeService {

    /**
     * Access the users preferred local TimeZone.
     *
     * @return The user's local TimeZone.
     */
    TimeZone getLocalTimeZone();

    /**
     * Clear local time zone for specified user. Should be called when locale or timezone for user is changed.
     *
     * @return true if successful
     */
    boolean clearLocalTimeZone(String userId);
}
