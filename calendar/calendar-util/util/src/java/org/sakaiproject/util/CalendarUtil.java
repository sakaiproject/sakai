/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.util.Calendar;

/**
* <p>CalendarUtil is a bunch of utility methods added to a java Calendar object.</p>
*/
public class CalendarUtil
{	
	/** The calendar object this is based upon. */
	Calendar m_calendar = null;

	/**
	* Construct.
	*/
	public CalendarUtil() 
	{
		 m_calendar = Calendar.getInstance();						  

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
	
	
	public void prevDate()
	{
		m_calendar.set (Calendar.DAY_OF_MONTH, getDayOfMonth() -1);
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
	* Get the day of the week.
	* @return the day of the week.
	*/
	public int getDay_Of_Week() 
	{
		return m_calendar.get(Calendar.DAY_OF_WEEK);

	}	//. getDay_Of_Week

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
		return getMonthInteger() + "/" + getDayOfMonth() + "/" +  getYear();

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
		
		return (getDay_Of_Week() - 1);		

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

}   // CalendarUtil



