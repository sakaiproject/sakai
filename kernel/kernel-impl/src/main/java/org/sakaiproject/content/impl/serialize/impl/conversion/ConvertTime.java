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

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.sakaiproject.content.impl.util.GMTDateformatter;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;

/**
 * <p>
 * MyTime is an implementation of the Time API Time.
 * </p>
 */
public class ConvertTime implements Time
{
	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The milliseconds since... same as Date */
	protected long m_millisecondsSince = 0;

	/**
	 * construct from a string, in our format, GMT values
	 * 
	 * @param str
	 *        time format string
	 * @throws ParseException 
	 */
	public ConvertTime(String str) 
	{
		Date date = GMTDateformatter.parse(str);
		m_millisecondsSince = date.getTime();
	}
	/**
	 * construct as now
	 */
	public ConvertTime()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * construct from a Long
	 * 
	 * @param l
	 *        time value in ms since...
	 */
	public ConvertTime(long l)
	{
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
	 * @param minuet
	 *        minute in hour (0..59)
	 * @param second
	 *        second in minute (0..59)
	 * @param millisecond
	 *        millisecond in second (0..999)
	 */
	public ConvertTime(TimeZone zone, int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * construct from time breakdown, and the zone.
	 * 
	 * @param zone
	 *        The time zone.
	 * @param tb
	 *        The TimeBreakdown with the values.
	 */
	public ConvertTime(TimeZone zone, TimeBreakdown tb)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone()
	{
		return new ConvertTime(m_millisecondsSince);
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringSql()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocal()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtFull()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalFull()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalFullZ()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtShort()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalShort()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtTime()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalTime()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalTimeZ()
	{

		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalTime24()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringGmtDate()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalDate()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringLocalShortDate()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}


	/**
	 * {@inheritDoc}
	 */
	public String toStringRFC822Local()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toStringFilePath()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
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
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean after(Time other)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}


	/**
	 * {@inheritDoc}
	 */
	public TimeBreakdown breakdownGmt()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public TimeBreakdown breakdownLocal()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplay()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}
}
