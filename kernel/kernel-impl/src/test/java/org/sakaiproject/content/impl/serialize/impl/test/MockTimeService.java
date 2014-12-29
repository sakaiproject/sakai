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

package org.sakaiproject.content.impl.serialize.impl.test;

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
public class MockTimeService implements TimeService
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#clearLocalTimeZone(java.lang.String)
	 */
	public boolean clearLocalTimeZone(String userId)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#different(org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time)
	 */
	public boolean different(Time a, Time b)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#getCalendar(java.util.TimeZone, int, int, int, int, int, int, int)
	 */
	public GregorianCalendar getCalendar(TimeZone zone, int year, int month, int day,
			int hour, int min, int sec, int ms)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#getLocalTimeZone()
	 */
	public TimeZone getLocalTimeZone()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTime()
	 */
	public Time newTime()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTime(long)
	 */
	public Time newTime(long value)
	{
		return new MockTime(value);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTime(java.util.GregorianCalendar)
	 */
	public Time newTime(GregorianCalendar cal)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeBreakdown(int, int, int, int, int, int, int)
	 */
	public TimeBreakdown newTimeBreakdown(int year, int month, int day, int hour,
			int minute, int second, int millisecond)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeGmt(java.lang.String)
	 */
	public Time newTimeGmt(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeGmt(int, int, int, int, int, int, int)
	 */
	public Time newTimeGmt(int year, int month, int day, int hour, int minute,
			int second, int millisecond)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeGmt(org.sakaiproject.time.api.TimeBreakdown)
	 */
	public Time newTimeGmt(TimeBreakdown breakdown)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeLocal(int, int, int, int, int, int, int)
	 */
	public Time newTimeLocal(int year, int month, int day, int hour, int minute,
			int second, int millisecond)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeLocal(org.sakaiproject.time.api.TimeBreakdown)
	 */
	public Time newTimeLocal(TimeBreakdown breakdown)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time, boolean, boolean)
	 */
	public TimeRange newTimeRange(Time start, Time end, boolean startIncluded,
			boolean endIncluded)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(java.lang.String)
	 */
	public TimeRange newTimeRange(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(org.sakaiproject.time.api.Time)
	 */
	public TimeRange newTimeRange(Time startAndEnd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(long, long)
	 */
	public TimeRange newTimeRange(long start, long duration)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.time.api.TimeService#newTimeRange(org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time)
	 */
	public TimeRange newTimeRange(Time start, Time end)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
