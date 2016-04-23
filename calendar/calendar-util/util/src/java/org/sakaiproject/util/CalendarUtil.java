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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
* <p>CalendarUtil is a bunch of utility methods added to a java Calendar object.</p>
*/
public class CalendarUtil
{	
	/** Our logger. */
	private static Logger M_log = LoggerFactory.getLogger(CalendarUtil.class);
	
	/** The calendar object this is based upon. */
	Calendar m_calendar = null;
	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	ResourceLoader rb = new ResourceLoader("calendar");

	Date dateSunday = null;
	Date dateMonday = null;
	Date dateTuesday = null;
	Date dateWednesday = null;
	Date dateThursday = null;
	Date dateFriday = null;
	Date dateSaturday = null;

	Date dateJanuary = null;
	Date dateFebruary = null;
	Date dateMarch = null;
	Date dateApril = null;
	Date dateMay = null;
	Date dateJune = null;
	Date dateJuly = null;
	Date dateAugust = null;
	Date dateSeptember = null;
	Date dateOctober = null;
	Date dateNovember = null;
	Date dateDecember = null;

	public final static String NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID = "new_assignment_duedate_calendar_assignment_id";
	
	/**
	* Construct.
	*/
	public CalendarUtil() 
	{
		Locale locale = rb.getLocale();
		m_calendar = Calendar.getInstance(locale);
		initDates();
		
	}	// CalendarUtil
	
	/**
	* Construct.
	*/
	public CalendarUtil(Calendar calendar) 
	{
		m_calendar = calendar;
		initDates();
		
	}	// CalendarUtil

	void initDates() {
	  Calendar calendarSunday = Calendar.getInstance();
	  Calendar calendarMonday = Calendar.getInstance();
	  Calendar calendarTuesday = Calendar.getInstance();
	  Calendar calendarWednesday = Calendar.getInstance();
	  Calendar calendarThursday = Calendar.getInstance();
	  Calendar calendarFriday = Calendar.getInstance();
	  Calendar calendarSaturday = Calendar.getInstance();

	  calendarSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	  calendarMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	  calendarTuesday.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
	  calendarWednesday.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
	  calendarThursday.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
	  calendarFriday.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
	  calendarSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

	  dateSunday = calendarSunday.getTime();
	  dateMonday = calendarMonday.getTime();
	  dateTuesday = calendarTuesday.getTime();
	  dateWednesday = calendarWednesday.getTime();
	  dateThursday = calendarThursday.getTime();
	  dateFriday = calendarFriday.getTime();
	  dateSaturday = calendarSaturday.getTime();

	  Calendar calendarJanuary = Calendar.getInstance();
	  Calendar calendarFebruary = Calendar.getInstance();
	  Calendar calendarMarch = Calendar.getInstance();
	  Calendar calendarApril = Calendar.getInstance();
	  Calendar calendarMay = Calendar.getInstance();
	  Calendar calendarJune = Calendar.getInstance();
	  Calendar calendarJuly = Calendar.getInstance();
	  Calendar calendarAugust = Calendar.getInstance();
	  Calendar calendarSeptember = Calendar.getInstance();
	  Calendar calendarOctober = Calendar.getInstance();
	  Calendar calendarNovember = Calendar.getInstance();
	  Calendar calendarDecember = Calendar.getInstance();

	  calendarJanuary.set(Calendar.MONTH, Calendar.JANUARY); 
	  calendarFebruary.set(Calendar.MONTH, Calendar.FEBRUARY); 
	  calendarMarch.set(Calendar.MONTH, Calendar.MARCH); 
	  calendarApril.set(Calendar.MONTH, Calendar.APRIL); 
	  calendarMay.set(Calendar.MONTH, Calendar.MAY); 
	  calendarJune.set(Calendar.MONTH, Calendar.JUNE); 
	  calendarJuly.set(Calendar.MONTH, Calendar.JULY); 
	  calendarAugust.set(Calendar.MONTH, Calendar.AUGUST); 
	  calendarSeptember.set(Calendar.MONTH, Calendar.SEPTEMBER); 
	  calendarOctober.set(Calendar.MONTH, Calendar.OCTOBER); 
	  calendarNovember.set(Calendar.MONTH, Calendar.NOVEMBER); 
	  calendarDecember.set(Calendar.MONTH, Calendar.DECEMBER); 

	  dateJanuary = calendarJanuary.getTime();
	  dateFebruary = calendarFebruary.getTime();
	  dateMarch = calendarMarch.getTime();
	  dateApril = calendarApril.getTime();
	  dateMay = calendarMay.getTime();
	  dateJune = calendarJune.getTime();
	  dateJuly = calendarJuly.getTime();
	  dateAugust = calendarAugust.getTime();
	  dateSeptember = calendarSeptember.getTime();
	  dateOctober = calendarOctober.getTime();
	  dateNovember = calendarNovember.getTime();
	  dateDecember = calendarDecember.getTime();

	}

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
		// set day to first of month
		m_calendar.set(Calendar.DAY_OF_MONTH,1);
		
		// set to next month
		m_calendar.add(Calendar.MONTH, 1);
		
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
	public String[] getCalendarMonthNames(boolean longNames) {

		Locale currentLocale = rb.getLocale();
		String[] months = null; 
		
		if (longNames) {

		  SimpleDateFormat longMonth = new SimpleDateFormat("MMMM", currentLocale);

		  months = new String[] {
	  		longMonth.format(dateJanuary),
	  		longMonth.format(dateFebruary),
	  		longMonth.format(dateMarch),
	  		longMonth.format(dateApril),
	 		longMonth.format(dateMay),
	  		longMonth.format(dateJune),
	  		longMonth.format(dateJuly),
	  		longMonth.format(dateAugust),
	  		longMonth.format(dateSeptember),
	  		longMonth.format(dateOctober),
	  		longMonth.format(dateNovember),
	  		longMonth.format(dateDecember)
		  };

		  return months;

		}

		SimpleDateFormat shortMonth = new SimpleDateFormat("MMM", currentLocale);

		months = new String[] {
	  		shortMonth.format(dateJanuary),
	  		shortMonth.format(dateFebruary),
	  		shortMonth.format(dateMarch),
	  		shortMonth.format(dateApril),
	 		shortMonth.format(dateMay),
	  		shortMonth.format(dateJune),
	  		shortMonth.format(dateJuly),
	  		shortMonth.format(dateAugust),
	  		shortMonth.format(dateSeptember),
	  		shortMonth.format(dateOctober),
	  		shortMonth.format(dateNovember),
	  		shortMonth.format(dateDecember)
		};

		return months;

	}

	public String getDayOfWeekName(int index) {
	  Locale currentLocale = rb.getLocale();
	  SimpleDateFormat longDay = new SimpleDateFormat("EEEE", currentLocale);

	  switch(index) {
	    case 0:
	      return longDay.format(dateSunday);
	    case 1:
	      return longDay.format(dateMonday);
	    case 2:
	      return longDay.format(dateTuesday);
	    case 3:
	      return longDay.format(dateWednesday);
	    case 4:
	      return longDay.format(dateThursday);
	    case 5:
	      return longDay.format(dateFriday);
	    case 6:
	      return longDay.format(dateSaturday);
	  }

	  return null;
	}

	/** Returns array of weekday names, using the locale-specific first day-of-week
    ** 
    ** @param longNames indicates whether to use short or long version of weekday names
	 **/
	public String[] getCalendarDaysOfWeekNames(boolean longNames)
	{
		int firstDayOfWeek = getFirstDayOfWeek();

		Locale currentLocale = rb.getLocale();
		SimpleDateFormat longDay = new SimpleDateFormat("EEEE", currentLocale);
		SimpleDateFormat shortDay = new SimpleDateFormat("EEE", currentLocale);
		
		String[] weekDays = null; 
		String[] longWeekDays = new String[] 
		{
			longDay.format(dateSunday),
			longDay.format(dateMonday),
			longDay.format(dateTuesday),
			longDay.format(dateWednesday),
			longDay.format(dateThursday),
			longDay.format(dateFriday),
			longDay.format(dateSaturday)
		};
		String[] shortWeekDays = new String[] 
		{
			shortDay.format(dateSunday),
			shortDay.format(dateMonday),
			shortDay.format(dateTuesday),
			shortDay.format(dateWednesday),
			shortDay.format(dateThursday),
			shortDay.format(dateFriday),
			shortDay.format(dateSaturday)
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
	
	public Date getTime(){
		return m_calendar.getTime();
	}
	public Date getPrevTime(int days){
		setPrevDate(days);
		return m_calendar.getTime();

	}
	
	/**
	 * Get the String representing AM in the users Locale 
	 * @return A String representing the morning for the current user.
	 */
	public static String getLocalAMString() {
		return getLocalAMString(new DateTime());
	}

	// Used for tests
	static String getLocalAMString(DateTime now) {
		//we need an AM date
		DateTime dt = now.withTimeAtStartOfDay();
		Locale locale= new ResourceLoader("calendar").getLocale();
		DateTimeFormatter df = new DateTimeFormatterBuilder().appendHalfdayOfDayText().toFormatter().withLocale(locale);
		return df.print(dt);
	}
	
	/**
	 * Get the string representing PM in the users Locale
	 * @return A String representing the afternoon for the current user.
	 */
	public static String getLocalPMString() {
		return getLocalPMString(new DateTime());
	}

	// Used for tests
	static String getLocalPMString(DateTime now) {
		//we need an PM date
		DateTime dt = now.withTimeAtStartOfDay().plusHours(14);
		Locale locale = new ResourceLoader("calendar").getLocale();
		DateTimeFormatter df = new DateTimeFormatterBuilder().appendHalfdayOfDayText().toFormatter().withLocale(locale);
		return df.print(dt);
	}
	
}	 // CalendarUtil
