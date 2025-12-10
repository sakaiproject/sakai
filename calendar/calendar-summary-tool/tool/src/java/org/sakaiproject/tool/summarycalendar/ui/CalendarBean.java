/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarConstants;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventVector;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.CalendarChannelReferenceMaker;
import org.sakaiproject.util.CalendarReferenceToChannelConverter;
import org.sakaiproject.util.CalendarUtil;
import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.EntryProvider;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;

@Slf4j
public class CalendarBean {

	public static final String 						MODE_MONTHVIEW			= "month";
	public static final String 						MODE_WEEKVIEW			= "week";
	public static final String 						PRIORITY_HIGH			= "priority_high";
	public static final String 						PRIORITY_MEDIUM			= "priority_medium";
	public static final String 						PRIORITY_LOW			= "priority_low";
	private static final String 					imgLocation				= "/../library/image/sakai/";
	private static final String 					SCHEDULE_TOOL_ID		= "sakai.schedule";
	
	private static final String 					MERGED_CALENDARS_PROP 	= "mergedCalendarReferences";

	/** Resource bundle */
	private static final ResourceLoader				msgs					= new ResourceLoader("calendar");
	
	private CalendarUtil calendarUtil = new CalendarUtil();
	
	/** Bean members */
	private String									viewMode				= MODE_MONTHVIEW;
	private String									prevViewMode			= null;
	private Date									today					= null;
	private Date									viewingDate			= null;
	private Date									selectedDay				= null;
	private boolean									selectedDayHasEvents	= false;
	private String									selectedEventRef		= null;
	private String									selectedCalendarRef		= null;
	private EventSummary							selectedEvent			= null;

	/** Private members */
	private boolean									updateEventList			= true;
	private List									weeks					= new ArrayList();
	private MonthWeek								week1					= new MonthWeek();
	private MonthWeek								week2					= new MonthWeek();
	private MonthWeek								week3					= new MonthWeek();
	private MonthWeek								week4					= new MonthWeek();
	private MonthWeek								week5					= new MonthWeek();
	private MonthWeek								week6					= new MonthWeek();
	private CalendarEventVector						calendarEventVector		= null;
	private String									siteId					= null;

	private Map	<String, String>					eventIconMap			= new HashMap<String, String>();
	
	private long									lastModifiedPrefs		= 0l;
	private Map										priorityColorsMap		= null;
	private String									highPrCSSProp			= "";
	private String									mediumPrCSSProp			= "";
	private String									lowPrCSSProp			= "";
	private Map										priorityEventsMap		= null;
	private List									highPriorityEvents		= null;
	private List									mediumPriorityEvents	= null;
	private List									lowPriorityEvents		= null;
	
	
	/** Sakai services */
	private transient CalendarService				M_ca					= (CalendarService) ComponentManager.get(CalendarService.class.getName());
	private transient ExternalCalendarSubscriptionService M_ecs				= (ExternalCalendarSubscriptionService) ComponentManager.get(ExternalCalendarSubscriptionService.class.getName());
	private transient TimeService					M_ts					= (TimeService) ComponentManager.get(TimeService.class.getName());
	private transient SiteService					M_ss					= (SiteService) ComponentManager.get(SiteService.class.getName());
	private transient SecurityService				M_as					= (SecurityService) ComponentManager.get(SecurityService.class.getName());
	private transient ToolManager					M_tm					= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private transient PreferencesService			M_ps					= (PreferencesService) ComponentManager.get(PreferencesService.class.getName());
	private transient SessionManager				M_sm					= (SessionManager) ComponentManager.get(SessionManager.class.getName());

	private static final String DEFAULT_MONTH_DATEFORMAT = "MMMM yyyy";

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public CalendarBean(){		
		readPreferences();
		// go to today events it is first time loading
		selectedDay = getToday();
	}
	
	public String getInitValues() {
		long lastModified = PrefsBean.getPreferenceLastModified();
		if(lastModifiedPrefs != lastModified)
			readPreferences();
		
		// re-read events from API for selected month/week
		calendarEventVector = null;
		
		return "";
	}

	// ######################################################################################
	// Private methods
	// ######################################################################################
	private void readPreferences() {
		log.debug("Reading preferences...");
		lastModifiedPrefs = PrefsBean.getPreferenceLastModified();
		
		// view mode
		prevViewMode = viewMode;
		viewMode = PrefsBean.getPreferenceViewMode();
		
		// priority colors (CSS properties)
		priorityColorsMap = PrefsBean.getPreferencePriorityColors();
		if(priorityColorsMap != null) {
			highPrCSSProp = (String) priorityColorsMap.get(PrefsBean.PREFS_HIGHPRIORITY_COLOR);
			mediumPrCSSProp = (String) priorityColorsMap.get(PrefsBean.PREFS_MEDIUMPRIORITY_COLOR);
			lowPrCSSProp = (String) priorityColorsMap.get(PrefsBean.PREFS_LOWPRIORITY_COLOR);
			
			highPrCSSProp = (highPrCSSProp == null || highPrCSSProp.trim().length() == 0)? "" : "background-color: " + highPrCSSProp;
			mediumPrCSSProp = (mediumPrCSSProp == null || mediumPrCSSProp.trim().length() == 0)? "" : "background-color: " + mediumPrCSSProp;
			lowPrCSSProp = (lowPrCSSProp == null || lowPrCSSProp.trim().length() == 0)? "" : "background-color: " + lowPrCSSProp;
		}
		
		// priority events
		priorityEventsMap = PrefsBean.getPreferencePriorityEvents();
		if(priorityEventsMap != null) {
			highPriorityEvents = (List) priorityEventsMap.get(PrefsBean.PREFS_HIGHPRIORITY_EVENTS);
			mediumPriorityEvents = (List) priorityEventsMap.get(PrefsBean.PREFS_MEDIUMPRIORITY_EVENTS);
			lowPriorityEvents = (List) priorityEventsMap.get(PrefsBean.PREFS_LOWPRIORITY_EVENTS);
		}else{
			highPriorityEvents = new ArrayList();
			mediumPriorityEvents = new ArrayList();
			lowPriorityEvents = new ArrayList();
		}
	}
	
	private List getCalendarReferences() {
		// get merged calendars channel refs
		List referenceList = M_ca.getCalendarReferences(getSiteId());
		return referenceList;
	}
	
	/**
	 ** loadChannels -- load specified primaryCalendarReference or merged
	 ** calendars if initMergeList is defined
	 **/
	private MergedList loadChannels(String primaryCalendarReference, String initMergeList, MergedList.EntryProvider entryProvider) {
		MergedList mergedCalendarList = new MergedList();
		String[] channelArray;
		boolean isOnWorkspaceTab = M_ss.isUserSite(getSiteId());

		// Figure out the list of channel references that we'll be using.
		// MyWorkspace is special: if not superuser, and not otherwise defined,
		// get all channels
		if(isOnWorkspaceTab && !M_as.isSuperUser() && initMergeList == null){
			channelArray = mergedCalendarList.getAllPermittedChannels(new CalendarChannelReferenceMaker());
		}else{
			channelArray = mergedCalendarList.getChannelReferenceArrayFromDelimitedString(primaryCalendarReference, initMergeList);
		}
		if(entryProvider == null){
			entryProvider = new MergedListEntryProviderFixedListWrapper(new EntryProvider(), primaryCalendarReference, channelArray, new CalendarReferenceToChannelConverter());
		}
		mergedCalendarList.loadChannelsFromDelimitedString(isOnWorkspaceTab, false, entryProvider, StringUtil.trimToZero(M_sm.getCurrentSessionUserId()), channelArray, M_as.isSuperUser(), getSiteId());

		return mergedCalendarList;
	}

	private String getSiteId() {
		if(siteId == null){
			Placement placement = M_tm.getCurrentPlacement();
			siteId = placement.getContext();
		}
		return siteId;
	}
	
	public String getUserId() {
		return M_sm.getCurrentSessionUserId();
	}
	
	private CalendarEventVector getEventsFromSchedule() {
		if(calendarEventVector == null) {
			Calendar firstDay;
			Calendar lastDay;
			
			if(viewMode.equals(MODE_WEEKVIEW)){
				// WEEK VIEW
				
				// select first day
				firstDay = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
				firstDay.setTime(getViewingDate());
				firstDay.set(Calendar.HOUR_OF_DAY, 0);
				firstDay.set(Calendar.MINUTE, 0);
				firstDay.set(Calendar.SECOND, 0);
				firstDay.set(Calendar.MILLISECOND, 0);
				int dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
				// TODO Allow dynamic choice of first day of week
				while(dayOfWeek != Calendar.SUNDAY){
					firstDay.add(Calendar.DAY_OF_WEEK, -1);
					dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
				}
				
				// select last day
				lastDay = (Calendar) firstDay.clone();
				lastDay.add(Calendar.DAY_OF_WEEK, 6);
				lastDay.set(Calendar.HOUR_OF_DAY, 23);
				lastDay.set(Calendar.MINUTE, 59);
				lastDay.set(Calendar.SECOND, 59);
				lastDay.set(Calendar.MILLISECOND, 999);
				dayOfWeek = lastDay.get(Calendar.DAY_OF_WEEK);
				// TODO Allow dynamic choice of first day of week
				while(dayOfWeek != Calendar.SUNDAY){
					lastDay.add(Calendar.DAY_OF_WEEK, 1);
					dayOfWeek = lastDay.get(Calendar.DAY_OF_WEEK);
				}
			}else{
				// MONTH VIEW
				
				// select first day
				firstDay = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
				firstDay.setTime(getViewingDate());
				int selYear = firstDay.get(Calendar.YEAR);
				int selMonth = firstDay.get(Calendar.MONTH);
				firstDay.set(Calendar.YEAR, selYear);
				firstDay.set(Calendar.DAY_OF_MONTH, 1);
				firstDay.set(Calendar.HOUR_OF_DAY, 0);
				firstDay.set(Calendar.MINUTE, 0);
				firstDay.set(Calendar.SECOND, 0);
				firstDay.set(Calendar.MILLISECOND, 0);
				int dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
				// TODO Allow dynamic choice of first day of week
				while(dayOfWeek != Calendar.SUNDAY){
					firstDay.add(Calendar.DAY_OF_WEEK, -1);
					dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
				}
				
				// select last day
				lastDay = (Calendar) firstDay.clone();
				lastDay.set(Calendar.YEAR, selYear);
				lastDay.set(Calendar.MONTH, selMonth);
				lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
				lastDay.set(Calendar.HOUR_OF_DAY, 23);
				lastDay.set(Calendar.MINUTE, 59);
				lastDay.set(Calendar.SECOND, 59);
				lastDay.set(Calendar.MILLISECOND, 999);
				dayOfWeek = lastDay.get(Calendar.DAY_OF_WEEK);
				// TODO Allow dynamic choice of first day of week
				while(dayOfWeek != Calendar.SUNDAY){
					lastDay.add(Calendar.DAY_OF_WEEK, 1);
					dayOfWeek = lastDay.get(Calendar.DAY_OF_WEEK);
				}
			}
			
			
			Time firstTime = M_ts.newTime(firstDay.getTimeInMillis());
			Time lastTime = M_ts.newTime(lastDay.getTimeInMillis());
			TimeRange range = M_ts.newTimeRange(firstTime, lastTime);
			calendarEventVector = M_ca.getEvents(getCalendarReferences(), range);
		}
		return calendarEventVector;
	}
	
	
	private CalendarEventVector getScheduleEventsForDay(Calendar c) {
		CalendarEventVector cev = new CalendarEventVector();
		
		TimeZone timeZone = getCurrentUserTimezone();
		DateTime start = new DateTime(c).withZone(DateTimeZone.forTimeZone(timeZone)).withTime(0, 0, 0, 0);
		log.debug("looking for events for: {}", start);
		Time sod = M_ts.newTime(start.getMillis());
		DateTime endOfDay = new DateTime(c).withZone(DateTimeZone.forTimeZone(timeZone)).withTime(23, 59, 59, 0);
		Time eod = M_ts.newTime(endOfDay.getMillis());
		TimeRange range = M_ts.newTimeRange(sod, eod);
		
		Iterator<CalendarEvent> i = getEventsFromSchedule().iterator();
		while(i.hasNext()){
			CalendarEvent ce = (CalendarEvent) i.next();
			TimeRange tr = ce.getRange();
			if(range.contains(tr.firstTime()) || range.contains(tr.lastTime())){
				log.debug("found event: {}", ce.getDisplayName());
				cev.add(ce);
			}
		}
		return cev;
	}

	/**
	 * Get the TimeZone for the current user
	 * @return
	 */
	private TimeZone getCurrentUserTimezone() {
		
		TimeZone tz = TimeService.getLocalTimeZone();
		log.debug("got tz {}", tz.getDisplayName());
		return tz;
	}
//	}
	
	private List getDayEvents(CalendarEventVector dayEventVector) {
		ListIterator i = dayEventVector.listIterator();
		List eventList = new ArrayList();
		while (i.hasNext()){
			CalendarEvent e = (CalendarEvent) i.next();
			EventSummary es = new EventSummary();
			es.setDisplayName(e.getDisplayName());
			es.setType(e.getType());
			es.setTypeLocalized(calendarUtil.getLocalizedEventType(e.getType()));
			es.setCalendarRef(e.getCalendarReference());
			es.setEventRef(e.getId());
			es.setUrl(e.getUrl());
			es.setAttachments(e.getAttachments());
			es.setSite(e.getSiteName());
			eventList.add(es);
		}
		return eventList;
	}

	private int getDayEventCount(CalendarEventVector dayEventVector) {
		return dayEventVector.size();
	}

	private String getDayPriorityCSSProperty(CalendarEventVector dayEventVector) {
		ListIterator i = dayEventVector.listIterator();
		String highestPriorityFound = "";
		while (i.hasNext()){
			CalendarEvent e = (CalendarEvent) i.next();
			String type = e.getType();
			if(highPriorityEvents != null && highPriorityEvents.contains(type))
				highestPriorityFound = PRIORITY_HIGH;
			else if(mediumPriorityEvents != null && mediumPriorityEvents.contains(type) && !highestPriorityFound.equals(PRIORITY_HIGH))
				highestPriorityFound = PRIORITY_MEDIUM;
			else if(lowPriorityEvents != null && lowPriorityEvents.contains(type) && !highestPriorityFound.equals(PRIORITY_HIGH) && !highestPriorityFound.equals(PRIORITY_MEDIUM))
				highestPriorityFound = PRIORITY_LOW;
			
			if(highestPriorityFound.equals(PRIORITY_HIGH))
				break;
		}
		if(highestPriorityFound.equals(""))
			return "";
		else if(highestPriorityFound.equals(PRIORITY_LOW))
			return lowPrCSSProp;
		else if(highestPriorityFound.equals(PRIORITY_MEDIUM))
			return mediumPrCSSProp;
		else if(highestPriorityFound.equals(PRIORITY_HIGH))
			return highPrCSSProp;
		return "";
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public void currDay(ActionEvent e) {
		Calendar cal = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
		setViewingDate(cal.getTime());
		// show events for today if any
		selectedDay = getToday();
		selectedEventRef = null;
		updateEventList = true;
	}
	
	public void prev(ActionEvent e) {
		updateEventList = true;
		if(viewMode.equals(MODE_WEEKVIEW)){
			// week view
			prevWeek(e);
		}else{
			// month view
			prevMonth(e);
		}
	}
	
	public void next(ActionEvent e) {
		updateEventList = true;
		if(viewMode.equals(MODE_WEEKVIEW)){
			// week view
			nextWeek(e);
		}else{
			// month view
			nextMonth(e);
		}
	}
	
	private void prevMonth(ActionEvent e) {
		Calendar cal = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.MONTH, -1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}

	private void nextMonth(ActionEvent e) {
		Calendar cal = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.MONTH, +1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}
	
	private void prevWeek(ActionEvent e) {
		Calendar cal = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}

	private void nextWeek(ActionEvent e) {
		Calendar cal = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.WEEK_OF_YEAR, +1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}

	public void selectDate(ActionEvent e) {
		try{
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			Map paramMap = context.getRequestParameterMap();
			String dateStr = (String) paramMap.get("selectedDay");
			DateFormat df = new SimpleDateFormat(msgs.getString("date_link_format"), msgs.getLocale());
			df.setTimeZone(getCurrentUserTimezone());
			selectedDay = df.parse(dateStr);
			selectedEventRef = null;
			updateEventList = true;
		}catch(Exception ex){
			log.error("Error getting selectedDate: {}", ex.toString());
		}
	}

	public void selectEvent(ActionEvent e) {
		try{
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			Map paramMap = context.getRequestParameterMap();
			selectedCalendarRef = (String) paramMap.get("calendarRef");
			selectedEventRef = (String) paramMap.get("eventRef");
			selectedEvent = null;
			updateEventList = false;
		}catch(Exception ex){
			log.error("Error getting selectedEventRef: {}", ex.toString());
		}
	}

	public void backToEventList(ActionEvent e) {
		try{
			selectedEventRef = null;
			updateEventList = true;
		}catch(Exception ex){
			log.error("Error in backToEventList: {}", ex.toString());
		}
	}
	
	public String getViewMode() {
		return viewMode;
	}
	
	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	private void initializeWeeksDataStructure() {
		weeks = new ArrayList();
		weeks.add(week1);
		weeks.add(week2);
		weeks.add(week3);
		weeks.add(week4);
		weeks.add(week5);
		weeks.add(week6);
		for(int w = 0; w < 6; w++){
			MonthWeek week = (MonthWeek) weeks.get(w);
			for(int d = 0; d < 7; d++){
				week.setDay(d, new Day());
			}
			weeks.set(w, week);
		}
	}

	public List getCalendar() {
		if(viewMode.equals(MODE_WEEKVIEW)){
			// WEEK VIEW
			return getWeek();
		}else{
			// MONTH VIEW
			return getWeeks();
		}
	}
	
	private List getWeeks() {
		if(reloadCalendarEvents()) {
			initializeWeeksDataStructure();
			
			// selected month
			Calendar c = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
			c.setTime(getViewingDate());
			int selYear = c.get(Calendar.YEAR);
			int selMonth = c.get(Calendar.MONTH);
			c.set(Calendar.MONTH, selMonth);
			c.set(Calendar.DAY_OF_MONTH, 1);
			int dayOfWeek = new CalendarUtil(c).getDay_Of_Week(true);
			int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
			int currDay = 1;
			// prev month
			c.add(Calendar.MONTH, -1);
			int prevMonthLastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

			// fill weeks
			int weekNo = -1;
			int nextMonthDay = 1;
			while (currDay <= lastDay){
				MonthWeek week = null;
				try{
					++weekNo;
					week = (MonthWeek) weeks.get(weekNo);
				}catch(IndexOutOfBoundsException e){
					// if previous had less weeks (4, 5)
					if(weekNo == 4){
						weeks.add(week5);
						week = week5;
					}
					if(weekNo == 5){
						weeks.add(week6);
						week = week6;
					}
				}
				for(int i = 0; i < 7; i++){
					Day day = week.getDay(i);
					CalendarEventVector vector = null;
					if((weekNo == 0) && (i < dayOfWeek - 1)){
						int nDay = prevMonthLastDay - dayOfWeek + 2 + i;
						c.set(Calendar.MONTH, selMonth - 1);
						c.set(Calendar.DAY_OF_MONTH, nDay);
						vector = getScheduleEventsForDay(c);
						day = new Day(c, getDayEventCount(vector) > 0);
						day.setOccursInOtherMonth(true);
						day.setBackgroundCSSProperty(getDayPriorityCSSProperty(vector));
					}else if(currDay > lastDay){
						c.set(Calendar.MONTH, selMonth + 1);
						c.set(Calendar.DAY_OF_MONTH, nextMonthDay++);
						vector = getScheduleEventsForDay(c);
						day = new Day(c, getDayEventCount(vector) > 0);
						day.setOccursInOtherMonth(true);
						day.setBackgroundCSSProperty(getDayPriorityCSSProperty(vector));
					}else{
						c.set(Calendar.YEAR, selYear);
						c.set(Calendar.MONTH, selMonth);
						c.set(Calendar.DAY_OF_MONTH, currDay++);
						vector = getScheduleEventsForDay(c);
						day = new Day(c, getDayEventCount(vector) > 0);
						day.setOccursInOtherMonth(false);
						day.setBackgroundCSSProperty(getDayPriorityCSSProperty(vector));
					}
					day.setToday(sameDay(c, getToday()));
					day.setSelected(selectedDay != null && sameDay(c, selectedDay));
					day.setDayEvents(getDayEvents(vector));
					week.setDay(i, day);

				}
				weeks.set(weekNo, week);
			}
			// remove unused weeks (5th, 6th week of month)
			while (((weeks.size() - 1) - weekNo) > 0){
				weeks.remove(weeks.size() - 1);
			}
		}
		return weeks;
	}
	
	private List getWeek() {
		if(reloadCalendarEvents()) {
			// initialize days
			weeks = new ArrayList();
			for(int d = 0; d < 7; d++){
				week1.setDay(d, new Day());
			}
			weeks.add(week1);
			
			// selected week
			Calendar c = Calendar.getInstance(getCurrentUserTimezone(), msgs.getLocale());
			c.setTime(getViewingDate());
			int selMonth = c.get(Calendar.MONTH);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			//int selWeek = c.get(Calendar.WEEK_OF_YEAR);
			
			// select first day of week (locale-specific)
			while(dayOfWeek != c.getFirstDayOfWeek()) {
				c.add(Calendar.DAY_OF_WEEK, -1);
				dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			}
			
			for(int i=0; i<7; i++){
				Day day;
				boolean sameMonth = (selMonth == c.get(Calendar.MONTH));
				boolean selected = (selectedDay != null) && (sameDay(c, selectedDay));

				CalendarEventVector vector = getScheduleEventsForDay(c);
				day = new Day(c, getDayEventCount(vector) > 0);
				day.setOccursInOtherMonth(!sameMonth);
				day.setBackgroundCSSProperty(getDayPriorityCSSProperty(vector));
				day.setToday(sameDay(c, getToday()));
				day.setSelected(selected);	
				day.setDayEvents(getDayEvents(vector));
	
				week1.setDay(i, day);
				c.add(Calendar.DAY_OF_WEEK, +1);
			}
			weeks.set(0, week1);
		}
		return weeks;
	}

	private boolean reloadCalendarEvents() {
		boolean reload = (weeks == null) || (weeks.size() == 0) || updateEventList || (!prevViewMode.equals(viewMode));
		return reload;
	}
	
	private boolean sameDay(Calendar date1, Date date2) {
		Calendar cal1 = Calendar.getInstance(getCurrentUserTimezone(), msgs.getLocale());
		cal1.setTime(date1.getTime());
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);

		Calendar cal2 = Calendar.getInstance(getCurrentUserTimezone(), msgs.getLocale());
		cal2.setTime(date2);
		cal2.set(Calendar.HOUR_OF_DAY, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);

		return (cal2.get(Calendar.YEAR) == cal1.get(Calendar.YEAR)) && (cal2.get(Calendar.MONTH) == cal1.get(Calendar.MONTH)) && (cal2.get(Calendar.DAY_OF_MONTH) == cal1.get(Calendar.DAY_OF_MONTH));
	}

	public Date getToday() {
		if(today == null){
			Calendar c = Calendar.getInstance(getCurrentUserTimezone(), msgs.getLocale());
			today = c.getTime();
		}
		return today;
	}

	public String getCaption() {
		SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_MONTH_DATEFORMAT, msgs.getLocale());
		formatter.setTimeZone(getCurrentUserTimezone());
		return formatter.format(getViewingDate());
	}

	public boolean isViewingSelectedDay() {
		if (selectedDay == null) {
			selectedDayHasEvents = false;
			return false;
		}

		Calendar t = Calendar.getInstance(getCurrentUserTimezone(), msgs.getLocale());
		t.setTime(selectedDay);
		selectedDayHasEvents = getScheduleEventsForDay(t).size() > 0;

		return selectedDayHasEvents && selectedEventRef == null;
	}

	public boolean isViewingSelectedEvent() {
		return selectedEventRef != null;
	}

	public String getSelectedDayAsString() {
		SimpleDateFormat formatter = new SimpleDateFormat(msgs.getString("date_format"), msgs.getLocale());
		formatter.setTimeZone(getCurrentUserTimezone());
		return StringUtils.capitalize(formatter.format(selectedDay));
	}

	public List getSelectedDayEvents() {
		Calendar c = Calendar.getInstance(getCurrentUserTimezone(), msgs.getLocale());
		c.setTime(selectedDay);
		return getDayEvents(getScheduleEventsForDay(c));
	}

	public EventSummary getSelectedEvent() {
		if(selectedEvent == null){
			try{
				org.sakaiproject.calendar.api.Calendar calendar = M_ca.getCalendar(selectedCalendarRef);
				CalendarEvent event = calendar.getEvent(selectedEventRef);
				selectedEvent = new EventSummary();
				selectedEvent.setDisplayName(event.getDisplayName());
				selectedEvent.setDate(event.getRange());
				selectedEvent.setType(event.getType());
				selectedEvent.setTypeLocalized(calendarUtil.getLocalizedEventType(event.getType()));
				selectedEvent.setDescription(event.getDescriptionFormatted());
				selectedEvent.setLocation(event.getLocation());
				Site site = M_ss.getSite(calendar.getContext());
				selectedEvent.setSite(site.getTitle());
				String eventUrl = buildEventUrl(site, event.getReference());
				selectedEvent.setUrl(eventUrl);
				selectedEvent.setAttachments(event.getAttachments());
				//Checking assignment If the event is assignment due date
				try{
					String assignmentId = event.getField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);
					if (assignmentId != null && assignmentId.trim().length() > 0)
					{
						StringBuilder entityId = new StringBuilder( ASSN_ENTITY_PREFIX );
						entityId.append( assignmentId );
						if (entityBroker == null)
						{
							entityBroker = (EntityBroker) ComponentManager.get("org.sakaiproject.entitybroker.EntityBroker");
						}
						entityBroker.executeCustomAction(entityId.toString(), ASSN_ENTITY_ACTION, null, null);						
					}
					
				}catch(EntityNotFoundException e){
					final String openDateErrorDescription = msgs.getFormattedMessage("java.alert.opendatedescription",
									event.getField(CalendarConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
					selectedEvent.setOpenDateErrorDescription(openDateErrorDescription);
					selectedEvent.setOpenDateError(true);
				}				
				
				// groups
				if(M_as.unlock("calendar.all.groups", "/site/"+calendar.getContext())){
					Collection grps = event.getGroupObjects();
					if(grps.size() > 0){
						StringBuilder sb = new StringBuilder();
						Iterator gi = grps.iterator();
						while(gi.hasNext()){
							Group g = (Group) gi.next();
							if(sb.length() > 0)
								sb.append(", ");
							sb.append(g.getTitle());
						}
						selectedEvent.setGroups(sb.toString());
					}
				}
				
			}catch(IdUnusedException e){
				log.error("IdUnusedException: {}", e.getMessage());
			}catch(PermissionException e){
				log.error("Permission exception: {}", e.getMessage());
			}
		}
		return selectedEvent;
	}
	
	private String buildEventUrl(Site site, String eventRef) {
		StringBuilder url = new StringBuilder();
		ToolConfiguration tc = null;
		if ("!worksite".equals(site.getId()))
		{
			// Institutional Calendar events are set up in "!worksite", but users can't view these events.
			// Get the schedule tool from the current context, if none found, take them to their workspace
			String siteId = M_tm.getCurrentPlacement().getContext();
			try
			{
				Site currentSite = M_ss.getSite(siteId);
				tc = currentSite.getToolForCommonId(SCHEDULE_TOOL_ID);
				if (tc == null)
				{
					String myWorkspaceId = M_ss.getUserSiteId(getUserId());
					Site myWorkspace = M_ss.getSite(myWorkspaceId);
					tc = myWorkspace.getToolForCommonId(SCHEDULE_TOOL_ID);
				}
			}
			catch (IdUnusedException e)
			{
				log.error("IdUnusedException: {}", e.getMessage());
			}
		}
		if (tc == null)
		{
			tc = site.getToolForCommonId(SCHEDULE_TOOL_ID);
		}
		if(tc != null) {
			url.append(ServerConfigurationService.getPortalUrl());
			url.append("/directtool/");
			url.append(tc.getId());
			url.append("?eventReference=");
			url.append(Validator.escapeUrl(eventRef));
			url.append("&panel=Main&sakai_action=doDescription&sakai.state.reset=true");
			return url.toString();
		}else{
			// no schedule tool in site
			return null;
		}
	}

	public Map<String, String> getEventIconMap() {
		Map<String, String> spanIconMap = new HashMap<>();
		Map<String, String> iconMap = CalendarEventType.getIcons();
		Set<String> eventKeys = iconMap.keySet();
		for (String eventType: eventKeys)
		{
			spanIconMap.put(eventType, "<span class=\"icon " + iconMap.get(eventType) + "\"></span>");
		}
		
		return spanIconMap;
	}
	
	public String getImgLocation() {
		return this.imgLocation;
	}

	public Date getViewingDate() {
		if(viewingDate == null){
			Calendar c = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
			viewingDate = c.getTime();
		}
		return viewingDate;
	}

	public void setViewingDate(Date selectedMonth) {
		this.viewingDate = selectedMonth;
	}
	
	public String[] getDayOfWeekNames() {
		Calendar c = Calendar.getInstance(getCurrentUserTimezone(),msgs.getLocale());
		return new CalendarUtil(c).getCalendarDaysOfWeekNames(false);
	}
	
	//SAK-19700 method to get name of tool so it can be rendered with the option link, for screenreaders
	public String getToolTitle() {
		return M_tm.getCurrentPlacement().getTitle();
	}
	
	//SAK-19700 renders a complete Options link with an additional span link for accessiblity
	public String getAccessibleOptionsLink() {
		StringBuilder sb = new StringBuilder();
		sb.append(msgs.getString("menu_prefs"));
		sb.append("<span class=\"skip\">");
		sb.append(getToolTitle());
		sb.append("</span>");
		return sb.toString();
		
	}
	
	/**
	 * Tests if the options section should be displayed.
	 */
	public boolean isPreferencesVisible() {
		return M_as.unlock(CalendarService.AUTH_OPTIONS_CALENDAR, M_ca.calendarReference(getSiteId(), SiteService.MAIN_CONTAINER));
	}
	private final static String ASSN_ENTITY_ID     = "assignment";
	private final static String ASSN_ENTITY_ACTION = "item";
	private EntityBroker entityBroker;
	private final static String ASSN_ENTITY_PREFIX = EntityReference.SEPARATOR+ASSN_ENTITY_ID+EntityReference.SEPARATOR+ASSN_ENTITY_ACTION+EntityReference.SEPARATOR;
}
