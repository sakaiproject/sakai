/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class provides filtering (currently only by date) for use
 * in the CalendarAction list view.
 */
class CalendarFilter
{
	
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("calendar");
	
	/**
	 * Used to parse date entered by the user. This format should really be internationalized.
	 */
	public final static String LIST_VIEW_DATE_FORMAT_PARSE = "MM/dd/yy";

	/** 
	 * This is the same format, but in a form that is used to suggest the correct format to enter dates.
	 */
	public final static String LIST_VIEW_DATE_FORMAT_DISPLAY = "mm/dd/yy";

	/**
	 * Number of years that we want to cover in the list view.
	 */
	private static final int LIST_VIEW_YEAR_RANGE = 8;

	/**
	 * Format object to go along with the above format string.
	 */
	private final static SimpleDateFormat LIST_VIEW_DATE_FORMAT =
		new SimpleDateFormat(LIST_VIEW_DATE_FORMAT_PARSE);

	/**
	 * The end year is the current date plus half the range.
	 */
	public static final int LIST_VIEW_ENDING_YEAR =
		TimeService.newTime().breakdownLocal().getYear()
			+ LIST_VIEW_YEAR_RANGE / 2;

	/**
	 * The start year is the current date minus half the range.
	 */
	public static final int LIST_VIEW_STARTING_YEAR =
		TimeService.newTime().breakdownLocal().getYear()
			- LIST_VIEW_YEAR_RANGE / 2;

	/** Mode to show all future events */
	public final static String SHOW_FUTURE_RANGE = "SHOW_FUTURE";

	/** Mode to show the default range of dates */
	public final static String SHOW_ALL_RANGE = "SHOW_ALL";
	
	/** Mode to show ranges of dates specified by the user */
	public final static String SHOW_CUSTOM_RANGE = "SHOW_CUSTOM_RANGE";
	
	/** Mode to show only events for today. */
	public final static String SHOW_DAY = "SHOW_DAY";
	
	/** Mode to show only events for this month. */
	public final static String SHOW_MONTH = "SHOW_MONTH";
	
	/** Mode to show only events for this week. */
	public final static String SHOW_WEEK = "SHOW_WEEK";
	
	/** Mode to only show events for this year. */
	public final static String SHOW_YEAR = "SHOW_YEAR";
	
	private Time startingListViewDate;
	private Time endingListViewDate;
	
	private final static int LIST_VIEW_ENDING_DAY = 31;
	private final static int LIST_VIEW_ENDING_MONTH = 12;

	// Default start/end dates for the list view.
	private final static int LIST_VIEW_STARTING_DAY = 1;
	private final static int LIST_VIEW_STARTING_MONTH = 1;
	private boolean listViewDatesCustomized = false;

	// Default to showing all events within the default range.
	private String listViewFilterMode = SHOW_ALL_RANGE;	

	/**
	 * Filter events based on the custom date range
	 */
	public Iterator filterEvents(Iterator it)
	{
		// Don't filter if none of our filters are enabled.
		if (SHOW_ALL_RANGE.equals(getListViewFilterMode()))
		{
			return it;
		}
		else
		{
			// pull the range of events from vector
			Vector events = new Vector();
			TimeRange filterTimeRange = this.getListViewTimeRange();

			while (it.hasNext())
			{
				CalendarEvent test = (CalendarEvent) it.next();

				if (filterTimeRange.overlaps(test.getRange()))
				{
					events.add(test);
				}
			}

			return events.iterator();
		}
	}

	/**
	 * Get a string representing the end date.
	 */
	synchronized public String getEndingListViewDateString()
	{
		LIST_VIEW_DATE_FORMAT.setTimeZone(TimeService.getLocalTimeZone());

		return LIST_VIEW_DATE_FORMAT.format(
			new Date(endingListViewDate.getTime()));
	}

	/**
	 * Get the ending year for the list view.
	 */
	public int getEndingListViewYear()
	{
		GregorianCalendar cal =
			new GregorianCalendar(TimeService.getLocalTimeZone());

		cal.setTime(new Date(endingListViewDate.getTime()));

		return cal.get(GregorianCalendar.YEAR);
	}

	/**
	 * Get the list view ending date as a Time object.
	 */
	public Time getListViewEndingTime()
	{
		return endingListViewDate;
	}

	
	public String getListViewFilterMode()
	{
		return listViewFilterMode;
	}

	/**
	 * Get the list view starting date as a Time object.
	 */
	public Time getListViewStartingTime()
	{
		return startingListViewDate;
	}

	/**
	 * Get a TimeRange object for the list view start/end dates.
	 */
	public TimeRange getListViewTimeRange()
	{
		return TimeService.newTimeRange(
			this.startingListViewDate,
			this.endingListViewDate);
	}

	/**
	 * Get a string representing the start date.
	 */
	synchronized public String getStartingListViewDateString()
	{
		LIST_VIEW_DATE_FORMAT.setTimeZone(TimeService.getLocalTimeZone());

		return LIST_VIEW_DATE_FORMAT.format(
			new Date(startingListViewDate.getTime()));
	}

	/**
	 * Get the starting year for the list view.
	 */
	public int getStartingListViewYear()
	{
		GregorianCalendar cal =
			new GregorianCalendar(TimeService.getLocalTimeZone());

		cal.setTime(new Date(startingListViewDate.getTime()));

		return cal.get(GregorianCalendar.YEAR);
	}

	/**
	 * Returns true if this starting/ending for the list view have been customized.
	 */
	public boolean isCustomListViewDates()
	{
		return listViewDatesCustomized;
	}

	/**
	 * Reset the start/end dates to their default values. 
	 */
	public void setListViewDateRangeToDefault()
	{
		setStartAndEndListViewDates(
			LIST_VIEW_STARTING_YEAR,
			LIST_VIEW_STARTING_MONTH,
			LIST_VIEW_STARTING_DAY,
			LIST_VIEW_ENDING_YEAR,
			LIST_VIEW_ENDING_MONTH,
			LIST_VIEW_ENDING_DAY);
	}

	
	public void setListViewFilterMode(String mode)
	{
		boolean validMode = true;
		int startYear = 0,
			startMonth = 0,
			startDay = 0,
			endYear = 0,
			endMonth = 0,
			endDay = 0;

		if (SHOW_ALL_RANGE.equals(mode))
		{
			// Use all the default values.
			startYear = LIST_VIEW_STARTING_YEAR;
			endYear = LIST_VIEW_ENDING_YEAR;

			startMonth = LIST_VIEW_STARTING_MONTH;
			endMonth = LIST_VIEW_ENDING_MONTH;

			startDay = LIST_VIEW_STARTING_DAY;
			endDay = LIST_VIEW_ENDING_DAY;
		}
		else if (SHOW_FUTURE_RANGE.equals(mode))
		{
			// Start on current day, month, year but end on default
			startYear = Calendar.getInstance().get(Calendar.YEAR);
			endYear = LIST_VIEW_ENDING_YEAR;

			startMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
			endMonth = LIST_VIEW_ENDING_MONTH;

			startDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			endDay = LIST_VIEW_ENDING_DAY;
		}
        else
		if (SHOW_DAY.equals(mode))
		{
			// To show the day, we only use one date for start/end.
			TimeBreakdown breakDown =
				TimeService.newTime().breakdownLocal();

			startYear = breakDown.getYear();
			endYear = startYear;

			startMonth = breakDown.getMonth();
			endMonth = startMonth;

			startDay = breakDown.getDay();
			endDay = startDay;
		}
		else
		if (SHOW_WEEK.equals(mode))
		{
			GregorianCalendar calStart =
				new GregorianCalendar(TimeService.getLocalTimeZone(), rb.getLocale());
			GregorianCalendar calEnd =
				(GregorianCalendar) calStart.clone();

			// Set the first/last days of the week.
			calStart.set(GregorianCalendar.DAY_OF_WEEK, calStart.getFirstDayOfWeek());
			calEnd.set(GregorianCalendar.DAY_OF_WEEK, calStart.getFirstDayOfWeek()+6);

			startYear = calStart.get(GregorianCalendar.YEAR);
			endYear = calEnd.get(GregorianCalendar.YEAR);

			startMonth = calStart.get(GregorianCalendar.MONTH) + 1;
			endMonth = calEnd.get(GregorianCalendar.MONTH) + 1;

			startDay = calStart.get(GregorianCalendar.DAY_OF_MONTH);
			endDay = calEnd.get(GregorianCalendar.DAY_OF_MONTH);
		}
		else
		if (SHOW_MONTH.equals(mode))
		{
			GregorianCalendar calStart =
				new GregorianCalendar(
					TimeService.getLocalTimeZone());
			GregorianCalendar calEnd =
				(GregorianCalendar) calStart.clone();

			// Set the first/last days of the month.
			calStart.set(GregorianCalendar.DAY_OF_MONTH, 1);
			calEnd.set(
				GregorianCalendar.DAY_OF_MONTH,
				calStart.getMaximum(
					GregorianCalendar.DAY_OF_MONTH));

			startYear = calStart.get(GregorianCalendar.YEAR);
			endYear = calEnd.get(GregorianCalendar.YEAR);

			startMonth = calStart.get(GregorianCalendar.MONTH) + 1;
			endMonth = calEnd.get(GregorianCalendar.MONTH) + 1;

			startDay = calStart.get(GregorianCalendar.DAY_OF_MONTH);
			endDay = calEnd.get(GregorianCalendar.DAY_OF_MONTH);
		}
		else
		if (SHOW_YEAR.equals(mode))
		{
			TimeBreakdown breakDown =
				TimeService.newTime().breakdownLocal();

			startYear = breakDown.getYear();
			endYear = startYear;

			startMonth = 1;
			endMonth = 12;

			startDay = 1; // January 1
			endDay = 31; // December 31st
		}
		else
		if (SHOW_CUSTOM_RANGE.equals(mode))
		{
			TimeBreakdown startBreakdown =
				startingListViewDate.breakdownLocal();
			TimeBreakdown endBreakdown =
				endingListViewDate.breakdownLocal();

			startYear = startBreakdown.getYear();
			endYear = endBreakdown.getYear();

			startMonth = startBreakdown.getMonth();
			endMonth = endBreakdown.getMonth();

			startDay = startBreakdown.getDay();
			endDay = endBreakdown.getDay();
		}
		else
		{
			validMode = false;
		}

		if (validMode)
		{
			listViewFilterMode = mode;

			setStartAndEndListViewDates(
				startYear,
				startMonth,
				startDay,
				endYear,
				endMonth,
				endDay);
		}
	}

	/**
	 * Set the start/end year, month, and day.  The time portion of the
	 * starting and ending days is automatically supplied to ensure that
	 * we will get full days of events.
	 */
	private void setStartAndEndListViewDates(
		int startYear,
		int startMonth,
		int startDay,
		int endYear,
		int endMonth,
		int endDay)
	{
		startingListViewDate =
			TimeService.newTimeLocal(
				startYear,
				startMonth,
				startDay,
				0,
				0,
				0,
				0);

		endingListViewDate =
			TimeService.newTimeLocal(endYear, endMonth, endDay, 23, 59, 59, 99);
	}

	/**
	 * Set the start/end dates from strings.  Format an error message and return false if there is a problem.
	 */
	synchronized public boolean setStartAndEndListViewDates(
		String startingDateStr,
		String endingDateStr,
		StringBuilder errorMessage)
	{
		Date startDate, endDate;

		LIST_VIEW_DATE_FORMAT.setTimeZone(TimeService.getLocalTimeZone());

		try
		{
			startDate = LIST_VIEW_DATE_FORMAT.parse(startingDateStr);
			endDate = LIST_VIEW_DATE_FORMAT.parse(endingDateStr);
		}
		catch (ParseException e)
		{
			errorMessage.append(rb.getString("java.alert.invalid"));
			return false;
		}

		// Do a sanity check
		if (startDate.after(endDate))
		{
			errorMessage.append(rb.getString("java.alert.start"));
			return false;
		}

		// Use Gregorian calendars to pick out the year, month, and day.
		GregorianCalendar calStart =
			new GregorianCalendar(TimeService.getLocalTimeZone());

		GregorianCalendar calEnd =
			new GregorianCalendar(TimeService.getLocalTimeZone());

		calStart.setTime(startDate);
		calEnd.setTime(endDate);

		startingListViewDate =
			TimeService.newTimeLocal(
				calStart.get(GregorianCalendar.YEAR),
				calStart.get(GregorianCalendar.MONTH) + 1,
				calStart.get(GregorianCalendar.DAY_OF_MONTH),
				0,
				0,
				0,
				0);

		endingListViewDate =
			TimeService.newTimeLocal(
				calEnd.get(GregorianCalendar.YEAR),
				calEnd.get(GregorianCalendar.MONTH) + 1,
				calEnd.get(GregorianCalendar.DAY_OF_MONTH),
				23,
				59,
				59,
				99);

		// Set a flag to indicate that we've modified the defaults
		// and to switch our viewing mode.
		listViewDatesCustomized = true;

		listViewFilterMode = SHOW_CUSTOM_RANGE;

		return true;
	}
}
