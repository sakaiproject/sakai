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
import java.time.Clock;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
* <p>CalendarUtil is a bunch of utility methods added to a java Calendar object.</p>
*/
public class CalendarUtil
{
	private Clock clock = Clock.systemDefaultZone();
	
	/** The calendar object this is based upon. */
	Calendar m_calendar = null;
	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	ResourceLoader rb;

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
	private Map<String, String> eventIconMap = new HashMap<String, String>();

	public final static String NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID = "new_assignment_duedate_calendar_assignment_id";
	/**
	* Construct.
	*/
	public CalendarUtil()
	{
		rb = new ResourceLoader("calendar");
		Locale locale = rb.getLocale();
		m_calendar = Calendar.getInstance(locale);
		initDates();

	}	// CalendarUtil
	
	/**
	* Construct.
	*/
	public CalendarUtil(Calendar calendar) 
	{
		rb = new ResourceLoader("calendar");
		m_calendar = calendar;
		initDates();
		
	}	// CalendarUtil

	/**
	 * Constructor for testing.
	 * @param clock the clock to use for the current time.
	 */
	public CalendarUtil(Clock clock, ResourceLoader rb)
	{
		this.clock = clock;
		this.rb = rb;
		m_calendar = getCalendarInstance();
		initDates();
	}

	/**
	 * Constructor for testing.
	 */
	public CalendarUtil(Calendar calendar, ResourceLoader rb)
	{
		this.rb = rb;
		m_calendar = calendar;
		initDates();
	}

	public CalendarUtil(ResourceLoader rb) {
		Locale locale = rb.getLocale();
		m_calendar = Calendar.getInstance(locale);
		initDates();
	}

	/**
	 * This creates a calendar based on the clock. This is to allow testing of the class.
	 * @return A calendar.
	 */
	private Calendar getCalendarInstance() {
		Calendar instance = Calendar.getInstance();
		instance.setTime(Date.from(clock.instant()));
		return instance;
	}

	void initDates() {
	  Calendar calendarSunday = getCalendarInstance();
	  Calendar calendarMonday = getCalendarInstance();
	  Calendar calendarTuesday = getCalendarInstance();
	  Calendar calendarWednesday = getCalendarInstance();
	  Calendar calendarThursday = getCalendarInstance();
	  Calendar calendarFriday = getCalendarInstance();
	  Calendar calendarSaturday = getCalendarInstance();

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

	  // Previously Calendar was used, but it had problems getting the month right
	  // when the current day of the month was 31.
	  YearMonth currentYearMonth = YearMonth.now(clock);
	  YearMonth jan = currentYearMonth.with(Month.JANUARY);
	  YearMonth feb = currentYearMonth.with(Month.FEBRUARY);
	  YearMonth mar = currentYearMonth.with(Month.MARCH);
	  YearMonth apr = currentYearMonth.with(Month.APRIL);
	  YearMonth may = currentYearMonth.with(Month.MAY);
	  YearMonth jun = currentYearMonth.with(Month.JUNE);
	  YearMonth jul = currentYearMonth.with(Month.JULY);
	  YearMonth aug = currentYearMonth.with(Month.AUGUST);
	  YearMonth sep = currentYearMonth.with(Month.SEPTEMBER);
	  YearMonth oct = currentYearMonth.with(Month.OCTOBER);
	  YearMonth nov = currentYearMonth.with(Month.NOVEMBER);
	  YearMonth dec = currentYearMonth.with(Month.DECEMBER);

	  dateJanuary = getDateFromYearMonth(jan);
	  dateFebruary = getDateFromYearMonth(feb);
	  dateMarch = getDateFromYearMonth(mar);
	  dateApril = getDateFromYearMonth(apr);
	  dateMay = getDateFromYearMonth(may);
	  dateJune = getDateFromYearMonth(jun);
	  dateJuly = getDateFromYearMonth(jul);
	  dateAugust = getDateFromYearMonth(aug);
	  dateSeptember = getDateFromYearMonth(sep);
	  dateOctober = getDateFromYearMonth(oct);
	  dateNovember = getDateFromYearMonth(nov);
	  dateDecember = getDateFromYearMonth(dec);

	}

	private Date getDateFromYearMonth(YearMonth ym) {
        return Date.from(ym.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
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
	* Set the calendar to the next year
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
	* Set the calendar to the prev year.
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
	* Set the calendar to the next week
	*/
	public void setNextWeek()
	{
		m_calendar.set(Calendar.WEEK_OF_MONTH,getWeekOfMonth()+1);

	}	// setNextWeek

	/**
	* Set the calendar to the prev week
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
		return m_calendar.get(Calendar.DAY_OF_MONTH);

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
		String[] months;
		
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
		
		String[] weekDays;
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

	// Non-static event type methods to get localized event names
	public Map<String, String> getLocalizedEventTypes() {
		Map<String, String> eventLegends = CalendarEventType.getLocalizedLegends();
		Set<Map.Entry<String, String>> set = eventLegends.entrySet();

		Map<String, String> localizedEventTypes = new HashMap<>();
		
		for (Map.Entry<String, String> me : set){
			localizedEventTypes.put(me.getKey(), rb.getString(me.getValue()));
		}
		Map sortedLocalizedEventTypes = sortByValue(localizedEventTypes);
		return sortedLocalizedEventTypes;
	}

	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public Map<String, String> getLocalizedEventTypesAndIcons() {
		Map<String, String> icons = CalendarEventType.getIcons();
		Map<String, String> localizedEventTypesAndIcons = new TreeMap<>();

		for (String eventType: icons.keySet()) {
			// Localized event types put first so that they can be sorted in the code below.
			localizedEventTypesAndIcons.put(getLocalizedEventType(eventType), icons.get(eventType));
		}
		return localizedEventTypesAndIcons;
	}

	public String getLocalizedEventType(String eventType) {
		return rb.getString(CalendarEventType.getLocalizedLegendFromEventType(eventType));
	}
}	 // CalendarUtil
