package org.sakaiproject.time.api;

import java.time.Instant;
import java.time.LocalDate;
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
    
    /**
     * Formats a point in time, in the given time zone, for display to the user in a concise way that still presents all relevant information
     * including date, time, and time zone.
     *
     * @param instant the instant in time
     * @param timezone the time zone to use when displaying the date
     * @param locale the locale to use when formatting the date for display
     * @return a formatted date/time for presentation to the user
     */
    public String shortLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale);

    /**
     * Formats a point in time, in the user's time zone, for display to the user in a concise way that still presents all relevant information
     * including date, time, and time zone.
     *
     * @param instant the instant in time
     * @param locale the locale to use when formatting the date for display
     * @return a formatted date/time for presentation to the user
     */
    public String shortLocalizedTimestamp(Instant instant, Locale locale);

    /**
     * Formats a date (month/day/year) in a concise but easily understood format for the given locale.
     * Typically presents unambiguous month/day/year values as opposed to a purely 2-digit value for each.
     * @param date month/day/year value
     * @param locale the locale for use when formatting the date for display
     * @return a formatted date for presentation to the user
     */
    public String shortLocalizedDate(LocalDate date, Locale locale);
}
