package org.sakaiproject.time.api;

import java.util.Date;
import java.util.Locale;
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
    
    /**
     * Gets the date formatter with the given formatting style for the default Long locale.
     * @param date
     * @param locale
     * @return
     */
    public String  dateFormatLong(Date date, Locale locale);
    
    /**
     * Gets the date/time formatter with the given formatting style for the default Long locale. 
     * @param date
     * @param locale
     * @return
     */
    public String  dateTimeFormatLong(Date date, Locale locale);
    
    
}
