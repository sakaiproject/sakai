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

import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

/**
 * @author ieb
 *
 */
public class ConversionTimeService implements TimeService
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#clearLocalTimeZone(java.lang.String)
	 */
	public boolean clearLocalTimeZone(String userId)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#different(org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time)
	 */
	public boolean different(Time a, Time b)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#getCalendar(java.util.TimeZone, int, int, int, int, int, int, int)
	 */
	public GregorianCalendar getCalendar(TimeZone zone, int year, int month, int day,
			int hour, int min, int sec, int ms)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#getLocalTimeZone()
	 */
	public TimeZone getLocalTimeZone()
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTime()
	 */
	public Time newTime()
	{
		return new ConvertTime();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTime(long)
	 */
	public Time newTime(long value)
	{
		return new ConvertTime(value);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTime(java.util.GregorianCalendar)
	 */
	public Time newTime(GregorianCalendar cal)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeBreakdown(int, int, int, int, int, int, int)
	 */
	public TimeBreakdown newTimeBreakdown(int year, int month, int day, int hour,
			int minute, int second, int millisecond)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeGmt(java.lang.String)
	 */
	public Time newTimeGmt(String value)
	{
		return new ConvertTime(value);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeGmt(int, int, int, int, int, int, int)
	 */
	public Time newTimeGmt(int year, int month, int day, int hour, int minute,
			int second, int millisecond)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeGmt(org.sakaiproject.time.api.TimeBreakdown)
	 */
	public Time newTimeGmt(TimeBreakdown breakdown)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeLocal(int, int, int, int, int, int, int)
	 */
	public Time newTimeLocal(int year, int month, int day, int hour, int minute,
			int second, int millisecond)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeLocal(org.sakaiproject.time.api.TimeBreakdown)
	 */
	public Time newTimeLocal(TimeBreakdown breakdown)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time, boolean, boolean)
	 */
	public TimeRange newTimeRange(Time start, Time end, boolean startIncluded,
			boolean endIncluded)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(java.lang.String)
	 */
	public TimeRange newTimeRange(String value)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(org.sakaiproject.time.api.Time)
	 */
	public TimeRange newTimeRange(Time startAndEnd)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(long, long)
	 */
	public TimeRange newTimeRange(long start, long duration)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time)
	 */
	public TimeRange newTimeRange(Time start, Time end)
	{
		throw new UnsupportedOperationException("This class is only to be used for conversion purposes");
	}

}
