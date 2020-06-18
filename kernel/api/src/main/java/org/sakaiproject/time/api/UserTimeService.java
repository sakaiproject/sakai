/**
 * Copyright (c) 2003-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * Access the current user's preferred local TimeZone.
     *
     * @return The user's local TimeZone.
     */
    TimeZone getLocalTimeZone();

    /**
     * Access the user's preferred local TimeZone.
     * @param userId
     * @return The user's local TimeZone.
     */
     TimeZone getLocalTimeZone(String userId);

    /**
     * Clear local time zone for specified user. Should be called when locale or timezone for user is changed.
     *
     * @return true if successful
     */
    boolean clearLocalTimeZone(String userId);

    /**
     * Gets the time formatter with the given formatting style in the user's locale and preferred timezone.
     * @param date
     * @param locale
     * @param format use java.text.DateFormat.SHORT, MEDIUM, LONG, or FULL
     * @return
     */
    public String  timeFormat(Date date, Locale locale, int format);

    /**
     * Gets the date formatter with the given formatting style in the user's locale and preferred timezone.
     * @param date
     * @param locale
     * @param format use java.text.DateFormat.SHORT, MEDIUM, LONG, or FULL
     * @return
     */
    public String  dateFormat(Date date, Locale locale, int format);

    /**
     * Gets the day of week localized and in the user's preferred timezone.
     * @param date
     * @param locale
     * @param format use java.text.DateFormat.SHORT, MEDIUM, LONG, or FULL
     * @return
     */
    public String dayOfWeekFormat(Date date, Locale locale, int format);

    /**
     * Gets the date/time formatter with the given formatting style in the user's locale and preferred timezone.
     * @param date
     * @param locale
     * @param format use java.text.DateFormat.SHORT, MEDIUM, LONG, or FULL
     * @return
     */
    public String  dateTimeFormat(Date date, Locale locale, int format);
    
    /**
     * Formats a point in time, in the given time zone, for display to the user in a concise way that still presents all relevant information
     * including date, time (to the minute), and time zone.
     *
     * @param instant the instant in time
     * @param timezone the time zone to use when displaying the date
     * @param locale the locale to use when formatting the date for display
     * @return a formatted date/time for presentation to the user
     */
    public String shortLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale);

    /**
     * Formats a point in time, in the given time zone, for display to the user in a concise way that still presents all relevant information
     * including date, time (to the second), and time zone.
     *
     * @param instant the instant in time
     * @param timezone the time zone to use when displaying the date
     * @param locale the locale to use when formatting the date for display
     * @return a formatted date/time for presentation to the user
     */
    public String shortPreciseLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale);

    /**
     * Formats a point in time, in the user's time zone, for display to the user in a concise way that still presents all relevant information
     * including date, time (to the minute), and time zone.
     *
     * @param instant the instant in time
     * @param locale the locale to use when formatting the date for display
     * @return a formatted date/time for presentation to the user
     */

    public String shortLocalizedTimestamp(Instant instant, Locale locale);
    /**
     * Formats a point in time, in the user's time zone, for display to the user in a concise way that still presents all relevant information
     * including date, time (to the second), and time zone.
     *
     * @param instant the instant in time
     * @param locale the locale to use when formatting the date for display
     * @return a formatted date/time for presentation to the user
     */
    public String shortPreciseLocalizedTimestamp(Instant instant, Locale locale);

    /**
     * Formats a date (month/day/year) in a concise but easily understood format for the given locale.
     * Typically presents unambiguous month/day/year values as opposed to a purely 2-digit value for each.
     * @param date month/day/year value
     * @param locale the locale for use when formatting the date for display
     * @return a formatted date for presentation to the user
     */
    public String shortLocalizedDate(LocalDate date, Locale locale);

    /**
     * A helper function to parse dates from the Sakai date/time picker.
     * Useful because Javascript Date object uses the client computer timezone
     * and not the user's preferred Sakai timezone.
     * @param dateString An ISO8601 date with zone info like 2015-02-19T02:25:00-06:00
     * @return a Date object localized to the user's preferred Sakai time zone
     */
	public Date parseISODateInUserTimezone(String dateString);

}
