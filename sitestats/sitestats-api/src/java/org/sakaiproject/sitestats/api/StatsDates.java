package org.sakaiproject.sitestats.api;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility methods for presenting dates in SiteStats
 *
 * @author plukasew
 */
public class StatsDates
{
	/**
	 * Formats a point in time for display to the user in a concise way that still presents all relevant information
	 * including date, time, and time zone.
	 *
	 * @param instant the instant on the timeline
	 * @param timezone the time zone to use when displaying the date
	 * @param locale the locale to use when formatting the date for display
	 * @return a formatted date/time for presentation to the user
	 */
	public static String shortLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale)
	{
		ZonedDateTime userDate = ZonedDateTime.ofInstant(instant, timezone.toZoneId());
		DateTimeFormatter userFormatter = new DateTimeFormatterBuilder()
				.appendLocalized(FormatStyle.MEDIUM, FormatStyle.SHORT)
				.appendLiteral(" ").appendZoneText(TextStyle.SHORT)
				.toFormatter(locale);
		return userDate.format(userFormatter);
	}
}
