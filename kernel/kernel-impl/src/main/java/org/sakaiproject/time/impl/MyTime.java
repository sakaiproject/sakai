/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
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

import java.text.ParsePosition;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;

/**
 * <p>
 * MyTime is an implementation of the Time API Time.
 * </p>
 */
public class MyTime implements Time
{
	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The milliseconds since... same as Date */
	protected long m_millisecondsSince = 0;

	private transient BasicTimeService timeService;


	/**
	 * construct from a string, in our format, GMT values
	 * 
	 * @param str
	 *        time format string
	 */
	public MyTime(BasicTimeService timeService, String str)
	{
		this.timeService = timeService;
		// use formatter A: yyyyMMddHHmmssSSS
		Date d = null;
		synchronized (timeService.M_fmtA)
		{
			ParsePosition pos = new ParsePosition(0);
			d = timeService.M_fmtA.parse(str, pos);
		}
		m_millisecondsSince = d.getTime();
	}

	/**
	 * construct as now
	 */
	public MyTime(BasicTimeService timeService)
	{
		this.timeService = timeService;
		m_millisecondsSince = timeService.getClock().millis();
	}

	/**
	 * construct from a Long
	 * 
	 * @param l
	 *        time value in ms since...
	 */
	public MyTime(BasicTimeService timeService, long l)
	{
		this.timeService = timeService;
		m_millisecondsSince = l;
	}

	/**
	 * construct from individual ints, and the zone.
	 * 
	 * @param zone
	 *        The time zone.
	 * @param year
	 *        full year (i.e. 1999, 2000)
	 * @param month
	 *        month in year (1..12)
	 * @param day
	 *        day in month (1..31)
	 * @param hour
	 *        hour in day (0..23)
	 * @param minute
	 *        minute in hour (0..59)
	 * @param second
	 *        second in minute (0..59)
	 * @param millisecond
	 *        millisecond in second (0..999)
	 */
	public MyTime(BasicTimeService timeService, TimeZone zone, int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		this.timeService = timeService;
		GregorianCalendar cal = BasicTimeService.newCalendar(zone, year, month - 1, day, hour,
				minute, second, millisecond);
		m_millisecondsSince = cal.getTimeInMillis();
	}

	/**
	 * construct from time breakdown, and the zone.
	 * 
	 * @param zone
	 *        The time zone.
	 * @param tb
	 *        The TimeBreakdown with the values.
	 */
	public MyTime(BasicTimeService timeService, TimeZone zone, TimeBreakdown tb)
	{
		this.timeService = timeService;
		GregorianCalendar cal = BasicTimeService.newCalendar(zone, tb.getYear(), tb.getMonth() - 1,
				tb.getDay(), tb.getHour(), tb.getMin(), tb.getSec(), tb.getMs());
		m_millisecondsSince = cal.getTimeInMillis();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone()
	{
		return new MyTime(timeService,m_millisecondsSince);
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		String s = null;
		synchronized (timeService.M_fmtA)
		{
			// format
			s = timeService.M_fmtA.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringSql()
	{
		String s = null;
		synchronized (timeService.M_fmtE)
		{
			// format
			s = timeService.M_fmtE.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocal()
	{
		String s = null;
		DateFormat fmtAl = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtAl;
		synchronized (fmtAl)
		{
			// format
			s = fmtAl.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtFull()
	{
		String s = null;
		synchronized (timeService.M_fmtB)
		{
			// format
			s = timeService.M_fmtB.format(new Date(getTime()));
		}

		// lower the case of AM/PM
		s = fix(s);

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalFull()
	{
		String s = null;
		DateFormat fmtBl = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtBl;
		synchronized (fmtBl)
		{
			// format
			s = fmtBl.format(new Date(getTime()));
		}

		// lower the case of AM/PM
		s = fix(s);

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalFullZ()
	{
		String s = null;
		DateFormat fmtBlz = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtBlz;
		synchronized (fmtBlz)
		{
			// format
			s = fmtBlz.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtShort()
	{
		String s = null;
		synchronized (timeService.M_fmtC)
		{
			// format
			s = timeService.M_fmtC.format(new Date(getTime()));
		}

		// lower the case of AM/PM
		s = fix(s);

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalShort()
	{
		String s = null;
		DateFormat fmtCl = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtCl;
		synchronized (fmtCl)
		{
			// format
			s = fmtCl.format(new Date(getTime()));
		}

		// lower the case of AM/PM
		s = fix(s);

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtTime()
	{
		String s = null;
		synchronized (timeService.M_fmtC)
		{
			// format
			s = timeService.M_fmtC.format(new Date(getTime()));
		}

		// lower the case of AM/PM
		s = fix(s);

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalTime()
	{
		String s = null;
		DateFormat fmtCl = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtCl;
		synchronized (fmtCl)
		{
			// format
			s = fmtCl.format(new Date(getTime()));
		}

		// lower the case of AM/PM
		s = fix(s);

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalTimeZ()
	{

		String s = null;
		DateFormat fmtClz = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtClz;

		synchronized (fmtClz)
		{
			// format
			s = fmtClz.format(new Date(getTime()));
		}
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalTime24()
	{
		String s = null;
		DateFormat fmtFl = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtFl;
		synchronized (fmtFl)
		{
			// format
			s = fmtFl.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtDate()
	{
		String s = null;
		synchronized (timeService.M_fmtD)
		{
			// format
			s = timeService.M_fmtD.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalDate()
	{
		String s = null;
		DateFormat fmtDl = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtDl;
		synchronized (fmtDl)
		{
			// format
			s = fmtDl.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalShortDate()
	{
		String s = null;
		DateFormat fmtD2 = timeService.getLocalTzFormat(timeService.getUserTimezoneLocale()).M_fmtD2;
		synchronized (fmtD2)
		{
			// format
			s = fmtD2.format(new Date(getTime()));
		}

		return s;
	}


	/**
	 * {@inheritDoc}
	 */
	public String toStringRFC822Local()
	{
        	return new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z").format(new Date(getTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringFilePath()
	{
		String s = null;
		synchronized (timeService.M_fmtG)
		{
			// format
			s = timeService.M_fmtG.format(new Date(getTime()));
		}

		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		boolean equals = false;

		if (obj instanceof MyTime)
		{
			equals = (((MyTime) obj).m_millisecondsSince == m_millisecondsSince);
		}

		return equals;
	}

	/**
	 * Objects that are equal must have the same hashcode
	 */
	public int hashCode() {
		Long m = m_millisecondsSince;
		return m.hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o)
	{
		return (m_millisecondsSince < ((MyTime) o).m_millisecondsSince ? -1
				: (m_millisecondsSince > ((MyTime) o).m_millisecondsSince ? 1 : 0));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTime(long l)
	{
		m_millisecondsSince = l;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTime()
	{
		return m_millisecondsSince;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean before(Time other)
	{
		if (other == null) 
		   return false;

		return (m_millisecondsSince < ((MyTime) other).m_millisecondsSince);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean after(Time other)
	{
		if (other == null) 
		   return true;

		return (m_millisecondsSince > ((MyTime) other).m_millisecondsSince);
	}

	/**
	 * Fix the AM/PM format of the time string - lower the case.
	 * 
	 * @param s
	 *        The time string.
	 * @return The time string fixed.
	 */
	protected String fix(String s)
	{
		// if the last two chars are either AM or PM, change to "am" or "pm"
		int len = s.length();
		if (s.endsWith("PM"))
			return s.substring(0, len - 2) + "pm";
		else if (s.endsWith("AM"))
			return s.substring(0, len - 2) + "am";
      else
         return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeBreakdown breakdownGmt()
	{
		String s = toString();
		TimeBreakdown b = timeService.newTimeBreakdown(Integer.parseInt(s.substring(0, 4)),
				Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)), Integer.parseInt(s.substring(8, 10)),
				Integer.parseInt(s.substring(10, 12)), Integer.parseInt(s.substring(12, 14)), Integer.parseInt(s.substring(14)));

		return b;
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeBreakdown breakdownLocal()
	{
		String s = toStringLocal();
		TimeBreakdown b = timeService.newTimeBreakdown(Integer.parseInt(s.substring(0, 4)),
				Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)), Integer.parseInt(s.substring(8, 10)),
				Integer.parseInt(s.substring(10, 12)), Integer.parseInt(s.substring(12, 14)), Integer.parseInt(s.substring(14)));

		return b;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplay()
	{
		return this.toStringLocalFull();
	}
}
