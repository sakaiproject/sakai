/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.time.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.TimeZone;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;

/**
 * <p>
 * BasicTimeService implements the Sakai TimeService
 * </p>
 */
@Slf4j
public class BasicTimeService implements TimeService
{
	/** The time zone for our GMT times. */
	protected TimeZone M_tz = null;

	/**
	 * a calendar to clone for GMT time construction
	 */
	protected GregorianCalendar M_GCal = null;

	/**
	 * The formatter for our special GMT format(s)
	 */
	protected DateFormat M_fmtA = null;

	protected DateFormat M_fmtB = null;

	protected DateFormat M_fmtC = null;

	protected DateFormat M_fmtD = null;

	protected DateFormat M_fmtE = null;

	protected DateFormat M_fmtG = null;

	// Map of Timezone/Locales to LocalTzFormat objects
	private Hashtable<String, LocalTzFormat> M_localeTzMap = new Hashtable<String, LocalTzFormat>();


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/
	@Setter private UserTimeService userTimeService;
	@Setter private UserLocaleServiceImpl userLocaleService;


	// Can be injected for testing
	private Clock clock = Clock.systemDefaultZone();

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	public Clock getClock() {
		return clock;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		Objects.requireNonNull(userLocaleService);
		Objects.requireNonNull(userTimeService);
		/** The time zone for our GMT times. */
		M_tz = TimeZone.getTimeZone("GMT");

		log.info("init()");

		/**
		 * a calendar to clone for GMT time construction
		 */
		M_GCal = newCalendar(M_tz, 0, 0, 0, 0, 0, 0, 0);

		// Note: formatting for GMT time representations
		M_fmtA = (DateFormat)(new SimpleDateFormat("yyyyMMddHHmmssSSS"));
		M_fmtB = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		M_fmtC = DateFormat.getTimeInstance(DateFormat.SHORT);
		M_fmtD = DateFormat.getDateInstance(DateFormat.MEDIUM);
		M_fmtE = (DateFormat)(new SimpleDateFormat("yyyyMMddHHmmss"));
		M_fmtG = (DateFormat)(new SimpleDateFormat("yyyy/DDD/HH/")); // that's year, day of year, hour

		M_fmtA.setTimeZone(M_tz);
		M_fmtB.setTimeZone(M_tz);
		M_fmtC.setTimeZone(M_tz);
		M_fmtD.setTimeZone(M_tz);
		M_fmtE.setTimeZone(M_tz);
		M_fmtG.setTimeZone(M_tz);

	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	protected String[] getUserTimezoneLocale()
	{
		String timeZone = userTimeService.getLocalTimeZone().getID();
		// Now, get user's preferred locale
		String localeId = userLocaleService.getLocalLocale();

		String[] timeZoneLocale = new String[] {timeZone, localeId};

		return timeZoneLocale;
	}

	protected LocalTzFormat getLocalTzFormat(String[] timeZoneLocale)
	{
		//we need to convert the String[] to a string key
		String tzLocaleString = stringAraytoKeyString(timeZoneLocale);

		LocalTzFormat tzFormat = M_localeTzMap.get(tzLocaleString);
		if (log.isDebugEnabled())
		{
			log.debug("M_localeTzMap contains: " + M_localeTzMap.size() + " members");
		}
		if (tzFormat == null)
		{
			tzFormat = new LocalTzFormat(timeZoneLocale[0], timeZoneLocale[1]);
			M_localeTzMap.put(tzLocaleString, tzFormat);
		}

		return tzFormat;
	}

	private String stringAraytoKeyString(String[] timeZoneLocale) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < timeZoneLocale.length; i++ )
		{
			if (i > 0)
			{
				sb.append("_");
			}
			sb.append(timeZoneLocale[i]);
		}

		if (log.isDebugEnabled())
		{
			log.debug("returing key: " + sb.toString());
		}
		return sb.toString();
	}


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: org.sakai.service.time.TimeService
	 *********************************************************************************************************************************************************************************************************************************************************/


	/**
	 * {@inheritDoc}
	 */
	public Time newTime()
	{
		return new MyTime(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeGmt(String value)
	{
		return new MyTime(this,value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTime(long value)
	{
		return new MyTime(this,value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTime(GregorianCalendar cal)
	{
		return new MyTime(this,cal.getTimeInMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeGmt(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		return new MyTime(this,M_tz, year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeGmt(TimeBreakdown breakdown)
	{
		return new MyTime(this,M_tz, breakdown);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeLocal(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		TimeZone tz_local = userTimeService.getLocalTimeZone();
		return new MyTime(this,tz_local, year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * {@inheritDoc}
	 */
	public Time newTimeLocal(TimeBreakdown breakdown)
	{
		TimeZone tz_local = userTimeService.getLocalTimeZone();
		return new MyTime(this,tz_local, breakdown);
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeBreakdown newTimeBreakdown(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		return new MyTimeBreakdown(year, month, day, hour, minute, second, millisecond);
	}
	@Override
	public TimeRange newTimeRange(Time start, Time end, boolean startIncluded, boolean endIncluded)
	{
		return new MyTimeRange(this, start, end, startIncluded, endIncluded);
	}

	@Override
	public TimeRange newTimeRange(String value)
	{
		return new MyTimeRange(this , value);
	}

	@Override
	public TimeRange newTimeRange(Time startAndEnd)
	{
		return new MyTimeRange(this, startAndEnd, startAndEnd, true, true);
	}

	@Override
	public TimeRange newTimeRange(long start, long duration)
	{
		return new MyTimeRange(this, start, duration);
	}

	@Override
	public TimeRange newTimeRange(Time start, Time end)
	{
		return new MyTimeRange(this, start, end, true, true);
	}


	@Override
	public TimeRange newTimeRange(Instant startAndEnd) {
		return new MyTimeRange(this, startAndEnd, startAndEnd, true, true);
	}

	@Override
	public TimeRange newTimeRange(Instant start, Instant end) {
		return new MyTimeRange(this, start, end, true, true);
	}

	@Override
	public TimeRange newTimeRange(Instant start, Instant end, boolean startIncluded, boolean endIncluded) {
		return new MyTimeRange(this, start, end, startIncluded, endIncluded);
	}

	@Override
	public TimeZone getLocalTimeZone()
	{
		return userTimeService.getLocalTimeZone();
	}

	@Override
	public TimeZone getLocalTimeZone(String userId) {
		return userTimeService.getLocalTimeZone(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean clearLocalTimeZone(String userId)
	{
		// Must not use && as need to clear them both.
		return userTimeService.clearLocalTimeZone(userId) & userLocaleService.clearLocalLocale(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public GregorianCalendar getCalendar(TimeZone zone, int year, int month, int day, int hour, int min, int sec, int ms)
	{
	    return newCalendar(zone, year, month, day, hour, min, sec, ms);
	}

	/**
	 * Compare two Time for differences, either may be null
	 * 
	 * @param a
	 *        One Time.
	 * @param b
	 *        The other Time.
	 * @return true if the Times are different, false if they are the same.
	 */
	public boolean different(Time a, Time b)
	{
		// if both null, they are the same
		if ((a == null) && (b == null)) return false;

		// if either are null (they both are not), they are different
		if ((a == null) || (b == null)) return true;

		// now we know neither are null, so compare
		return (!a.equals(b));
	}

	

	public static GregorianCalendar newCalendar(TimeZone zone, int year, int month, int day, int hour, int min, int sec, int ms)
	{
		GregorianCalendar rv = new GregorianCalendar(year, month, day, hour, min, sec);
		rv.setTimeZone(zone);
		rv.set(GregorianCalendar.MILLISECOND, ms);

		return rv;
	}

	@Override
	public String timeFormat(Date date, Locale locale, int df) {
		return userTimeService.timeFormat(date, locale, df);
	}

	@Override
	public String dateFormat(Date date, Locale locale, int df) {
		return userTimeService.dateFormat(date, locale, df);
	}

	@Override
	public String dayOfWeekFormat(Date date, Locale locale, int df) {
		return userTimeService.dayOfWeekFormat(date, locale, df);
	}

	@Override
	public String dateTimeFormat(Date date, Locale locale, int df) {
		return userTimeService.dateTimeFormat(date, locale, df);
	}

	@Override
	public String shortLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale) {
		return userTimeService.shortLocalizedTimestamp(instant, timezone, locale);
	}

	@Override
	public String shortLocalizedTimestamp(Instant instant, Locale locale) {
		return userTimeService.shortLocalizedTimestamp(instant, locale);
	}

	@Override
	public String shortLocalizedDate(LocalDate date, Locale locale) {
		return userTimeService.shortLocalizedDate(date, locale);
	}

	@Override
	public String shortPreciseLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale) {
		return userTimeService.shortPreciseLocalizedTimestamp(instant, timezone, locale);
	}

	@Override
	public String shortPreciseLocalizedTimestamp(Instant instant, Locale locale) {
		return userTimeService.shortPreciseLocalizedTimestamp(instant, getLocalTimeZone(), locale);
	}

	@Override
	public Date parseISODateInUserTimezone(String dateString) {
		return userTimeService.parseISODateInUserTimezone(dateString);
	}

}
