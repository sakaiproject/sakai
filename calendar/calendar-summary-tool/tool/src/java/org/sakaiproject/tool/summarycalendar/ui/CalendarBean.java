/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.io.Serializable;
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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.summarycalendar.jsf.InitializableBean;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.ResourceLoader;


public class CalendarBean extends InitializableBean implements Serializable {
	private static final long						serialVersionUID		= 3399742150736774779L;
	public static final String						DATE_FORMAT				= "MMM dd, yyyy";

	/** Our log (commons). */
	private static Log								LOG						= LogFactory.getLog(CalendarBean.class);

	/** Resource bundle */
	private transient ResourceLoader				msgs					= new ResourceLoader("org.sakaiproject.tool.summarycalendar.bundle.Messages");

	/** Bean members */
	private Date									today					= null;
	private Date									selectedMonth			= null;
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
	private List									calendarReferences		= new ArrayList();
	private String									siteId					= null;
	private String[]								months					= { "mon_jan", "mon_feb", "mon_mar", "mon_apr", "mon_may", "mon_jun", "mon_jul", "mon_aug", "mon_sep", "mon_oct",
			"mon_nov", "mon_dec"											};
	private Map										eventImageMap			= new HashMap();
	private boolean									firstTime				= true;

	private transient CalendarService				M_ca					= (CalendarService) ComponentManager.get(CalendarService.class.getName());
	private transient TimeService					M_ts					= (TimeService) ComponentManager.get(TimeService.class.getName());
	private transient SiteService					M_ss					= (SiteService) ComponentManager.get(SiteService.class.getName());
	private transient SecurityService				M_as					= (SecurityService) ComponentManager.get(SecurityService.class.getName());
	private transient ToolManager					M_tm					= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	//private transient ServerConfigurationService	M_config				= (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class.getName());

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("CalendarBean.init()");

		if(firstTime){
			selectedDay = getToday();
		}
		if(selectedDay != null){
			Calendar t = Calendar.getInstance();
			t.setTime(selectedDay);
			selectedDayHasEvents = getDayEventCount(t) > 0;
		}
		if(firstTime || (selectedDay != null && selectedDay.equals(getToday()))){
			firstTime = false;
			if(selectedDayHasEvents) selectedDay = getToday();
			else selectedDay = null;
		}
	}

	// ######################################################################################
	// Private methods
	// ######################################################################################
	private List getCalendarReferences() {
		if(calendarReferences == null || calendarReferences.size() == 0){
			MergedList mergedCalendarList = new MergedList();

			String[] channelArray = null;
			boolean isOnWorkspaceTab = M_ss.isUserSite(getSiteId());
			boolean isSuperUser = M_as.isSuperUser();

			// Figure out the list of channel references that we'll be using.
			// If we're on the workspace tab, we get everything.
			// Don't do this if we're the super-user, since we'd be
			// overwhelmed.
			calendarReferences = new ArrayList();
			if(isOnWorkspaceTab && !isSuperUser){
				channelArray = mergedCalendarList.getAllPermittedChannels(new CalendarChannelReferenceMaker());
				if(channelArray != null){
					for(int i = 0; i < channelArray.length; i++)
						calendarReferences.add(channelArray[i]);
				}
			}

			// add current site
			calendarReferences.add(M_ca.calendarReference(getSiteId(), SiteService.MAIN_CONTAINER));
		}
		return calendarReferences;
	}

	/*
	 * Callback class so that we can form references in a generic way. Method
	 * copied from Calendar legacy module: CalendarAction.java
	 */
	private final class CalendarChannelReferenceMaker implements MergedList.ChannelReferenceMaker {
		public String makeReference(String siteId) {
			return M_ca.calendarReference(siteId, SiteService.MAIN_CONTAINER);
		}
	}

	private String getSiteId() {
		if(siteId == null){
			Placement placement = M_tm.getCurrentPlacement();
			siteId = placement.getContext();
		}
		return siteId;
	}

	private List getDayEvents(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long startOfDay = c.getTimeInMillis();
		Time iTime = M_ts.newTime(startOfDay);
		long one_day = 86400000 - 1;
		Time fTime = M_ts.newTime(startOfDay + one_day);

		TimeRange range = M_ts.newTimeRange(iTime, fTime);
		ListIterator i = M_ca.getEvents(getCalendarReferences(), range).listIterator();

		List eventList = new ArrayList();
		while (i.hasNext()){
			CalendarEvent e = (CalendarEvent) i.next();
			EventSummary es = new EventSummary();
			es.setDisplayName(e.getDisplayName());
			es.setType(e.getType());
			es.setCalendarRef(e.getCalendarReference());
			es.setEventRef(e.getId());
			eventList.add(es);
		}
		return eventList;
	}

	private int getDayEventCount(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long startOfDay = c.getTimeInMillis();
		Time iTime = M_ts.newTime(startOfDay);
		long one_day = 86400000 - 1;
		Time fTime = M_ts.newTime(startOfDay + one_day);

		TimeRange range = M_ts.newTimeRange(iTime, fTime);
		return M_ca.getEvents(getCalendarReferences(), range).size();
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public void prevMonth(ActionEvent e) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(selectedMonth);
		cal.add(Calendar.MONTH, -1);
		setSelectedMonth(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
		updateEventList = true;
	}

	public void nextMonth(ActionEvent e) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(selectedMonth);
		cal.add(Calendar.MONTH, +1);
		setSelectedMonth(cal.getTime());
		selectedDay = null;
		selectedEventRef = null;
		updateEventList = true;
	}

	public void currMonth(ActionEvent e) {
		Calendar cal = Calendar.getInstance();
		setSelectedMonth(cal.getTime());
		// show events for today if any
		selectedDay = getToday();
		selectedEventRef = null;
		updateEventList = true;
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
			updateEventList = false;
		}catch(Exception ex){
			LOG.error("Error in backToEventList:" + ex.toString());
		}
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
		updateEventList = true;
	}

	public List getWeeks() {
		if(weeks == null || weeks.size() == 0) initializeWeeksDataStructure();
		if(updateEventList){
			// selected month
			Calendar c = Calendar.getInstance();
			c.setTime(getSelectedMonth());
			int selYear = c.get(Calendar.YEAR);
			int selMonth = c.get(Calendar.MONTH);
			c.set(Calendar.MONTH, selMonth);
			c.set(Calendar.DAY_OF_MONTH, 1);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
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
					if((weekNo == 0) && (i < dayOfWeek - 1)){
						int nDay = prevMonthLastDay - dayOfWeek + 2 + i;
						c.set(Calendar.MONTH, selMonth - 1);
						c.set(Calendar.DAY_OF_MONTH, nDay);
						day = new Day(c.getTime(), getDayEventCount(c) > 0);
						day.setOccursInOtherMonth(true);
					}else if(currDay > lastDay){
						c.set(Calendar.MONTH, selMonth + 1);
						c.set(Calendar.DAY_OF_MONTH, nextMonthDay++);
						day = new Day(c.getTime(), getDayEventCount(c) > 0);
						day.setOccursInOtherMonth(true);
					}else{
						c.set(Calendar.YEAR, selYear);
						c.set(Calendar.MONTH, selMonth);
						c.set(Calendar.DAY_OF_MONTH, currDay++);
						day = new Day(c.getTime(), getDayEventCount(c) > 0);
						day.setOccursInOtherMonth(false);
					}
					day.setToday(sameDay(c, getToday()));
					day.setSelected(selectedDay != null && sameDay(c, selectedDay));
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
		c.setTime(getSelectedMonth());
		String month = msgs.getString(months[c.get(Calendar.MONTH)]);
		String year = c.get(Calendar.YEAR) + "";
		return month + ", " + year;
	}

	public boolean isViewingSelectedDay() {
		return selectedDayHasEvents && selectedDay != null && selectedEventRef == null;
	}

	public boolean isViewingSelectedEvent() {
		return selectedEventRef != null;
	}

	public String getSelectedDayAsString() {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
		return formatter.format(selectedDay);
	}

	public List getSelectedDayEvents() {
		Calendar c = Calendar.getInstance();
		c.setTime(selectedDay);
		return getDayEvents(c);
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
				selectedEvent.setDescription(event.getDescription());
				selectedEvent.setLocation(event.getLocation());
				selectedEvent.setSite(M_ss.getSite(calendar.getContext()).getTitle());
				selectedEvent.setUrl(event.getUrl());
				// groups
				if(M_as.unlock("calendar.all.groups", "/site/"+calendar.getContext())){
					Collection grps = event.getGroupObjects();
					if(grps.size() > 0){
						StringBuffer sb = new StringBuffer();
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

	public Map getEventImageMap() {
		if(eventImageMap == null || eventImageMap.size() == 0){
			String px = "../../../library/image/sakai/";
			eventImageMap = new HashMap();
			eventImageMap.put("Academic Calendar", px + "academic_calendar.gif");
			eventImageMap.put("Activity", px + "activity.gif");
			eventImageMap.put("Cancellation", px + "cancelled.gif");
			eventImageMap.put("Class section - Discussion", px + "class_dis.gif");
			eventImageMap.put("Class section - Lab", px + "class_lab.gif");
			eventImageMap.put("Class section - Lecture", px + "class_lec.gif");
			eventImageMap.put("Class section - Small Group", px + "class_sma.gif");
			eventImageMap.put("Class session", px + "class_session.gif");
			eventImageMap.put("Computer Session", px + "computersession.gif");
			eventImageMap.put("Deadline", px + "deadline.gif");
			eventImageMap.put("Exam", px + "exam.gif");
			eventImageMap.put("Meeting", px + "meeting.gif");
			eventImageMap.put("Multidisciplinary Conference", px + "multi-conference.gif");
			eventImageMap.put("Quiz", px + "quiz.gif");
			eventImageMap.put("Special event", px + "special_event.gif");
			eventImageMap.put("Web Assignment", px + "webassignment.gif");
		}
		return eventImageMap;
	}

	public Date getSelectedMonth() {
		if(selectedMonth == null){
			Calendar c = Calendar.getInstance();
			selectedMonth = c.getTime();
		}
		return selectedMonth;
	}

	public void setSelectedMonth(Date selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

}
