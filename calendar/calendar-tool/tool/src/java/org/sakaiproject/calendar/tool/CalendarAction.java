/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarConstants;
import org.sakaiproject.calendar.api.CalendarEdit;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarEventVector;
import org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService;
import org.sakaiproject.calendar.api.ExternalSubscriptionDetails;
import org.sakaiproject.calendar.api.OpaqueUrl;
import org.sakaiproject.calendar.api.OpaqueUrlDao;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.calendar.cover.CalendarImporterService;
import org.sakaiproject.calendar.cover.CalendarService;
import org.sakaiproject.calendar.tool.CalendarActionState.LocalEvent;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletStateAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.CalendarChannelReferenceMaker;
import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.CalendarReferenceToChannelConverter;
import org.sakaiproject.util.CalendarUtil;
import org.sakaiproject.util.DateFormatterUtil;
import org.sakaiproject.util.EntryProvider;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * The schedule tool.
 */
@Slf4j
public class CalendarAction
extends VelocityPortletStateAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8571818334710261359L;

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("calendar");

	private static final String ALERT_MSG_KEY = "alertMessage";
	
	private static final String CONFIRM_IMPORT_WIZARD_STATE = "CONFIRM_IMPORT";
	private static final String WIZARD_IMPORT_FILE = "importFile";
	private static final String GENERIC_SELECT_FILE_IMPORT_WIZARD_STATE = "GENERIC_SELECT_FILE";
	private static final String OTHER_SELECT_FILE_IMPORT_WIZARD_STATE = "OTHER_SELECT_FILE";
	private static final String ICAL_SELECT_FILE_IMPORT_WIZARD_STATE = "ICAL_SELECT_FILE";
	private static final String WIZARD_IMPORT_TYPE = "importType";
	private static final String SELECT_TYPE_IMPORT_WIZARD_STATE = "SELECT_TYPE";
	private static final String IMPORT_WIZARD_SELECT_TYPE_STATE = SELECT_TYPE_IMPORT_WIZARD_STATE;
	private static final String STATE_SCHEDULE_IMPORT = "scheduleImport";
	private static final String CALENDAR_INIT_PARAMETER = "calendar";
	private static final String LIST_VIEW = "list";
	private static final String WEEK_VIEW = "week";
	private static final String DAY_VIEW = "day";
	private static final String MONTH_VIEW = "month";
	
	private static final String STATE_YEAR = "calYear";
	private static final String STATE_MONTH = "calMonth";
	private static final String STATE_DAY = "calDay";
	
	private static final String STATE_SET_FREQUENCY = "setFrequency";
	private static final String FREQUENCY_SELECT = "frequencySelect";
	private static final String TEMP_FREQ_SELECT = "tempFrequencySelect";
	private static final String FREQ_ONCE = "once";
	private static final String DEFAULT_FREQ = FREQ_ONCE;
	
	private static final String SSTATE__RECURRING_RULE = "rule";
	private static final String STATE_BEFORE_SET_RECURRENCE = "state_before_set_recurrence";
	
	private final static String TIME_FILTER_OPTION_VAR = "timeFilterOption";
	private final static String TIME_FILTER_SETTING_CUSTOM_START_YEAR = "customStartYear";
	private final static String TIME_FILTER_SETTING_CUSTOM_END_YEAR = "customEndYear";
	private final static String TIME_FILTER_SETTING_CUSTOM_START_MONTH = "customStartMonth";
	private final static String TIME_FILTER_SETTING_CUSTOM_END_MONTH = "customEndMonth";
	private final static String TIME_FILTER_SETTING_CUSTOM_START_DAY = "customStartDay";
	private final static String TIME_FILTER_SETTING_CUSTOM_END_DAY = "customEndDay";
	
	private static final String FORM_ALIAS			= "alias";
	private static final String FORM_ICAL_ENABLE = "icalEnable";
	private static final String ICAL_EXTENSION = ".ics";

	/** state selected view */
	private static final String STATE_SELECTED_VIEW = "state_selected_view";
	
	/** DELIMITER used to separate the list of custom fields for this calendar. */
	private final static String ADDFIELDS_DELIMITER = "_,_";

	protected final static String STATE_INITED = "calendar.state.inited";
	
	/** for sorting in list view */
	private static final String STATE_DATE_SORT_DSC = "dateSortedDsc";
	
	// for group/section awareness
	private final static String STATE_SCHEDULE_TO = "scheduleTo";
	private final static String STATE_SCHEDULE_TO_GROUPS = "scheduleToGroups";
	
	private ContentHostingService contentHostingService;
   
	private EntityBroker entityBroker;

	// Dependency: setup in init
	private SessionManager sessionManager;

	// Dependency: setup in init
	private OpaqueUrlDao opaqueUrlDao;

	private ExternalCalendarSubscriptionService externalCalendarSubscriptionService;

	private AliasService aliasService;
	
	private TaskService taskService;
   
	// tbd fix shared definition from org.sakaiproject.assignment.api.AssignmentEntityProvider
	private final static String ASSN_ENTITY_ID     = "assignment";
	private final static String ASSN_ENTITY_ACTION = "deepLink";
	private final static String ASSN_ENTITY_PREFIX = EntityReference.SEPARATOR+ASSN_ENTITY_ID+EntityReference.SEPARATOR+ASSN_ENTITY_ACTION+EntityReference.SEPARATOR;
	
	private final static String FULLCALENDAR_ASPECTRATIO = ServerConfigurationService.getString("calendar.fullCalendar.aspectRatio", "1.35");
	private final static String FULLCALENDAR_SCROLLTIME = ServerConfigurationService.getString("calendar.fullCalendar.scrollTime", "06:00:00"); 
   
	private NumberFormat monthFormat = null;

	public CalendarAction() {
		super();
		aliasService = ComponentManager.get(AliasService.class);
		externalCalendarSubscriptionService = ComponentManager.get(ExternalCalendarSubscriptionService.class);
		taskService = ComponentManager.get(TaskService.class);
	}
	
	/**
	 * Converts a string that is used to store additional attribute fields to an array of strings.
	 */
	private String[] fieldStringToArray(String addfields_str, String delimiter)
	{
		String [] fields = addfields_str.split(delimiter);
		List destStringList = new ArrayList();
			
		// Don't copy empty fields.
		for ( int i=0; i < fields.length; i++)
		{
			if ( fields[i].length() > 0 )
			{
				destStringList.add(fields[i]);
			}
		}
			
		return (String[]) destStringList.toArray(new String[destStringList.size()]);
	}
	
	// myYear class
	public class MyYear
	{
		private MyMonth[][] yearArray;
		
		private int year;
		private MyMonth m;
		
		public MyYear()
		{
			yearArray = new MyMonth[4][3];
			m = null;
			year = 0;
		}
		
		public void setMonth(MyMonth m, int x, int y)
		{
			
			yearArray[x][y] = m;
			
		}
		
		
		public MyMonth getMonth(int x, int y)
		{
			m = yearArray[x][y];
			return (m);
		}
		
		public void setYear(int y)
		{
			year = y;
		}
		
		public int getYear()
		{
			return year;
		}
		
		
	}// myYear class
	
	// my week
	public class MyWeek
	{
		private MyDate[] week;
		private int weekOfMonth;
		
		public MyWeek()
		{
			week = new MyDate[7];
			weekOfMonth = 0;
		}
		
		public void setWeek(int i, MyDate date)
		{
			week[i] = date;
		}
		
		public MyDate getWeek(int i)
		{
			return week[i];
		}
		
		public String getWeekRange()
		{
			String range = null;
			range =	week[0].getTodayDate() + "	  "+ "-" + " " + week[6].getTodayDate();
			return range;
		}
		
		public void setWeekOfMonth(int w)
		{
			weekOfMonth = w;
		}
		
		public int getWeekOfMonth()
		{
			return weekOfMonth+1;
		}
		
		
		
	}
	
	// myMonth class
	public class MyMonth
	{
		private MyDate[][] monthArray;
		private MyDate result;
		private String monthName;
		private int month;
		private int row;
		private int numberOfDaysInMonth;
		
		public MyMonth()
		{
			result = null;
			monthArray = new MyDate[6][7];
			month = 0;
			row = 0;
			numberOfDaysInMonth=0;
		}
		
		public void setRow(int r)
		{
			row = r;
		}
		
		public int getRow()
		{
			return row;
		}
		
		public void setNumberOfDaysInMonth(int daysInMonth)
		{
			numberOfDaysInMonth = daysInMonth;
		}
		
		public int getNumberOfDaysInMonth()
		{
			return numberOfDaysInMonth;
		}
		
		public void setDay(MyDate d,int x, int y)
		{
			monthArray[x][y] = d;
		}
		
		public MyDate getDay(int x,int y)
		{
			result = monthArray[x][y];
			return (result);
		}
		
		public void setMonthName(String name)
		{
			monthName = name;
		}
		
		public String getMonthName()
		{
			return monthName;
		}
		
		public void setMonth(int m)
		{
			month = m;
		}
		
		public int getMonth()
		{
			return month;
		}
		
	}// myMonth
	
	// myDay class
	public class MyDay
	{
		private String m_data;	// data will have the days in the month
		private String m_attachment_data;	// data need to be displayed and attached, currently
		// this si a string and it can be any structure in the future.
		private int m_flag; // 0 if it is not a current date , 1 if it is a current date
		private int day;
		private int year;
		private int month;
		private String dayName; // name for each day
		private String todayDate;
		
		public MyDay()
		{
			m_data = "";
			m_flag = 0;
			m_attachment_data = "";
			day = 0;
			dayName = "";
			todayDate = "";
		}
		
		
		public void setDay(int d)
		{
			day = d;
		}
		
		public int getDay()
		{
			return day;
		}
		
		public void setFlag(int flag)
		{
			m_flag = flag;
		}
		
		
		public void setData(String data)
		{
			m_data = data;
		}
		
		
		public int getFlag()
		{
			return m_flag;
		}
		
		
		public String getData()
		{
			return(m_data);
		}
		
		
		public void setAttachment(String data)
		{
			m_attachment_data = data;
		}
		
		
		public String getAttachment()
		{
			return(m_attachment_data);
		}
		
		public void setDayName(String dname)
		{
			dayName = dname;
		}
		
		public String getDayName()
		{
			SimpleDateFormat df = new SimpleDateFormat("EEE");
			GregorianCalendar calendar = new GregorianCalendar(getYear(),getMonth()-1,getDay());
			dayName = df.format(calendar.getTime());
			return dayName;
		}
		
		
		public void setTodayDate(String date)
		{
			todayDate = date;
		}
		
		public String getTodayDate()
		{
			return todayDate;
		}
		
		public void setYear(int y)
		{
			year = y;
		}
		
		public int getYear()
		{
			return year;
		}
		
		public void setMonth(int m)
		{
			month = m;
		}
		
		public int getMonth()
		{
			return month;
		}
	}// myDay class
	
	public class EventClass
	{
		private String displayName;
		private long firstTime;
		private String eventId;
		
		public EventClass()
		{
			displayName = "";
			firstTime = 0;
		}
		
		public void setDisplayName(String name)
		{
			displayName = name;
		}
		public void setFirstTime(long time)
		{
			firstTime = time;
		}
		
		public String getDisplayName()
		{
			return displayName;
		}
		
		public long getfirstTime()
		{
			return firstTime;
		}
		
		public void setId(String id)
		{
			eventId = id;
		}
		
		public String getId()
		{
			return eventId;
		}
	}
	
	public class EventDisplayClass
	{
		private CalendarEvent calendareventobj;
		private boolean eventConflict;
		private int eventPosition;
		
		public EventDisplayClass()
		{
			
			eventConflict = false;
			calendareventobj = null;
			eventPosition = 0;
		}
		
		
		public void setEvent(CalendarEvent ce, boolean eventconf, int pos)
		{
			eventConflict = eventconf;
			calendareventobj = ce;
			eventPosition = pos;
			
		}
		
		
		public void setFlag(boolean conflict)
		{
			eventConflict = conflict;
		}
		
		public void setPosition(int position)
		{
			eventPosition = position;
		}
		
		
		public int getPosition()
		{
			return eventPosition;
		}
		
		public CalendarEvent getEvent()
		{
			return calendareventobj;
		}
		
		public boolean getFlag()
		{
			return eventConflict;
		}
		
		
		
	}
	
	public class MyDate
	{
		private MyDay day = null;
		private MyMonth month = null;
		private MyYear year = null;
		private String dayName = "";
		private Iterator iteratorObj = null;
		private int flag = -1;
		private Vector eVector;
		
		
		
		public MyDate()
		{
			day = new MyDay();
			month = new MyMonth();
			year = new MyYear();
		}
		
		public void setTodayDate(int m, int d, int y)
		{
			day.setDay(d);
			month.setMonth(m);
			year.setYear(y);
		}
		
		
		public void setNumberOfDaysInMonth(int daysInMonth)
		{
			month.setNumberOfDaysInMonth(daysInMonth);
		}
		
		
		public int getNumberOfDaysInMonth()
		{
			return month.getNumberOfDaysInMonth();
		}
		
		
		public String getTodayDate()
		{
			DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT);
						
			return f.format(new Date(year.getYear(), month.getMonth(), day.getDay()));
		}
		
		public void setFlag(int i)
		{
			flag = i;
		}
		
		public int getFlag()
		{
			return flag;
		}
		
		public void setDayName(String name)
		{
			dayName = name;
		}
		
		public void setNameOfMonth(String name)
		{
			month.setMonthName(name);
		}
		
		public String getDayName()
		{
			return dayName;
		}
		
		public int getDay()
		{
			return day.getDay();
		}
		
		public int getMonth()
		{
			return month.getMonth();
		}
		
		public String getNameOfMonth()
		{
			return month.getMonthName();
		}
		
		public int getYear()
		{
			return year.getYear();
		}
		
		public void setEventBerWeek(Vector eventVector)
		{
			
			eVector = eventVector;
		}
		
		public void setEventBerDay(Vector eventVector)
		{
			
			eVector = eventVector;
		}
		
		public Vector getEventsPerDay(int index)
		{
			Vector dayVector = new Vector();

			if (eVector != null)
				dayVector = (Vector)eVector.get(index);

			if (dayVector == null)
				dayVector = new Vector();
			
			return dayVector;

		}
		
		
		public Vector getEventsBerWeek(int index)
		{
			Vector dayVector = new Vector();
			if (eVector != null)
				dayVector = (Vector)eVector.get(index);
				
			if (dayVector == null)
				dayVector = new Vector();
				
			return dayVector;
		}
		
		
		public void setEvents(Iterator t)
		{
			iteratorObj = t;
		}
		
		
		public Vector getEvents()
		{
			Vector vectorObj = new Vector();
			int i = 0;
			if (iteratorObj!=null)
			{
				while(iteratorObj.hasNext())
				{
					vectorObj.add(i,iteratorObj.next());
					i++;
				}
			}
			return vectorObj;
		}
		
	}
	
	public class Helper
	{
		private int numberOfActivity =0;
		
		
		public int getduration(long x, int b)
		{
			
			Long l = Long.valueOf(x);
			int v = l.intValue()/3600000;
			return v;
		}
		
		
		public int getFractionIn(long x,int b)
		{
			Long ll = Long.valueOf(x);
			int y = (ll.intValue()-(b*3600000));
			int m = (y/60000);
			return m;
		}
		
		
		public CalendarEvent getActivity(Vector mm)
		{
			int size = mm.size();
			numberOfActivity = size;
			
			CalendarEvent activityEvent,event=null;
			
			if(size>0)
			{
				activityEvent = (CalendarEvent)mm.elementAt(0);
				long temp = activityEvent.getRange().duration();
				for(int i =0; i<size;i++)
				{
					activityEvent = (CalendarEvent)mm.elementAt(i);
					if(temp<activityEvent.getRange().duration())
					{
						temp = activityEvent.getRange().duration();
						event = activityEvent;
					}
				}
			}
			else
				event = null;
			
			return event;
		}
		
		
		public int getNumberOfActivity()
		{
			return numberOfActivity;
		}
		
		public int getInt(long x)
		{
			Long temp = Long.valueOf(x);
			return(temp.intValue());
		}
	}
	
	/**
	 * An inner class that can be initiated to perform text formatting
	 */
	public class CalendarFormattedText
	{
		// constructor
		public CalendarFormattedText()
		{
			
		}
		
		/**
		 * Use of FormattedText object's trimFormattedText function.
		 * @param formattedText The formatted text to trim
		 * @param maxNumOfChars The maximum number of displayed characters in the returned trimmed formatted text.
		 * @return String A String to hold the trimmed formatted text
		 */
		public String trimFormattedText(String formattedText, int maxNumOfChars)
		{
			StringBuilder sb = new StringBuilder();
			ComponentManager.get(FormattedText.class).trimFormattedText(formattedText, maxNumOfChars, sb);
			return sb.toString();
		}
	}
	
	/**
	 * Given a current date via the calendarUtil paramter, returns a TimeRange for the week.
	 */
	public TimeRange getWeekTimeRange(
	CalendarUtil calendarUtil)
	{
		int dayofweek = 0;
		
		dayofweek = calendarUtil.getDay_Of_Week(true)-1;
		int tempCurrentYear = calendarUtil.getYear();
		int tempCurrentMonth = calendarUtil.getMonthInteger();
		int tempCurrentDay = calendarUtil.getDayOfMonth();
		
		calendarUtil.setPrevDate(dayofweek);
		
		Time startTime = TimeService.newTimeLocal(calendarUtil.getYear(),calendarUtil.getMonthInteger(),calendarUtil.getDayOfMonth(),00,00,00,000);
		
		calendarUtil.setDay(tempCurrentYear,tempCurrentMonth,tempCurrentDay);
		dayofweek = calendarUtil.getDay_Of_Week(true);
		
		if (dayofweek< 7)
		{
			for(int i = dayofweek; i<=6;i++)
			{
				calendarUtil.nextDate();
			}
		}
		
		Time endTime = TimeService.newTimeLocal(calendarUtil.getYear(),calendarUtil.getMonthInteger(),calendarUtil.getDayOfMonth(),23,59,59,000);
		
		return TimeService.newTimeRange(startTime,endTime,true,true);
		
	} // etWeekTimeRange
	
	/**
	 * Given a current date via the calendarUtil paramter, returns a TimeRange for the month.
	 */
	public TimeRange getMonthTimeRange(CalendarUtil calendarUtil)
	{
		
		int dayofweek = 0;
		
		calendarUtil.setDay(calendarUtil.getYear(), calendarUtil.getMonthInteger(), 1);
		int numberOfCurrentDays = calendarUtil.getNumberOfDays();
		int tempCurrentMonth = calendarUtil.getMonthInteger();
		int tempCurrentYear = calendarUtil.getYear();
		
		// get the index of the first day in the month
		int firstDay_of_Month = calendarUtil.getDay_Of_Week(true) - 1;
		
		// Construct the time range to get all the days in the current month plus the days in the first week in the previous month and
		// the days in the last week from the last month
		
		// get the days in the first week that exists in the prev month
		calendarUtil.setPrevDate(firstDay_of_Month);
		
		Time startTime = TimeService.newTimeLocal(calendarUtil.getYear(),calendarUtil.getMonthInteger(),calendarUtil.getDayOfMonth(),00,00,00,000);
		
		// set the date object to the current month and last day in the current month
		calendarUtil.setDay(tempCurrentYear,tempCurrentMonth,numberOfCurrentDays);
		
		// get the index of the last day in the current month
		dayofweek = calendarUtil.getDay_Of_Week(true);
		
		// move the date object to the last day in the last week of the current month , this day will be one of those days in the
		// following month
		if (dayofweek < 7)
		{
			for(int i = dayofweek; i<=6;i++)
			{
				calendarUtil.nextDate();
			}
		}
		
		
		Time endTime = TimeService.newTimeLocal(calendarUtil.getYear(),calendarUtil.getMonthInteger(),calendarUtil.getDayOfMonth(),23,59,59,000);
		return TimeService.newTimeRange(startTime,endTime,true,true);
	}

	/**
	 * This class controls the page that allows the user to customize which
	 * calendars will be merged with the current group.
	 */
	public class MergePage
	{
		private final String mergeScheduleButtonHandler = "doMerge";

		// Name used in the velocity template for the list of merged/non-merged calendars
		private final String mergedCalendarsCollection = "mergedCalendarsCollection";
		
		public MergePage()
		{
			super();
		}
		
		/**
		 * Build the context for showing merged view
		 */
		public void buildContext(
		VelocityPortlet portlet,
		Context context,
		RunData runData,
		CalendarActionState state,
		SessionState sstate)
		{			
			// load all calendar channels (either primary or merged calendars)
			MergedList calendarList = 
				loadChannels( state.getPrimaryCalendarReference(), 
								  portlet.getPortletConfig().getInitParameter(PORTLET_CONFIG_PARM_MERGED_CALENDARS),
								  new EntryProvider() );
		
			// Place this object in the context so that the velocity template
			// can get at it.
			context.put(mergedCalendarsCollection, calendarList);
			context.put("tlang",rb);
			sstate.setAttribute(
									  CalendarAction.SSTATE_ATTRIBUTE_MERGED_CALENDARS,
									  calendarList);

			buildMenu(portlet, context, runData, state);
		}
		
		/**
		 * Action is used when the docancel is requested when the user click on cancel  in the new view
		 */
		public void doCancel(
		RunData data,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);
			
			// cancel the options, release the site lock, cleanup
			cancelOptions();
			
			// Clear the previous state so that we don't get confused elsewhere.
			state.setPrevState("");
			
			sstate.removeAttribute(STATE_MODE);
		} // doCancel
		
		/**
		 * Handle the "Merge" button on the toolbar
		 */
		public void doMerge(
		RunData runData,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			// TODO: really?
			// get a lock on the site and setup for options work
			doOptions(runData, context);
			
			// if we didn't end up in options mode, bail out
			if (!MODE_OPTIONS.equals(sstate.getAttribute(STATE_MODE))) return;
			
			// Save the previous state so that we can get to it after we're done with the options mode.
			// if the previous state is Description, we need to remember one more step back
			// coz there is a back link in description view
			if ((state.getState()).equalsIgnoreCase("description"))
			{
				state.setPrevState(state.getReturnState() + "!!!fromDescription");
			}
			else
			{
				state.setPrevState(state.getState());
			}
			
			state.setState(CalendarAction.STATE_MERGE_CALENDARS);
		} // doMerge
		
		/**
		 * Handles the user clicking on the save button on the page to specify which
		 * calendars will be merged into the present schedule.
		 */
		public void doUpdate(
		RunData runData,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			// Get the merged calendar list out of our session state
			MergedList mergedCalendarList =
			(MergedList) sstate.getAttribute(
			CalendarAction.SSTATE_ATTRIBUTE_MERGED_CALENDARS);
			
			if (mergedCalendarList != null)
			{
				// Get the information from the run data and load it into
				// our calendar list that we have in the session state.
				mergedCalendarList.loadFromRunData(runData.getParameters());
			}
			
			// update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			
			// myWorkspace is special (a null mergedCalendar list defaults to all channels),
			// so we add the primary calendar here to indicate no other channels are wanted
			if (mergedCalendarList != null && isOnWorkspaceTab())
			{
				String channelRef = mergedCalendarList.getDelimitedChannelReferenceString();
				if (StringUtils.trimToNull(channelRef) == null )
					channelRef = state.getPrimaryCalendarReference();
				placement.getPlacementConfig().setProperty(
											 PORTLET_CONFIG_PARM_MERGED_CALENDARS, channelRef );
			}
			
			// Otherwise, just set the list as specified
			else if (mergedCalendarList != null && !isOnWorkspaceTab())
			{
				placement.getPlacementConfig().setProperty(
											 PORTLET_CONFIG_PARM_MERGED_CALENDARS,
											 mergedCalendarList.getDelimitedChannelReferenceString());
			}

			// handle the case of no merge calendars			
			else
			{
				placement.getPlacementConfig().remove(PORTLET_CONFIG_PARM_MERGED_CALENDARS);
			}
			
			// commit the change
			saveOptions();
			
			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);
			
			// Clear the previous state so that we don't get confused elsewhere.
			state.setPrevState("");
			
			sstate.removeAttribute(STATE_MODE);
			
		} // doUpdate
		
		/* (non-Javadoc)
		 * @see org.chefproject.actions.schedulePages.SchedulePage#getMenuHandlerID()
		 */
		public String getButtonHandlerID()
		{
			return mergeScheduleButtonHandler;
		}
		
		/* (non-Javadoc)
		 * @see org.chefproject.actions.schedulePages.SchedulePage#getMenuText()
		 */
		public String getButtonText()
		{
			return rb.getString("java.merge");
		}
		
	}
	
	
	/**
	 * This class controls the page that allows the user to add arbitrary
	 * attributes to the attribute list for the primary calendar that
	 * corresponds to the current group.
	 */
	public class CustomizeCalendarPage
	{
		
		//This is the session attribute name to store init and current addFields list
		
		// Name used in the velocity template for the list of calendar addFields
		private final static String ADDFIELDS_CALENDARS_COLLECTION = "addFieldsCalendarsCollection";
		private final static String ADDFIELDS_CALENDARS_COLLECTION_ISEMPTY = "addFieldsCalendarsCollectionIsEmpty";
		private final static String OPTIONS_BUTTON_HANDLER = "doCustomize";
		
		
		
		public CustomizeCalendarPage()
		{
			super();
		}
		
		/**
		 * Build the context for addfields calendar (Options menu)
		 */
		public void buildContext(
		VelocityPortlet portlet,
		Context context,
		RunData runData,
		CalendarActionState state,
		SessionState sstate)
		{
			String[] addFieldsCalendarArray = null;
			
			// Get a list of current calendar addFields.	 This is a comma-delimited list.
			if (sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_PAGE).toString().equals(CalendarAction.PAGE_MAIN))	 //when the 'Options' button click
			{
				//when the 'Options' button click
				
				Calendar calendarObj = null;
				
				String calId = state.getPrimaryCalendarReference();
				try
				{
					calendarObj = CalendarService.getCalendar(calId);
				}
				catch (IdUnusedException e)
				{
					context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereis"));
					log.warn(".buildCustomizeContext(): " + e);
					return;
				}
				catch (PermissionException e)
				{
					context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
					log.warn(".buildCustomizeContext(): " + e);
					return;
				}
				
				// Get a current list of add fields.  This is a comma-delimited string.
				String addfieldsCalendars = calendarObj.getEventFields();
				
				if (addfieldsCalendars != null)
				{
					addFieldsCalendarArray =
					fieldStringToArray(
					addfieldsCalendars,
					ADDFIELDS_DELIMITER);
				}
				
				sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS_INIT, addfieldsCalendars);
				sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS, addfieldsCalendars);
				
				context.put("delFields", (List)sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_DELFIELDS));
				sstate.removeAttribute(CalendarAction.SSTATE_ATTRIBUTE_DELFIELDS);
			}
			else //after the 'Options' button click
			{
				String addFieldsCollection = (String) sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS);
				
				if (addFieldsCollection != null)
					addFieldsCalendarArray = fieldStringToArray(addFieldsCollection, ADDFIELDS_DELIMITER);
			}
			
			// Place this object in the context so that the velocity template
			// can get at it.
			context.put(ADDFIELDS_CALENDARS_COLLECTION, addFieldsCalendarArray);
			context.put("tlang",rb);
			if (addFieldsCalendarArray == null)
				context.put(ADDFIELDS_CALENDARS_COLLECTION_ISEMPTY, Boolean.valueOf(true));
			else
				context.put(ADDFIELDS_CALENDARS_COLLECTION_ISEMPTY, Boolean.valueOf(false));

			buildMenu(portlet, context, runData, state);
			
		} //buildCustomizeCalendarContext

		/*
		 * Handles the adding of fields to events in the calendar.
		 */
		public void doAddfield(
		RunData runData,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			String addFields = (String) sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS);
			String [] addFieldsCalendarList = null;

			if (addFields != null)
				addFieldsCalendarList = fieldStringToArray(addFields,ADDFIELDS_DELIMITER);

			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);

			String addField = "";
			addField = runData.getParameters().getString("textfield").trim();
			String dupAddfield = "N";

			//prevent entry of some characters (can cause problem)
			addField = addField.replaceAll("	 "," ");
			addField = addField.replaceAll("'","");
			addField = addField.replaceAll("\"","");

			if (addField.length()==0)
			{
				addAlert(sstate, rb.getString("java.alert.youneed"));
			}
			else
			{
				if (addFieldsCalendarList != null)
				{
					for (int i=0; i < addFieldsCalendarList.length; i++)
					{
						if (addField.toUpperCase().equals(addFieldsCalendarList[i].toUpperCase()))
						{
							addAlert(sstate, rb.getString("java.alert.theadd"));
							dupAddfield = "Y";
							i = addFieldsCalendarList.length + 1;
						}
					}
					if (dupAddfield.equals("N"))
						addFieldsCalendarList = fieldStringToArray(addFields+ADDFIELDS_DELIMITER+addField, ADDFIELDS_DELIMITER);
				}
				else
				{
					String [] initString = new String[1];
					initString[0] = addField;
					addFieldsCalendarList = initString;
					
				}

				if (dupAddfield.equals("N"))
				{
					if (addFields != null)
						addFields = addFields + ADDFIELDS_DELIMITER + addField;
					else
						addFields = addField;
					
					sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS, addFields);
				}
			}

			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_PAGE, CalendarAction.PAGE_ADDFIELDS);
		
		}

		/**
		 * Handles a click on the cancel button in the page that allows the
		 * user to add/remove events to/from events that will be added to
		 * the calendar.
		 */
		public void doCancel(
		RunData data,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);
			
			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS, sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS_INIT));
			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_PAGE, CalendarAction.PAGE_MAIN);
		} // doCancel
		
		/**
		 * This initiates the page where the user can add/remove additional
		 * properties to/from events that will be added to the calendar.
		 */
		public void doCustomize(
		RunData runData,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			// Save the previous state so that we can get to it after we're done with the options mode.
			// if the previous state is Description, we need to remember one more step back
			// coz there is a back link in description view
			if ((state.getState()).equalsIgnoreCase("description"))
			{
				state.setPrevState(state.getReturnState() + "!!!fromDescription");
			}
			else
			{
				state.setPrevState(state.getState());
			}
			
			state.setState(CalendarAction.STATE_CUSTOMIZE_CALENDAR);
		}
		
		/*
		 * Handles the removal of event fields in the calendar.
		 */
		public void doDeletefield(
		RunData runData,
		Context context,
		CalendarActionState state,
		SessionState sstate)
		{
			
			ParameterParser params = runData.getParameters();
			String addFields = (String) sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS);
			String [] addFieldsCalendarList = null, newAddFieldsCalendarList = null;
			List delFields = new Vector();
			
			int nextNewFieldsIndex = 0;
			if (addFields != null)
			{
				addFieldsCalendarList = fieldStringToArray(addFields,ADDFIELDS_DELIMITER);
				
				// The longest the new array can possibly be is the current size of the list.
				newAddFieldsCalendarList = new String[addFieldsCalendarList.length];

				
				for (int i=0; i< addFieldsCalendarList.length; i++)
				{
					String fieldName = params.getString(addFieldsCalendarList[i]);
					
					// If a value is present, then that means that the user has checked
					// the box for the field to be removed.  Don't add it to the
					// new list of field names.  If it is not present, then add it
					// to the new list of field names.
					if ( fieldName == null || fieldName.length() == 0 )
					{
						newAddFieldsCalendarList[nextNewFieldsIndex++] = addFieldsCalendarList[i];
					}
					else
					{
						delFields.add(addFieldsCalendarList[i]);
					}
				}
				addFields = arrayToString(newAddFieldsCalendarList, ADDFIELDS_DELIMITER);
			}
			
			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);
			
			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS, addFields);
			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_DELFIELDS, delFields);
			
			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_PAGE, CalendarAction.PAGE_ADDFIELDS);
			
		}
		
		/*
		 * Handles the saving process of changes fields in calendar events.
		 */
		public void doUpdate( RunData runData, Context context, CalendarActionState state, SessionState sstate) {
			String addfields = (String) sstate.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS);

			while (addfields.startsWith(ADDFIELDS_DELIMITER)) {
				addfields = addfields.substring(ADDFIELDS_DELIMITER.length());
			}

			String calId = state.getPrimaryCalendarReference();
			try {
				CalendarEdit edit = CalendarService.editCalendar(calId);
				edit.setEventFields(addfields);
				CalendarService.commitCalendar(edit);
			} catch (IdUnusedException e) {
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereisno"));
				log.debug(".doUpdate customize calendar IdUnusedException"+e);
				return;
			} catch (PermissionException e) {
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdonthave"));
				log.debug(".doUpdate customize calendar "+e);
				return;
			} catch (InUseException e) {
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.someone"));
				log.debug(".doUpdate() for CustomizeCalendar: " + e);
				return;
			}

			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS, addfields);
			sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDFIELDS_PAGE, CalendarAction.PAGE_MAIN);

			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);

		} // doUpdate

		/* (non-Javadoc)
		 * @see org.chefproject.actions.schedulePages.SchedulePage#getMenuHandlerID()
		 */
		public String getButtonHandlerID()
		{
			return OPTIONS_BUTTON_HANDLER;
		}
		
		/* (non-Javadoc)
		 * @see org.chefproject.actions.schedulePages.SchedulePage#getMenuText()
		 */
		public String getButtonText()
		{
			return rb.getString("java.fields");
		}
		
		/**
		 * Loads additional fields information from the calendar object passed
		 * as a parameter and loads them into the context object for the Velocity
		 * template.
		 */
		public void loadAdditionalFieldsIntoContextFromCalendar(
		Calendar calendarObj,
		Context context)
		{
			// Get a current list of add fields.  This is a ADDFIELDS_DELIMITER string.
			String addfieldsCalendars = calendarObj.getEventFields();
			
			String[] addfieldsCalendarArray = null;
			
			if (addfieldsCalendars != null)
			{
				addfieldsCalendarArray =
				fieldStringToArray(
				addfieldsCalendars,
				ADDFIELDS_DELIMITER);
			}
			
			// Place this object in the context so that the velocity template
			// can get at it.
			context.put(ADDFIELDS_CALENDARS_COLLECTION, addfieldsCalendarArray);
			context.put("tlang",rb);
			if (addfieldsCalendarArray == null)
				context.put(ADDFIELDS_CALENDARS_COLLECTION_ISEMPTY, Boolean.valueOf(true));
			else
				context.put(ADDFIELDS_CALENDARS_COLLECTION_ISEMPTY, Boolean.valueOf(false));
		}
		
		/**
		 * Loads additional fields from the run data into a provided map object.
		 */
		public void loadAdditionalFieldsMapFromRunData(
		RunData rundata,
		Map addfieldsMap,
		Calendar calendarObj)
		{
			String addfields_str = calendarObj.getEventFields();
			if ( addfields_str != null && addfields_str.trim().length() != 0)
			{
				String [] addfields = fieldStringToArray(addfields_str, ADDFIELDS_DELIMITER);
				String eachfield;
				
				for (int i=0; i < addfields.length; i++)
				{
					eachfield = addfields[i];
					addfieldsMap.put(eachfield, rundata.getParameters().getString(eachfield));
				}
			}
		}
	}
	

	
	
	/**
	 * This class controls the page that allows the user to configure
	 * external calendar subscriptions.
	 */
	public class CalendarSubscriptionsPage
	{
		private final String institutionalSubscriptionsCollection = "institutionalSubscriptionsCollection";

		private final String institutionalSubscriptionsAvailable = "institutionalSubscriptionsAvailable";

		private final String userSubscriptionsCollection = "userSubscriptionsCollection";

		public CalendarSubscriptionsPage()
		{
			super();
		}

		/**
		 * Build the context
		 */
		public void buildContext(VelocityPortlet portlet, Context context,
				RunData runData, CalendarActionState state, SessionState sstate)
		{
			String channel = state.getPrimaryCalendarReference();
			Set<ExternalSubscriptionDetails> availableInstitutionalSubscriptions= externalCalendarSubscriptionService
					.getAvailableInstitutionalSubscriptionsForChannel(channel);
			Set<ExternalSubscriptionDetails> subscribedByUser = externalCalendarSubscriptionService
					.getSubscriptionsForChannel(channel, false);

			// Institutional subscriptions
			List<SubscriptionWrapper> institutionalSubscriptions = new ArrayList<>();
			for (ExternalSubscriptionDetails available : availableInstitutionalSubscriptions)
			{
				boolean selected = false;
				for (ExternalSubscriptionDetails subscribed : subscribedByUser)
				{
					if (subscribed.getReference().equals(available.getReference()))
					{
						selected = true;
						break;
					}
				}
				institutionalSubscriptions.add(new SubscriptionWrapper(available, selected));
			}

			// User subscriptions
			List<SubscriptionWrapper> userSubscriptions = (List<SubscriptionWrapper>) sstate
					.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS);
			if (userSubscriptions == null)
			{
				userSubscriptions = new ArrayList<>();
				for (ExternalSubscriptionDetails subscribed : subscribedByUser)
				{
					if (!subscribed.isInstitutional())
					{
						userSubscriptions.add(new SubscriptionWrapper(subscribed, true));
					}
				}
			}

			// Sort collections by name
			Collections.sort(institutionalSubscriptions);
			Collections.sort(userSubscriptions);

			// Place in context so that the velocity template can get at it.
			context.put("tlang", rb);
			context.put(institutionalSubscriptionsAvailable, !institutionalSubscriptions
					.isEmpty());
			context.put(institutionalSubscriptionsCollection, institutionalSubscriptions);
			context.put(userSubscriptionsCollection, userSubscriptions);
			sstate.setAttribute(SSTATE_ATTRIBUTE_SUBSCRIPTIONS,
					institutionalSubscriptions);
			sstate.setAttribute(SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS, userSubscriptions);
			buildMenu(portlet, context, runData, state);
		}

		/**
		 * Action is used when the doCancel is requested when the user click on
		 * cancel
		 */
		public void doCancel(RunData data, Context context, CalendarActionState state,
				SessionState sstate)
		{
			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);

			// cancel the options, release the site lock, cleanup
			cancelOptions();

			// Clear the previous state so that we don't get confused elsewhere.
			state.setPrevState("");

			sstate.removeAttribute(STATE_MODE);
			sstate.removeAttribute(SSTATE_ATTRIBUTE_SUBSCRIPTIONS);
			sstate.removeAttribute(SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS);

		} // doCancel

		/**
		 * Action is used when the doAddSubscription is requested
		 */
		public void doAddSubscription(RunData runData, Context context,
				CalendarActionState state, SessionState sstate)
		{
			List<SubscriptionWrapper> addSubscriptions = (List<SubscriptionWrapper>) sstate
					.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS);

			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);

			String calendarName = runData.getParameters().getString("calendarName")
					.trim();
			String calendarUrl = runData.getParameters().getString("calendarUrl").trim();
			calendarUrl = calendarUrl.replaceAll("webcals://", "https://");
			calendarUrl = calendarUrl.replaceAll("webcal://", "http://");

			if (calendarName.length() == 0) {
				addAlert(sstate, rb.getString("java.alert.subsnameempty"));
			} else if (calendarUrl.length() == 0) {
				addAlert(sstate, rb.getString("java.alert.subsurlempty"));
			} else if(!ComponentManager.get(FormattedText.class).validateURL(calendarUrl)) {
				addAlert(sstate,rb.getString("java.alert.subsurlinvalid"));
			} else {
				String contextId = EntityManager.newReference(
						state.getPrimaryCalendarReference()).getContext();
				String id = externalCalendarSubscriptionService
						.getIdFromSubscriptionUrl(calendarUrl);
				String ref = externalCalendarSubscriptionService
						.calendarSubscriptionReference(contextId, id);
				String currentUserId = sessionManager.getCurrentSessionUserId();
				String currentUserTzid = TimeService.getLocalTimeZone().getID();
				
				addSubscriptions.add(new SubscriptionWrapper(calendarName, ref, currentUserId, currentUserTzid, true));

				// Sort collections by name
				Collections.sort(addSubscriptions);
				sstate.setAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS,
						addSubscriptions);
			}

		} // doAddSubscription

		/**
		 * Handle the "Subscriptions" button on the toolbar
		 */
		public void doSubscriptions(RunData runData, Context context,
				CalendarActionState state, SessionState sstate)
		{
			doOptions(runData, context);

			// if we didn't end up in options mode, bail out
			if (!MODE_OPTIONS.equals(sstate.getAttribute(STATE_MODE))) return;

			// Save the previous state so that we can get to it after we're done
			// with the options mode.
			// state.setPrevState(state.getState());
			// Save the previous state so that we can get to it after we're done
			// with the options mode.
			// if the previous state is Description, we need to remember one
			// more step back
			// coz there is a back link in description view
			if ((state.getState()).equalsIgnoreCase("description"))
			{
				state.setPrevState(state.getReturnState() + "!!!fromDescription");
			}
			else
			{
				state.setPrevState(state.getState());
			}

			state.setState(CalendarAction.STATE_CALENDAR_SUBSCRIPTIONS);
		} // doSubscriptions

		/**
		 * Handles the user clicking on the save button on the page to specify
		 * which calendars will be merged into the present schedule.
		 */
		public void doUpdate(RunData runData, Context context, CalendarActionState state,
				SessionState sstate)
		{
			List<SubscriptionWrapper> calendarSubscriptions = (List<SubscriptionWrapper>) sstate
					.getAttribute(SSTATE_ATTRIBUTE_SUBSCRIPTIONS);
			List<SubscriptionWrapper> addSubscriptions = (List<SubscriptionWrapper>) sstate
					.getAttribute(CalendarAction.SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS);
			List<String> subscriptionTC = new LinkedList<String>();
			List<String> subscriptionTCWithTZ = new LinkedList<String>();
			ParameterParser params = runData.getParameters();

			// Institutional Calendars
			if (calendarSubscriptions != null)
			{
				for (SubscriptionWrapper subs : calendarSubscriptions)
				{
					if (params.getString(subs.getReference()) != null)
					{
						subscriptionTC.add(subs.getReference());
					}
				}
			}

			// Other Calendars
			if (addSubscriptions != null)
			{
				for (SubscriptionWrapper add : addSubscriptions)
				{
					if (params.getString(add.getReference()) != null)
					{
						String name = add.getDisplayName();
						if (name == null || name.equals("")) name = add.getUrl();

						if (add.getUserId()==null) {
							// Backward compatibility: reference/name
							StringBuilder sb = new StringBuilder(add.getReference());
							sb.append(ExternalCalendarSubscriptionService.SUBS_NAME_DELIMITER);
							sb.append(name);
							subscriptionTC.add(sb.toString());
						} else {
							// With TZ: reference/user/tzid/name
							StringBuilder sb = new StringBuilder(add.getReference());
							sb.append(ExternalCalendarSubscriptionService.SUBS_NAME_DELIMITER);
							sb.append(add.getUserId());
							sb.append(ExternalCalendarSubscriptionService.SUBS_NAME_DELIMITER);
							sb.append(add.getUserTzid());
							sb.append(ExternalCalendarSubscriptionService.SUBS_NAME_DELIMITER);
							sb.append(name);
							subscriptionTCWithTZ.add(sb.toString());
						}
					}
				}
			}

			// Update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			if (placement != null)
			{
				Properties config = placement.getPlacementConfig();
				if (config != null)
				{
					String propValue = "";
					if (!subscriptionTC.isEmpty()) {
						propValue = subscriptionTC.stream().collect(Collectors.joining(ExternalCalendarSubscriptionService.SUBS_REF_DELIMITER));										
					}
					
					String propValueWithTZ = "";
					if (!subscriptionTCWithTZ.isEmpty()) {
						propValueWithTZ = subscriptionTCWithTZ.stream().collect(Collectors.joining(ExternalCalendarSubscriptionService.SUBS_REF_DELIMITER));										
					}
					
					config.setProperty(ExternalCalendarSubscriptionService.TC_PROP_SUBCRIPTIONS, propValue);
					config.setProperty(ExternalCalendarSubscriptionService.TC_PROP_SUBCRIPTIONS_WITH_TZ, propValueWithTZ);
					
					// commit the change
					saveOptions();
				}
			}

			// Go back to whatever state we were in beforehand.
			state.setReturnState(CalendarAction.STATE_INITED);

			// Clear the previous state so that we don't get confused elsewhere.
			state.setPrevState("");

			sstate.removeAttribute(STATE_MODE);
			sstate.removeAttribute(SSTATE_ATTRIBUTE_SUBSCRIPTIONS);
			sstate.removeAttribute(SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS);

		} // doUpdate

		public class SubscriptionWrapper implements Comparable<SubscriptionWrapper>
		{
			private String reference;

			private String url;

			private String displayName;

			private boolean isInstitutional;

			private boolean isSelected;

			private String userId;
			
			private String userTzid;
			
			public SubscriptionWrapper()
			{
			}

			public SubscriptionWrapper(ExternalSubscriptionDetails subscription, boolean selected)
			{
				this.reference = subscription.getReference();
				this.url = subscription.getSubscriptionUrl();
				this.displayName = subscription.getSubscriptionName();
				this.isInstitutional = subscription.isInstitutional();
				this.isSelected = selected;
				if (subscription.getUserId()!=null) {
					this.setUserId(subscription.getUserId());
				}
				this.setUserTzid(subscription.getTzid());
			}

			public SubscriptionWrapper(String calendarName, String ref, String userId, String userTzid, boolean selected)
			{
				Reference _reference = EntityManager.newReference(ref);
				this.reference = ref;
				// this.id = _reference.getId();
				this.url = externalCalendarSubscriptionService
						.getSubscriptionUrlFromId(_reference.getId());
				this.displayName = calendarName;
				this.isInstitutional = externalCalendarSubscriptionService
						.isInstitutionalCalendar(ref);
				this.isSelected = selected;
				this.setUserId(userId);
				this.setUserTzid(userTzid);
			}

			public String getReference()
			{
				return reference;
			}

			public void setReference(String ref)
			{
				this.reference = ref;
			}

			public String getUrl()
			{
				return url;
			}

			public void setUrl(String url)
			{
				this.url = url;
			}

			public String getDisplayName()
			{
				return displayName;
			}

			public void setDisplayName(String displayName)
			{
				this.displayName = displayName;
			}

			public boolean isInstitutional()
			{
				return isInstitutional;
			}

			public void setInstitutional(boolean isInstitutional)
			{
				this.isInstitutional = isInstitutional;
			}

			public boolean isSelected()
			{
				return isSelected;
			}

			public void setSelected(boolean isSelected)
			{
				this.isSelected = isSelected;
			}
			
			public String getUserId() {
				return userId;
			}
			
			public void setUserId(String userId) {
				this.userId = userId;
			}
			
			public String getUserTzid() {
				return userTzid;
			}
			
			public void setUserTzid(String userTzid) {
				this.userTzid = userTzid;
			}

			public int compareTo(SubscriptionWrapper sub)
			{
				if(this.getDisplayName() == null || sub.getDisplayName() == null)
					return this.getUrl().compareTo(sub.getUrl());
				else
					return this.getDisplayName().compareTo(sub.getDisplayName());
			}

		}
	}
	
	/**
	 * Utility class to figure out permissions for a calendar object.
	 */
	static public class CalendarPermissions
	{
		/**
		 * Priate constructor, doesn't allow instances of this object.
		 */
		private CalendarPermissions()
		{
			super();
		}
		
		/**
		 * Returns true if the primary and selected calendar are the same, but not null.
		 */
		static boolean verifyPrimarySelectedMatch(String primaryCalendarReference, String selectedCalendarReference)
		{
			//
			// Both primary and secondary calendar ids must be specified.
			// These must also match to be able to delete an event
			//
			if ( primaryCalendarReference == null ||
			selectedCalendarReference == null ||
			!primaryCalendarReference.equals(selectedCalendarReference) )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		/**
		 * Utility routint to get the calendar for a given calendar id.
		 */
		static private Calendar getTheCalendar(String calendarReference)
		{
			Calendar calendarObj = null;
			
			try
			{
				calendarObj = CalendarService.getCalendar(calendarReference);
				
				if (calendarObj == null)
				{
					// If the calendar isn't there, try adding it.
					CalendarService.commitCalendar(
					CalendarService.addCalendar(calendarReference));
					calendarObj = CalendarService.getCalendar(calendarReference);
				}
			}
			
			catch (IdUnusedException e)
			{
				log.debug("CalendarPermissions.getTheCalendar(): ",e);
			}
			
			catch (PermissionException e)
			{
				log.debug("CalendarPermissions.getTheCalendar(): " + e);
			}
			
			catch (IdUsedException e)
			{
				log.debug("CalendarPermissions.getTheCalendar(): " + e);
			}
			
			catch (IdInvalidException e)
			{
				log.debug("CalendarPermissions.getTheCalendar(): " + e);
			}
			
			return calendarObj;
		}
		
		/**
		 * Returns true if the current user can see the events in a calendar.
		 */
		static public boolean allowViewEvents(String calendarReference)
		{
			Calendar calendarObj = getTheCalendar(calendarReference);
			
			if (calendarObj == null)
			{
				return false;
			}
			else
			{
				return calendarObj.allowGetEvents();
			}
		}
		
		/**
		 * Returns true if the current user is allowed to delete events on the calendar id
		 * passed in as the selectedCalendarReference parameter.	 The selected calendar must match
		 * the primary calendar for this function to return true.
		 * @param primaryCalendarReference calendar id for the default channel
		 * @param selectedCalendarReference calendar id for the event the user has just selected
		 */
		public static boolean allowDeleteEvent(String primaryCalendarReference, String selectedCalendarReference, String eventId)
		{
			//
			// Both primary and secondary calendar ids must be specified.
			// These must also match to be able to delete an event
			//
			if ( !verifyPrimarySelectedMatch(primaryCalendarReference, selectedCalendarReference) )
			{
				return false;
			}
			
			Calendar calendarObj = getTheCalendar(primaryCalendarReference);
			
			if (calendarObj == null)
			{
				return false;
			}
			else
			{
				CalendarEvent event = null;
				try
				{
					event = calendarObj.getEvent(eventId);
				}
				catch (IdUnusedException e)
				{
					log.debug("CalendarPermissions.canDeleteEvent(): " + e);
				}
				catch (PermissionException e)
				{
					log.debug("CalendarPermissions.canDeleteEvent(): " + e);
				}
				
				if (event == null)
				{
					return false;
				}
				else
				{
					return calendarObj.allowRemoveEvent(event);
				}
			}
		}
		
		/**
		 * Returns true if the current user is allowed to revise events on the calendar id
		 * passed in as the selectedCalendarReference parameter.	 The selected calendar must match
		 * the primary calendar for this function to return true.
		 * @param primaryCalendarReference calendar id for the default channel
		 * @param selectedCalendarReference calendar reference for the event the user has just selected
		 */
		static public boolean allowReviseEvents(String primaryCalendarReference, String selectedCalendarReference, String eventId)
		{
			//
			// Both primary and secondary calendar ids must be specified.
			// These must also match to be able to delete an event
			//
			if ( !verifyPrimarySelectedMatch(primaryCalendarReference, selectedCalendarReference) )
			{
				return false;
			}
			
			Calendar calendarObj = getTheCalendar(primaryCalendarReference);
			
			if (calendarObj == null)
			{
				return false;
			}
			else
			{
				return calendarObj.allowEditEvent(eventId);
			}
		}
		
		/**
		 * Returns true if the current user is allowed to create events on the calendar id
		 * passed in as the selectedCalendarReference parameter.	 The selected calendar must match
		 * the primary calendar for this function to return true.
		 * @param primaryCalendarReference calendar reference for the default channel
		 * @param selectedCalendarReference calendar reference for the event the user has just selected
		 */
		static public boolean allowCreateEvents(String primaryCalendarReference, String selectedCalendarReference)
		{
			// %%% Note: disabling this check as the allow create events should ONLY be on the primary,
			// we don't care about the selected -ggolden
/*
			//
			// The primary and selected calendar ids must match, unless the selected calendar
			// is null or empty.
			//
 
			if ( selectedCalendarReference != null &&
				 selectedCalendarReference.length() > 0 &&
				 !verifyPrimarySelectedMatch(primaryCalendarReference, selectedCalendarReference) )
			{
				return false;
			}
 */
			
			Calendar calendarObj = getTheCalendar(primaryCalendarReference);
			
			if (calendarObj == null)
			{
				return false;
			}
			else
			{
				return calendarObj.allowAddEvent();
			}
		}
		
		/**
		 * Returns true if the user is allowed to merge events from different calendars
		 * within the default channel.
		 */
		static public boolean allowMergeCalendars(String calendarReference)
		{
			return CalendarService.allowMergeCalendar(calendarReference);
		}
		
		/**
		 * Returns true if the use is allowed to modify properties of the calendar itself,
		 * and not just the events within the calendar.
		 */
		static public boolean allowModifyCalendarProperties(String calendarReference)
		{
			return CalendarService.allowEditCalendar(calendarReference);
		}

		/**
		 * Returns true if the use is allowed to import (and export) events 
		 * into the calendar.
		 */
		static public boolean allowImport(String calendarReference)
		{
			return CalendarService.allowImportCalendar(calendarReference);
		}

		/**
		 * Returns true if the user is allowed to subscribe external calendars 
		 * into the calendar.
		 */
		static public boolean allowSubscribe(String calendarReference)
		{
			return CalendarService.allowSubscribeCalendar(calendarReference);
		}
		
		/**
		 * Returns true if the user is allowed to subscribe to the implicit
		 * calendar.
		 */
		static public boolean allowSubscribeThis(String calendarReference)
		{
			return CalendarService.allowSubscribeThisCalendar(calendarReference);
	}
	}
	
	private final static String SSTATE_ATTRIBUTE_ADDFIELDS_PAGE =
	"addfieldsPage";
	private final static String SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS_INIT =
	"addfieldsInit";
	private final static String SSTATE_ATTRIBUTE_ADDFIELDS_CALENDARS =
	"addfields";
	private final static String SSTATE_ATTRIBUTE_DELFIELDS = "delFields";
	
	private final static String SSTATE_ATTRIBUTE_SUBSCRIPTIONS = "calendarSubscriptions";
	private final static String SSTATE_ATTRIBUTE_ADDSUBSCRIPTIONS = "addCalendarSubscriptions";

	private final static String STATE_NEW = "new";
	private static final String EVENT_REFERENCE_PARAMETER = "eventReference";
	
	private static final String EVENT_CONTEXT_VAR = "event";
	private static final String NO_EVENT_FLAG_CONTEXT_VAR = "noEvent";
	private static final String NOT_OPEN_EVENT_FLAG_CONTEXT_VAR = "notOpenEvent";
	//
	// These are variables used in the context for communication between this
	// action class and the Velocity template.
	
	// This is the property name in the portlet config for the list of calendars
	// that are not merged.
	private final static String PORTLET_CONFIG_PARM_MERGED_CALENDARS = "mergedCalendarReferences";
   
	// default calendar view property
	private final static String PORTLET_CONFIG_DEFAULT_VIEW = "defaultCalendarView";
	private final static String PORTLET_CONFIG_DEFAULT_SUBVIEW = "defaultCalendarSubview";
	
	private final static String PAGE_MAIN = "main";
	private final static String PAGE_ADDFIELDS = "addFields";
	
	/** The flag name and value in state to indicate an update to the portlet is needed. */
	private final static String SSTATE_ATTRIBUTE_MERGED_CALENDARS = "mergedCalendars";
	
	// String constants for user interface states
	private final static String STATE_MERGE_CALENDARS = "mergeCalendars";
	private final static String STATE_CALENDAR_SUBSCRIPTIONS = "calendarSubscriptions";
	private final static String STATE_CUSTOMIZE_CALENDAR = "customizeCalendar";
	
	// for detailed event view navigator
	private final static String STATE_PREV_ACT = "toPrevActivity";
	private final static String STATE_NEXT_ACT = "toNextActivity";
	private final static String STATE_CURRENT_ACT = "toCurrentActivity";
	private final static String STATE_EVENTS_LIST ="eventIds";
	private final static String STATE_NAV_DIRECTION = "navigationDirection";
	
	private MergePage mergedCalendarPage =	new MergePage();
	
	private CustomizeCalendarPage customizeCalendarPage =	new CustomizeCalendarPage();
	
	private CalendarSubscriptionsPage calendarSubscriptionsPage =	new CalendarSubscriptionsPage();
	
	private String defaultStateView;
	
	/**
	 * See if the current tab is the workspace tab (i.e. user site)
	 * @return true if we are currently on the "My Workspace" tab.
	 */
	private static boolean isOnWorkspaceTab()
	{
		return SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
	}
	
	
	protected Class getStateClass()
	{
		return CalendarActionState.class;
		
	}	 // getStateClass
	
	/**
	 ** loadChannels -- load specified primaryCalendarReference 
	 ** or merged calendars if initMergeList is defined
	 **/
	private MergedList loadChannels( String primaryCalendarReference, 
												String initMergeList,
												MergedList.EntryProvider entryProvider )
	{
		MergedList mergedCalendarList = new MergedList();		
		String[] channelArray = null;
		
		// Figure out the list of channel references that we'll be using.
		// MyWorkspace is special: if not superuser, and not otherwise defined, get all channels
		if ( isOnWorkspaceTab()	 && !SecurityService.isSuperUser() && initMergeList == null )
			 channelArray = mergedCalendarList.getAllPermittedChannels(new CalendarChannelReferenceMaker());
		else
			channelArray = mergedCalendarList.getChannelReferenceArrayFromDelimitedString(
												primaryCalendarReference, initMergeList );
												
		if (entryProvider == null )
		{
			entryProvider = new MergedListEntryProviderFixedListWrapper(
										  new EntryProvider(), 
										  primaryCalendarReference,
										  channelArray,
										  new CalendarReferenceToChannelConverter());
		}

		mergedCalendarList.loadChannelsFromDelimitedString(
								isOnWorkspaceTab(),
								false,
								entryProvider,
								StringUtils.trimToEmpty(sessionManager.getCurrentSessionUserId()),
								channelArray, 
								SecurityService.isSuperUser(),
								ToolManager.getCurrentPlacement().getContext());
								
		return mergedCalendarList;
	}
	
	/**
	 * Gets an array of all the calendars whose events we can access.
	 */
	private List getCalendarReferenceList(VelocityPortlet portlet, String primaryCalendarReference, boolean isOnWorkspaceTab)
	{
		// load all calendar channels (either primary or merged calendars)
		MergedList mergedCalendarList = 
			loadChannels( primaryCalendarReference, 
							  portlet.getPortletConfig().getInitParameter(PORTLET_CONFIG_PARM_MERGED_CALENDARS),
							  null );
		
		// add external calendar subscriptions
      List referenceList = mergedCalendarList.getReferenceList();
      Set<ExternalSubscriptionDetails> subscriptionDetailsList = externalCalendarSubscriptionService.getCalendarSubscriptionChannelsForChannels(
    		  primaryCalendarReference,
    		  referenceList);
      subscriptionDetailsList.stream().forEach(x->referenceList.add(x.getReference()));
      
      return referenceList;
	}
	
	/**
	 * Gets the session state from the Jetspeed RunData
	 */
	static private SessionState getSessionState(RunData runData)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData)runData).getJs_peid();
		return ((JetspeedRunData)runData).getPortletSessionState(peid);
	}
	
	public String buildMainPanelContext(  VelocityPortlet portlet,
	Context context,
	RunData runData,
	SessionState sstate)
	{
		CalendarActionState state = (CalendarActionState)getState(portlet, runData, CalendarActionState.class);
		
		String template = (String)getContext(runData).get("template");
		
		// get current state (view); if not set use tool default or default to calendar view
		String stateName = state.getState();
		if (StringUtils.isBlank(stateName)) { 
			stateName = portlet.getPortletConfig().getInitParameter(PORTLET_CONFIG_DEFAULT_VIEW);
			if (StringUtils.isBlank(stateName)) {
				stateName = ServerConfigurationService.getString("calendar.default.view", CALENDAR_INIT_PARAMETER);
				state.setState(stateName);
			}
		}

		switch (stateName) {
			case STATE_SCHEDULE_IMPORT:
				buildImportContext(portlet, context, runData, state, getSessionState(runData));
				break;
			case STATE_MERGE_CALENDARS: 
				// build the context to display the options panel
				mergedCalendarPage.buildContext(portlet, context, runData, state, getSessionState(runData));
				break;
			case STATE_CALENDAR_SUBSCRIPTIONS:
				// build the context to display the options panel
				calendarSubscriptionsPage.buildContext(portlet, context, runData, state, getSessionState(runData));
				break;
			case STATE_CUSTOMIZE_CALENDAR:
				// build the context to display the options panel
				//needed to track when user clicks 'Save' or 'Cancel'
				String sstatepage = "";				
				Object statepageAttribute = sstate.getAttribute(SSTATE_ATTRIBUTE_ADDFIELDS_PAGE);				
				if ( statepageAttribute != null ) {
					sstatepage = statepageAttribute.toString();
				}				
				if (!sstatepage.equals(PAGE_ADDFIELDS)) {
					sstate.setAttribute(SSTATE_ATTRIBUTE_ADDFIELDS_PAGE, PAGE_MAIN);
				}				
				customizeCalendarPage.buildContext(portlet, context, runData, state, getSessionState(runData));
				break;
			case "revise":
			case "goToReviseCalendar":
				// build the context for the normal view show
				buildReviseContext(portlet, context, runData, state);				
				break;
			case "description":
				// build the context for the basic step of adding file
				buildDescriptionContext(portlet, context, runData, state);				
				break;
			case STATE_NEW:
				// build the context to display the property list
				buildNewContext(portlet, context, runData, state);
				break;
			case "icalEx":
				buildIcalExportPanelContext(portlet, context, runData, state);
				break;
			case "opaqueUrlClean":
				buildOpaqueUrlCleanContext(portlet, context, runData, state);
				break;
			case "opaqueUrlExisting":
				buildOpaqueUrlExistingContext(portlet, context, runData, state);
				break;
			case "delete":
				// build the context to display the property list
				buildDeleteContext(portlet, context, runData, state);
				break;
			case LIST_VIEW:
				// build the context to display the list view
				buildListContext(portlet, context, runData, state);
				break;
			case STATE_SET_FREQUENCY:
				buildFrequencyContext(portlet, context, runData, state);
				break;
			case MODE_PERMISSIONS:
				template = build_permissions_context(portlet, context, runData, getSessionState(runData));
				state.setState(state.getPrevState());
				break;
			case CALENDAR_INIT_PARAMETER:
			default:
				buildViewCalendarContext(portlet, context, runData, state);
				break;
		}

		if (StringUtils.equalsAny(stateName, "description", LIST_VIEW, CALENDAR_INIT_PARAMETER)) {
		    // SAK-23566 capture the view calendar events
		    EventTrackingService ets = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
		    String calendarRef = state.getPrimaryCalendarReference();
		    if (ets != null && calendarRef != null) {
		        // need to cleanup the cal references which look like /calendar/calendar/4ea74c4d-3f9e-4c32-b03f-15e7915e6051/main
		        String eventRef = StringUtils.replace(calendarRef, "/main", "/"+stateName);
		        String calendarEventId = state.getCalendarEventId();
		        if (StringUtils.isNotBlank(calendarEventId) && stateName.equals("description")) {
		            eventRef += "/"+calendarEventId;
		        }
		        ets.post(ets.newEvent("calendar.read", eventRef, false));
		    }
		}

		TimeZone timeZone = TimeService.getLocalTimeZone();
		context.put("timezone", timeZone.getDisplayName(timeZone.inDaylightTime(new Date()), TimeZone.SHORT) );
		
		//the AM/PM strings
		context.put("amString", CalendarUtil.getLocalAMString());
		context.put("pmString", CalendarUtil.getLocalPMString());
		
		// group realted variables
		context.put("siteAccess", CalendarEvent.EventAccess.SITE);
		context.put("groupAccess", CalendarEvent.EventAccess.GROUPED);
		
		context.put("message", state.getState());
		context.put("state", state.getKey());
		context.put("tlang",rb);
		context.put("eventIconMap", CalendarEventType.getIcons());
		context.put("localizedEventTypes", new CalendarUtil().getLocalizedEventTypes());
		context.put("iconsAndLocalizedEventTypes", new CalendarUtil().getLocalizedEventTypesAndIcons());
		context.put("dateFormat", getDateFormatString());
		context.put("timeFormat", getTimeFormatString());

		return template;
		
	}	 // buildMainPanelContext
	
	
	private void buildImportContext(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state, SessionState state2)
	{
		// Place this object in the context so that the velocity template
		// can get at it.
			
		// Start at the beginning if nothing is set yet.
		if ( state.getImportWizardState() == null )
		{
			state.setImportWizardState(IMPORT_WIZARD_SELECT_TYPE_STATE);
		}
		
		// (optional) ical.public.userdefined.subscribe import (ical.experimental is deprecated)
		context.put("icalEnable", 
						ServerConfigurationService.getBoolean("ical.public.userdefined.subscribe",ServerConfigurationService.getBoolean("ical.experimental",true)));
		
		// Set whatever the current wizard state is.
		context.put("importWizardState", state.getImportWizardState());
		context.put("tlang",rb);
		// Set the imported events into the context.
		context.put("wizardImportedEvents", state.getWizardImportedEvents());
		
		String calId = state.getPrimaryCalendarReference();
		try
		{
			Calendar calendarObj = CalendarService.getCalendar(calId);
			String scheduleTo = (String)state2.getAttribute(STATE_SCHEDULE_TO);
			if (scheduleTo != null && scheduleTo.length() != 0)
			{
				context.put("scheduleTo", scheduleTo);
			}
			else
			{
				if (calendarObj.allowAddEvent())
				{
					// default to make site selection
					context.put("scheduleTo", "site");
				}
				else if (calendarObj.getGroupsAllowAddEvent().size() > 0)
				{
					// to group otherwise
					context.put("scheduleTo", "groups");
				}
			}
		
			Collection groups = calendarObj.getGroupsAllowAddEvent();
			if (groups.size() > 0)
			{
				context.put("groups", groups);
			}

			buildMenu(portlet, context, runData, state);
		}
		catch(IdUnusedException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereis"));
			log.debug(".buildImportContext(): " + e);
		}
		catch (PermissionException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
			log.debug(".buildImportContext(): " + e);
		}
	}

	/**
	 * Addes the primary calendar reference (this site's default calendar)
	 * to the calendar action state object.
	 */
	private void setPrimaryCalendarReferenceInState(VelocityPortlet portlet, CalendarActionState state)
	{
		String calendarReference = state.getPrimaryCalendarReference();
		
		if (calendarReference == null)
		{
			
			calendarReference = StringUtils.trimToNull(portlet.getPortletConfig().getInitParameter(CALENDAR_INIT_PARAMETER));
			if (calendarReference == null)
			{
				// form a reference to the default calendar for this request's site
				calendarReference = CalendarService.calendarReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
				state.setPrimaryCalendarReference(calendarReference);
			}
		}
	}

	/**
	 * Build the context for editing the frequency
	 */
	protected void buildFrequencyContext(VelocityPortlet portlet,
	Context context,
	RunData runData,
	CalendarActionState state)
	{
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		// under 3 conditions, we get into this page
		// 1st, brand new event, no freq or rule set up, coming from revise page
		// 2nd, exisitng event, coming from revise page
		// 3rd, new event, stay in this page after changing frequency by calling js function onchange()
		// 4th, existing event, stay in this page after changing frequency by calling js function onchange()
		
		// sstate attribute TEMP_FREQ_SELECT is one of the flags
		// if this attribute is not null, means changeFrequency() is called thru onchange().
		// Then rule is another flag, if rule is null, means new event
		// Combination of these 2 flags should cover all the conditions
	
		RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);

		CalendarUtil calutil = new CalendarUtil();

		// defaultly set frequency to be once
		// if there is a saved state frequency attribute, replace the default one
		String freq = CalendarAction.DEFAULT_FREQ;
		
		if (sstate.getAttribute(TEMP_FREQ_SELECT) != null)
		{
			freq = (String)sstate.getAttribute(TEMP_FREQ_SELECT);
			if (rule != null)
				context.put("rule", rule);
			sstate.removeAttribute(TEMP_FREQ_SELECT);
		}
		else
		{
			if (rule != null)
			{
				freq = rule.getFrequency();
				context.put("rule", rule);
			}
		}
		context.put("freq", freq);
		context.put("tlang",rb);
		context.put("cutil",calutil);
		// get the data the user just input in the preview new/revise page
		context.put("savedData",state.getNewData());
		
		context.put("realDate", TimeService.newTime());
				
		if (DEFAULT_FREQ.equals(freq))
		{
			LocalEvent savedData = state.getNewData();
			Time m_time = TimeService.newTimeLocal(savedData.getYearInt(), savedData.getMonth(), savedData.getDay(), 0, 0, 0, 0);
			context.put("freqOnceDate", m_time.toStringLocalDate());
		}
	} // buildFrequencyContext
	
	/**
	 * Build the context for showing revise view
	 */
	protected void buildReviseContext(VelocityPortlet portlet,
	Context context,
	RunData runData,
	CalendarActionState state)
	{
		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		context.put("tlang",rb);
		Calendar calendarObj = null;
		CalendarEvent calEvent = null;
		CalendarUtil calObj= new CalendarUtil(); //null;
		MyDate dateObj1 = null;
		dateObj1 = new MyDate();
		boolean getEventsFlag = false;
		
		List attachments = state.getAttachments();
		
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		Time m_time = TimeService.newTime();
		TimeBreakdown b = m_time.breakdownLocal();
		int stateYear = b.getYear();
		int stateMonth = b.getMonth();
		int stateDay = b.getDay();
		if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null))
		{
			stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
			stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
			stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
		}
		calObj.setDay(stateYear, stateMonth, stateDay);
		
		
		dateObj1.setTodayDate(calObj.getMonthInteger(),calObj.getDayOfMonth(),calObj.getYear());
		String calId = state.getPrimaryCalendarReference();
		if( state.getIsNewCalendar() == false)
		{
			if (CalendarService.allowGetCalendar(calId)== false)
			{
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotallow"));
				return;
			}
			else
			{
				try
				{
					calendarObj = CalendarService.getCalendar(calId);

					if(calendarObj.allowGetEvent(state.getCalendarEventId()))
					{
						calEvent = calendarObj.getEvent(state.getCalendarEventId());
						getEventsFlag = true;
						context.put("selectedGroupRefsCollection", calEvent.getGroups());

						// all the groups the user is allowed to do remove from
						context.put("allowedRemoveGroups", calendarObj.getGroupsAllowRemoveEvent(calEvent.isUserOwner()));
					}
					else
						getEventsFlag = false;
					
					// Add any additional fields in the calendar.
					customizeCalendarPage.loadAdditionalFieldsIntoContextFromCalendar( calendarObj, context);
					context.put("tlang",rb);
					context.put("calEventFlag","true");
					context.put(STATE_NEW, "false");
					// if from the metadata view of announcement, the message is already the system resource
					if ( state.getState().equals("goToReviseCalendar") )
					{
						context.put("backToRevise", "false");
					}
					// if from the attachments editing view or preview view of announcement
					else if (state.getState().equals("revise"))
					{
						context.put("backToRevise", "true");
					}
					
					//Vector attachments = state.getAttachments();
					if ( attachments != null )
					{
						context.put("attachments", attachments);
					}
					else
					{
						context.put("attachNull", "true");
					}
					
					context.put("fromAttachmentFlag",state.getfromAttachmentFlag());
				}
				catch(IdUnusedException e)
				{
					context.put(ALERT_MSG_KEY, rb.getString("java.alert.therenoactv"));
					log.debug(".buildReviseContext(): " + e);
					return;
				}
				catch (PermissionException e)
				{
					context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotperm"));
					log.debug(".buildReviseContext(): " + e);
					return;
				}
			}
		}
		else
		{
			// if this a new annoucement, get the subject and body from temparory record
			context.put(STATE_NEW, "true");
			context.put("tlang",rb);
			context.put("attachments", attachments);
			context.put("fromAttachmentFlag",state.getfromAttachmentFlag());
		}
		
		// Output for recurring events
		
		// for an existing event
		// if the saved recurring rule equals to string FREQ_ONCE, set it as not recurring
		// if there is a saved recurring rule in sstate, display it
		// otherwise, output the event's rule instead
		if ((((String) sstate.getAttribute(FREQUENCY_SELECT)) != null) 
			&& (((String) sstate.getAttribute(FREQUENCY_SELECT)).equals(FREQ_ONCE)))
		{
			context.put("rule", null);
		}
		else
		{
			RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);
			if (rule == null)
			{
				rule = calEvent.getRecurrenceRule();
			}
			else
				context.put("rule", rule);
			
			if (rule != null)
			{
				context.put("freq", rule.getFrequencyDescription());
			} // if (rule != null)
		} //if ((String) sstate.getAttribute(FREQUENCY_SELECT).equals(FREQ_ONCE))
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			
			String scheduleTo = (String)sstate.getAttribute(STATE_SCHEDULE_TO);
			if (scheduleTo != null && scheduleTo.length() != 0)
			{
				context.put("scheduleTo", scheduleTo);
			}
			else
			{
				if (calendarObj.allowAddCalendarEvent())
				{
					// default to make site selection
					context.put("scheduleTo", "site");
				}
				else if (calendarObj.getGroupsAllowAddEvent().size() > 0)
				{
					// to group otherwise
					context.put("scheduleTo", "groups");
				}
			}
		
			Collection groups = calendarObj.getGroupsAllowAddEvent();

			// add to these any groups that the message already has
			calEvent = calendarObj.getEvent(state.getCalendarEventId());
			if (calEvent != null)
			{
				Collection otherGroups = calEvent.getGroupObjects();
				for (Iterator i = otherGroups.iterator(); i.hasNext();)
				{
					Group g = (Group) i.next();
					
					if (!groups.contains(g))
					{
						groups.add(g);
					}
				}					
			}

			if (groups.size() > 0)
			{
				context.put("groups", groups);
			}
		}
		catch(IdUnusedException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereis"));
			log.debug(".buildNewContext(): " + e);
			return;
		}
		catch (PermissionException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
			log.debug(".buildNewContext(): " + e);
			return;
		}
		context.put("tlang",rb);
		context.put("event", calEvent);
		context.put("helper",new Helper());
		context.put("message","revise");
		context.put("savedData",state.getNewData());
		context.put("getEventsFlag", Boolean.valueOf(getEventsFlag));
		
		if(state.getIsNewCalendar()==true)
			context.put("vmtype",STATE_NEW);
		else
			context.put("vmtype","revise");
		
		context.put("service", contentHostingService);
		
		// output the real time
		context.put("realDate", TimeService.newTime());
		
	} // buildReviseContext
	
	
	
	/**
	 * Build the context for showing description for events
	 */
	protected void buildDescriptionContext(VelocityPortlet portlet,
	Context context,
	RunData runData,
	CalendarActionState state)
	{
		
		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		context.put("tlang",rb);
		context.put("Context", ToolManager.getCurrentPlacement().getContext());
		context.put("CalendarService", CalendarService.getInstance());
		context.put("SiteService", SiteService.getInstance());
					 
		Calendar calendarObj = null;
		CalendarEvent calEvent = null;
		
		MyDate dateObj1 = null;
		dateObj1 = new MyDate();
		
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		navigatorContextControl(portlet, context, runData, (String)sstate.getAttribute(STATE_NAV_DIRECTION));
		boolean prevAct = sstate.getAttribute(STATE_PREV_ACT) != null;
		boolean nextAct = sstate.getAttribute(STATE_NEXT_ACT) != null;
		context.put("prevAct", Boolean.valueOf(prevAct));
		context.put("nextAct", Boolean.valueOf(nextAct));
		
		Time m_time = TimeService.newTime();
		TimeBreakdown b = m_time.breakdownLocal();
		int stateYear = b.getYear();
		int stateMonth = b.getMonth();
		int stateDay = b.getDay();
		if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null))
		{
			stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
			stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
			stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
		}
		CalendarUtil calObj= new CalendarUtil();
		calObj.setDay(stateYear, stateMonth, stateDay);
		
		// get the today date in month/day/year format
		dateObj1.setTodayDate(calObj.getMonthInteger(),calObj.getDayOfMonth(),calObj.getYear());
		
		// get the event id from the CalendarService.
		// send the event to the vm
		String ce = state.getCalendarEventId();
		
		String selectedCalendarReference = state.getSelectedCalendarReference();
		
		if ( !CalendarPermissions.allowViewEvents(selectedCalendarReference) )
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotallow")); 
			log.debug("here in buildDescription not showing event");
			return;
		}
		else
		{
			try
			{
				calendarObj = CalendarService.getCalendar(selectedCalendarReference);
				calEvent = calendarObj.getEvent(ce);
				
				// Add any additional fields in the calendar.
				customizeCalendarPage.loadAdditionalFieldsIntoContextFromCalendar( calendarObj, context);
				
				context.put(EVENT_CONTEXT_VAR, calEvent);
				context.put("tlang",rb);
				
				// Get the attachments from assignment tool for viewing
				String assignmentId = calEvent.getField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);
				
				if (assignmentId != null && assignmentId.length() > 0)
				{
					// pass in the assignment reference to get the assignment data we need
					Map<String, Object> assignData = new HashMap<String, Object>();
					StringBuilder entityId = new StringBuilder( ASSN_ENTITY_PREFIX );
					entityId.append( (CalendarService.getCalendar(calEvent.getCalendarReference())).getContext() );
					entityId.append( EntityReference.SEPARATOR );
					entityId.append( assignmentId );
					try{
						ActionReturn ret = entityBroker.executeCustomAction(entityId.toString(), ASSN_ENTITY_ACTION, null, null);
						if (ret != null && ret.getEntityData() != null) {
							Object returnData = ret.getEntityData().getData();
							assignData = (Map<String, Object>)returnData;
						}
						context.put("assignmenturl", (String) assignData.get("assignmentUrl"));
						context.put("assignmentTitle", (String) assignData.get("assignmentTitle"));
					}catch(SecurityException e){
						final String openDateErrorDescription = rb.getFormattedMessage("java.alert.opendatedescription",
								calEvent.getField(CalendarConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
						context.put(ALERT_MSG_KEY, rb.getString("java.alert.opendate") + " " + openDateErrorDescription);
						context.put(NOT_OPEN_EVENT_FLAG_CONTEXT_VAR, Boolean.TRUE.toString());
						return;
 					}
				}
						
				
				String ownerId = calEvent.getCreator();
				if ( ownerId != null && ! ownerId.equals("") )
				// if the user not defined, assigned the owner_name as ""
				try 
				{
					String ownerName = 
							 UserDirectoryService.getUser( ownerId ).getDisplayName();
					context.put("owner_name", ownerName);
				} 
				catch (UserNotDefinedException e) {
					context.put("owner_name", "");
				}
				
				String siteName = calEvent.getSiteName();
				if ( siteName != null )
					context.put("site_name", siteName );
               
				RecurrenceRule rule = calEvent.getRecurrenceRule();
				// for a brand new event, there is no saved recurring rule
				if (rule != null)
				{
					context.put("freq", rule.getFrequencyDescription());
					
					context.put("rule", rule);
				}
				
				// show all the groups in this calendar that user has get event in
				Collection groups = calendarObj.getGroupsAllowGetEvent();
				if (groups != null)
				{
					context.put("groupRange", calEvent.getGroupRangeForDisplay(calendarObj));
				}
			}
			catch (IdUnusedException  e)
			{
				log.debug(".buildDescriptionContext(): " + e);
				context.put(NO_EVENT_FLAG_CONTEXT_VAR, Boolean.TRUE.toString());
			}
			catch (PermissionException e)
			{
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotpermadd"));
				log.debug(".buildDescriptionContext(): " + e);
				return;
			}
		}
		
		
		context.put(
				"allowDelete",
				Boolean.valueOf(CalendarPermissions.allowDeleteEvent(
						state.getPrimaryCalendarReference(),
						state.getSelectedCalendarReference(),
						state.getCalendarEventId())));
		context.put(
				"allowRevise",
				Boolean.valueOf(CalendarPermissions.allowReviseEvents(
						state.getPrimaryCalendarReference(),
						state.getSelectedCalendarReference(),
						state.getCalendarEventId())));

	}	 // buildDescriptionContext

	protected boolean isDefaultView(CalendarActionState state, Placement currentPlacement) {
		String currentView = state.getState();
		String defaultView = currentPlacement.getPlacementConfig().getProperty(PORTLET_CONFIG_DEFAULT_VIEW);
		return StringUtils.equals(defaultView, currentView);
	}

	protected Vector getNewEvents(int year, int month, int day, CalendarActionState state, RunData rundata, int time, int numberofcycles,Context context,CalendarEventVector CalendarEventVectorObj)
	{
		boolean firstTime = true; // Don't need to do complex checking the first time.
		Vector events = new Vector(); // A vector of vectors, each of the vectors containing a range of previous events.
		
		Time timeObj = TimeService.newTimeLocal(year,month,day,time,00,00,000);
		
		long duration = ((30*60)*(1000));
		Time updatedTime = TimeService.newTime(timeObj.getTime()+ duration);
		
		/*** include the start time ***/
		TimeRange timeRangeObj = TimeService.newTimeRange(timeObj,updatedTime,true,false);
		
		for (int range = 0; range <=numberofcycles;range++)
		{
			Iterator calEvent = null;
			
			calEvent = CalendarEventVectorObj.getEvents(timeRangeObj);
			
			Vector vectorObj = new Vector(); // EventDisplay of calevent.
			EventDisplayClass eventDisplayObj;
			Vector newVectorObj = null; // Ones we haven't see before?
			boolean swapflag=true;
			EventDisplayClass eventdisplayobj = null;
			
			if (calEvent.hasNext()) // While we still have more events in this range.
			{
				int i = 0; // Size of vectorObj
				while (calEvent.hasNext())
				{
					eventdisplayobj = new EventDisplayClass();
					eventdisplayobj.setEvent((CalendarEvent)calEvent.next(),false,i);
					
					vectorObj.add(i,eventdisplayobj); // Copy into vector, wrapping in EventDisplay
					i++;
				} // while
				
				if(firstTime) // First range
				{
					events.add(range,vectorObj);
					firstTime = false;
				}
				else
				{
					while(swapflag == true)
					{
						swapflag=false;
						for(int mm = 0; mm<events.size();mm++) // Loop through all the previous ranges.
						{
							// 
							Vector evectorObj = (Vector)events.elementAt(mm); // One vector range.
							if(!evectorObj.isEmpty())
							{
								for(int eom = 0; eom<evectorObj.size();eom++) // loop through previous range.
								{
									if(!"".equals(evectorObj.elementAt(eom)))
									{
										// Event ID.
										String eomId = (((EventDisplayClass)evectorObj.elementAt(eom)).getEvent()).getId();
										newVectorObj = new Vector();
										for(int mv = 0; mv<vectorObj.size();mv++) // Loop back through the current range.
										{
											if(!"".equals(vectorObj.elementAt(mv)))
											{
												String vectorId = (((EventDisplayClass)vectorObj.elementAt(mv)).getEvent()).getId();
												if (vectorId.equals(eomId)) // Exists in a previous range.
												{
													eventDisplayObj = (EventDisplayClass)vectorObj.elementAt(mv);
													eventDisplayObj.setFlag(true);
													if (mv != eom) // index of current range, 
													{
														swapflag = true;
														vectorObj.removeElementAt(mv);
														for(int x = 0 ; x<eom;x++)
														{
															if(!vectorObj.isEmpty())
															{
																newVectorObj.add(x,vectorObj.elementAt(0)); // Copy data into new array.
																vectorObj.removeElementAt(0);
															}
															else
															{
																newVectorObj.add(x,"");
															}
														}// for
														newVectorObj.add(eom, eventDisplayObj); // Add the one that's out of position.
														int neweom = eom;
														neweom = neweom+1;
														
														while(vectorObj.isEmpty()==false)
														{
															newVectorObj.add(neweom,vectorObj.elementAt(0));
															vectorObj.removeElementAt(0);
															neweom++;
														}
														
														for(int vv =0;vv<newVectorObj.size();vv++)
														{
															vectorObj.add(vv,newVectorObj.elementAt(vv));
														}
													}	 // if
												}	 // if
											}	 // if
										}	 //for
									}	 // if
								}	 // for
							}	 // if
						}	 // for
					}	 // while
					
					events.add(range,vectorObj);
				}	 // if - else firstTime
				
				timeRangeObj.shiftForward(1800000);
			}
			else
			{
				events.add(range,vectorObj);
				timeRangeObj.shiftForward(1800000);
			}
		} // for
		return events;
	} // getNewEvents

	/**
	 * Build the context for showing New view
	 */
	protected void buildNewContext(VelocityPortlet portlet,
	Context context,
	RunData runData,
	CalendarActionState state)
	{
		context.put("tlang",rb);
		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		
		MyDate dateObj1 = new MyDate();
		
		CalendarUtil calObj= new CalendarUtil();
		
		// set real today's date as default
		Time m_time = TimeService.newTime();
		TimeBreakdown b = m_time.breakdownLocal();
		calObj.setDay(b.getYear(), b.getMonth(), b.getDay());
		
		dateObj1.setTodayDate(calObj.getMonthInteger(),calObj.getDayOfMonth(),calObj.getYear());
		
		// get the event id from the CalendarService.
		// send the event to the vm
		dateObj1.setNumberOfDaysInMonth(calObj.getNumberOfDays());
		List attachments = state.getAttachments();
		context.put("attachments",attachments);
		
		String calId = state.getPrimaryCalendarReference();
		Calendar calendarObj = null;
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			Collection groups = calendarObj.getGroupsAllowAddEvent();

			String peid = ((JetspeedRunData)runData).getJs_peid();
			SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
			
			if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null))
			{
				int stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
				int stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
				int stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
				
				calObj.setDay(stateYear, stateMonth, stateDay);
				dateObj1.setTodayDate(calObj.getMonthInteger(),calObj.getDayOfMonth(),calObj.getYear());
			}
		
			String scheduleTo = (String)sstate.getAttribute(STATE_SCHEDULE_TO);
			if (scheduleTo != null && scheduleTo.length() != 0)
			{
				context.put("scheduleTo", scheduleTo);
			}
			else
			{
				if (calendarObj.allowAddCalendarEvent())
				{
					// default to make site selection
					context.put("scheduleTo", "site");
				}
				else if (groups.size() > 0)
				{
					// to group otherwise
					context.put("scheduleTo", "groups");
				}
			}

			if (groups.size() > 0)
			{
				List schToGroups = (List)(sstate.getAttribute(STATE_SCHEDULE_TO_GROUPS));
				context.put("scheduleToGroups", schToGroups);
				
				context.put("groups", groups);
			}
		}
		catch(IdUnusedException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereis"));
			log.debug(".buildNewContext(): " + e);
			return;
		}
		catch (PermissionException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
			log.debug(".buildNewContext(): " + e);
			return;
		}
		
		// Add any additional fields in the calendar.
		customizeCalendarPage.loadAdditionalFieldsIntoContextFromCalendar( calendarObj, context);
		
		// Output for recurring events
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		// if the saved recurring rule equals to string FREQ_ONCE, set it as not recurring
		// if there is a saved recurring rule in sstate, display it		
		RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);

		if (rule != null)
		{
			context.put("freq", rule.getFrequencyDescription());

			context.put("rule", rule);
		}

		context.put("date",dateObj1);
		context.put("savedData",state.getNewData());
		context.put("helper",new Helper());
		context.put("realDate", TimeService.newTime());

		buildMenu(portlet, context, runData, state);
	} // buildNewContext
	
	/**
	 * Setup for iCal Export.
	 */
	public String buildIcalExportPanelContext(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state)
	{
		String calId = state.getPrimaryCalendarReference();
		Calendar calendarObj = null;
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
		}
		catch ( Exception e )
		{
			log.debug(".buildIcalExportPanelContext: " + e);
		}

		context.put("tlang", rb);

		// provide form names
		context.put("form-alias", FORM_ALIAS);
		context.put("form-ical-enable", FORM_ICAL_ENABLE);
		context.put("form-submit", BUTTON + "doIcalExport");
		context.put("form-cancel", BUTTON + "doCancel");

		if ( calendarObj != null )
		{
			List aliasList =	aliasService.getAliases( calendarObj.getReference() );
			if ( ! aliasList.isEmpty() )
			{
				String alias[] = ((Alias)aliasList.get(0)).getId().split("\\.");
				context.put("alias", alias[0] );
			}
		}

		context.put("serverName", ServerConfigurationService.getServerName());

		String icalInfoArr[] = {String.valueOf(ServerConfigurationService.getInt("calendar.export.next.months",12)),
			String.valueOf(ServerConfigurationService.getInt("calendar.export.previous.months",6))};
		String icalInfoStr = rb.getFormattedMessage("ical.info", icalInfoArr);
		context.put("icalInfoStr",icalInfoStr);
			
		// Add iCal Export URL
		Reference calendarRef = EntityManager.newReference(calId);
		String icalUrl = ServerConfigurationService.getAccessUrl()
			+ CalendarService.calendarICalReference(calendarRef);
		context.put("icalUrl", icalUrl );
		
		boolean exportAllowed = CalendarPermissions.allowImport(	calId );
		context.put("allow_export", String.valueOf(exportAllowed) );
		
		boolean exportEnabled = CalendarService.getExportEnabled(calId);
		context.put("enable_export", String.valueOf(exportEnabled) );

		buildMenu(portlet, context, runData, state);

		// pick the "export" template based on the standard template name
		String template = (String) getContext(runData).get("template");
		return template + "_icalexport";

	} // buildIcalExportPanelContext
	
	/**
	 * Setup for Opaque URL Export ("No URL").
	 */
	protected void buildOpaqueUrlCleanContext(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state)
	{
		context.put("isMyWorkspace", isOnWorkspaceTab());
		context.put("form-generate", BUTTON + "doOpaqueUrlGenerate");
		context.put("form-cancel", BUTTON + "doCancel");
		String icalInfoArr[] = {String.valueOf(ServerConfigurationService.getInt("calendar.export.next.months",12)),
			String.valueOf(ServerConfigurationService.getInt("calendar.export.previous.months",6))};
		String icalInfoStr = rb.getFormattedMessage("ical.info", icalInfoArr);
		context.put("icalInfoStr",icalInfoStr);
		buildMenu(portlet, context, runData, state);
	}
	
	/**
	 * Setup for Opaque URL Export ("URL exists").
	 */
	protected void buildOpaqueUrlExistingContext(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state)
	{
		String calId = state.getPrimaryCalendarReference();
		Reference calendarRef = EntityManager.newReference(calId);
		String opaqueUrl = ServerConfigurationService.getAccessUrl()
			+ CalendarService.calendarOpaqueUrlReference(calendarRef);

		String icalInfoArr[] = {String.valueOf(ServerConfigurationService.getInt("calendar.export.next.months",12)),
			String.valueOf(ServerConfigurationService.getInt("calendar.export.previous.months",6))};
		String icalInfoStr = rb.getFormattedMessage("ical.info", icalInfoArr);
		context.put("icalInfoStr",icalInfoStr);

		context.put("opaqueUrl", opaqueUrl);
		context.put("webcalUrl", opaqueUrl.replaceFirst("http", "webcal"));
		context.put("isMyWorkspace", isOnWorkspaceTab());
		context.put("form-regenerate", BUTTON + "doOpaqueUrlRegenerate");
		context.put("form-delete", BUTTON + "doOpaqueUrlDelete");
		context.put("form-cancel", BUTTON + "doCancel");
		buildMenu(portlet, context, runData, state);
	}
	
	/**
	 * Build the context for showing delete view
	 */
	protected void buildDeleteContext(VelocityPortlet portlet,
	Context context,
	RunData runData,
	CalendarActionState state)
	{
		context.put("tlang",rb);
		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		
		Calendar calendarObj = null;
		CalendarEvent calEvent = null;
		
		// get the event id from the CalendarService.
		// send the event to the vm
		String calId = state.getPrimaryCalendarReference();
		String calendarEventObj = state.getCalendarEventId();
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			calEvent = calendarObj.getEvent(calendarEventObj);
			
			RecurrenceRule rule = calEvent.getRecurrenceRule();
			// for a brand new event, there is no saved recurring rule
			if (rule != null)
			{
				context.put("freq", rule.getFrequencyDescription());				
				context.put("rule", rule);
			}
			
			context.put("message","delete");
			context.put("event",calEvent);
			
			// show all the groups in this calendar that user has get event in
			Collection groups = calendarObj.getGroupsAllowGetEvent();
			if (groups != null)
			{
				context.put("groupRange", calEvent.getGroupRangeForDisplay(calendarObj));
			}
		}
		catch (IdUnusedException  e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.noexist"));
			log.debug(".buildDeleteContext(): " + e);
		}
		catch (PermissionException	 e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youcreate"));
			log.debug(".buildDeleteContext(): " + e);
		}		
	}	 // buildDeleteContext
	
	
	
	/**
	 * calculate the days in the month and there events if any
	 * @param month is int
	 * @param m_calObj is object of calendar
	 */
	public MyMonth calMonth(int month, CalendarUtil m_calObj, CalendarActionState state, CalendarEventVector CalendarEventVectorObj)
	{
		int numberOfDays = 0;
		int firstDay_of_Month = 0;
		boolean start = true;
		MyMonth monthObj = null;
		MyDate dateObj = null;
		Iterator eventList = null;
		Time startTime = null;
		Time endTime = null;
		TimeRange timeRange = null;
		
		// new objects of myYear, myMonth, myDay, myWeek classes.
		monthObj = new MyMonth();
		
		// set the calendar to the begining of the month
		m_calObj.setDay(m_calObj.getYear(), month, 1);
		numberOfDays = m_calObj.getNumberOfDays();
		
		// get the index of the first day in the month
		firstDay_of_Month = m_calObj.getDay_Of_Week(true) - 1;
		
		// get the index of the day
		monthObj.setMonthName(calendarUtilGetMonth(m_calObj.getMonthInteger()));
		
		// get the index of first day (-1) to display (may be in previous month)
		m_calObj.setPrevDate(firstDay_of_Month+1);
		
		for(int weekInMonth = 0; weekInMonth < 1; weekInMonth++)
		{
			// got the seven days in the first week of the month do..
			for(int dayInWeek = 0; dayInWeek < 7; dayInWeek++)
			{
				dateObj = new MyDate();
				m_calObj.nextDate();
				// check if reach the first day of the month.
				if ((dayInWeek == firstDay_of_Month) || (start == false))
				{
					// check if the current day of the month has been match, if yes set the flag to highlight the day in the
					// user interface.
					if ((m_calObj.getDayOfMonth() == state.getcurrentDay()) && (state.getcurrentMonth()== m_calObj.getMonthInteger()) && (state.getcurrentYear() == m_calObj.getYear()))
					{
						dateObj.setFlag(1);
					}
					
					// Each monthObj contains dayObjs for the number of the days in the month.
					dateObj.setTodayDate(m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),m_calObj.getYear());
					
					startTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),00,00,00,000);
					endTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),23,59,59,999);
					
					eventList = CalendarEventVectorObj.getEvents(TimeService.newTimeRange(startTime,endTime,true,true));
					
					dateObj.setEvents(eventList);
					
					// keep iterator of events in the dateObj
					numberOfDays--;
					monthObj.setDay(dateObj,weekInMonth,dayInWeek);
					start = false;
					
				}
				else if (start == true)
				{
					// fill empty spaces for the first days in the first week in the month before reach the first day of the month
					dateObj.setTodayDate(m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),m_calObj.getYear());
					
					startTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),00,00,00,000);
					endTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),23,59,59,999);
					
					timeRange = TimeService.newTimeRange(startTime,endTime,true,true);
					
					eventList = CalendarEventVectorObj.getEvents(timeRange);
					dateObj.setEvents(eventList);
					
					monthObj.setDay(dateObj,weekInMonth,dayInWeek);
					dateObj.setFlag(0);
				}// end else
			}// end for m
		}// end for i
		
		// Construct the weeks left in the month and save it in the monthObj.
		// row is the max number of rows in the month., Col is equal to 7 which is the max number of col in the month.
		for(int row = 1; row<6; row++)
		{
			// Col is equal to 7 which is the max number of col in tin he month.
			for(int col = 0; col<7; col++)
			{
				if (numberOfDays != 0)
				{
					dateObj = new MyDate();
					m_calObj.nextDate();
					if ((m_calObj.getDayOfMonth() == state.getcurrentDay()) && (state.getcurrentMonth()== m_calObj.getMonthInteger()) && (state.getcurrentYear() == m_calObj.getYear()))
						dateObj.setFlag(1);
					
					dateObj.setTodayDate(m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),m_calObj.getYear());
					startTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),00,00,00,000);
					endTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),23,59,59,999);
					
					
					timeRange = TimeService.newTimeRange(startTime,endTime,true,true);
					eventList = CalendarEventVectorObj.getEvents(timeRange);
					dateObj.setEvents(eventList);
					
					numberOfDays--;
					monthObj.setDay(dateObj,row,col);
					monthObj.setRow(row);
				}
				else // if it is not the end of week , complete the week wih days from next month.
				{
					if ((m_calObj.getDay_Of_Week(true))== 7) // if end of week, exit the loop
					{
						row  = 7;
						col = 8;
					}
					else // if it is not the end of week, complete with days from next month
					{
						dateObj = new MyDate();
						m_calObj.nextDate();
						dateObj.setTodayDate(m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),m_calObj.getYear());
						
						startTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),00,00,00,000);
						endTime = TimeService.newTimeLocal(m_calObj.getYear(),m_calObj.getMonthInteger(),m_calObj.getDayOfMonth(),23,59,59,999);
						
						timeRange = TimeService.newTimeRange(startTime,endTime,true,true);
						
						eventList = CalendarEventVectorObj.getEvents(timeRange);
						dateObj.setEvents(eventList);
						monthObj.setDay(dateObj,row,col);
						monthObj.setRow(row);
						dateObj.setFlag(0);
					}
				}
			}// end for
		}// end for
		
		return monthObj;
	}
	
	
	public void doAttachments(RunData rundata, Context context)
	{
		// get into helper mode with this helper tool
		startHelper(rundata.getRequest(), "sakai.filepicker");

		// setup the parameters for the helper
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		CalendarActionState State = (CalendarActionState)getState( context, rundata, CalendarActionState.class );
		
		int houri;

		// put a the real attachments into the stats - let the helper update it directly if the user chooses to save their attachment editing.
		List attachments = State.getAttachments();
		state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, attachments);

		String hour = "";
		hour = rundata.getParameters().getString("startHour");
		String title ="";
		title = rundata.getParameters().getString("activitytitle");
		String minute = "";
		minute = rundata.getParameters().getString("startMinute");
		String dhour = "";
		dhour = rundata.getParameters().getString("duHour");
		String dminute = "";
		dminute = rundata.getParameters().getString("duMinute");
		String description = "";
		description = rundata.getParameters().getString("description");
		description = processFormattedTextFromBrowser(state, description);
		String month = "";
		month = rundata.getParameters().getString("month");		
		String day = "";
		day = rundata.getParameters().getString("day");
		String year = "";
		year = rundata.getParameters().getString("yearSelect");
		String timeType = "";
		timeType = rundata.getParameters().getString("startAmpm");
		String type = "";
		type = rundata.getParameters().getString("eventType");
		String location = "";
		location = rundata.getParameters().getString("location");
		
		readEventGroupForm(rundata, context);
		
		// read the recurrence modification intention
		String intentionStr = rundata.getParameters().getString("intention");
		if (intentionStr == null) intentionStr = "";
		
		Calendar calendarObj = null;
		
		String calId = State.getPrimaryCalendarReference();
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
		}
		catch(IdUnusedException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereis"));
			log.debug(".buildCustomizeContext(): " + e);
			return;
		}
		catch (PermissionException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont")); 
			log.debug(".buildCustomizeContext(): " + e);
			return;
		}
		
		Map addfieldsMap = new HashMap();
		
		// Add any additional fields in the calendar.
		customizeCalendarPage. loadAdditionalFieldsMapFromRunData(rundata, addfieldsMap, calendarObj);
		
		if (timeType.equals("pm"))
		{
			if (Integer.parseInt(hour)>11)
				houri = Integer.parseInt(hour);
			else
				houri = Integer.parseInt(hour)+12;
		}
		else if (timeType.equals("am") && Integer.parseInt(hour)==12)
		{
			houri = 24;
		}
		else
		{
			houri = Integer.parseInt(hour);
		}
		
		State.clearData();
		State.setNewData(State.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
		
		// **************** changed for the new attachment editor **************************

	} // doAttachments
	
	/**
	 * Action is used when doDescription is requested when the user click on an event
	 */
	public void doDescription(RunData data, Context context)
	{
		CalendarEvent calendarEventObj = null;
		Calendar calendarObj = null;
		
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		// "crack" the reference (a.k.a dereference, i.e. make a Reference)
		// and get the event id and calendar reference
		Reference ref = EntityManager.newReference(data.getParameters().getString(EVENT_REFERENCE_PARAMETER));
		String eventId;
		String calId;
		if (CalendarService.REF_TYPE_EVENT_SUBSCRIPTION.equals(ref.getSubType())) {
			calId = CalendarService.calendarSubscriptionReference(ref.getContext(), ref.getContainer());
			eventId = ExternalCalendarSubscriptionService.decodeIdFromRecurrence(ref.getId());
		} else {
			calId = CalendarService.calendarReference(ref.getContext(), ref.getContainer());
			eventId = ref.getId();
		}

		// %%% get the event object from the reference new Reference(data.getParameters().getString(EVENT_REFERENCE_PARAMETER)).getResource() -ggolden
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			try
			{
				calendarEventObj = calendarObj.getEvent(eventId);
				
				TimeBreakdown b = calendarEventObj.getRange().firstTime().breakdownLocal();
				
				sstate.setAttribute(STATE_YEAR,	Integer.valueOf(b.getYear()));
				sstate.setAttribute(STATE_MONTH,	 Integer.valueOf(b.getMonth()));
				sstate.setAttribute(STATE_DAY,  Integer.valueOf(b.getDay()));
				
				sstate.setAttribute(STATE_NAV_DIRECTION, STATE_CURRENT_ACT);
				
			}
			catch (IdUnusedException err)
			{
				// if this event doesn't exist, let user not go to the detail view
				// set the state recorded ID as null
				// show the alert message
				log.debug(".IdUnusedException " + err);
				state.setCalendarEventId("", "");
				String errorCode = rb.getString("java.error");
				addAlert(sstate, errorCode);
				return;
			}
			catch (PermissionException err)
			{
				addAlert(sstate, rb.getString("java.alert.youcreate"));
				log.debug(".PermissionException " + err);
				return;
			}
		}
		catch (IdUnusedException  e)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
			return;
		}
		catch (PermissionException	 e)
		{
			addAlert(sstate, rb.getString("java.alert.youcreate"));
			return;
		}
		
		// store the state coming from, like day view, week view, month view or list view
		String returnState = state.getState();
		state.setPrevState(CalendarAction.STATE_INITED);
		state.setReturnState(CalendarAction.STATE_INITED);
		state.setState("description");
		state.setAttachments(null);
		state.setCalendarEventId(calId, eventId);
	}		// doDescription

	/**
	 * Action is used when doOk is requested when user click on Back button
	 */
	public void doOk(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		
		// return to the state coming from
		String returnState = state.getReturnState();
		if( "".equals(returnState) || CalendarAction.STATE_INITED.equals(returnState) ) {
			returnState = this.defaultStateView;
		}
		state.setState(returnState);
	}
	
	
	/**
	 * Action is used when the user click on the doRevise in the menu
	 */
	public void doRevise(RunData data, Context context)
	{
		CalendarEvent calendarEventObj = null;
		Calendar calendarObj = null;
		
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		String calId = state.getPrimaryCalendarReference();
		state.setPrevState(state.getState());
		state.setState("goToReviseCalendar");
		state.setIsNewCalendar(false);
		state.setfromAttachmentFlag("false");
		sstate.setAttribute(FREQUENCY_SELECT, null);
		sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);

		state.clearData();
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			try
			{
				String eventId = state.getCalendarEventId();
				// get the edit object, and lock the event for the furthur revise
				CalendarEventEdit edit = calendarObj.getEditEvent(eventId, org.sakaiproject.calendar.api.CalendarService.EVENT_MODIFY_CALENDAR);
				state.setEdit(edit);
				state.setPrimaryCalendarEdit(edit);
				calendarEventObj = calendarObj.getEvent(eventId);
				state.setAttachments(calendarEventObj.getAttachments());
			}
			catch (IdUnusedException err)
			{
				// if this event doesn't exist, let user stay in activity view
				// set the state recorded ID as null
				// show the alert message
				// reset the menu button display, no revise/delete
				log.debug(".IdUnusedException " + err);
				state.setState("description");
				state.setCalendarEventId("", "");
				String errorCode = rb.getString("java.alert.event"); 
				addAlert(sstate, errorCode);
			}
			catch (PermissionException err)
			{
				log.debug(".PermissionException " + err);
			}
			catch (InUseException err)
			{
				log.debug(".InUseException " + err);
				state.setState("description");
				String errorCode = rb.getString("java.alert.eventbeing");
				addAlert(sstate, errorCode);
			}
		}
		catch (IdUnusedException  e)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
		}
		catch (PermissionException	 e)
		{
			addAlert(sstate, rb.getString("java.alert.youcreate"));
		}
		
	} // doRevise
	
	
	/**
	 * Handle the "continue" button on the schedule import wizard.
	 */
	public void doScheduleContinue(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		if ( SELECT_TYPE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()) )
		{
			// If the type is Outlook, the next state is
			// the "other" file select mode where we just select a file without
			// all of the extra info on the generic import page.
			
			String importType = data.getParameters ().getString(WIZARD_IMPORT_TYPE);
			
			
			if ( CalendarImporterService.OUTLOOK_IMPORT.equals(importType) || CalendarImporterService.ICALENDAR_IMPORT.equals(importType))
			{
				if (CalendarImporterService.OUTLOOK_IMPORT.equals(importType))
				{
					state.setImportWizardType(CalendarImporterService.OUTLOOK_IMPORT);
					state.setImportWizardState(OTHER_SELECT_FILE_IMPORT_WIZARD_STATE);
				}
				else if (CalendarImporterService.ICALENDAR_IMPORT.equals(importType))
				{
					state.setImportWizardType(CalendarImporterService.ICALENDAR_IMPORT);
					state.setImportWizardState(ICAL_SELECT_FILE_IMPORT_WIZARD_STATE);
				}
			}
			else
			{
				// Remember the type we're importing
				state.setImportWizardType(CalendarImporterService.CSV_IMPORT);
				
				state.setImportWizardState(GENERIC_SELECT_FILE_IMPORT_WIZARD_STATE);
			}
		}
		else if ( GENERIC_SELECT_FILE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()) )
		{ 
			boolean importSucceeded = false;
			
			// Do the import and send us to the confirm page
			FileItem importFile = data.getParameters().getFileItem(WIZARD_IMPORT_FILE);
			
			try (InputStream stream = importFile.getInputStream())
			{
				Map columnMap = CalendarImporterService.getDefaultColumnMap(CalendarImporterService.CSV_IMPORT);
				
				String [] addFieldsCalendarArray = getCustomFieldsArray(state, sstate);

				if ( addFieldsCalendarArray != null )
				{
					// Add all custom columns.	 Assume that there will be no 
					// name collisions. (Maybe a marginal assumption.)
					for ( int i=0; i < addFieldsCalendarArray.length; i++)
					{
						columnMap.put(
							addFieldsCalendarArray[i],
							addFieldsCalendarArray[i]);
					}
				}

				state.setWizardImportedEvents(
						CalendarImporterService.doImport(
								CalendarImporterService.CSV_IMPORT,
								stream,
								columnMap,
								addFieldsCalendarArray));

				importSucceeded = true;
			}
			catch (ImportException e)
			{
				addAlert(sstate, e.getMessage());
			}
			catch (IOException e)
			{
				log.warn("Failed to close stream.", e);
			}

			if ( importSucceeded )
			{
				// If all is well, go on to the confirmation page. 
				state.setImportWizardState(CONFIRM_IMPORT_WIZARD_STATE);
			}
			else
			{
				// If there are errors, send us back to the file selection page.
				state.setImportWizardState(GENERIC_SELECT_FILE_IMPORT_WIZARD_STATE);
			}
		}
		else if ( OTHER_SELECT_FILE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()) ||
					 ICAL_SELECT_FILE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()) )
		{ 
			boolean importSucceeded = false;

			// Do the import and send us to the confirm page
			FileItem importFile = data.getParameters().getFileItem(WIZARD_IMPORT_FILE);
			
			String [] addFieldsCalendarArray = getCustomFieldsArray(state, sstate);
			
			try (InputStream stream = importFile.getInputStream())
			{
				state.setWizardImportedEvents(
					CalendarImporterService.doImport(
						state.getImportWizardType(),
						stream,
						null,
						addFieldsCalendarArray));
						
				importSucceeded = true;
			}
			catch (ImportException e)
			{
				addAlert(sstate, e.getMessage());
			}
			catch (IOException e)
			{
				log.warn("Failed to close stream.", e);
			}
				
			if ( importSucceeded )
			{
				// If all is well, go on to the confirmation page. 
				state.setImportWizardState(CONFIRM_IMPORT_WIZARD_STATE);
			}
			else
			{
				// If there are errors, send us back to the file selection page.
				state.setImportWizardState(OTHER_SELECT_FILE_IMPORT_WIZARD_STATE);
			}
		}
		else if ( CONFIRM_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()) )
		{
			// If there are errors, send us back to Either
			// the OTHER_SELECT_FILE or GENERIC_SELECT_FILE states.
			// Otherwise, we're done.
			
			List wizardCandidateEventList = state.getWizardImportedEvents();
			
			// for group awareness - read user selection
			readEventGroupForm(data, context);
			
			String scheduleTo = (String)sstate.getAttribute(STATE_SCHEDULE_TO);
			Collection groupChoice = (Collection) sstate.getAttribute(STATE_SCHEDULE_TO_GROUPS);
			
			if ( scheduleTo != null && 
				  ( scheduleTo.equals("site") || 
					 (scheduleTo.equals("groups") && groupChoice!=null && groupChoice.size()>0) ) )
			{
				for ( int i =0; i < wizardCandidateEventList.size(); i++ )
				{
					// The line numbers are one-based.
					String selectionName =	"eventSelected" + (i+1);
					String selectdValue = data.getParameters().getString(selectionName);
					
					if ( Boolean.TRUE.toString().equals(selectdValue) )
					{
						// Add the events
						String calId = state.getPrimaryCalendarReference();
						try
						{
							Calendar calendarObj = CalendarService.getCalendar(calId);
							CalendarEvent event = (CalendarEvent) wizardCandidateEventList.get(i);
							
							CalendarEventEdit newEvent = calendarObj.addEvent();
							state.setEdit(newEvent);
							
							if ( event.getDescriptionFormatted() != null )
							{
								newEvent.setDescriptionFormatted( event.getDescriptionFormatted() ); 
							}
							
							// Range must be present at this point, so don't check for null.
							newEvent.setRange(event.getRange());
	
							if ( event.getDisplayName() != null )
							{
								newEvent.setDisplayName(event.getDisplayName());
							}
							 
							// The type must have either been set or defaulted by this point.
							newEvent.setType(event.getType());
							 
							if ( event.getLocation() != null )
							{
								newEvent.setLocation(event.getLocation());
							}
							 
							if ( event.getRecurrenceRule() != null )
							{
								newEvent.setRecurrenceRule(event.getRecurrenceRule()); 
							}
							
							String [] customFields = getCustomFieldsArray(state, sstate); 
							
							// Set the creator
							newEvent.setCreator();
						
							// Copy any custom fields.
							if ( customFields != null )
							{
								for ( int j = 0; j < customFields.length; j++ )
								{
									newEvent.setField(customFields[j], event.getField(customFields[j]));
								}
							} 
	
							// group awareness
							try
							{
								// for site event
								if (scheduleTo.equals("site"))
								{
									newEvent.clearGroupAccess();
								}

								// for grouped event
								else if (scheduleTo.equals("groups"))
								{
									Site site = SiteService.getSite(calendarObj.getContext());
									
									// make a collection of Group objects from the collection of group ref strings
									Collection groups = new Vector();
									for (Iterator iGroups = groupChoice.iterator(); iGroups.hasNext();)
									{
										String groupRef = (String) iGroups.next();
										groups.add(site.getGroup(groupRef));
									}
									
									newEvent.setGroupAccess(groups, true);
								}
							}
							catch (Exception e)
							{
								log.warn("doScheduleContinue: " + e);
							}
							
							calendarObj.commitEvent(newEvent);
							state.setEdit(null);							
						}
						catch (IdUnusedException e)
						{
							addAlert(sstate, e.getMessage());
							log.debug(".doScheduleContinue(): " + e);
							break;
						}
						catch (PermissionException e)
						{
							addAlert(sstate, e.getMessage());
							log.debug(".doScheduleContinue(): " + e);
							break;
						}
					}
				}
				
				// Cancel wizard mode.
				doCancelImportWizard(data, context);
			}
			else
			{
				addAlert(sstate, rb.getString("java.alert.youchoosegroup"));
			}
		}
		
	}

	/**
	 * Get an array of custom field names (if any)
	 */
	private String[] getCustomFieldsArray(
		CalendarActionState state,
		SessionState sstate)
	{
		Calendar calendarObj = null;
		
		try
		{
			calendarObj =
				CalendarService.getCalendar(
					state.getPrimaryCalendarReference());
		}
		catch (IdUnusedException e1)
		{
			// Ignore
		}
		catch (PermissionException e)
		{
			addAlert(sstate, e.getMessage());
		}
		
		// Get a current list of add fields.  This is a comma-delimited string.
		String[] addFieldsCalendarArray = null;
				
		if ( calendarObj != null )
		{
			String addfieldsCalendars = calendarObj.getEventFields();
			if (addfieldsCalendars != null)
			{
				addFieldsCalendarArray =
					fieldStringToArray(
						addfieldsCalendars,
						ADDFIELDS_DELIMITER);
			}
		}
		return addFieldsCalendarArray;
	}
	
	/**
	 * Handle the back button on the schedule import wizard
	 */
	public void doScheduleBack(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		
		if (GENERIC_SELECT_FILE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState())
			|| ICAL_SELECT_FILE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState())
			|| OTHER_SELECT_FILE_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()))
		{
			state.setImportWizardState(SELECT_TYPE_IMPORT_WIZARD_STATE);
		}
		else
		if (CONFIRM_IMPORT_WIZARD_STATE.equals(state.getImportWizardState()))
		{
			if (CalendarImporterService.OUTLOOK_IMPORT.equals(state.getImportWizardType()))
			{
				state.setImportWizardState(OTHER_SELECT_FILE_IMPORT_WIZARD_STATE);
			}
			else if (CalendarImporterService.ICALENDAR_IMPORT.equals(state.getImportWizardType()))
			{
				state.setImportWizardState(ICAL_SELECT_FILE_IMPORT_WIZARD_STATE);
			}
			else
			{
				state.setImportWizardState(GENERIC_SELECT_FILE_IMPORT_WIZARD_STATE);
			}
		}
	}
	
	/**
	 * Called when the user cancels the import wizard.
	 */
	public void doCancelImportWizard(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);

		// Get rid of any events
		state.setWizardImportedEvents(null);
		
		// Make sure that we start the wizard at the beginning.
		state.setImportWizardState(null);
		
		// Return to the previous state.
		state.setState(this.defaultStateView);
	}
	
	/**
	 * Action is used when the docancel is requested when the user click on cancel  in the new view
	 */
	public void doCancel(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		Calendar calendarObj = null;
		String currentState = state.getState();
		String returnState = this.defaultStateView;
		
		if (currentState.equals(STATE_NEW))
		{
			// no need to release the lock. 
			// clear the saved recurring rule and the selected frequency
			sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
			sstate.setAttribute(FREQUENCY_SELECT, null);
		}
		else
			if (currentState.equals(STATE_CUSTOMIZE_CALENDAR))
			{
				customizeCalendarPage.doCancel(data, context, state, getSessionState(data));
				returnState=state.getPrevState();
				if (returnState.endsWith("!!!fromDescription"))
				{
					state.setReturnState(returnState.substring(0, returnState.indexOf("!!!fromDescription")));
					returnState = "description";
				} else {
					returnState = this.defaultStateView;
				}
			}
			else
				if (currentState.equals(STATE_CALENDAR_SUBSCRIPTIONS))
				{
					calendarSubscriptionsPage.doCancel(data, context, state, getSessionState(data));
					//returnState=state.getPrevState();
					returnState=state.getReturnState();
					
					if (returnState.endsWith("!!!fromDescription"))
					{
						state.setReturnState(returnState.substring(0, returnState.indexOf("!!!fromDescription")));
						returnState = "description";
					} else {
						returnState = this.defaultStateView;
					}
				}
				else
				if (currentState.equals(STATE_MERGE_CALENDARS))
				{
					mergedCalendarPage.doCancel(data, context, state, getSessionState(data));
					returnState=state.getReturnState();
					
					if (returnState.endsWith("!!!fromDescription"))
					{
						state.setReturnState(returnState.substring(0, returnState.indexOf("!!!fromDescription")));
						returnState = "description";
					} else {
						returnState = this.defaultStateView;
					}
				}
				else	// in revise view, state name varies
					if ((currentState.equals("revise"))|| (currentState.equals("goToReviseCalendar")))
					{
						String calId = state.getPrimaryCalendarReference();
						
						if (state.getPrimaryCalendarEdit() != null)
						{
							try
							{
								calendarObj = CalendarService.getCalendar(calId);
								
								// the event is locked, now we need to release the lock
								calendarObj.cancelEvent(state.getPrimaryCalendarEdit());
								state.setPrimaryCalendarEdit(null);
								state.setEdit(null);
							}
							catch (IdUnusedException  e)
							{
								addAlert(sstate, rb.getString("java.alert.noexist"));
							}
							catch (PermissionException	 e)
							{
								addAlert(sstate, rb.getString("java.alert.youcreate"));
							}
						}
						// clear the saved recurring rule and the selected frequency
						sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
						sstate.setAttribute(FREQUENCY_SELECT, null);
					}
					else if (currentState.equals(STATE_SET_FREQUENCY))// cancel at frequency editing page
					{
						returnState = (String)sstate.getAttribute(STATE_BEFORE_SET_RECURRENCE);
					}

		state.setState(returnState);
		
		state.setAttachments(null);
	}	 // doCancel
	
	
	/**
	 * Action is used when the doBack is called when the user click on the back on the EventActivity view
	 */
	public void doBack(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		Calendar calendarObj = null;
		String calId = state.getPrimaryCalendarReference();
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			
			// the event is locked, now we need to release the lock
			calendarObj.cancelEvent(state.getPrimaryCalendarEdit());
			state.setPrimaryCalendarEdit(null);
			state.setEdit(null);
		}
		catch (IdUnusedException  e)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
		}
		catch (PermissionException	 e)
		{
			addAlert(sstate, rb.getString("java.alert.youcreate"));
		}
		
		String returnState = state.getReturnState();
		if(StringUtils.isBlank(returnState) || CalendarAction.STATE_INITED.equals(returnState)) {
			returnState = this.defaultStateView;
		}
		state.setState(returnState);
		
	}	 // doBack
	
	
	/**
	 * Action is used when the doDelete is called when the user click on delete in menu
	 */
	
	public void doDelete(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		CalendarEvent calendarEventObj = null;
		Calendar calendarObj = null;
		String calId = state.getPrimaryCalendarReference();
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			
			try
			{
				String eventId = state.getCalendarEventId();
				// get the edit object, and lock the event for the furthur revise
				CalendarEventEdit edit = calendarObj.getEditEvent(eventId,
																				  org.sakaiproject.calendar.api.CalendarService.EVENT_REMOVE_CALENDAR);
				state.setEdit(edit);
				state.setPrimaryCalendarEdit(edit);
				calendarEventObj = calendarObj.getEvent(eventId);
				state.setAttachments(calendarEventObj.getAttachments());
				
				// after deletion, it needs to go back to previous page
				// if coming from description, it won't go back to description
				// but the state one step ealier
				String returnState = state.getState();
				if (!returnState.equals("description"))
				{
					state.setReturnState(returnState);
				}
				state.setState("delete");

				// Delete task
				String reference = "/calendar/dashboard/" + calendarObj.getContext() + Entity.SEPARATOR + calendarObj.getId() + Entity.SEPARATOR + eventId;
				taskService.removeTaskByReference(reference);
			}
			catch (IdUnusedException err)
			{
				// if this event doesn't exist, let user stay in activity view
				// set the state recorded ID as null
				// show the alert message
				// reset the menu button display, no revise/delete
				log.debug(".IdUnusedException " + err);
				state.setState("description");
				state.setCalendarEventId("", "");
				String errorCode = rb.getString("java.alert.event");
				addAlert(sstate, errorCode);
			}
			catch (PermissionException err)
			{
				log.debug(".PermissionException " + err);
			}
			catch (InUseException err)
			{
				log.debug(".InUseException delete" + err);
				state.setState("description");
				String errorCode = rb.getString("java.alert.eventbeing");
				addAlert(sstate, errorCode);
			}
		}
		catch (IdUnusedException  e)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
		}
		catch (PermissionException	 e)
		{
			addAlert(sstate, rb.getString("java.alert.youcreate"));
		}
	}	 // doDelete
	
	/**
	 * Action is used when the doConfirm is called when the user click on confirm to delete event in the delete view.
	 */
	
	public void doConfirm(RunData data, Context context)
	{
		Calendar calendarObj = null;
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		// read the intention field
		String intentionStr = data.getParameters().getString("intention");
		int intention = CalendarService.MOD_NA;
		if ("t".equals(intentionStr)) intention = CalendarService.MOD_THIS;

		String calId = state.getPrimaryCalendarReference();
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			CalendarEventEdit edit = state.getPrimaryCalendarEdit();
			calendarObj.removeEvent(edit, intention);
			state.setPrimaryCalendarEdit(null);
		}
		catch (IdUnusedException  e)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
			log.debug(".doConfirm(): " + e);
		}
		catch (PermissionException	 e)
		{
			addAlert(sstate, rb.getString("java.alert.youcreate"));
			log.debug(".doConfirm(): " + e);
		}
		
		String stateName = ServerConfigurationService.getString("calendar.default.view", defaultStateView);
		state.setState(stateName);
		state.setReturnState(stateName);
		
	} // doConfirm
	
	
	public void doView (RunData data, Context context)
	{
			SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
	
			String viewMode = data.getParameters ().getString("view");
			if (StringUtils.isNotBlank(viewMode) && viewMode.equalsIgnoreCase(rb.getString("java.listeve"))) {
				doList(data, context);
			} else {
				doViewCalendar(data, context);
			}
			state.setAttribute(STATE_SELECTED_VIEW, viewMode);
	
	}	// doView

	/**
	 * Action doViewCalendar is requested when the user click on Calendar on menu
	 */
	public void doViewCalendar(RunData data, Context context) {
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		state.setState(CALENDAR_INIT_PARAMETER);
		this.defaultStateView = CALENDAR_INIT_PARAMETER;
	}	 // doViewCalendar

	/**
	 * Action doCustomDate is requested when the user specifies a start/end date
	 * to filter the list view.
	 */
	public void doCustomdate(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		String sY = data.getParameters().getString(TIME_FILTER_SETTING_CUSTOM_START_YEAR);
		String sM = data.getParameters().getString(TIME_FILTER_SETTING_CUSTOM_START_MONTH);
		String sD = data.getParameters().getString(TIME_FILTER_SETTING_CUSTOM_START_DAY);
		String eY = data.getParameters().getString(TIME_FILTER_SETTING_CUSTOM_END_YEAR);
		String eM = data.getParameters().getString(TIME_FILTER_SETTING_CUSTOM_END_MONTH);
		String eD = data.getParameters().getString(TIME_FILTER_SETTING_CUSTOM_END_DAY);
		if (sM.length() == 1) sM = "0"+sM;
		if (eM.length() == 1) eM = "0"+eM;
		if (sD.length() == 1) sD = "0"+sD;
		if (eD.length() == 1) eD = "0"+eD;
		sY = sY.substring(2);
		eY = eY.substring(2);
		
		String startingDateStr = sM + "/" + sD + "/" + sY;
		String endingDateStr	  = eM + "/" + eD + "/" + eY;
		
		// Pass in a buffer for a possible error message.
		StringBuilder errorMessage = new StringBuilder();
		
		// Try to simultaneously set the start/end dates.
		// If that doesn't work, add an error message.
		if ( !state.getCalendarFilter().setStartAndEndListViewDates(startingDateStr, endingDateStr, errorMessage) )
		{
			addAlert(sstate, errorMessage.toString());
		}
		
	}	 // doCustomdate
	
	/**
	 * Action doFilter is requested when the user clicks on the list box
	 * to select a filtering mode for the list view.
	 */
	public void doFilter(RunData data, Context context)
	{
		CalendarActionState state =
			(CalendarActionState) getState(context,
				data,
				CalendarActionState.class);

		state.getCalendarFilter().setListViewFilterMode(
			data.getParameters().getString(TIME_FILTER_OPTION_VAR));
		
	}	 // doFilter

	/*
	 * Action is requsted when the user select day from menu in Activityevent view.
	 */
	
	public void doActivityday(RunData data, Context context)
	{
		
		CalendarEvent ce = null;
		Calendar calendarObj = null;
		
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		CalendarUtil m_calObj = new CalendarUtil();
		
		String id = state.getCalendarEventId();
		
		String calId = state.getPrimaryCalendarReference();
		
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			ce = calendarObj.getEvent(id);
		}
		catch (IdUnusedException  e)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
			log.warn(".doActivityday(): " + e);
			return;
		}
		catch (PermissionException	 e)
		{
			addAlert(sstate, rb.getString("java.alert.youcreate"));
			log.warn(".doActivityday(): " + e);
			return;
		}
		
		TimeRange tr = ce.getRange();
		Time t = tr.firstTime();
		TimeBreakdown b = t.breakdownLocal();
		m_calObj.setDay(b.getYear(),b.getMonth(),b.getDay()) ;
		
		sstate.setAttribute(STATE_YEAR, Integer.valueOf(b.getYear()));
		sstate.setAttribute(STATE_MONTH, Integer.valueOf(b.getMonth()));
		sstate.setAttribute(STATE_DAY, Integer.valueOf(b.getDay()));
		
		state.setState("day");
		
	} // doActivityDay
	
	/**
	 * Enter the schedule import wizard
	 */
	public void doImport(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		sstate.removeAttribute(STATE_SCHEDULE_TO);
		sstate.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
		
		// Remember the state prior to entering the wizard.
		state.setPrevState(state.getState());
		
		// Enter wizard mode.
		state.setState(STATE_SCHEDULE_IMPORT);
		
	}	 // doImport

	/**
	 * Action doIcalExportName acts on a "Export" request
	 */
	public void doIcalExportName(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		sstate.removeAttribute(STATE_SCHEDULE_TO);
		sstate.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
		
		//	 store the state coming from
		String returnState = state.getState();
		if ( ! returnState.equals("description") )
		{
			state.setReturnState(returnState);
		}
		
		state.clearData();
		state.setAttachments(null);
		state.setPrevState(state.getState());
		state.setState("icalEx");
	}	 // doIcalExportName
	
	/**
	 * Action doIcalExport acts on a "Submit" request in the icalexport form
	 */
	public void doIcalExport(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		String enable = StringUtils.trimToNull(data.getParameters().getString(FORM_ICAL_ENABLE));
		String alias = StringUtils.trimToNull(data.getParameters().getString(FORM_ALIAS));
      
		// this will verify that no invalid characters are used
		if ( ! Validator.escapeResourceName(alias).equals(alias) )
		{
			addAlert(sstate, rb.getString("java.alert.invalidname"));
			return;
		}
		
		String calId = state.getPrimaryCalendarReference();
		Calendar calendarObj = null;
		
		boolean oldExportEnabled = CalendarService.getExportEnabled(calId);
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
		
			List aliasList =	aliasService.getAliases( calendarObj.getReference() );
			String oldAlias = null;
			if ( ! aliasList.isEmpty() )
			{
				String aliasSplit[] = ((Alias)aliasList.get(0)).getId().split("\\.");
				oldAlias =	aliasSplit[0];
			}
		
			// Add the desired alias (if changed)
			if ( alias != null && (oldAlias == null || !oldAlias.equals(alias)) )
			{
				// first, clear any alias set to this calendar
				aliasService.removeTargetAliases(calendarObj.getReference());
				
				alias += ICAL_EXTENSION;
				aliasService.setAlias(alias, calendarObj.getReference());
			}
		}
		catch (IdUnusedException ie)
		{
			addAlert(sstate, rb.getString("java.alert.noexist"));
			log.debug(".doIcalExport() Other: " + ie);
			return;
		}
		catch (IdUsedException ue)
		{
			addAlert(sstate, rb.getString("java.alert.dupalias"));
			return;
		}
		catch (PermissionException pe)
		{
			addAlert(sstate, rb.getString("java.alert.youdont"));
			return;
		}
		catch (IdInvalidException e)
		{
			addAlert(sstate, rb.getString("java.alert.unknown"));
			log.debug(".doIcalExport() Other: " + e);
			return;
		}
		
		// enable/disable export (if changed)
		if ( enable != null && !oldExportEnabled )
			CalendarService.setExportEnabled( calId, true );
		else if ( enable == null && oldExportEnabled )
			CalendarService.setExportEnabled( calId, false );
			
		String returnState = "icalEx";
		state.setState(returnState);
		
	}	 // doIcalExport
	
	public void doNew(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		//clean group awareness state info
		sstate.removeAttribute(STATE_SCHEDULE_TO);
		sstate.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
		
		// store the state coming from
		String returnState = state.getState();
		if ( ! returnState.equals("description") )
		{
			state.setReturnState(returnState);
		}
		
		state.clearData();
		state.setAttachments(null);
		state.setPrevState(state.getState());
		state.setState(STATE_NEW);
		state.setCalendarEventId("", "");
		state.setIsNewCalendar(true);
		sstate.setAttribute(FREQUENCY_SELECT, null);
		sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
		
	}	 // doNew
	
	/**
	 * Read user inputs in announcement form
	 * @param data
	 * @param checkForm need to check form data or not
	 */
	protected void readEventGroupForm(RunData rundata, Context context)
	{
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state =
			((JetspeedRunData) rundata).getPortletSessionState(peid);

		String scheduleTo = rundata.getParameters().getString("scheduleTo");
		state.setAttribute(STATE_SCHEDULE_TO, scheduleTo);
		if (scheduleTo.equals("groups"))
		{
			String[] groupChoice = rundata.getParameters().getStrings("selectedGroups");
			if (groupChoice != null)
			{
				state.setAttribute(STATE_SCHEDULE_TO_GROUPS, new ArrayList(Arrays.asList(groupChoice)));
			}
			
			if (groupChoice== null || groupChoice.length == 0)
			{
				state.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
			}
		}
		else
		{
			state.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
		}
		
	}	// readEventGroupForm
	
	/**
	 * Action doAdd is requested when the user click on the add in the new view to add an event into a calendar.
	 */
	
	public void doAdd(RunData runData, Context context) {
		CalendarUtil m_calObj = new CalendarUtil();// null;
		Calendar calendarObj = null;
		int houri;
		
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		Time m_time = TimeService.newTime();
		TimeBreakdown b = m_time.breakdownLocal();
		int stateYear = b.getYear();
		int stateMonth = b.getMonth();
		int stateDay = b.getDay();
		if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null))
		{
			stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
			stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
			stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
		}
		m_calObj.setDay(stateYear, stateMonth, stateDay);
		
		String hour = "";
		hour = runData.getParameters().getString("startHour");
		String title ="";
		title = runData.getParameters().getString("activitytitle");
		String minute = "";
		minute = runData.getParameters().getString("startMinute");
		String dhour = "";
		dhour = runData.getParameters().getString("duHour");
		String dminute = "";
		dminute = runData.getParameters().getString("duMinute");
		String description = "";
		description = runData.getParameters().getString("description");
		description = processFormattedTextFromBrowser(sstate, description);
		String month = "";
		month = runData.getParameters().getString("month");
		
		String day = "";
		day = runData.getParameters().getString("day");
		String year = "";
		year = runData.getParameters().getString("yearSelect");
		String timeType = "";
		timeType = runData.getParameters().getString("startAmpm");
		String type = "";
		type = runData.getParameters().getString("eventType");
		String location = "";
		location = runData.getParameters().getString("location");

        String siteId = ToolManager.getCurrentPlacement().getContext();
		
		String calId = state.getPrimaryCalendarReference();
		try {
			calendarObj = CalendarService.getCalendar(calId);
		} catch(IdUnusedException e) {
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereisno"));
			log.debug(".doAdd(): " + e);
			return;
		} catch (PermissionException e) {
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
			log.debug(".doAdd(): " + e);
			return;
		}

		// for section awareness - read user selection
		readEventGroupForm(runData, context);

		Map addfieldsMap = new HashMap();

		// Add any additional fields in the calendar.
		customizeCalendarPage.loadAdditionalFieldsMapFromRunData(runData, addfieldsMap, calendarObj);
		
		if (timeType.equals("pm")) {
			if (Integer.parseInt(hour)>11)
				houri = Integer.parseInt(hour);
			else
				houri = Integer.parseInt(hour)+12;
		}
		else if (timeType.equals("am") && Integer.parseInt(hour)==12) {
			// set 12 AM as the beginning of one day
			houri = 0;
		}
		else {
			houri = Integer.parseInt(hour);
		}

		Time now_time = TimeService.newTime();
		Time event_startTime = TimeService.newTimeLocal(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), houri, Integer.parseInt(minute), 0,	 0);

		// conditions for an new event:
		// 1st, frequency not touched, no save state rule or state freq (0, 0)
		// --> non-recurring, no alert needed (0)
		// 2st, frequency revised, there is a state-saved rule, and state-saved freq exists (1, 1)
		// --> no matter if the start has been modified, compare the ending and starting date, show alert if needed (1)
		// 3th, frequency revised, the state saved rule is null, but state-saved freq exists (0, 1)
		// --> non-recurring, no alert needed (0)
		// so the only possiblityto show the alert is under condistion 2.
		
		boolean earlierEnding = false;
		
		String freq = "";
		if ((( freq = (String) sstate.getAttribute(FREQUENCY_SELECT))!= null) && (!(freq.equals(FREQ_ONCE)))) {
			RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);
			if (rule != null) {
				Time startingTime = TimeService.newTimeLocal(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),houri,Integer.parseInt(minute),00,000);
				Time endingTime = rule.getUntil();
				if ((endingTime != null) && endingTime.before(startingTime))
					earlierEnding = true;
			} // if (rule != null)
		} // if state saved freq is not null, and it not equals "once"

		String intentionStr = ""; // there is no recurrence modification intention for new event

		String scheduleTo = (String)sstate.getAttribute(STATE_SCHEDULE_TO);
		Collection groupChoice = (Collection) sstate.getAttribute(STATE_SCHEDULE_TO_GROUPS);
		
		if(title.length() == 0) {
			String errorCode = rb.getString("java.pleasetitle");
			addAlert(sstate, errorCode);

			state.setNewData(state.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
			state.setState(STATE_NEW);
		}
		else if(hour.equals("100") || minute.equals("100")) {
			String errorCode = rb.getString("java.pleasetime");
			addAlert(sstate, errorCode);

			state.setNewData(state.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
			state.setState(STATE_NEW);
		} else if( earlierEnding ) {
			// if ending date is earlier than the starting date, show alert
			addAlert(sstate, rb.getString("java.theend") );

			state.setNewData(calId, title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
			state.setState(STATE_NEW);
		} else if (!DateFormatterUtil.checkDate(Integer.parseInt(day), Integer.parseInt(month), Integer.parseInt(year))) {
			addAlert(sstate, rb.getString("date.invalid"));
			state.setNewData(state.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
			state.setState(STATE_NEW);
		} else if (scheduleTo.equals("groups") && ((groupChoice == null) || (groupChoice.size() == 0))) {
			state.setNewData(state.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
			state.setState(STATE_NEW);
			addAlert(sstate, rb.getString("java.alert.youchoosegroup"));
		} else {
			try {
				calendarObj = CalendarService.getCalendar(calId);

				Time timeObj = TimeService.newTimeLocal(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),houri,Integer.parseInt(minute),00,000);

				long du = (((Integer.parseInt(dhour) * 60)*60)*1000) + ((Integer.parseInt(dminute)*60)*(1000));
				Time endTime = TimeService.newTime(timeObj.getTime() + du);
				boolean includeEndTime = false;
				if (du==0) {
					includeEndTime = true;
				}
				TimeRange range = TimeService.newTimeRange(timeObj, endTime, true, includeEndTime);
				List attachments = state.getAttachments();

				// prepare to create the event
				Collection<Group> groups = new Vector<>();
				CalendarEvent.EventAccess access = CalendarEvent.EventAccess.GROUPED;
				if (scheduleTo.equals("site")) access = CalendarEvent.EventAccess.SITE;

				if (access == CalendarEvent.EventAccess.GROUPED) {
					// make a collection of Group objects from the collection of group ref strings
					Site site = SiteService.getSite(calendarObj.getContext());
					for (Iterator iGroups = groupChoice.iterator(); iGroups.hasNext();) {
						String groupRef = (String) iGroups.next();
						groups.add(site.getGroup(groupRef));
					}
				}

				// create the event = must create it with grouping / access to start with
				CalendarEvent event = calendarObj.addEvent(range, title, "", type, location, access, groups, attachments);

				// edit it further
				CalendarEventEdit edit = calendarObj.getEditEvent(event.getId(), org.sakaiproject.calendar.api.CalendarService.EVENT_ADD_CALENDAR);
				edit.setDescriptionFormatted(description);
				edit.setCreator();
				String timeZone = TimeService.getLocalTimeZone().getID();
				// we obtain the time zone where the event is created
				// and save it as an event's property
				// it is necessary to generate re-occurring events correctly
				edit.setField("createdInTimeZone",timeZone);
				setFields(edit, addfieldsMap);

				RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);
				// for a brand new event, there is no saved recurring rule
				if (rule != null)
					edit.setRecurrenceRule(rule);
				else
					edit.setRecurrenceRule(null);
				
				// save it
				calendarObj.commitEvent(edit);
				state.setEdit(null);
				state.setIsNewCalendar(false);
				m_calObj.setDay(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));
				sstate.setAttribute(STATE_YEAR, Integer.valueOf(m_calObj.getYear()));
				sstate.setAttribute(STATE_MONTH, Integer.valueOf(m_calObj.getMonthInteger()));
				sstate.setAttribute(STATE_DAY, Integer.valueOf(m_calObj.getDayOfMonth()));

				// clear the saved recurring rule and the selected frequency
				sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
				sstate.setAttribute(FREQUENCY_SELECT, null);

				// return to the calendar view after saving the event
				state.setState(CALENDAR_INIT_PARAMETER);

				// clean state
				sstate.removeAttribute(STATE_SCHEDULE_TO);
				sstate.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
				
				// Create task
				String reference = "/calendar/dashboard/" + calendarObj.getContext() + Entity.SEPARATOR + calendarObj.getId() + Entity.SEPARATOR + edit.getId();
				Task task = new Task();
				task.setSiteId(calendarObj.getContext());
				task.setReference(reference);
				task.setSystem(true);
				task.setDescription(title);
				Date dueDate = new Date(event_startTime.getTime());
				task.setDue(dueDate == null ? null : dueDate.toInstant());
				Set<String> users = new HashSet();
				if (CalendarEvent.EventAccess.SITE.equals(access)) {
					Site site = SiteService.getSite(calendarObj.getContext());
					users = site.getUsersIsAllowed("section.role.student");
				} else if (CalendarEvent.EventAccess.GROUPED.equals(access)){
					for (Group group : groups) {
						task.getGroups().add(group.getReference());
						users.addAll(group.getMembers().stream()
							.map(m -> m.getUserId()).collect(Collectors.toSet()));
					}
				}
				if (users.size() == 0) {
					users.add(UserDirectoryService.getCurrentUser().getId());
				}
				taskService.createTask(task, users, Priorities.HIGH);
	
			} catch (IdUnusedException e) {
				addAlert(sstate, rb.getString("java.alert.noexist"));
				log.debug(".doAdd(): " + e);
			} catch (PermissionException e) {
				addAlert(sstate, rb.getString("java.alert.youcreate"));
				log.debug(".doAdd(): " + e);
			} catch (InUseException e) {
				addAlert(sstate, rb.getString("java.alert.noexist"));
				log.debug(".doAdd(): " + e);
			}

		}	 // elseif
	}	 // doAdd
	
	/**
	 * Action doUpdateGroupView is requested when the user click on the Update button on the list view.
	 */
	
	public void doUpdateGroupView(RunData runData, Context context)
	{
		readEventGroupForm(runData, context);		
		//stay at the list view
	}
	
	/**
	 * Action doUpdate is requested when the user click on the save button on the revise screen.
	 */
	
	public void doUpdate(RunData runData, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		CalendarUtil m_calObj= new CalendarUtil();
		
		// read the intention field
		String intentionStr = runData.getParameters().getString("intention");
		int intention = CalendarService.MOD_NA;
		if ("t".equals(intentionStr)) intention = CalendarService.MOD_THIS;
		
		// See if we're in the "options" state.
		if (state.getState().equalsIgnoreCase(STATE_MERGE_CALENDARS))
		{
			mergedCalendarPage.doUpdate(runData, context, state, getSessionState(runData));
			
			// ReturnState was set up above.	 Switch states now.
			String returnState = state.getReturnState();
			if(returnState.equals(CalendarAction.STATE_INITED)) {
				returnState = STATE_MERGE_CALENDARS;
			}
			if (returnState.endsWith("!!!fromDescription"))
			{
				state.setReturnState(returnState.substring(0, returnState.indexOf("!!!fromDescription")));
				state.setState("description");
			}
			else
			{
				state.setReturnState(STATE_MERGE_CALENDARS);
				state.setState(returnState);
			}
			
		}
		else
			if (state.getState().equalsIgnoreCase(STATE_CALENDAR_SUBSCRIPTIONS))
			{
				calendarSubscriptionsPage.doUpdate(runData, context, state, getSessionState(runData));
				
				// ReturnState was set up above.  Switch states now.
				String returnState = state.getReturnState();
				if(returnState.compareTo(CalendarAction.STATE_INITED) == 0) {
					returnState = this.defaultStateView;
				}
				if (StringUtils.endsWith(returnState, "!!!fromDescription"))
				{
					state.setReturnState(returnState.substring(0, returnState.indexOf("!!!fromDescription")));
					state.setState("description");
				}
				else
				{
					state.setReturnState(this.defaultStateView);
					state.setState(returnState);
				}
				
			}
		else
			if (state.getState().equalsIgnoreCase(STATE_CUSTOMIZE_CALENDAR))
			{
				//customizeCalendarPage.doDeletefield( runData, context, state, getSessionState(runData));
				customizeCalendarPage.doUpdate(runData, context, state, getSessionState(runData));
				
				// ReturnState was set up above.	 Switch states now.
				String returnState = state.getReturnState();
				if (returnState.endsWith("!!!fromDescription"))
				{
					state.setReturnState(returnState.substring(0, returnState.indexOf("!!!fromDescription")));
					state.setState("description");
				}
				else
				{
					state.setReturnState(CalendarAction.STATE_CUSTOMIZE_CALENDAR);
					state.setState(CalendarAction.STATE_CUSTOMIZE_CALENDAR);
				}
			}
			else
			{
				int houri;
				Calendar calendarObj = null;
				
				String hour = "";
				hour = runData.getParameters().getString("startHour");
				String title = "";
				title = runData.getParameters().getString("activitytitle");
				String minute = "";
				minute = runData.getParameters().getString("startMinute");
				String dhour = "";
				dhour = runData.getParameters().getString("duHour");
				String dminute = "";
				dminute = runData.getParameters().getString("duMinute");
				String description = "";
				description = runData.getParameters().getString("description");
				description = processFormattedTextFromBrowser(sstate, description);
				String month = "";
				month = runData.getParameters().getString("month");
				String day = "";
				day = runData.getParameters().getString("day");
				String year = "";
				year = runData.getParameters().getString("yearSelect");
				String timeType = "";
				timeType = runData.getParameters().getString("startAmpm");
				String type = "";
				type = runData.getParameters().getString("eventType");
				String location = "";
				location = runData.getParameters().getString("location");
				
				String calId = state.getPrimaryCalendarReference();
				try
				{
					calendarObj = CalendarService.getCalendar(calId);
				}
				catch (IdUnusedException e)
				{
					context.put(ALERT_MSG_KEY,rb.getString("java.alert.theresisno"));
					log.debug(".doUpdate() Other: " + e);
					return;
				}
				catch (PermissionException e)
				{
					context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
					log.debug(".doUpdate() Other: " + e);
					return;
				}
				
				// for group/section awareness
				readEventGroupForm(runData, context);
				
				String scheduleTo = (String)sstate.getAttribute(STATE_SCHEDULE_TO);
				Collection groupChoice = (Collection) sstate.getAttribute(STATE_SCHEDULE_TO_GROUPS);
				
				Map addfieldsMap = new HashMap();
				
				// Add any additional fields in the calendar.
				customizeCalendarPage.loadAdditionalFieldsMapFromRunData(runData, addfieldsMap, calendarObj);
				
				if (timeType.equals("pm"))
				{
					if (Integer.parseInt(hour)>11)
						houri = Integer.parseInt(hour);
					else
						houri = Integer.parseInt(hour)+12;
				}
				else if (timeType.equals("am") && Integer.parseInt(hour)==12)
				{
					houri = 0;
				}
				else
				{
					houri = Integer.parseInt(hour);
				}
				
				// conditions for an existing event: (if recurring event, if state-saved-rule exists, if state-saved-freq exists)
				// 1st, an existing recurring one, just revised without frequency change, no save state rule or state freq (1, 0, 0)
				// --> the starting time might has been modified, compare the ending and starting date, show alert if needed (1)
				// 2st, and existing non-recurring one, just revised, no save state rule or state freq (0, 0, 0)
				// --> non-recurring, no alert needed (0)
				// 3rd, an existing recurring one, frequency revised, there is a state-saved rule, and state-saved freq exists (1, 1, 1)
				// --> no matter if the start has been modified, compare the ending and starting date, show alert if needed (1)
				// 4th, an existing recurring one, changed to non-recurring, the state saved rule is null, but state-saved freq exists (1, 0, 1)
				// --> non-recurring, no alert needed (0)
				// 5th, an existing non-recurring one, changed but kept as non-recurring, the state-saved rule is null, but state-saved freq exists (1, 0, 1)
				// --> non-recurring, no alert needed (0)
				// 6th, an existing recurring one, changed only the starting time, showed alert for ealier ending time, 
				// so the only possiblity to show the alert is under condistion 1 & 3: recurring one stays as recurring
				
				boolean earlierEnding = false;
				
				
				CalendarEventEdit edit = state.getPrimaryCalendarEdit();
				if (edit != null)
				{
					RecurrenceRule editRule = edit.getRecurrenceRule();
					if ( editRule != null)
					{
						String freq = (String) sstate.getAttribute(FREQUENCY_SELECT);
						RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);
						boolean comparisonNeeded = false;
						
						if ((freq == null) && (rule == null))
						{
							// condition 1: recurring without frequency touched, but the starting might change
							rule = editRule;
							comparisonNeeded = true;
						}
						else if ((freq != null) && (!(freq.equals(FREQ_ONCE))))
						{
							// condition 3: recurring with frequency changed, and stays at recurring
							comparisonNeeded = true;
						}
						if (comparisonNeeded) // if under condition 1 or 3
						{
							if (rule != null)
							{
								Time startingTime = TimeService.newTimeLocal(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),houri,Integer.parseInt(minute),00,000);
								
								Time endingTime = rule.getUntil();
								if ((endingTime != null) && endingTime.before(startingTime))
									earlierEnding = true;
							} // if (editRule != null)
						} // if (comparisonNeeded) // if under condition 1 or 3
					} // if (calEvent.getRecurrenceRule() != null)
				} // if (edit != null)

				if(title.length()==0)
				{
					String errorCode = rb.getString("java.pleasetitle");
					addAlert(sstate, errorCode);
					
					state.setNewData(calId, title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
					state.setState("revise");
				}
				/*
				else if(hour.equals("0") && minute.equals("0"))
				{
					String errorCode = "Please enter a time";
					addAlert(sstate, errorCode);
					
					state.setNewData(calId, title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap);
					state.setState("revise");
				}
				 */
				else if( earlierEnding ) // if ending date is earlier than the starting date, show alert
				{
					addAlert(sstate, rb.getString("java.theend") );
					
					state.setNewData(calId, title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
					state.setState("revise");
				}
				else if (!DateFormatterUtil.checkDate(Integer.parseInt(day), Integer.parseInt(month), Integer.parseInt(year)))
				{
					addAlert(sstate, rb.getString("date.invalid"));
					state.setNewData(calId, title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
					state.setState("revise");
				}
				else if (scheduleTo.equals("groups") && ((groupChoice == null) || (groupChoice.size() == 0)))
				{
					state.setNewData(state.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
					state.setState("revise");
					addAlert(sstate, rb.getString("java.alert.youchoosegroup"));
				}
				else
				{
					try
					{
						calendarObj = CalendarService.getCalendar(calId);
						Time timeObj = TimeService.newTimeLocal(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),houri,Integer.parseInt(minute),00,000);
						
						long du = (((Integer.parseInt(dhour) * 60)*60)*1000) + ((Integer.parseInt(dminute)*60)*(1000));
						Time endTime = TimeService.newTime(timeObj.getTime() + du);
						boolean includeEndTime = false;
						TimeRange range = null;
						if (du==0)
						{
							range = TimeService.newTimeRange(timeObj);
						}
						else
						{
							range = TimeService.newTimeRange(timeObj, endTime, true, includeEndTime);
						}
						List attachments = state.getAttachments();
						Collection groups = new Vector();
																if (edit != null)
						{
							edit.setRange(range);
							edit.setDescriptionFormatted(description);
							edit.setDisplayName(title);
							edit.setType(type);
							edit.setLocation(location);

							setFields(edit, addfieldsMap);
							edit.replaceAttachments(attachments);

							RecurrenceRule rule = (RecurrenceRule) sstate.getAttribute(CalendarAction.SSTATE__RECURRING_RULE);

							// conditions:
							// 1st, an existing recurring one, just revised, no save state rule or state freq (0, 0)
							// --> let edit rule untouched 
							// 2st, and existing non-recurring one, just revised, no save state rule or state freq (0, 0)
							// --> let edit rule untouched 
							// 3rd, an existing recurring one, frequency revised, there is a state-saved rule, and state-saved freq exists (1, 1)
							// --> replace the edit rule with state-saved rule
							// 4th, and existing recurring one, changed to non-recurring, the state saved rule is null, but state-saved freq exists (0, 1)
							// --> replace the edit rule with state-saved rule
							// 5th, and existing non-recurring one, changed but kept as non-recurring, the state-saved rule is null, but state-saved freq exists (0, 1)
							// --> replace the edit rule with state-saved rule
							// so if the state-saved freq exists, replace the event rule
							
							String freq = (String) sstate.getAttribute(FREQUENCY_SELECT);
							if (sstate.getAttribute(FREQUENCY_SELECT) != null)
							{
								edit.setRecurrenceRule(rule);
							}
							
							// section awareness
							try
							{
								// for site event
								if (scheduleTo.equals("site"))
								{
									edit.clearGroupAccess();
								}

								// for grouped event
								else if (scheduleTo.equals("groups"))
								{
									Site site = SiteService.getSite(calendarObj.getContext());
									
									// make a collection of Group objects from the collection of group ref strings
									for (Iterator iGroups = groupChoice.iterator(); iGroups.hasNext();)
									{
										String groupRef = (String) iGroups.next();
										groups.add(site.getGroup(groupRef));
									}
									
									edit.setGroupAccess(groups, edit.isUserOwner());
								}
							}
							catch (Exception e)
							{
								log.warn("doUpdate", e);
							}

							calendarObj.commitEvent(edit, intention);
							state.setPrimaryCalendarEdit(null);
							state.setEdit(null);
							state.setIsNewCalendar(false);
							
							// post update-type events for the revised schedule event
							postEventsForChanges(edit);
							
						} // if (edit != null)
						
						m_calObj.setDay(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));
						
						sstate.setAttribute(STATE_YEAR, Integer.valueOf(m_calObj.getYear()));
						sstate.setAttribute(STATE_MONTH, Integer.valueOf(m_calObj.getMonthInteger()));
						sstate.setAttribute(STATE_DAY, Integer.valueOf(m_calObj.getDayOfMonth()));
						
						// clear the saved recurring rule and the selected frequency
						sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
						sstate.setAttribute(FREQUENCY_SELECT, null);

						// set the return state as the one before new/revise
						String returnState = state.getReturnState();
						if (returnState != null)
						{
							state.setState(returnState);
						}
						else
						{
							state.setState(CALENDAR_INIT_PARAMETER);
						}
						
						// clean state
						sstate.removeAttribute(STATE_SCHEDULE_TO);
						sstate.removeAttribute(STATE_SCHEDULE_TO_GROUPS);
						
						// Create task
						String reference = "/calendar/dashboard/" + calendarObj.getContext() + Entity.SEPARATOR + calendarObj.getId() + Entity.SEPARATOR + edit.getId();
						Optional<Task> optTask = taskService.getTask(reference);
						if (optTask.isPresent()) {
							Task task = optTask.get(); 
							task.setDescription(title);
							Date dueDate = new Date(timeObj.getTime());
							task.setDue(dueDate == null ? null : dueDate.toInstant());
							taskService.saveTask(task);
						} else {
							Task task = new Task();
							task.setSiteId(calendarObj.getContext());
							task.setReference(reference);
							task.setSystem(true);
							task.setDescription(title);
							Date dueDate = new Date(timeObj.getTime());
							task.setDue(dueDate == null ? null : dueDate.toInstant());
							Set<String> users = new HashSet();
							if ("site".equals(scheduleTo)) {
								Site site = SiteService.getSite(calendarObj.getContext());
								users = site.getUsersIsAllowed("section.role.student");
							} else if ("groups".equals(scheduleTo)){
								for (Iterator groupsIter = groups.iterator(); groupsIter.hasNext();) {
									Group groupTask = (Group) groupsIter.next();
									Set<Member> members = groupTask.getMembers();
									for (Iterator membersIter = members.iterator(); membersIter.hasNext();) {
										Member member = (Member) membersIter.next();
										users.add(member.getUserId());
									}
								}
							}
							if (users.size() == 0) {
								users.add(UserDirectoryService.getCurrentUser().getId());
							}
							taskService.createTask(task, users, Priorities.HIGH);
						}
					}
					catch (IdUnusedException  e)
					{
						addAlert(sstate, rb.getString("java.alert.noexist"));
						log.debug(".doUpdate(): " + e);
					}
					catch (PermissionException	 e)
					{
						addAlert(sstate, rb.getString("java.alert.youcreate"));
						log.debug(".doUpdate(): " + e);
					} // try-catch
				} // if(title.length()==0)				
				String stateName = ServerConfigurationService.getString("calendar.default.view", defaultStateView);
				state.setState(stateName);
				state.setReturnState(stateName);
			} // if (state.getState().equalsIgnoreCase(STATE_CUSTOMIZE_CALENDAR))
		
	}	 // doUpdate
	
	/**
	 * Handle the button click to remove fields to calendar events.
	 */
	public void doDeletefield(RunData runData, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		customizeCalendarPage.doDeletefield( runData, context, state, getSessionState(runData));
	    doUpdate(runData, context);
	}
	
	/**
	 * Handle the button click to add fields to calendar events.
	 */
	public void doAddfield(RunData runData, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		customizeCalendarPage.doAddfield( runData, context, state, getSessionState(runData));
		doUpdate(runData, context);
	}
	
	
	public void doAddSubscription(RunData runData, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		calendarSubscriptionsPage.doAddSubscription( runData, context, state, getSessionState(runData));
	}

	/**
	 * Action doPrev_activity is requested when the user navigates to the previous message in the detailed view.
	 */

	public void doPrev_activity(RunData runData, Context context)
	{
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		sstate.setAttribute(STATE_NAV_DIRECTION, STATE_PREV_ACT);
	} //doPrev_activity

	/**
	 * Action doNext_activity is requested when the user navigates to the previous message in the detailed view.
	 */
	public void doNext_activity(RunData runData, Context context)
	{
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		sstate.setAttribute(STATE_NAV_DIRECTION, STATE_NEXT_ACT);
	} // doNext_activity

	/*
	 * detailNavigatorControl will handle the goNext/goPrev buttons in detailed view,
	 * as well as figure out the prev/next message if available
	 */
	private void navigatorContextControl(VelocityPortlet portlet, Context context, RunData runData, String direction)
	{
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		String eventId = state.getCalendarEventId();
		
		List events = prepEventList(portlet, context, runData);
		
		int index = -1;
		int size = events.size();
		for (int i=0; i<size; i++)
		{
			CalendarEvent e = (CalendarEvent) events.get(i);
			if (e.getId().equals(eventId))
				index = i;
		}
		
		// navigate to the previous activity
		if (STATE_PREV_ACT.equals(direction) && index > 0) 
		{
			CalendarEvent ce = (CalendarEvent) events.get(--index);
			Reference ref = EntityManager.newReference(ce.getReference());
			eventId = ref.getId();
			String calId = null;
			if(CalendarService.REF_TYPE_EVENT_SUBSCRIPTION.equals(ref.getSubType())) 
				calId = CalendarService.calendarSubscriptionReference(ref.getContext(), ref.getContainer());
			else
				calId = CalendarService.calendarReference(ref.getContext(), ref.getContainer());
			
			state.setCalendarEventId(calId, eventId);
			state.setAttachments(null);
		}
		// navigate to the next activity
		else if (STATE_NEXT_ACT.equals(direction) && index < size-1) 
		{
			CalendarEvent ce = (CalendarEvent) events.get(++index);
			Reference ref = EntityManager.newReference(ce.getReference());
			eventId = ref.getId();
			String calId = null;
			if(CalendarService.REF_TYPE_EVENT_SUBSCRIPTION.equals(ref.getSubType())) 
				calId = CalendarService.calendarSubscriptionReference(ref.getContext(), ref.getContainer());
			else
				calId = CalendarService.calendarReference(ref.getContext(), ref.getContainer());
			
			state.setCalendarEventId(calId, eventId);
			state.setAttachments(null);
		}
		
		if (index > 0)
			sstate.setAttribute(STATE_PREV_ACT, "");
		else
			sstate.removeAttribute(STATE_PREV_ACT);
		
		if(index < size-1)
			sstate.setAttribute(STATE_NEXT_ACT, "");
		else
			sstate.removeAttribute(STATE_NEXT_ACT);
		
		sstate.setAttribute(STATE_NAV_DIRECTION, STATE_CURRENT_ACT);
		
	} // navigatorControl
	
	private CalendarEventVector prepEventList(VelocityPortlet portlet,
			Context context,
			RunData runData)
	{
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		TimeRange fullTimeRange =
			TimeService.newTimeRange(
				TimeService.newTimeLocal(
					CalendarFilter.LIST_VIEW_STARTING_YEAR,
					1,
					1,
					0,
					0,
					0,
					0),
				TimeService.newTimeLocal(
					CalendarFilter.LIST_VIEW_ENDING_YEAR,
					12,
					31,
					23,
					59,
					59,
					999));
		
		// We need to get events from all calendars for the full time range.
		CalendarEventVector masterEventVectorObj =
			CalendarService.getEvents(
				getCalendarReferenceList(
					portlet,
					state.getPrimaryCalendarReference(),
					isOnWorkspaceTab()),
				fullTimeRange);
		
		sstate.setAttribute(STATE_EVENTS_LIST, masterEventVectorObj);
		return masterEventVectorObj;
		
	} // eventList
	
	
	/**
	 * Action is to parse the function calls
	 **/
	public void doParse(RunData data, Context context) {
		ParameterParser params = data.getParameters();
		String source = params.getString("source");
		switch (source) {
			case STATE_NEW:
				// create new event
				doNew(data, context);
				break;
			case "revise":
				// revise an event
				doRevise(data, context);
				break;
			case "delete":
				// delete event
				doDelete(data, context);
				break;
			case "bylist":
				// view by list
				doList(data, context);
				break;
			case "bycalendar":
			default:
				// view by calendar
				doViewCalendar(data, context);
				break;
		}
	}	 // doParse
	
	
	/**
	 * Action doList is requested when the user click on the list in the toolbar
	 */
	public void doList(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);
		
		Time m_time = TimeService.newTime();
		TimeBreakdown b = m_time.breakdownLocal();
		int stateYear = b.getYear();
		int stateMonth = b.getMonth();
		int stateDay = b.getDay();
		if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null))
		{
			stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
			stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
			stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
		}
		
		String sM;
		String eM;
		String sD;
		String eD;
		String sY;
		String eY;
		
		CalendarUtil calObj = new CalendarUtil();
		calObj.setDay(stateYear, stateMonth, stateDay);

		int dayofweek = calObj.getDay_Of_Week(true);
		calObj.setPrevDate(dayofweek-1);
		sY = Integer.valueOf(calObj.getYear()).toString();
		sM = Integer.valueOf(calObj.getMonthInteger()).toString();
		sD = Integer.valueOf(calObj.getDayOfMonth()).toString();

		for(int i = 0; i < 6; i++) {
			calObj.getNextDate();
		}
		eY = Integer.valueOf(calObj.getYear()).toString();
		eM = Integer.valueOf(calObj.getMonthInteger()).toString();
		eD = Integer.valueOf(calObj.getDayOfMonth()).toString();

		if (sM.length() == 1) sM = "0"+sM;
		if (eM.length() == 1) eM = "0"+eM;
		if (sD.length() == 1) sD = "0"+sD;
		if (eD.length() == 1) eD = "0"+eD;
		sY = sY.substring(2);
		eY = eY.substring(2);

		String startingDateStr = sM + "/" + sD + "/" + sY;
		String endingDateStr	  = eM + "/" + eD + "/" + eY;
		state.getCalendarFilter().setListViewFilterMode(CalendarFilter.SHOW_CUSTOM_RANGE);

		sstate.removeAttribute(STATE_SCHEDULE_TO);
		sstate.removeAttribute(STATE_SCHEDULE_TO_GROUPS);

		// Pass in a buffer for a possible error message.
		StringBuilder errorMessage = new StringBuilder();

		// Try to simultaneously set the start/end dates.
		// If that doesn't work, add an error message.
		if ( !state.getCalendarFilter().setStartAndEndListViewDates(startingDateStr, endingDateStr, errorMessage) ) {
			addAlert(sstate, errorMessage.toString());
		}

		state.setState(LIST_VIEW);
		this.defaultStateView = LIST_VIEW;
	}	 // doList
	
	/**
	 * Action doSort_by_date_toggle is requested when the user click on the sorting icon in the list view
	 */
	public void doSort_by_date_toggle(RunData data, Context context)
	{
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState sstate = ((JetspeedRunData)data).getPortletSessionState(peid);

		boolean dateDsc = sstate.getAttribute(STATE_DATE_SORT_DSC) != null;
		if (dateDsc)
			sstate.removeAttribute(STATE_DATE_SORT_DSC);
		else
			sstate.setAttribute(STATE_DATE_SORT_DSC, "");
		
	}	 // doSort_by_date_toggle
	
	/**
	 * Handle a request from the "merge" page to merge calendars from other groups into this group's Schedule display.
	 */
	public void doMerge(RunData runData, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		mergedCalendarPage.doMerge( runData, context, state, getSessionState(runData));
	} // doMerge
	
	/**
	 * Handle a request from the "subscriptions" page to subscribe calendars.
	 */
	public void doSubscriptions(RunData runData, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, runData, CalendarActionState.class);
		
		calendarSubscriptionsPage.doSubscriptions( runData, context, state, getSessionState(runData));
	} // doMerge
	
	/**
	 * Action is used when the user clicks on the 'Subscribe' link:
	 */
	public void doOpaqueUrl(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		state.setPrevState(state.getState());
		state.setReturnState(state.getState());
		OpaqueUrl opaqUrl = 
			opaqueUrlDao.getOpaqueUrl(sessionManager.getCurrentSessionUserId(), state.getPrimaryCalendarReference());
		String newState = (opaqUrl == null) ? "opaqueUrlClean" : "opaqueUrlExisting";
		state.setState(newState);
	}
	
	public void doOpaqueUrlGenerate(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		opaqueUrlDao.newOpaqueUrl(sessionManager.getCurrentSessionUserId(), state.getPrimaryCalendarReference());
		state.setState("opaqueUrlExisting");
	}
	
	public void doOpaqueUrlRegenerate(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		String userUUID = sessionManager.getCurrentSessionUserId();
		String calendarRef = state.getPrimaryCalendarReference();
		opaqueUrlDao.deleteOpaqueUrl(userUUID, calendarRef);
		opaqueUrlDao.newOpaqueUrl(userUUID, calendarRef);
	}
	
	public void doOpaqueUrlDelete(RunData data, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, data, CalendarActionState.class);
		opaqueUrlDao.deleteOpaqueUrl(sessionManager.getCurrentSessionUserId(), state.getPrimaryCalendarReference());
		state.setState("opaqueUrlClean");
	}
	
	/**
	 * Handle a request to set options.
	 */
	public void doCustomize(RunData runData, Context context)
	{
		CalendarActionState state =
		(CalendarActionState) getState(context,
		runData,
		CalendarActionState.class);
		
		customizeCalendarPage.doCustomize(
		runData,
		context,
		state,
		getSessionState(runData));
	}
	
	/**
	 * Build the context for showing list view
	 */
	protected void buildListContext(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state)
	{
		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		context.put("tlang",rb);
		MyMonth monthObj2 = null;
		MyDate dateObj1 = new MyDate();
		CalendarEventVector calendarEventVectorObj = null;
		boolean allowed = false;
		LinkedHashMap yearMap = new LinkedHashMap();

		// Initialize month format object		
		if ( monthFormat == null ) 
		{
			monthFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
			monthFormat.setMinimumIntegerDigits(2);
		}
		
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		
		Time m_time = TimeService.newTime();
		TimeBreakdown b = m_time.breakdownLocal();
		int stateYear = b.getYear();
		int stateMonth = b.getMonth();
		int stateDay = b.getDay();
		if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null))
		{
			stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
			stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
			stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
		}
		
		// Set up list filtering information in the context.
		context.put(TIME_FILTER_OPTION_VAR, state.getCalendarFilter().getListViewFilterMode());

		//
		// Fill in the custom dates
		//
		String sDate; // starting date
		String eDate; // ending date
		
		java.util.Calendar userCal = java.util.Calendar.getInstance();
		context.put("ddStartYear", Integer.valueOf(userCal.get(java.util.Calendar.YEAR) - 3));
		context.put("ddEndYear", Integer.valueOf(userCal.get(java.util.Calendar.YEAR) + 4));
		
		String sM;
		String eM;
		String sD;
		String eD;
		String sY;
		String eY;
		
		if (state.getCalendarFilter().isCustomListViewDates() )
		{
			sDate = state.getCalendarFilter().getStartingListViewDateString();
			eDate = state.getCalendarFilter().getEndingListViewDateString();
			
			sM = sDate.substring(0, 2);
			eM = eDate.substring(0, 2);
			sD = sDate.substring(3, 5);
			eD = eDate.substring(3, 5);
			sY = "20" + sDate.substring(6);
			eY = "20" + eDate.substring(6);
		}
		else
		{
			//default to current week
			CalendarUtil calObj = new CalendarUtil();
			calObj.setDay(stateYear, stateMonth, stateDay);
			TimeRange weekRange = getWeekTimeRange( calObj );
			sD = String.valueOf( weekRange.firstTime().breakdownLocal().getDay() );
			sY = String.valueOf( weekRange.firstTime().breakdownLocal().getYear() );
			sM = monthFormat.format( weekRange.firstTime().breakdownLocal().getMonth() );
			
			eD = String.valueOf( weekRange.lastTime().breakdownLocal().getDay() );
			eY = String.valueOf( weekRange.lastTime().breakdownLocal().getYear() );
			eM = monthFormat.format( weekRange.lastTime().breakdownLocal().getMonth() );
		}
		
		context.put(TIME_FILTER_SETTING_CUSTOM_START_YEAR,	 Integer.valueOf(sY));
		context.put(TIME_FILTER_SETTING_CUSTOM_END_YEAR,	 Integer.valueOf(eY));
		context.put(TIME_FILTER_SETTING_CUSTOM_START_MONTH, Integer.valueOf(sM));
		context.put(TIME_FILTER_SETTING_CUSTOM_END_MONTH,	 Integer.valueOf(eM));
		context.put(TIME_FILTER_SETTING_CUSTOM_START_DAY,	 Integer.valueOf(sD));
		context.put(TIME_FILTER_SETTING_CUSTOM_END_DAY,		 Integer.valueOf(eD));
		
		CalendarUtil calObj= new CalendarUtil();
		calObj.setDay(stateYear, stateMonth, stateDay);
		dateObj1.setTodayDate(calObj.getMonthInteger(),calObj.getDayOfMonth(),calObj.getYear());
		
		// fill this month object with all days avilable for this month
		if (CalendarService.allowGetCalendar(state.getPrimaryCalendarReference())== false)
		{
			allowed = false;
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotallow"));
			calendarEventVectorObj = new	CalendarEventVector();
		}
		else
		{
			try
			{
				allowed = CalendarService.getCalendar(state.getPrimaryCalendarReference()).allowAddEvent();
			}
			catch(IdUnusedException e)
			{
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.therenoactv"));
				log.debug(".buildMonthContext(): " + e);
			}
			catch (PermissionException e)
			{
				context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotperm"));
				log.debug(".buildMonthContext(): " + e);
			}
		}
		
		int yearInt, monthInt, dayInt=1;
		
		TimeRange fullTimeRange =
			TimeService.newTimeRange(
				TimeService.newTimeLocal(
					CalendarFilter.LIST_VIEW_STARTING_YEAR,
					1,
					1,
					0,
					0,
					0,
					0),
				TimeService.newTimeLocal(
					CalendarFilter.LIST_VIEW_ENDING_YEAR,
					12,
					31,
					23,
					59,
					59,
					999));
		
		// We need to get events from all calendars for the full time range.
		CalendarEventVector masterEventVectorObj =
			CalendarService.getEvents(
				getCalendarReferenceList(
					portlet,
					state.getPrimaryCalendarReference(),
					isOnWorkspaceTab()),
				fullTimeRange);
		
		// groups awareness - filtering
		String calId = state.getPrimaryCalendarReference();

		String scheduleTo = (String)sstate.getAttribute(STATE_SCHEDULE_TO);
		
		boolean showAllEvents   = StringUtils.isBlank(scheduleTo) || "all".equalsIgnoreCase(scheduleTo);
		boolean showSiteEvents  = "site".equalsIgnoreCase(scheduleTo);
		boolean showGroupEvents = "groups".equalsIgnoreCase(scheduleTo);

		try
		{
			Calendar calendarObj = CalendarService.getCalendar(calId);
			context.put("cal", calendarObj);
			
			if (scheduleTo != null && scheduleTo.length() != 0)
			{
				context.put("scheduleTo", scheduleTo);
			}
			else
			{
				if (calendarObj.allowGetEvents())
				{
					// default to all events selection
					context.put("scheduleTo", "all");
				}
				else if (calendarObj.getGroupsAllowGetEvent().size() > 0)
				{
					// to group otherwise
					context.put("scheduleTo", "groups");
				}
			}
		
			Collection groups = calendarObj.getGroupsAllowGetEvent();
			if (groups.size() > 0)
			{
				context.put("groups", groups);
				
			}
			List schToGroups = (List)(sstate.getAttribute(STATE_SCHEDULE_TO_GROUPS));
			context.put("scheduleToGroups", schToGroups);
			
			CalendarEventVector newEventVectorObj = new CalendarEventVector();
			if (showAllEvents) {
				newEventVectorObj.addAll(masterEventVectorObj);
			} else {
				for (Iterator i = masterEventVectorObj.iterator(); i.hasNext();)
				{
					CalendarEvent e = (CalendarEvent)(i.next());

					String origSiteId = (CalendarService.getCalendar(e.getCalendarReference())).getContext();
					if (!origSiteId.equals(ToolManager.getCurrentPlacement().getContext()))
					{
						context.put("fromColExist", Boolean.TRUE);
					}

					if (showGroupEvents && schToGroups != null && !schToGroups.isEmpty())
					{
						boolean eventInGroup = false;
						for (Iterator j = schToGroups.iterator(); j.hasNext();)
						{
							String groupRangeForDisplay = e.getGroupRangeForDisplay(calendarObj);
							String groupId = j.next().toString();
							Site site = SiteService.getSite(calendarObj.getContext());
							Group group = site.getGroup(groupId);
							if (groupRangeForDisplay.equals(group.getTitle()))
								eventInGroup = true;
						}
						if (eventInGroup){
							newEventVectorObj.add(e);
						}
					}
					if(showSiteEvents){
						String groupRangeForDisplay = e.getGroupRangeForDisplay(calendarObj);
						if(StringUtils.isBlank(groupRangeForDisplay) || "site".equalsIgnoreCase(groupRangeForDisplay)){
							newEventVectorObj.add(e);
						}
					}
				}
				masterEventVectorObj.clear();
				masterEventVectorObj.addAll(newEventVectorObj);
			}
		}
		catch(IdUnusedException e)
		{
			log.debug(".buildListContext(): " + e);
		}
		catch (PermissionException e)
		{
			log.debug(".buildListContext(): " + e);
		}
		
		boolean dateDsc = sstate.getAttribute(STATE_DATE_SORT_DSC) != null;
		context.put("currentDateSortAsc", Boolean.valueOf(!dateDsc));
		
		boolean dateAsc = !dateDsc;
		
		for (yearInt = dateAsc ? CalendarFilter.LIST_VIEW_STARTING_YEAR : CalendarFilter.LIST_VIEW_ENDING_YEAR;
			dateAsc ? yearInt <= CalendarFilter.LIST_VIEW_ENDING_YEAR : yearInt >= CalendarFilter.LIST_VIEW_STARTING_YEAR ;
			yearInt = yearInt + (dateAsc ? 1 : -1)
		){
			ArrayList<MyMonth> arrayOfMonths = new ArrayList(20);
			for(
					monthInt = dateAsc ? 1 : 12;
					dateAsc ? monthInt <13 : monthInt >=1;
					monthInt = monthInt + (dateAsc ? 1 : -1)
			){
				CalendarUtil AcalObj = new CalendarUtil();

				monthObj2 = new MyMonth();
				AcalObj.setDay(yearInt, monthInt, dayInt);

				dateObj1.setTodayDate(AcalObj.getMonthInteger(),AcalObj.getDayOfMonth(),AcalObj.getYear());

				// Get the events for the particular month from the
				// master list of events.
				calendarEventVectorObj =
					new CalendarEventVector(
						state.getCalendarFilter().filterEvents(
							masterEventVectorObj.getEvents(
								getMonthTimeRange((CalendarUtil) AcalObj))));

				if (!calendarEventVectorObj.isEmpty())
				{
					AcalObj.setDay(dateObj1.getYear(),dateObj1.getMonth(),dateObj1.getDay());

					monthObj2 = calMonth(monthInt, (CalendarUtil)AcalObj, state, calendarEventVectorObj);

					AcalObj.setDay(dateObj1.getYear(),dateObj1.getMonth(),dateObj1.getDay());

					if (!calendarEventVectorObj.isEmpty())
						arrayOfMonths.add(monthObj2);
				}
			}
			if (!arrayOfMonths.isEmpty())
				yearMap.put(Integer.valueOf(yearInt), arrayOfMonths.iterator());
		}
		
		context.put("yearMap", yearMap);
		
		int row = 5;
		context.put("row",Integer.valueOf(row));
		calObj.setDay(stateYear, stateMonth, stateDay);
		
		// using session state stored year-month-day to replace saving calObj
		sstate.setAttribute(STATE_YEAR, Integer.valueOf(stateYear));
		sstate.setAttribute(STATE_MONTH, Integer.valueOf(stateMonth));
		sstate.setAttribute(STATE_DAY, Integer.valueOf(stateDay));
		
		state.setState(LIST_VIEW);
		context.put("date",dateObj1);
		
		// output CalendarService and SiteService
		context.put("CalendarService", CalendarService.getInstance());
		context.put("SiteService", SiteService.getInstance());
		context.put("Context", ToolManager.getCurrentPlacement().getContext());
		
		buildMenu(portlet, context, runData, state);
		
		// added by zqian for toolbar
		context.put("allow_new", Boolean.valueOf(allowed));
		context.put("allow_delete", Boolean.valueOf(false));
		context.put("allow_revise", Boolean.valueOf(false));
		context.put("realDate", TimeService.newTime());
		
		context.put("selectedView", rb.getString("java.listeve"));
		
		context.put("isDefaultView", isDefaultView(state, ToolManager.getCurrentPlacement()));
		context.put("isUpdater", SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()));
		
		context.put("tlang",rb);

		context.put("calendarFormattedText", new CalendarFormattedText());

		String currentUserId = UserDirectoryService.getCurrentUser().getId();
		boolean canViewEventAudience = SecurityService.unlock(currentUserId, org.sakaiproject.calendar.api.CalendarService.AUTH_VIEW_AUDIENCE, "/site/" + ToolManager.getCurrentPlacement().getContext());
        context.put("canViewAudience", !canViewEventAudience);

	}	 // buildListContext
	
	private void buildPrintLink(VelocityPortlet portlet, RunData runData, CalendarActionState state, Context context ) {
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);
		String stateName = state.getState();
		
		if (StringUtils.equalsAny(stateName, CALENDAR_INIT_PARAMETER, LIST_VIEW)) {
			int printType = CalendarService.UNKNOWN_VIEW;
			String timeRangeString;
			switch (stateName) {
				case LIST_VIEW:
					printType = CalendarService.LIST_VIEW;
					timeRangeString = TimeService.newTimeRange(state.getCalendarFilter().getListViewStartingTime(), state.getCalendarFilter().getListViewEndingTime()).toString();
					break;
				case CALENDAR_INIT_PARAMETER:
				default:
					printType = CalendarService.WEEK_VIEW;
					timeRangeString = "";
					break;			
			}

			// set the actual list of calendars into the user's session:
			List calRefList = getCalendarReferenceList(portlet, state.getPrimaryCalendarReference(), isOnWorkspaceTab());			
			sessionManager.getCurrentSession().setAttribute(CalendarService.SESSION_CALENDAR_LIST, calRefList);

			// Create the PDF print version URL
			boolean dateDesc = sstate.getAttribute(STATE_DATE_SORT_DSC) != null;
			Reference calendarRef = EntityManager.newReference(state.getPrimaryCalendarReference());
			String userDisplayName = UserDirectoryService.getCurrentUser().getDisplayName();
			String calendarPdfReference = CalendarService.calendarPdfReference(calendarRef.getContext(), calendarRef.getId(), printType, timeRangeString, userDisplayName, dateDesc);
			String printableVersionUrl = String.format("%s%s", ServerConfigurationService.getAccessUrl(), calendarPdfReference);			
			context.put("printableVersionUrl", printableVersionUrl);
		}
	}
	/**
	 * Build the menu.
	 */
	private void buildMenu( VelocityPortlet portlet,
	Context context,
	RunData runData,
	CalendarActionState state,
	boolean allow_new,
	boolean allow_delete,
	boolean allow_revise,
	boolean allow_merge_calendars,
	boolean allow_modify_calendar_properties,
	boolean allow_import_export,
	boolean allow_subscribe,
	boolean allow_subscribe_this)
	{
		Menu bar = new MenuImpl(portlet, runData, "CalendarAction");
		String status = state.getState();
		boolean viewing = false;
		if (StringUtils.equalsAny(status, CALENDAR_INIT_PARAMETER, LIST_VIEW)) {
			viewing = true;
			allow_revise = false;
			allow_delete = false;
		}

		MenuEntry home = new MenuEntry(rb.getString("java.view"), "doViewCalendar");
		home.setIsCurrent(viewing);
		bar.add(home);

		MenuEntry add = new MenuEntry(rb.getString("java.new"), rb.getString("java.new.title"), null, allow_new, MenuItem.CHECKED_NA, "doNew");
		add.setIsCurrent(status.equals(STATE_NEW));
		bar.add(add);
		
		// See if we are allowed to import items.
		if ( allow_import_export )
		{
			MenuEntry importEntry = new MenuEntry(rb.getString("java.import"), rb.getString("java.import.title"), null, allow_new, MenuItem.CHECKED_NA, "doImport");
			importEntry.setIsCurrent(status.equals(STATE_SCHEDULE_IMPORT));
			bar.add(importEntry);
		}
		
		//
		// See if we are allowed to merge items.
		//
		MenuEntry merge = new MenuEntry(mergedCalendarPage.getButtonText(), null, allow_merge_calendars, MenuItem.CHECKED_NA, mergedCalendarPage.getButtonHandlerID());
		merge.setIsCurrent(status.equals(STATE_MERGE_CALENDARS));
		bar.add(merge);
		
		// See if we are allowed to configure external calendar subscriptions
		if ( allow_subscribe && ServerConfigurationService.getBoolean(ExternalCalendarSubscriptionService.SAK_PROP_EXTSUBSCRIPTIONS_ENABLED,true))
			{
				MenuEntry subscriptions = new MenuEntry(rb.getString("java.subscriptions"), rb.getString("java.subscriptions.title"), null, allow_subscribe, MenuItem.CHECKED_NA, "doSubscriptions");
				subscriptions.setIsCurrent(status.equals(STATE_CALENDAR_SUBSCRIPTIONS));
				bar.add(subscriptions);
			}
		
		// See if we are allowed to export items.
		String calId = state.getPrimaryCalendarReference();
		if ( (allow_import_export || CalendarService.getExportEnabled(calId)) && 
			  ServerConfigurationService.getBoolean("ical.public.userdefined.subscribe",ServerConfigurationService.getBoolean("ical.experimental",true)))
		{
			MenuEntry export = new MenuEntry(rb.getString("java.export"), rb.getString("java.export.title"), null, allow_new, MenuItem.CHECKED_NA, "doIcalExportName");
			export.setIsCurrent(status.equals("icalEx"));
			bar.add(export);
		}
		
		
		// A link for subscribing to the implicit calendar if the user is logged in.
		if ( sessionManager.getCurrentSessionUserId() != null &&
				(ServerConfigurationService.getBoolean("ical.public.secureurl.subscribe", ServerConfigurationService.getBoolean("ical.opaqueurl.subscribe", true))) )
		{
			MenuEntry privateExport = new MenuEntry(rb.getString("java.opaque_subscribe"), rb.getString("java.opaque_subscribe.title"), null, allow_subscribe_this, MenuItem.CHECKED_NA, "doOpaqueUrl");
			privateExport.setIsCurrent(status.equals("opaqueUrlClean") || status.equals("opaqueUrlExisting"));
			bar.add(privateExport);
		}
		
		//2nd menu bar for the PDF print only
		Menu bar_print = new MenuImpl(portlet, runData, "CalendarAction");
		buildPrintLink( portlet, runData, state, context );	
      
		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			bar_print.add( new MenuEntry(rb.getString("java.default_view"), "doDefaultview") );
		}
					
		MenuEntry customize = new MenuEntry(customizeCalendarPage.getButtonText(), null, allow_modify_calendar_properties, MenuItem.CHECKED_NA, customizeCalendarPage.getButtonHandlerID());
		customize.setIsCurrent(status.equals(STATE_CUSTOMIZE_CALENDAR));
		bar.add(customize);
		
		// add permissions, if allowed
		//SAK-21684 don't show in myworkspace site unless super user.
		String currentSiteId = ToolManager.getCurrentPlacement().getContext();
		if (SecurityService.isSuperUser() || (SiteService.allowUpdateSite(currentSiteId) && !SiteService.isUserSite(currentSiteId)))
		{
			bar.add( new MenuEntry(rb.getString("java.permissions"), "doPermissions") );
		}
		
		// Set menu state attribute
		SessionState stateForMenus = ((JetspeedRunData)runData).getPortletSessionState(portlet.getID());
		stateForMenus.setAttribute(MenuItem.STATE_MENU, bar);
		context.put("tlang",rb);
		context.put(Menu.CONTEXT_MENU, bar);
		context.put(Menu.CONTEXT_ACTION, "CalendarAction");
		
	}	 // buildMenu

	private void buildMenu(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state) {

		buildMenu(
			portlet,
			context,
			runData,
			state,
			CalendarPermissions.allowCreateEvents(
				state.getPrimaryCalendarReference(),
				state.getSelectedCalendarReference()),
			CalendarPermissions.allowDeleteEvent(
				state.getPrimaryCalendarReference(),
				state.getSelectedCalendarReference(),
				state.getCalendarEventId()),
			CalendarPermissions.allowReviseEvents(
				state.getPrimaryCalendarReference(),
				state.getSelectedCalendarReference(),
				state.getCalendarEventId()),
			CalendarPermissions.allowMergeCalendars(
				state.getPrimaryCalendarReference()),
			CalendarPermissions.allowModifyCalendarProperties(
				state.getPrimaryCalendarReference()),
			CalendarPermissions.allowImport(
				state.getPrimaryCalendarReference()),
			CalendarPermissions.allowSubscribe(
				state.getPrimaryCalendarReference()),
			CalendarPermissions.allowSubscribeThis(
				state.getPrimaryCalendarReference()));
	}
	
	/**
	 * Align the edit's fields with these values.
	 * @param edit The CalendarEventEdit.
	 * @param values The map of name-value pairs.
	 */
	private void setFields(CalendarEventEdit edit, Map values)
	{
		Set<Entry<String, String>> keys = values.entrySet();
		for (Iterator<Entry<String, String>> it = keys.iterator(); it.hasNext(); )
		{
			Entry entry = it.next();
			String name = (String)entry.getKey() ;
			String value = (String) entry.getValue();
			edit.setField(name, value);
		}
		
	}	 // setFields
	
   
	/** Set current calendar view as tool default
	 **/
	public void doDefaultview( RunData rundata, Context context ) {
		CalendarActionState state = (CalendarActionState)getState(context, rundata, CalendarActionState.class);
		String view = state.getState();

		// Basic data validation
		if (StringUtils.equalsAny(view, CALENDAR_INIT_PARAMETER, LIST_VIEW)) {
			Placement placement = ToolManager.getCurrentPlacement();
			log.debug("Setting default view to {}", view);
			placement.getPlacementConfig().setProperty(PORTLET_CONFIG_DEFAULT_VIEW, view);
			// The CALENDAR view is generic but also there could be a subview, week, month or day view.
			String defaultCalendarView = rundata.getParameters().get("calendar_default_subview");
			if (StringUtils.isNotBlank(defaultCalendarView)) {
				String subView = WEEK_VIEW;
				if (StringUtils.containsIgnoreCase(defaultCalendarView, LIST_VIEW)) {
					subView = LIST_VIEW;
				} else if (StringUtils.containsIgnoreCase(defaultCalendarView, MONTH_VIEW)) {
					subView = MONTH_VIEW;
				} else if (StringUtils.containsIgnoreCase(defaultCalendarView, DAY_VIEW)) {
					subView = DAY_VIEW;
				} else if (StringUtils.containsIgnoreCase(defaultCalendarView, WEEK_VIEW)) {
					subView = WEEK_VIEW;
				}
				log.debug("Setting default subview to {}", subView);
				placement.getPlacementConfig().setProperty(PORTLET_CONFIG_DEFAULT_SUBVIEW, subView);
			}
			saveOptions();
		}
	}
	
	/**
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData data, Context context)
	{
		// setup the parameters for the helper
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		CalendarActionState cstate = (CalendarActionState) getState(context, data, CalendarActionState.class);

		cstate.setPrevState(cstate.getState());
		cstate.setState(MODE_PERMISSIONS);

		state.setAttribute(STATE_TOOL_KEY, CALENDAR_INIT_PARAMETER);
	}

	/**
	 * Action doFrequency is requested when "set Frequency" button is clicked in new/revise page
	 */
	
	public void doEditfrequency(RunData rundata, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, rundata, CalendarActionState.class);
		
		String peid = ((JetspeedRunData)rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData)rundata).getPortletSessionState(peid);
		
		String calId = "";
		Calendar calendarObj = null;
		
		String eventId = state.getCalendarEventId();
		
		try
		{
			calId = state.getPrimaryCalendarReference();
			calendarObj = CalendarService.getCalendar(calId);
			
			String freq = (String) sstate.getAttribute(FREQUENCY_SELECT);
			
			// conditions when the doEditfrequency is called:
			// 1. new/existing event, in create-new/revise page first time: freq is null.
			//		It has been set to null in both doNew & doRevise.
			//		Make sure to re-set the freq in this step.
			// 2. new/existing event, back from cancel/save-frequency-setting page: freq is sth, because when
			// the first time doEditfrequency is called, there is a freq set up already
			
			// condition 1 -
			if ((freq == null)||(freq.equals("")))
			{
				// if a new event
				if ((eventId == null)||(eventId.equals("")))
				{
					// set the frequency to be default "once", rule to be null
					sstate.setAttribute(FREQUENCY_SELECT, DEFAULT_FREQ);
					sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
				}
				else
				{ // exiting event
					try
					{
						if(calendarObj.allowGetEvents())
						{
							CalendarEvent event = calendarObj.getEvent(eventId);
							RecurrenceRule rule = event.getRecurrenceRule();
							if (rule == null)
							{
								// not recurring, i.e., frequency is once
								sstate.setAttribute(FREQUENCY_SELECT, DEFAULT_FREQ);
								sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
							}
							else
							{
								sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, rule);
								sstate.setAttribute(FREQUENCY_SELECT, rule.getFrequencyDescription());
							} // if (rule==null)
						} // if allowGetEvents
					} // try
					catch(IdUnusedException e)
					{
						log.debug(".doEditfrequency() + calendarObj.getEvent(): " + e);
					} // try-cath
				} // if ((eventId == null)||(eventId.equals(""))
			}
			else
			{
				// condition 2, state freq is set, and state rule is set already
			}
		} // try
		catch(IdUnusedException e)
		{
			log.debug(".doEditfrequency() + CalendarService.getCalendar(): " + e);
		}
		catch (PermissionException e)
		{
			log.debug(".doEditfrequency() + CalendarService.getCalendar(): " + e);
		}
		
		
		int houri;
		
		String hour = "";
		hour = rundata.getParameters().getString("startHour") != null ? rundata.getParameters().getString("startHour") : "100"; // SAK-51010 applying same default value as non-24h format
		String title ="";
		title = rundata.getParameters().getString("activitytitle");
		String minute = "";
		minute = rundata.getParameters().getString("startMinute");
		String dhour = "";
		dhour = rundata.getParameters().getString("duHour");
		String dminute = "";
		dminute = rundata.getParameters().getString("duMinute");
		String description = "";
		description = rundata.getParameters().getString("description");
		description = processFormattedTextFromBrowser(sstate, description);
		String month = "";
		month = rundata.getParameters().getString("month");
		
		String day = "";
		day = rundata.getParameters().getString("day");
		String year = "";
		year = rundata.getParameters().getString("yearSelect");
		String timeType = "";
		timeType = rundata.getParameters().getString("startAmpm");
		String type = "";
		type = rundata.getParameters().getString("eventType");
		String location = "";
		location = rundata.getParameters().getString("location");

		readEventGroupForm(rundata, context);
		
		// read the recurrence modification intention
		String intentionStr = rundata.getParameters().getString("intention");
		if (intentionStr == null) intentionStr = "";
		
		try
		{
			calendarObj = CalendarService.getCalendar(calId);
			Map addfieldsMap = new HashMap();
			
			// Add any additional fields in the calendar.
			customizeCalendarPage.loadAdditionalFieldsMapFromRunData(rundata, addfieldsMap, calendarObj);
			
			if (timeType.equals("pm"))
			{
				if (Integer.parseInt(hour)>11)
					houri = Integer.parseInt(hour);
				else
					houri = Integer.parseInt(hour)+12;
			}
			else if (timeType.equals("am") && Integer.parseInt(hour)==12)
			{
				houri = 24;
			}
			else
			{
				houri = Integer.parseInt(hour);
			}
			state.clearData();
			state.setNewData(state.getPrimaryCalendarReference(), title,description,Integer.parseInt(month),Integer.parseInt(day),year,houri,Integer.parseInt(minute),Integer.parseInt(dhour),Integer.parseInt(dminute),type,timeType,location, addfieldsMap, intentionStr);
		}
		catch(IdUnusedException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.thereis"));
			log.debug(".doEditfrequency(): " + e);
		}
		catch (PermissionException e)
		{
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.youdont"));
			log.debug(".doEditfrequency(): " + e);
		}
		
		sstate.setAttribute(STATE_BEFORE_SET_RECURRENCE, state.getState());
		state.setState(STATE_SET_FREQUENCY);
		
	}	 // doEditfrequency
	
	/**
	 * Action doChangefrequency is requested when the user changes the selected frequency at the frequency setting page
	 */
	
	public void doChangefrequency(RunData rundata, Context context)
	{
		CalendarActionState state = (CalendarActionState)getState(context, rundata, CalendarActionState.class);
		
		String freqSelect = rundata.getParameters().getString(FREQUENCY_SELECT);
		
		String peid = ((JetspeedRunData)rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData)rundata).getPortletSessionState(peid);
		
		sstate.setAttribute(FREQUENCY_SELECT, freqSelect);
		
		//TEMP_FREQ_SELECT only works for the onchange javascript function when user changes the frequency selection
		// and will be discarded when buildFrequecyContext has caught its value
		sstate.setAttribute(TEMP_FREQ_SELECT, freqSelect);
		
		state.setState(STATE_SET_FREQUENCY);
		
	}	 // doChangefrequency
	
	/**
	 * Action doSavefrequency is requested when the user click on the "Save" button in the frequency setting page
	 */
	
	public void doSavefrequency(RunData rundata, Context context)
	{		
		CalendarActionState state = (CalendarActionState)getState(context, rundata, CalendarActionState.class);
		
		String peid = ((JetspeedRunData)rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData)rundata).getPortletSessionState(peid);
		
		String returnState = (String)sstate.getAttribute(STATE_BEFORE_SET_RECURRENCE);
		
		// if by any chance, the returnState is not available, 
		// then reset it as either "new" or "revise". 
		// For new event, the id is null or empty string
		if ((returnState == null)||(returnState.equals("")))
		{
			String eventId = state.getCalendarEventId();
			if ((eventId == null) || (eventId.equals("")))
				returnState = STATE_NEW;
			else
				returnState = "revise";
		}
		state.setState(returnState);
		
		// get the current frequency setting the user has selected - daily, weekly, or etc.
		String freq = (String) rundata.getParameters().getString(FREQUENCY_SELECT);
			
		if ((freq == null)||(freq.equals(FREQ_ONCE)))
		{
			sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, null);
			sstate.setAttribute(FREQUENCY_SELECT, FREQ_ONCE);
		}
		else
		{
			sstate.setAttribute(FREQUENCY_SELECT, freq);
			
			String interval = rundata.getParameters().getString("interval");
			int intInterval = Integer.parseInt(interval);
			
			RecurrenceRule rule = null;

			String CountOrTill = rundata.getParameters().getString("CountOrTill");
			if (CountOrTill.equals("Never"))
			{
				rule = CalendarService.newRecurrence(freq, intInterval);
			}
			else if (CountOrTill.equals("Till"))
			{
				String endMonth = rundata.getParameters().getString("endMonth");
				String endDay = rundata.getParameters().getString("endDay");
				String endYear = rundata.getParameters().getString("endYear");
				int intEndMonth = Integer.parseInt(endMonth);
				int intEndDay = Integer.parseInt(endDay);
				int intEndYear = Integer.parseInt(endYear);
				
				//construct time object from individual ints, Local Time values
				Time endTime = TimeService.newTimeLocal(intEndYear, intEndMonth, intEndDay, 23, 59, 59, 999);
				rule = CalendarService.newRecurrence(freq, intInterval, endTime);
			}
			else if (CountOrTill.equals("Count"))
			{
				String count = rundata.getParameters().getString("count");
				int intCount = Integer.parseInt(count);
				rule = CalendarService.newRecurrence(freq, intInterval, intCount);
			}
			sstate.setAttribute(CalendarAction.SSTATE__RECURRING_RULE, rule);
		}
		
	}	 // doSavefrequency
	
	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);
		
		if (contentHostingService == null)
		{
			contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		}
		
		if (entityBroker == null)
		{
			entityBroker = (EntityBroker) ComponentManager.get("org.sakaiproject.entitybroker.EntityBroker");
		}
		if (sessionManager == null)
		{
			sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
		}
		if (opaqueUrlDao == null)
		{
			opaqueUrlDao = (OpaqueUrlDao) ComponentManager.get(OpaqueUrlDao.class);
		}


		// retrieve the state from state object
		CalendarActionState calState = (CalendarActionState)getState( portlet, rundata, CalendarActionState.class );

		setPrimaryCalendarReferenceInState(portlet, calState);

		// setup the observer to notify our main panel
		if (state.getAttribute(STATE_INITED) == null)
		{
			state.setAttribute(STATE_INITED,STATE_INITED);
			
			// load all calendar channels (either primary or merged calendars)
			MergedList mergedCalendarList = 
				loadChannels( calState.getPrimaryCalendarReference(), 
								  portlet.getPortletConfig().getInitParameter(PORTLET_CONFIG_PARM_MERGED_CALENDARS),
								  null );
		}
	} // initState

	/**
	  *  Takes an array of tokens and converts into separator-separated string.
	  *
	  * @param String[] The array of strings input.
	  * @param String The string separator.
	  * @return String A string containing tokens separated by seperator.
	  */
	 protected String arrayToString(String[] array, String separators)
	 {
		 StringBuilder sb = new StringBuilder("");
		 String empty = "";
		  
		 if (array == null)
			 return empty;

		 if (separators == null)
			 separators = ",";

		 for (int ix=0; ix < array.length; ix++) 
		 {
			 if (array[ix] != null && !array[ix].equals("")) 
			 {
				 sb.append(array[ix] + separators);
			 }
		 }
		 String str = sb.toString();
		 if (!str.equals("")) 
		 {
			 str = str.substring(0, (str.length() - separators.length()));
		 }
		 return str;
	 }
	 
	/**
	 * Processes formatted text that is coming back from the browser 
	 * (from the formatted text editing widget).
	 * @param state Used to pass in any user-visible alerts or errors when processing the text
	 * @param strFromBrowser The string from the browser
	 * @return The formatted text
	 */
	private String processFormattedTextFromBrowser(SessionState state, String strFromBrowser)
	{
		return ComponentManager.get(FormattedText.class).processFormattedText(strFromBrowser, null, null);
	}
	
	
	/**
	* Access the current month as a string.
	* @return the current month as a string.
	*/
	public String calendarUtilGetMonth(int l_month)
	{
		// get the index for the month. Note, the index is increased by 1, u need to deduct 1 first
		CalendarUtil calUtil = new CalendarUtil();
		String[] months = calUtil.getCalendarMonthNames(true);

		if (l_month >12) 
		{
			return rb.getString("java.thismonth");
		}

		return months[l_month-1];

	}	// getMonth
	
	/**
	* Get the name of the day.
	* @return the name of the day.
	*/
	private String calendarUtilGetDay(int dayofweek) 
	{		

		CalendarUtil calUtil = new CalendarUtil();
		String[] l_ndays = calUtil.getCalendarDaysOfWeekNames(true);
		
		if ( dayofweek > 7 ) 
		{
			dayofweek = 1;
		}
		else if ( dayofweek <=0 ) 
		{
			dayofweek = 7;
		}
		
		return l_ndays[dayofweek - 1];
			
	}	// calendarUtilGetDay
	
	/**
	 * @param newEvent
	 */
	public void postEventsForChanges(CalendarEventEdit newEvent) {
		// determine which events should be posted by comparing with saved properties
		try
		{
			Calendar calendar = CalendarService.getCalendar(newEvent.getCalendarReference());
			if (calendar != null)
			{
				try
				{
					CalendarEvent savedEvent = calendar.getEvent(newEvent.getId());
					if(savedEvent == null) {
						
					} else {
						
						EventTrackingService m_eventTrackingService = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
					    
						// has type changed? 
						String savedType = savedEvent.getType();
						String newType = newEvent.getType();
						if(savedType == null || newType == null) {
							// TODO: is this an error?
						} else {
							if (!newType.equals(savedType))
							{
								// post type-change event
								m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TYPE, newEvent.getReference() + "::" + savedType + "::" + newType, true));
							}
						}
						
						// has title changed?
						if(savedEvent.getDisplayName() != null && ! savedEvent.getDisplayName().equals(newEvent.getDisplayName())) {
							// post title-change event
							m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TITLE, newEvent.getReference(), true));
						}
						
						// has start-time changed?
						TimeRange savedRange = savedEvent.getRange();
						TimeRange newRange = newEvent.getRange();
						if(savedRange == null || newRange == null) {
							// TODO: Is this an error?
						} else if(savedRange.firstTime() != null && savedRange.firstTime().compareTo(newRange.firstTime()) != 0) {
							// post time-change event 
							m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TIME, newEvent.getReference(), true));
						}
						
						// has access changed?
						if(savedEvent.getAccess() != newEvent.getAccess()) {
							// post access-change event
							m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_ACCESS, newEvent.getReference(), true));
						} else {
							Collection savedGroups = savedEvent.getGroups();
							Collection newGroups = newEvent.getGroups();
							if(! (savedGroups.containsAll(newGroups) && newGroups.containsAll(savedGroups))) {
								// post access-change event
								m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_ACCESS, newEvent.getReference(), true));
							}
						}
						
						// has frequency changed (other than exclusions)? 
						RecurrenceRule savedRule = savedEvent.getRecurrenceRule();
						RecurrenceRule newRule = newEvent.getRecurrenceRule();
						if(savedRule == null && newRule == null) {
							// do nothing -- no change
						} else if(savedRule == null || newRule == null) {
							// post frequency-change event
							m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY, newEvent.getReference(), true));
						} else {
							// check for changes in properties of the rules
							// (rule.getCount() rule.getFrequency() rule.getInterval() rule.getUntil() 
							if(savedRule.getCount() != newRule.getCount() || savedRule.getInterval() != newRule.getInterval()) {
								// post frequency-change event
								m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY, newEvent.getReference(), true));								
							} else if((savedRule.getFrequency() != null && ! savedRule.getFrequency().equals(newRule.getFrequency())) || (savedRule.getFrequency() == null && newRule.getFrequency() != null)) {
								// post frequency-change event
								m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY, newEvent.getReference(), true));								
							} else if((savedRule.getUntil() == null && newRule.getUntil() != null) 
									|| (savedRule.getUntil() != null && newRule.getUntil() == null)
									|| (savedRule.getUntil() != null && savedRule.getUntil().getTime() != newRule.getUntil().getTime())) {
								// post frequency-change event
								m_eventTrackingService.post(m_eventTrackingService.newEvent(CalendarService.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY, newEvent.getReference(), true));								
							}
						}
					} // if
				}
				catch (IdUnusedException Exception)
				{
					log.warn(this + ":postEventsForChanges IdUnusedException Cannot find calendar event for " +  newEvent.getId());
				}
				catch (PermissionException Exception)
				{
					log.warn(this + ":postEventsForChanges PermissionException Cannot find calendar event for " +  newEvent.getId());
				}
			} // if
		}
		catch (IdUnusedException Exception)
		{
			log.warn(this + ":postEventsForChanges IdUnusedException Cannot find calendar for " +  newEvent.getCalendarReference());
		}
		catch (PermissionException Exception)
		{
			log.warn(this + ":postEventsForChanges PermissionException Cannot find calendar for " +  newEvent.getCalendarReference());
		}
	} 

	/**
	 * Build the context for showing the calendar view
	 */
	protected void buildViewCalendarContext(VelocityPortlet portlet, Context context, RunData runData, CalendarActionState state) {

		boolean allowed = false;
		String peid = ((JetspeedRunData)runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData)runData).getPortletSessionState(peid);

		if (!CalendarService.allowGetCalendar(state.getPrimaryCalendarReference())) {
			context.put(ALERT_MSG_KEY,rb.getString("java.alert.younotallowsee"));
		} else {
			try {
				allowed = CalendarService.getCalendar(state.getPrimaryCalendarReference()).allowAddEvent();
			} catch(IdUnusedException e) {
				context.put(ALERT_MSG_KEY, rb.getString("java.alert.therenoactv"));
			}
			catch (PermissionException e) {
				context.put(ALERT_MSG_KEY, rb.getString("java.alert.younotperm"));
			}
		}

		ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		int stateYear = currentZonedDateTime.get(ChronoField.YEAR);
		int stateMonth = currentZonedDateTime.get(ChronoField.MONTH_OF_YEAR);
		int stateDay = currentZonedDateTime.get(ChronoField.DAY_OF_MONTH);
		if ((sstate.getAttribute(STATE_YEAR) != null) && (sstate.getAttribute(STATE_MONTH) != null) && (sstate.getAttribute(STATE_DAY) != null)) {
			stateYear = ((Integer)sstate.getAttribute(STATE_YEAR)).intValue();
			stateMonth = ((Integer)sstate.getAttribute(STATE_MONTH)).intValue();
			stateDay = ((Integer)sstate.getAttribute(STATE_DAY)).intValue();
		}
		// Full calendar accepts as current date in the format YYYY-MM-DD
		// See https://fullcalendar.io/docs/date-parsing
		context.put("currentDate", String.format("%d-%s-%s", stateYear, stateMonth < 10 ? "0"+stateMonth : String.valueOf(stateMonth), stateDay < 10 ? "0"+stateDay : String.valueOf(stateDay)));

		context.put("tlang",rb);
		state.setState(CALENDAR_INIT_PARAMETER);
		buildMenu(portlet, context, runData, state);
		context.put("allow_new", Boolean.valueOf(allowed));
		context.put("allow_delete", Boolean.valueOf(false));
		context.put("allow_revise", Boolean.valueOf(false));
		context.put(Menu.CONTEXT_ACTION, "CalendarAction");
		context.put("selectedView", rb.getString("java.bycalendar"));
		
		context.put("isDefaultView", isDefaultView(state, ToolManager.getCurrentPlacement()));
		context.put("defaultSubview", ToolManager.getCurrentPlacement().getPlacementConfig().getProperty(PORTLET_CONFIG_DEFAULT_SUBVIEW));
		context.put("isUpdater", SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()));
		context.put("aspectRatio", FULLCALENDAR_ASPECTRATIO);
		context.put("scrollTime", FULLCALENDAR_SCROLLTIME);
		
	} // buildCalendarContext

}	 // CalendarAction
