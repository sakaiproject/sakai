/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventVector;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.CalendarUtil;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.MergedListEntryProviderBase;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;


public class CalendarBean {

	public static final String 						MODE_MONTHVIEW			= "month";
	public static final String 						MODE_WEEKVIEW			= "week";
	public static final String 						PRIORITY_HIGH			= "priority_high";
	public static final String 						PRIORITY_MEDIUM			= "priority_medium";
	public static final String 						PRIORITY_LOW			= "priority_low";
	public static final String						DATE_FORMAT				= "MMMMM dd, yyyy";
	private static final String 					imgLocation				= "../../../library/image/sakai/";
	private static final String 					SCHEDULE_TOOL_ID		= "sakai.schedule";
	
	/** Used to retrieve non-notification sites for MyWorkspace page */
	private static final String 					TABS_EXCLUDED_PREFS 	= "sakai:portal:sitenav";
	private final String 							TAB_EXCLUDED_SITES 		= "exclude";
	private static final String 					MERGED_CALENDARS_PROP 	= "mergedCalendarReferences";
	
	/** Our log (commons). */
	private static Log								LOG						= LogFactory.getLog(CalendarBean.class);

	/** Resource bundle */
	private transient ResourceLoader				msgs					= new ResourceLoader("calendar");
	
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
	private String[]								months					= { "month.jan", "month.feb", "month.mar", "month.apr", "month.may", "month.jun", "month.jul", "month.aug", "month.sep", "month.oct",
			"month.nov", "month.dec"											};

	private Map										eventImageMap			= new HashMap();
	
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
	

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public CalendarBean(){		
		readPreferences();
		// go to today events it is first time loading
		selectedDay = getToday();
	}
	
	public String getInitValues() {
		// reload localized event types
		EventTypes.reloadLocalization();
		
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
		LOG.debug("Reading preferences...");
		lastModifiedPrefs = PrefsBean.getPreferenceLastModified();
		
		// view mode
		prevViewMode = viewMode;
		viewMode = PrefsBean.getPreferenceViewMode();
		
		// priority colors (CSS properties)
		priorityColorsMap = PrefsBean.getPreferencePriorityColors();
		highPrCSSProp = (String) priorityColorsMap.get(PrefsBean.PREFS_HIGHPRIORITY_COLOR);
		mediumPrCSSProp = (String) priorityColorsMap.get(PrefsBean.PREFS_MEDIUMPRIORITY_COLOR);
		lowPrCSSProp = (String) priorityColorsMap.get(PrefsBean.PREFS_LOWPRIORITY_COLOR);
		
		highPrCSSProp = highPrCSSProp.equals("")? "" : "background-color: " + highPrCSSProp;
		mediumPrCSSProp = mediumPrCSSProp.equals("")? "" : "background-color: " + mediumPrCSSProp;
		lowPrCSSProp = lowPrCSSProp.equals("")? "" : "background-color: " + lowPrCSSProp;
		
		// priority events
		priorityEventsMap = PrefsBean.getPreferencePriorityEvents();
		highPriorityEvents = (List) priorityEventsMap.get(PrefsBean.PREFS_HIGHPRIORITY_EVENTS);
		mediumPriorityEvents = (List) priorityEventsMap.get(PrefsBean.PREFS_MEDIUMPRIORITY_EVENTS);
		lowPriorityEvents = (List) priorityEventsMap.get(PrefsBean.PREFS_LOWPRIORITY_EVENTS);
	}
	
	private List getCalendarReferences() {
		// get merged calendars channel refs
		String initMergeList = null;
		try{
			ToolConfiguration tc = M_ss.getSite(getSiteId()).getToolForCommonId(SCHEDULE_TOOL_ID);
			if(tc != null) {
				initMergeList = tc.getPlacementConfig().getProperty(MERGED_CALENDARS_PROP);
			}
		}catch(IdUnusedException e){
			initMergeList = null;
		}
		
		// load all calendar channels (either primary or merged calendars)
		String primaryCalendarReference = M_ca.calendarReference(getSiteId(), SiteService.MAIN_CONTAINER);
 		MergedList mergedCalendarList = loadChannels(primaryCalendarReference, initMergeList, null);
 		
		// add external calendar subscriptions
        List referenceList = mergedCalendarList.getReferenceList();
        Set subscriptionRefList = M_ecs.getCalendarSubscriptionChannelsForChannels(referenceList);
        referenceList.addAll(subscriptionRefList);
				
		return referenceList;
	}
	
	/**
	 ** loadChannels -- load specified primaryCalendarReference or merged
	 ** calendars if initMergeList is defined
	 **/
	private MergedList loadChannels(String primaryCalendarReference, String initMergeList, MergedList.EntryProvider entryProvider) {
		MergedList mergedCalendarList = new MergedList();
		String[] channelArray = null;
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
	
	/*
	 * Callback class so that we can form references in a generic way.
	 */
	private final class CalendarChannelReferenceMaker implements MergedList.ChannelReferenceMaker {
		public String makeReference(String siteId) {
			return M_ca.calendarReference(siteId, SiteService.MAIN_CONTAINER);
		}
	}
	
	/**
	 * Provides a list of merged calendars by iterating through all
	 * available calendars.
	 */
	private final class EntryProvider extends MergedListEntryProviderBase {
 		/** calendar channels from hidden sites */
 		private final List excludedSites = new ArrayList();
 		
 		public EntryProvider() {
 			this(false);
 		}
 		
 		public EntryProvider(boolean excludeHiddenSites) {
 			if(excludeHiddenSites) {
	 			List<String> excludedSiteIds = getExcludedSitesFromTabs();
	 			if(excludedSiteIds != null) {
		 			for(String siteId : excludedSiteIds) {
		 				excludedSites.add(M_ca.calendarReference(siteId, SiteService.MAIN_CONTAINER));
		 			}
	 			}
 			}
 		}
 		
		/*
		 * (non-Javadoc)
		 * @see
		 * org.sakaiproject.util.MergedListEntryProviderBase#makeReference(java
		 * .lang.String)
		 */
		public Object makeObjectFromSiteId(String id) {
			String calendarReference = M_ca.calendarReference(id, SiteService.MAIN_CONTAINER);
			Object calendar = null;

			if(calendarReference != null){
				try{
					calendar = M_ca.getCalendar(calendarReference);
				}catch(IdUnusedException e){
					// The channel isn't there.
				}catch(PermissionException e){
					// We can't see the channel
				}
			}

			return calendar;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.chefproject.actions.MergedEntryList.EntryProvider#allowGet(java
		 * .lang.Object)
		 */
		public boolean allowGet(String ref) {
			return !excludedSites.contains(ref) && M_ca.allowGetCalendar(ref);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.chefproject.actions.MergedEntryList.EntryProvider#getContext(
		 * java.lang.Object)
		 */
		public String getContext(Object obj) {
			if(obj == null){
				return "";
			}

			org.sakaiproject.calendar.api.Calendar calendar = (org.sakaiproject.calendar.api.Calendar) obj;
			return calendar.getContext();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.chefproject.actions.MergedEntryList.EntryProvider#getReference
		 * (java.lang.Object)
		 */
		public String getReference(Object obj) {
			if(obj == null){
				return "";
			}

			org.sakaiproject.calendar.api.Calendar calendar = (org.sakaiproject.calendar.api.Calendar) obj;
			return calendar.getReference();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.chefproject.actions.MergedEntryList.EntryProvider#getProperties
		 * (java.lang.Object)
		 */
		public ResourceProperties getProperties(Object obj) {
			if(obj == null){
				return null;
			}

			org.sakaiproject.calendar.api.Calendar calendar = (org.sakaiproject.calendar.api.Calendar) obj;
			return calendar.getProperties();
		}
	}
	
	/**
	 * Used by callback to convert channel references to channels.
	 */
	private final class CalendarReferenceToChannelConverter implements MergedListEntryProviderFixedListWrapper.ReferenceToChannelConverter {
		public Object getChannel(String channelReference) {
			try{
				return M_ca.getCalendar(channelReference);
			}catch(IdUnusedException e){
				return null;
			}catch(PermissionException e){
				return null;
			}
		}
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
				firstDay = Calendar.getInstance(msgs.getLocale());
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
			}else{
				// MONTH VIEW
				
				// select first day
				firstDay = Calendar.getInstance(msgs.getLocale());
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
				while(dayOfWeek != Calendar.SATURDAY){
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
		
		// find start and end of day
		Calendar startOfDay = (Calendar) c.clone();
		startOfDay.set(Calendar.HOUR_OF_DAY, 0);
		startOfDay.set(Calendar.MINUTE, 0);
		startOfDay.set(Calendar.SECOND, 0);
		startOfDay.set(Calendar.MILLISECOND, 0);
		Time sod = M_ts.newTime(startOfDay.getTimeInMillis());
		Calendar endOfDay = (Calendar) c.clone();
		endOfDay.set(Calendar.HOUR_OF_DAY, 23);
		endOfDay.set(Calendar.MINUTE, 59);
		endOfDay.set(Calendar.SECOND, 59);
		endOfDay.set(Calendar.MILLISECOND, 999);
		Time eod = M_ts.newTime(endOfDay.getTimeInMillis());
		TimeRange range = M_ts.newTimeRange(sod, eod);
		
		Iterator i = getEventsFromSchedule().iterator();
		while(i.hasNext()){
			CalendarEvent ce = (CalendarEvent) i.next();
			TimeRange tr = ce.getRange();
			if(range.contains(tr.firstTime()) || range.contains(tr.lastTime())){
				cev.add(ce);
			}
		}
		return cev;
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
			es.setTypeLocalized(EventTypes.getLocalizedEventType(e.getType()));
			es.setCalendarRef(e.getCalendarReference());
			es.setEventRef(e.getId());
			es.setUrl(e.getUrl());
			es.setAttachments(e.getAttachments());
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
		Calendar cal = Calendar.getInstance(msgs.getLocale());
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
		Calendar cal = Calendar.getInstance(msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.MONTH, -1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}

	private void nextMonth(ActionEvent e) {
		Calendar cal = Calendar.getInstance(msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.MONTH, +1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}
	
	private void prevWeek(ActionEvent e) {
		Calendar cal = Calendar.getInstance(msgs.getLocale());
		cal.setTime(viewingDate);
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		setViewingDate(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
	}

	private void nextWeek(ActionEvent e) {
		Calendar cal = Calendar.getInstance(msgs.getLocale());
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
			DateFormat df = new SimpleDateFormat(DATE_FORMAT);
			selectedDay = df.parse(dateStr);
			selectedEventRef = null;
			updateEventList = true;
		}catch(Exception ex){
			LOG.error("Error getting selectedDate:" + ex.toString());
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
			LOG.error("Error getting selectedEventRef:" + ex.toString());
		}
	}

	public void backToEventList(ActionEvent e) {
		try{
			selectedEventRef = null;
			updateEventList = true;
		}catch(Exception ex){
			LOG.error("Error in backToEventList:" + ex.toString());
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
			Calendar c = Calendar.getInstance(msgs.getLocale());
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
						day = new Day(c.getTime(), getDayEventCount(vector) > 0);
						day.setOccursInOtherMonth(true);
						day.setBackgroundCSSProperty(getDayPriorityCSSProperty(vector));
					}else if(currDay > lastDay){
						c.set(Calendar.MONTH, selMonth + 1);
						c.set(Calendar.DAY_OF_MONTH, nextMonthDay++);
						vector = getScheduleEventsForDay(c);
						day = new Day(c.getTime(), getDayEventCount(vector) > 0);
						day.setOccursInOtherMonth(true);
						day.setBackgroundCSSProperty(getDayPriorityCSSProperty(vector));
					}else{
						c.set(Calendar.YEAR, selYear);
						c.set(Calendar.MONTH, selMonth);
						c.set(Calendar.DAY_OF_MONTH, currDay++);
						vector = getScheduleEventsForDay(c);
						day = new Day(c.getTime(), getDayEventCount(vector) > 0);
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
			Calendar c = Calendar.getInstance(msgs.getLocale());
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
				Day day = (Day) week1.getDay(i);
				boolean sameMonth = (selMonth == c.get(Calendar.MONTH));
				boolean selected = (selectedDay != null) && (sameDay(c, selectedDay));

				CalendarEventVector vector = getScheduleEventsForDay(c);
				day = new Day(c.getTime(), getDayEventCount(vector) > 0);
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
		date1.set(Calendar.HOUR_OF_DAY, 0);
		date1.set(Calendar.MINUTE, 0);
		date1.set(Calendar.SECOND, 0);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date2);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		return (cal.get(Calendar.YEAR) == date1.get(Calendar.YEAR)) && (cal.get(Calendar.MONTH) == date1.get(Calendar.MONTH)) && (cal.get(Calendar.DAY_OF_MONTH) == date1.get(Calendar.DAY_OF_MONTH));
	}

	public Date getToday() {
		if(today == null){
			Calendar c = Calendar.getInstance();
			today = c.getTime();
		}
		return today;
	}

	public String getCaption() {
		Calendar c = Calendar.getInstance();
		c.setTime(getViewingDate());
		String month = msgs.getString(months[c.get(Calendar.MONTH)]);
		String year = c.get(Calendar.YEAR) + "";
		return month + ", " + year;
	}

	public boolean isViewingSelectedDay() {
		if(selectedDay != null){
			Calendar t = Calendar.getInstance();
			t.setTime(selectedDay);
			selectedDayHasEvents = getScheduleEventsForDay(t).size() > 0;
		}
		
		/*return selectedDayHasEvents && selectedDay != null && selectedEventRef == null;*/
		return selectedDayHasEvents && selectedDay != null && selectedEventRef == null;
	}

	public boolean isViewingSelectedEvent() {
		return selectedEventRef != null;
	}

	public String getSelectedDayAsString() {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, msgs.getLocale());
		return StringUtils.capitalize(formatter.format(selectedDay));
	}

	public List getSelectedDayEvents() {
		Calendar c = Calendar.getInstance();
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
				selectedEvent.setTypeLocalized(EventTypes.getLocalizedEventType(event.getType()));
				selectedEvent.setDescription(event.getDescriptionFormatted());
				selectedEvent.setLocation(event.getLocation());
				Site site = M_ss.getSite(calendar.getContext());
				selectedEvent.setSite(site.getTitle());
				String eventUrl = buildEventUrl(site, event.getReference());
				selectedEvent.setUrl(eventUrl);
				selectedEvent.setAttachments(event.getAttachments());
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
				LOG.error("IdUnusedException: " + e.getMessage());
			}catch(PermissionException e){
				e.printStackTrace();
				LOG.error("Permission exception: " + e.getMessage());
			}
		}
		return selectedEvent;
	}
	
	private String buildEventUrl(Site site, String eventRef) {
		StringBuilder url = new StringBuilder();
		ToolConfiguration tc = site.getToolForCommonId(SCHEDULE_TOOL_ID);
		if(tc != null) {
			url.append(ServerConfigurationService.getPortalUrl());
			url.append("/directtool/");
			url.append(tc.getId());
			url.append("?eventReference=");
			url.append(eventRef);
			url.append("&panel=Main&sakai_action=doDescription");		
			return url.toString();
		}else{
			// no schedule tool in site
			return null;
		}
	}

	// tbd: this needs to used gif files defined in calendar-tool/tool/src/config/.../calendar.config
	public synchronized Map getEventImageMap() {
		if(eventImageMap == null || eventImageMap.size() == 0){
			eventImageMap = new HashMap();
			eventImageMap.put("Academic Calendar", imgLocation + "academic_calendar.gif");
			eventImageMap.put("Activity", imgLocation + "activity.gif");
			eventImageMap.put("Cancellation", imgLocation + "cancelled.gif");
			eventImageMap.put("Class section - Discussion", imgLocation + "class_dis.gif");
			eventImageMap.put("Class section - Lab", imgLocation + "class_lab.gif");
			eventImageMap.put("Class section - Lecture", imgLocation + "class_lec.gif");
			eventImageMap.put("Class section - Small Group", imgLocation + "class_sma.gif");
			eventImageMap.put("Class session", imgLocation + "class_session.gif");
			eventImageMap.put("Computer Session", imgLocation + "computersession.gif");
			eventImageMap.put("Deadline", imgLocation + "deadline.gif");
			eventImageMap.put("Exam", imgLocation + "exam.gif");
			eventImageMap.put("Meeting", imgLocation + "meeting.gif");
			eventImageMap.put("Multidisciplinary Conference", imgLocation + "multi-conference.gif");
			eventImageMap.put("Quiz", imgLocation + "quiz.gif");
			eventImageMap.put("Special event", imgLocation + "special_event.gif");
			eventImageMap.put("Web Assignment", imgLocation + "webassignment.gif");
		}
		return eventImageMap;
	}
	
	public String getImgLocation() {
		return this.imgLocation;
	}

	public Date getViewingDate() {
		if(viewingDate == null){
			Calendar c = Calendar.getInstance(msgs.getLocale());
			viewingDate = c.getTime();
		}
		return viewingDate;
	}

	public void setViewingDate(Date selectedMonth) {
		this.viewingDate = selectedMonth;
	}
	
	/**
	 * Pulls excluded site ids from Tabs preferences
	 */
	private List getExcludedSitesFromTabs() {
		final Preferences prefs = M_ps.getPreferences(M_sm.getCurrentSessionUserId());
		final ResourceProperties props = prefs.getProperties(TABS_EXCLUDED_PREFS);
		final List l = props.getPropertyList(TAB_EXCLUDED_SITES);
		return l;		
	}
	
	public String[] getDayOfWeekNames() {
		return new CalendarUtil().getCalendarDaysOfWeekNames(false);
	}
}
