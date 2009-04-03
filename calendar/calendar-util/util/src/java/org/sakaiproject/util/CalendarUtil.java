/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Locale;
import org.sakaiproject.util.ResourceLoader;

/**
* <p>CalendarUtil is a bunch of utility methods added to a java Calendar object.</p>
*/
public class CalendarUtil
{	
	/** The calendar object this is based upon. */
	Calendar m_calendar = null;
	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	ResourceLoader rb = new ResourceLoader("calendar");

	/**
	* Construct.
	*/
	public CalendarUtil() 
	{
		Locale locale = rb.getLocale();
		m_calendar = Calendar.getInstance(locale);
		
	}	// CalendarUtil
	
	/**
	* Construct.
	*/
	public CalendarUtil(Calendar calendar) 
	{
		m_calendar = calendar;
		
	}	// CalendarUtil
		
	/**
	* Access the current user.
	* @return the current year.
	*/
	public int getYear() 
	{
		return m_calendar.get(Calendar.YEAR);

	}	// getYear
	
	
	/**
	* Set the calendar to the next day, and return this.
	* @return the next day.
	*/
	public String getNextDate()
	{
		m_calendar.set (Calendar.DAY_OF_MONTH, getDayOfMonth() + 1);
		return getTodayDate ();

	}	// getNextDate
	
	
	public void nextDate()
	{
		m_calendar.set (Calendar.DAY_OF_MONTH, getDayOfMonth() + 1);
	}
	
	
	/**
	* Set the calendar to the prev day, and return this.
	* @return the prev day.
	*/
	public String getPrevDate() 
	{
		m_calendar.set (Calendar.DAY_OF_MONTH, getDayOfMonth() -1);
		return getTodayDate();
	}	// getPrevDate
	
	
	public void setPrevDate(int days)
	{
		m_calendar.set (Calendar.DAY_OF_MONTH, getDayOfMonth() - days);
	}
	
	
	/**
	* Set the calendar to the next month, and return this.
	* @return the next month.
	*/
	public int getNextMonth()
	{
		m_calendar.set(Calendar.MONTH, getMonthInteger());
		
		setDay(getYear(),getMonthInteger(),1);
		
		return getMonthInteger();
	
	}	// getNextMonth

	/**
	* Set the calendar to the next year, and return this.
	* @return the next year.
	*/
	public void setNextYear()
	{
		m_calendar.set(Calendar.YEAR,getYear()+1);
		setDay(getYear(),getMonthInteger(),1);

	}	// setNextYear

	/**
	* Set the calendar to the prev month, and return this.
	* @return the prev month.
	*/
	public int getPrevMonth()
	{
		m_calendar.set(Calendar.MONTH, getMonthInteger()-2);
		
		return (getMonthInteger()-1);

	}	// getPrevMonth	

	/**
	* Set the calendar to the prev year, and return this.
	* @return the prev year.
	*/
	public void setPrevYear()
	{
		m_calendar.set(Calendar.YEAR,getYear()-1);

	}	// setPrevYear
	
	/**
	* Get the day of the week
   *
   * @param useLocale return locale specific day of week
	* e.g. <code>SUNDAY = 1, MONDAY = 2, ...</code> in the U.S.,
	*		 <code>MONDAY = 1, TUESDAY = 2, ...</code> in France. 
	* @return the day of the week.
	*/
	public int getDay_Of_Week( boolean useLocale ) 
	{
		int dayofweek = m_calendar.get(Calendar.DAY_OF_WEEK);
		if ( useLocale )
		{
			if ( dayofweek >= m_calendar.getFirstDayOfWeek() )
				dayofweek = dayofweek - (m_calendar.getFirstDayOfWeek()-Calendar.SUNDAY);
			else
				dayofweek = dayofweek + Calendar.SATURDAY - (m_calendar.getFirstDayOfWeek()-Calendar.SUNDAY);
		}
		return dayofweek;

	}	// getDay_Of_Week

	/**
	* Set the calendar to the next week, and return this.
	* @return the next week.
	*/
	public void setNextWeek()
	{
		m_calendar.set(Calendar.WEEK_OF_MONTH,getWeekOfMonth()+1);

	}	// setNextWeek

	/**
	* Set the calendar to the prev week, and return this.
	* @return the prev week.
	*/
	public void setPrevWeek()
	{
		m_calendar.set(Calendar.WEEK_OF_MONTH,getWeekOfMonth()-1);

	}	// setPrevWeek

	/**
	* Get the day of the week in month.
	* @return the day of week in month.
	*/
	public int getDayOfWeekInMonth() 
	{
		return m_calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);

	}	// getDayOfWeekInMonth

	/**
	* Get the week of month.
	* @return the week of month.
	*/
	public int getWeekOfMonth() 
	{
		return m_calendar.get(Calendar.WEEK_OF_MONTH);

	}	// getWeekOfMonth


	/**
	* Get the month as an int value.
	* @return the month as an int value.
	*/
	public int getMonthInteger() 
	{
		// 1 will be added here to be able to use this func alone to construct e.g adate
		// to get the name of the month from getMonth(), 1 must be deducted.
		return 1+m_calendar.get(Calendar.MONTH);

	}	// getMonthInteger

	/**
	* Get the day of month.
	* @return the day of month.
	*/
	public int getDayOfMonth() 
	{
		int dayofmonth = m_calendar.get(Calendar.DAY_OF_MONTH);
		return dayofmonth;

	}	// getDayOfMonth
	
	/**
	* Get the current date, formatted.
	* @return the current date, formatted.
	*/
	public String getTodayDate() 
	{
		return dateFormat.format(m_calendar.getTime());

	}	// getTodayDate

	/**
	* Get the number of days.
	* @return the number of days.
	*/
	public int getNumberOfDays()
	{
		return m_calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

	}	// getNumberOfDays

	/**
	* Set the calendar to this day.
	* @param d the day.
	*/
	public void setDayOfMonth(int d)
	{
		m_calendar.set(Calendar.DAY_OF_MONTH,d);

	}	// setDayOfMonth
	
	/**
	* Set the calendar to this month
	* @param d the month.
	*/
	public void setMonth(int d)
	{
		m_calendar.set(Calendar.MONTH,d);

	}	// setMonth

	/**
	* Set the calendar to the first day of this month, and return this day of week.
	* @param month The month.
	* @return The calendar's day of week once set to this month.
	*/
	public int getFirstDayOfMonth(int month)
	{
		m_calendar.set(Calendar.MONTH,month);
		m_calendar.set(Calendar.DAY_OF_MONTH,1);
		
		return (getDay_Of_Week(true) - 1);		

	}	// getFirstDayOfMonth

	/**
	* Set the calendar to this day.
	* @param year The year.
	* @param month The month.
	* @param day The day.
	*/
	public void setDay(int year, int month, int day)
	{
		 m_calendar.set(year,month-1,day);

	}	// setDay

	/** Returns array of weekday names, using the locale-specific first day-of-week
    ** 
    ** @param longNames indicates whether to use short or long version of weekday names
	 **/
	public String[] getCalendarDaysOfWeekNames(boolean longNames)
	{
		int firstDayOfWeek = getFirstDayOfWeek();
		
		String[] weekDays = null; 
		String[] longWeekDays = new String[] 
		{
			rb.getString("day.sunday"),
			rb.getString("day.monday"),
			rb.getString("day.tuesday"),
			rb.getString("day.wednesday"),
			rb.getString("day.thursday"),
			rb.getString("day.friday"),
			rb.getString("day.saturday")
		};
		String[] shortWeekDays = new String[] 
		{
			rb.getString("day.sun"),
			rb.getString("day.mon"),
			rb.getString("day.tue"),
			rb.getString("day.wed"),
			rb.getString("day.thu"),
			rb.getString("day.fri"),
			rb.getString("day.sat")
		};
		
		if ( longNames )
			weekDays = longWeekDays;
		else
			weekDays = shortWeekDays;

		String[] localeDays = new String[7];

		for(int col = firstDayOfWeek; col<=7; col++)
			localeDays[col-firstDayOfWeek] = weekDays[col-1];
			
		for (int col = 0; col<firstDayOfWeek-1;col++)
			localeDays[6-col] = weekDays[col];

		return localeDays;
	}
   
	/** Returns the locale-specific first day of the week (numeric)
	 **/
	public int getFirstDayOfWeek()
	{
		return m_calendar.getFirstDayOfWeek();
	}
	
}	 // CalendarUtil
