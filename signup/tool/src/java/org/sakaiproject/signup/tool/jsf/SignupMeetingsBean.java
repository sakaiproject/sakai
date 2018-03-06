/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.signup.logic.Permission;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.tool.jsf.attendee.AttendeeSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.AttendanceSignupBean;
import org.sakaiproject.signup.tool.jsf.organizer.OrganizerSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between main
 * events/meetings list view page:<b>orgSignupMeetings.jsp</b> and backbone
 * system. It provides all the necessary business logic
 * </P>
 */
@Slf4j
public class SignupMeetingsBean implements SignupBeanConstants {

	protected UIData meetingTable;

	protected String viewDateRang = ALL_FUTURE;// default setting

	protected SignupMeetingService signupMeetingService;

	protected List<SignupMeetingWrapper> signupMeetings;

	protected SakaiFacade sakaiFacade;

	protected AttendeeSignupMBean attendeeSignupMBean;

	protected OrganizerSignupMBean organizerSignupMBean;

	protected AttendanceSignupBean attendanceSignupBean;

	private NewSignupMeetingBean newSignupMeetingBean;

	protected SignupSorter signupSorter = new SignupSorter();

	protected Boolean showAllRecurMeetings = null;

	protected boolean enableExpandOption = false;

	protected List<SelectItem> viewDropDownList;

	protected final String disabledSelectView = "none";

	protected String meetingUnavailableMessages;
	
	protected Boolean categoriesExist = null;
	
	protected Boolean locationsExist = null;
	
	
	@Getter @Setter
	protected String categoryFilter = CATERGORY_FILER_ALL; // default setting is blank, which means all categories


	/**
	 * Default Constructor
	 * 
	 */
	public SignupMeetingsBean() {
	}

	/**
	 * This is a getter method.
	 * 
	 * @return the current user display name.
	 */
	public String getCurrentUserDisplayName() {
		return sakaiFacade.getUserDisplayName(sakaiFacade.getCurrentUserId());
	}

	/**
	 * This is a JSF action call method by UI to navigate to add new
	 * event/meeting page.
	 * 
	 * @return an action outcome string.
	 */
	public String addMeeting() {
		getNewSignupMeetingBean().reset();
		return ADD_MEETING_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to remove the selected
	 * events/meetings.
	 * 
	 * @return an action outcome string.
	 */
	public String removeMeetings() {
		Set<SignupMeeting> meetingsSet = new HashSet<SignupMeeting>();

		try {
			for (SignupMeetingWrapper mWrapper : getSignupMeetings()) {
				if (mWrapper.isSelected()) {
					
					//SIGNUP-139 
					//if this meeting wrapper is the first one in a set of recurring meetings
					//and we have recurring meetings
					//then get all meetings have the same meeting.recurrenceId (as they are part of the set) and add to the list for removal
					if(mWrapper.isFirstOneRecurMeeting() && mWrapper.getRecurEventsSize() > 1) {
						SignupMeeting topLevel = mWrapper.getMeeting();
					
						List<SignupMeeting> recurrentMeetings = signupMeetingService.getRecurringSignupMeetings(sakaiFacade.getCurrentLocationId(), sakaiFacade.getCurrentUserId(), topLevel.getRecurrenceId(), topLevel.getStartTime());
						
						meetingsSet.addAll(recurrentMeetings);
					} 
					meetingsSet.add(mWrapper.getMeeting());
				}
			}
			
			List<SignupMeeting> meetings = new ArrayList<SignupMeeting>(meetingsSet);
			
			signupMeetingService.removeMeetings(meetings);
			/* record the logs of the removed meetings */
			for (SignupMeeting meeting : meetings) {
				log.info("Meeting Name:"
						+ meeting.getTitle()
						+ " - UserId:"
						+ sakaiFacade.getCurrentUserId()
						+ " - has removed the meeting at meeting startTime:"
						+ getSakaiFacade().getTimeService().newTime(meeting.getStartTime().getTime())
								.toStringLocalFull());

				Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_MTNG_REMOVE, ToolManager.getCurrentPlacement().getContext() + " meetingId|title:"
						+ meeting.getId() + "|" + meeting.getTitle() + " at startTime:" + getSakaiFacade().getTimeService().newTime(meeting.getStartTime().getTime())
						.toStringLocalFull());

			}

			try {
				signupMeetingService.removeCalendarEvents(meetings);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.removal_failed"));
				log.error(Utilities.rb.getString("error.calendarEvent.removal_failed") + " - " + e.getMessage());
			}
			/*cleanup attachments in contentHS*/
			for (SignupMeeting m : meetings) {
				List<SignupAttachment> attachs= m.getSignupAttachments();
				if(attachs !=null){
					for (SignupAttachment attach : attachs) {
						getNewSignupMeetingBean().getAttachmentHandler().removeAttachmentInContentHost(attach);
					}
				}
			}

		} catch (Exception e) {
			log.error(Utilities.rb.getString("Failed.remove.event") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("Failed.remove.event"));
		}
		signupMeetings = null;// TODO:do it more efficiently

		return MAIN_EVENTS_LIST_PAGE_URL;
	}

	/**
	 * Get a list of locations for the UI. Unfiltered. 
	 * @return
	 */
	private long lastUpdatedLocTime = 0;
	private List<SelectItem> allLocations = null;
	public List<SelectItem> getAllLocations(){
		long curr_time=(new Date()).getTime();
		//avoid multiple calls for one page loading : not refresh within one second
		if(allLocations == null || curr_time - lastUpdatedLocTime > 1000){
			//Set<String> set = new HashSet<String>();
			List<SelectItem> locations= new ArrayList<SelectItem>();
			//List<SignupMeetingWrapper> allMeetings = getMeetingWrappers(VIEW_ALL, null);
			List<String> allLocs = null;
			try {
				allLocs = signupMeetingService.getAllLocations(sakaiFacade.getCurrentLocationId());
			} catch (Exception e) {
				//do nothing
				allLocs=null;
			}
			if(allLocs !=null){
				for(String lc : allLocs) {
					if(StringUtils.isNotBlank(lc)) {
						locations.add(new SelectItem(lc));
					}
				}
			}			
		
			if(!locations.isEmpty()){
				//avoid multiple call later
				this.locationsExist = new Boolean(true);
			}
			else{
				this.locationsExist = new Boolean(false);
			}
			
			lastUpdatedLocTime = curr_time;
			allLocations = locations;
		}
		
		return allLocations;
		
	}
	
	private long lastUpdatedCatTime = 0;
	private List<SelectItem> allCategories = null;
	public List<SelectItem> getAllCategories(){
		
		long curr_time=(new Date()).getTime();
		//avoid multiple calls for one page loading : not refresh within one second
		if(allCategories == null || curr_time - lastUpdatedCatTime > 1000){			
			List<SelectItem> categories = new ArrayList<SelectItem>();		
			List<String> allCats = null;
			try {
				allCats = signupMeetingService.getAllCategories(sakaiFacade.getCurrentLocationId());
			} catch (Exception e) {
				//do nothing
				allCats=null;
			}
			if(allCats !=null){
				for(String c : allCats) {
					if(StringUtils.isNotBlank(c)) {
						categories.add(new SelectItem(c));
					}
				}
			}
			
			if(!categories.isEmpty()){
				//avoid multiple call later
				this.categoriesExist = new Boolean(true);
			}
			else{
				this.categoriesExist = new Boolean(false);
			}
			
			lastUpdatedCatTime = curr_time;
			categories.add(0, new SelectItem(CATERGORY_FILER_ALL, Utilities.rb.getString("filter_categories_top")));
			allCategories = categories;
		}
		
		return allCategories;
	}

	/**
	 * This is a JSF action call method by UI to navigate to view the specific
	 * event/meeting page.
	 * 
	 * @return an action outcome string.
	 */
	public String processSignup() {
		// TODO ??? need to check if we have covered the case for people, who
		// have only view permission; we need
		// to disable everything in that page
		SignupMeetingWrapper meetingWrapper=null;
		try{
			meetingWrapper = (SignupMeetingWrapper) meetingTable.getRowData();
		}
		catch (Exception ex){
			/* sometimes, it throw 
			 * java.lang.IllegalArgumentException
			 * at javax.faces.model.ListDataModel.getRowData(ListDataModel.java:139)
			 * Retry for user
			 */
			//reset main page data
			setSignupMeetings(null);
			return MAIN_EVENTS_LIST_PAGE_URL;
		}
		
		Permission permission = meetingWrapper.getMeeting().getPermission();
		try {
			if (permission.isUpdate()) {
				organizerSignupMBean.init(meetingWrapper);
				return ORGANIZER_MEETING_PAGE_URL;
			}

			attendeeSignupMBean.init(meetingWrapper);

		} catch (Exception e) {
			return MAIN_EVENTS_LIST_PAGE_URL;
		}
		return ATTENDEE_MEETING_PAGE_URL;
	}
	
	/**
	 * This is a JSF action call method by UI to navigate to view the specific
	 * event/meeting attendance page.
	 * 
	 * @return an action outcome string.
	 */
	public String processSignupAttendance() {
		
		SignupMeetingWrapper meetingWrapper = (SignupMeetingWrapper) meetingTable.getRowData();
		Permission permission = meetingWrapper.getMeeting().getPermission();
		try {
			if (permission.isUpdate()) {
				attendanceSignupBean.init(meetingWrapper);
				return ATTENDANCE_PAGE_URL;
			}

		} catch (Exception e) {
			return ATTENDANCE_PAGE_URL;
		}
		return ATTENDANCE_PAGE_URL;
	}

	/**
	 * This is a ValueChange Listener to watch the view-range type selection by
	 * user.
	 * 
	 * @param vce
	 *            a ValuechangeEvent object.
	 * @return a outcome string.
	 */
	public String processSelectedRange(ValueChangeEvent vce) {
		String viewRange = (String) vce.getNewValue();
		if (disabledSelectView.equals(viewRange))
			return MAIN_EVENTS_LIST_PAGE_URL;// do-nothing for IE browser
		// case

		setViewDateRang(viewRange);
		setSignupMeetings(null);// reset

		return MAIN_EVENTS_LIST_PAGE_URL;

	}

	/**
	 * This is a ValueChange Listener to watch the show-all-recurring-events
	 * check-box value change by user.
	 * 
	 * @param vce
	 *            a ValuechangeEvent object.
	 * @return a outcome string.
	 */
	public String processExpandAllRcurEvents(ValueChangeEvent vce) {
		Boolean expandAllEvents = (Boolean) vce.getNewValue();
		setShowAllRecurMeetings(expandAllEvents.booleanValue());
		List<SignupMeetingWrapper> smWrappers = getSignupMeetings();
		if (smWrappers != null) {
			if (isShowAllRecurMeetings()) {
				for (SignupMeetingWrapper smWrp : smWrappers) {
					smWrp.setRecurEventsSize(0);
					smWrp.setSubRecurringMeeting(false);
				}
			} else {
				getSignupSorter().setSortAscending(true);
				getSignupSorter().setSortColumn(SignupSorter.DATE_COLUMN);
				getSignupSorter().sort(smWrappers);
				markingRecurMeetings(smWrappers);
				setSignupMeetings(smWrappers);
			}
		}

		return MAIN_EVENTS_LIST_PAGE_URL;

	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a SakaiFacade object.
	 */
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	/**
	 * This is a setter.
	 * 
	 * @param sakaiFacade
	 *            a SakaiFacade object.
	 */
	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a SignupMeetingService object.
	 */
	public SignupMeetingService getSignupMeetingService() {
		return signupMeetingService;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 */
	public void setSignupMeetingService(SignupMeetingService signupMeetingService) {
		this.signupMeetingService = signupMeetingService;
	}

	/**
	 * This is a getter method for UI and filters the list according to what has been set.
	 * 
	 * @return a list of SignupMeetingWrapper objects.
	 */
	public List<SignupMeetingWrapper> getSignupMeetings() {
		try {
			if (signupMeetings == null || isRefresh()) {
				loadMeetings(getViewDateRang(), getCategoryFilter());
				setLastUpdatedTime(new Date().getTime());
			}			

		} catch (Exception e) {
			log.error(Utilities.rb.getString("failed.fetch_allEvents_from_db") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("failed.fetch_allEvents_from_db"));
		}
		return signupMeetings;
	}

	/**
	 * This is a getter method for UI and returns all signup meetings. Ignores any filters.
	 * 
	 * @return a list of SignupMeetingWrapper objects.
	 */
	// TODO how to handle this more efficiently
	public List<SignupMeetingWrapper> getAllSignupMeetings() {
		try {
			loadMeetings(VIEW_ALL, null);
		} catch (Exception e) {
			log.error(Utilities.rb.getString("failed.fetch_allEvents_from_db") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("failed.fetch_allEvents_from_db"));
		}
		return signupMeetings;
	}

	/**
	 * Loads the signup meetings and updates the bean state
	 * @param viewRange
	 * @param categoryFilter
	 */
	private void loadMeetings(String viewRange, String categoryFilter) {
				
		List<SignupMeetingWrapper> mWrappers = getMeetingWrappers(viewRange, categoryFilter);
		if (!isShowAllRecurMeetings()) {
			markingRecurMeetings(mWrappers);
		}
		setSignupMeetings(mWrappers);
	}

	private void markingRecurMeetings(List<SignupMeetingWrapper> smList) {
		if (smList == null || smList.size() == 0)
			return;

		/*
		 * Assume that the list is already sorted by Date (default Date sorting
		 * by sql-query)
		 */
		for (int i = 0; i < smList.size(); i++) {
			SignupMeetingWrapper smWrapper = (SignupMeetingWrapper) smList.get(i);
			Long firstRecurId = smWrapper.getMeeting().getRecurrenceId();
			if (firstRecurId != null && firstRecurId.longValue() >= 0 && !smWrapper.isSubRecurringMeeting()) {
				int index = 0;
				for (int j = i + 1; j < smList.size(); j++) {
					SignupMeetingWrapper nextOne = (SignupMeetingWrapper) smList.get(j);
					if (nextOne.getMeeting().getRecurrenceId() != null
							&& nextOne.getMeeting().getRecurrenceId().longValue() == firstRecurId.longValue()) {
						nextOne.setSubRecurringMeeting(true);
						nextOne.setRecurId(firstRecurId + "_" + index++);

					}
				}
				smWrapper.setRecurEventsSize(index + 1);
			}
		}
	}

	private List<SignupMeetingWrapper> getMeetingWrappers(String viewRange, String categoryFilter) {
		String currentUserId = sakaiFacade.getCurrentUserId();
		if(!isUserLoggedInStatus()){
			/*Let user log-in first*/
			return null;
		}
		
		List<SignupMeeting> signupMeetings = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		if (VIEW_ALL.equals(viewRange)) {
			signupMeetings = signupMeetingService.getAllSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId);
		} else if (!OLD_DAYS.equals(viewRange)) {
			/* including today's day for search */
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinutes = calendar.get(Calendar.MINUTE);
			calendar.add(Calendar.HOUR, -1 * currentHour);
			calendar.add(Calendar.MINUTE, -1 * currentMinutes);
			String searchDateStr = viewRange;

			if (VIEW_MY_SIGNED_UP.equals(viewRange) || VIEW_IMMEDIATE_AVAIL.equals(viewRange)) {
				searchDateStr = ALL_FUTURE;
			}

			signupMeetings = signupMeetingService.getSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId, calendar.getTime(), Utilities.getUserDefinedDate(Integer.parseInt(searchDateStr)));

		} else if (OLD_DAYS.equals(viewRange)) {
			// calendar.add(Calendar.HOUR, 1 * 24);//exluding today for search
			signupMeetings = signupMeetingService.getSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId, calendar.getTime());
		}

		if (signupMeetings == null || signupMeetings.isEmpty()) {
			return null;
		}
		
		//SIGNUP-173 filter list by categoryFilter 
		//if no category, add them all
		List<SignupMeeting> filteredCategorySignupMeetings = new ArrayList<SignupMeeting>();
		if(StringUtils.isNotBlank(categoryFilter) && !StringUtils.equals(CATERGORY_FILER_ALL,categoryFilter)) {
			for(SignupMeeting s: signupMeetings) {
				if(StringUtils.equals(s.getCategory(), categoryFilter)) {
					filteredCategorySignupMeetings.add(s);
				}
			}
		} else {
			filteredCategorySignupMeetings.addAll(signupMeetings);
		}

		List<SignupMeetingWrapper> wrappers = new ArrayList<SignupMeetingWrapper>();
		for (SignupMeeting meeting : filteredCategorySignupMeetings) {
			SignupMeetingWrapper wrapper = new SignupMeetingWrapper(meeting, sakaiFacade.getUserDisplayName(meeting
					.getCreatorUserId()), sakaiFacade.getCurrentUserId(), getSakaiFacade());
			wrappers.add(wrapper);
		}

		/* filter out not-relevant ones */
		signupFilter filter = new signupFilter(currentUserId, viewRange);
		filter.filterSignupMeetings(wrappers);

		/* show user the option check-box to expand all */
		setEnableExpandOption(false);
		for (SignupMeetingWrapper meetingWrp : wrappers) {
			if (meetingWrp.getMeeting().isRecurredMeeting())
				setEnableExpandOption(true);
		}

		return wrappers;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if atleast one of the meeting has delete permission for the
	 *         current user.
	 */
	public boolean isAllowedToDelete() {
		if (sakaiFacade.isAllowedSite(sakaiFacade.getCurrentUserId(), SakaiFacade.SIGNUP_DELETE_SITE, sakaiFacade.getCurrentLocationId())) {
			return true;
		}

		if (getSignupMeetings() == null) {
			return false;
		}

		for (SignupMeetingWrapper meetingW : signupMeetings) {
			if (meetingW.getMeeting().getPermission().isDelete())
				return true;
		}

		return false;

	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if at least one of the meetings has update permission for the
	 *         current user.
	 */
	public boolean isAllowedToUpdate() {
		// Do we really need to loop through 1000 meetings if the user has elevated site permissions?!
		if (sakaiFacade.isAllowedSite(sakaiFacade.getCurrentUserId(), SakaiFacade.SIGNUP_UPDATE_SITE, sakaiFacade.getCurrentLocationId())) {
			return true;
		}

		// This call to getSignupMeetings() is going to make Hibernate load lots and lots of data
		if (getSignupMeetings() == null) {
			return false;
		}

		for (SignupMeetingWrapper meetingW : signupMeetings) {
			if (meetingW.getMeeting().getPermission().isUpdate()) {
				return true;
			}
		}

		return false;

	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if atleast one of the meeting is available to the current
	 *         user.
	 */
	public boolean isMeetingsAvailable() {
		getSignupMeetings();
		return !(signupMeetings == null || signupMeetings.isEmpty());
	}
	
	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if we have categories already
	 *       
	 */
	public boolean isCategoriesAvailable() {
		//getSignupMeetings();
		if(this.categoriesExist == null){	
			//initialization first
			getAllCategories();			
		}
		
		return this.categoriesExist.booleanValue();
	}
	
	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if we have categories already
	 *       
	 */
	public boolean isLocationsAvailable() {
		//getSignupMeetings();
		if(this.locationsExist == null){	
			//initialization first
			getAllLocations();			
		}
		
		return this.locationsExist.booleanValue();
	}

	/**
	 * This provides information of user's View selection for UI
	 * 
	 * @return true if user has select 'all future meetings' or 'all' for view
	 */
	public boolean isSelectedViewFutureMeetings() {
		boolean t = false;
		if (getViewDateRang().equals(ALL_FUTURE))
			t = true;
		return t;
	}

	/**
	 * This provides information of user's View selection for UI
	 * 
	 * @return true if user has select 'all meetings' or 'all' for view
	 */
	public boolean isSelectedViewAllMeetings() {
		boolean t = false;
		if (getViewDateRang().equals(VIEW_ALL))
			t = true;
		return t;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if atleast one of the meeting has create permission for the
	 *         current user.
	 */
	private CreateSitesGroups createSitesGroups = null;

	public boolean isAllowedToCreate() {
		boolean allowed = signupMeetingService.isAllowedToCreateAnyInSite(sakaiFacade.getCurrentUserId(), sakaiFacade
				.getCurrentLocationId());
		/*
		 * Temporary bug fix for AuthZ code ( isAllowed(..) ), which gives wrong
		 * permission for the first time at 'Create new or Copy meeting pages',
		 * once it's fixed, remove this below and make it into a more clean way
		 */
		if (allowed && createSitesGroups == null) {
			createSitesGroups = new CreateSitesGroups(null, sakaiFacade, signupMeetingService);
		}

		return allowed;
	}

	/*
	 * provide a way to let other bean to access this same object (temporary
	 * fixing Authz code problem)
	 */
	public CreateSitesGroups getCreateSitesGroups() {
		return createSitesGroups;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupMeetings
	 *            a list of SignupMeetingWrapper objects.
	 */
	public void setSignupMeetings(List<SignupMeetingWrapper> signupMeetings) {
		this.signupMeetings = signupMeetings;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an UIData object.
	 */
	public UIData getMeetingTable() {
		return meetingTable;
	}

	/**
	 * This is a setter.
	 * 
	 * @param meetingTable
	 *            an UIData object.
	 */
	public void setMeetingTable(UIData meetingTable) {
		this.meetingTable = meetingTable;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return an AttendeeSginupMBean object.
	 */
	public AttendeeSignupMBean getAttendeeSignupMBean() {
		return attendeeSignupMBean;
	}

	/**
	 * This is a setter.
	 * 
	 * @param attendeeSignupMBean
	 *            an AttendeeSignupMBean object.
	 */
	public void setAttendeeSignupMBean(AttendeeSignupMBean attendeeSignupMBean) {
		this.attendeeSignupMBean = attendeeSignupMBean;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return an OrganizerSignupMBean object
	 */
	public OrganizerSignupMBean getOrganizerSignupMBean() {
		return organizerSignupMBean;
	}

	/**
	 * This is a setter.
	 * 
	 * @param organizerSignupMBean
	 *            an OrganizerSignupMBean object
	 */
	public void setOrganizerSignupMBean(OrganizerSignupMBean organizerSignupMBean) {
		this.organizerSignupMBean = organizerSignupMBean;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getViewDateRang() {
		return viewDateRang;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param viewDateRang
	 *            a string value.
	 */
	public void setViewDateRang(String viewDateRang) {
		this.viewDateRang = viewDateRang;
	}

	/* The following methods provide data auto-refresh for current page */
	private long lastUpdatedTime = new Date().getTime();

	private boolean isRefresh() {
		if ((new Date()).getTime() - lastUpdatedTime > dataRefreshInterval)
			return true;

		return false;
	}

	private void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	/**
	 * This is a getter to obtain the SignupSorter object.
	 * 
	 * @return A SignupSorter object.
	 */
	public SignupSorter getSignupSorter() {
		return signupSorter;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupSorter
	 *            A SignupSorter object.
	 */
	public void setSignupSorter(SignupSorter signupSorter) {
		this.signupSorter = signupSorter;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isShowAllRecurMeetings() {
		if (showAllRecurMeetings == null) {
			if (getSakaiFacade().getServerConfigurationService().getBoolean("signup.showAllRecurMeetings.default", false)) {
				showAllRecurMeetings = true;
			}
			else {
				showAllRecurMeetings = false;
			}
		}
		return showAllRecurMeetings;
	}

	/**
	 * This is a setter method for UI.
	 * 
	 * @param showAllRecurMeetings
	 *            a boolean value
	 */
	public void setShowAllRecurMeetings(boolean showAllRecurMeetings) {
		this.showAllRecurMeetings = showAllRecurMeetings;
	}

	private String iframeId = "";

	/**
	 * This is a getter method which provide current Iframe id for refresh
	 * IFrame purpose.
	 * 
	 * @return a String
	 */
	public String getIframeId() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		String iFrameId = (String) request.getAttribute("sakai.tool.placement.id");
		return iFrameId;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isEnableExpandOption() {
		return enableExpandOption;
	}

	/**
	 * This is a setter method for UI.
	 * 
	 * @param enableExpandOption
	 *            a boolean value
	 */
	public void setEnableExpandOption(boolean enableExpandOption) {
		this.enableExpandOption = enableExpandOption;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a list of SelectItem which provide user's choices for view
	 */
	public List<SelectItem> getViewDropDownList() {
		if (viewDropDownList == null) {
			initViewDropDownList();
		}
		return viewDropDownList;
	}

	private void initViewDropDownList() {
		List<SelectItem> viewDrpDwnList = new ArrayList<SelectItem>();

		viewDrpDwnList.add(new SelectItem(THIRTY_DAYS, Utilities.rb.getString("view_current_month")));
		viewDrpDwnList.add(new SelectItem(NINTY_DAYS, Utilities.rb.getString("view_current_three_months")));
		viewDrpDwnList.add(new SelectItem(ALL_FUTURE, Utilities.rb.getString("view_all_future_meetings")));
		/*
		 * viewDrpDwnList.add(new SelectItem(OLD_DAYS,
		 * Utilities.rb.getString("view_previous_events")));
		 */
		viewDrpDwnList.add(new SelectItem(VIEW_ALL, Utilities.rb.getString("view_all")));
		viewDrpDwnList.add(new SelectItem(disabledSelectView, Utilities.rb.getString("dropDown_line_separator"),
				"line-separator", true));
		viewDrpDwnList.add(new SelectItem(VIEW_MY_SIGNED_UP, Utilities.rb.getString("view_my_signed_up")));
		viewDrpDwnList.add(new SelectItem(VIEW_IMMEDIATE_AVAIL, Utilities.rb.getString("view_immediate_avail_ones")));
		setViewDropDownList(viewDrpDwnList);
	}

	private void setViewDropDownList(List<SelectItem> viewDropDownList) {
		this.viewDropDownList = viewDropDownList;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a String message.
	 */
	public String getMeetingUnavailableMessages() {
		this.meetingUnavailableMessages = "";
		if (isMeetingsAvailable())
			return this.meetingUnavailableMessages;

		/* no meeting available cases: */
		if (isAllowedToCreate() && isSelectedViewFutureMeetings())
			setMeetingUnavailableMessages(Utilities.rb.getString("no_events_in_future_organizer"));
		else if (isAllowedToCreate() && isSelectedViewAllMeetings())
			setMeetingUnavailableMessages(Utilities.rb.getString("no_events_in_timeframe_organizer"));
		else if (!isAllowedToCreate() && isSelectedViewAllMeetings())
			setMeetingUnavailableMessages(Utilities.rb.getString("no_events_in_timeframe_attendee"));
		else if (isSelectedViewImmediateAvail())
			setMeetingUnavailableMessages(Utilities.rb.getString("no_future_events_in_immediate_available"));
		else if (isSelectedViewMySignedUp())
			setMeetingUnavailableMessages(Utilities.rb.getString("no_future_events_I_have_signed_up"));
		else if (!isAllowedToCreate() && isSelectedViewFutureMeetings() || !isSelectedViewAllMeetings()
				&& !isSelectedViewFutureMeetings())
			setMeetingUnavailableMessages(Utilities.rb.getString("no_events_in_this_period_attendee_orgnizer"));

		return this.meetingUnavailableMessages;
	}

	public boolean isSelectedViewImmediateAvail() {
		boolean t = false;
		if (getViewDateRang().equals(VIEW_IMMEDIATE_AVAIL))
			t = true;
		return t;
	}

	public boolean isSelectedViewMySignedUp() {
		boolean t = false;
		if (getViewDateRang().equals(VIEW_MY_SIGNED_UP))
			t = true;
		return t;
	}

	private void setMeetingUnavailableMessages(String meetingUnavailableMessages) {
		this.meetingUnavailableMessages = meetingUnavailableMessages;
	}

	/**
	 * It's getter method for JSF bean
	 * 
	 * @return a NewSignupMeetingBean object
	 */
	public NewSignupMeetingBean getNewSignupMeetingBean() {
		return newSignupMeetingBean;
	}

	/**
	 * It's a setter method for JSF bean.
	 * 
	 * @param newSignupMeetingBean
	 *            a NewSignupMeetingBean object
	 */
	public void setNewSignupMeetingBean(NewSignupMeetingBean newSignupMeetingBean) {
		this.newSignupMeetingBean = newSignupMeetingBean;
	}
	
	public AttendanceSignupBean getAttendanceSignupBean() {
		return attendanceSignupBean;
	}

	public void setAttendanceSignupBean(AttendanceSignupBean attendanceSignupBean) {
		this.attendanceSignupBean = attendanceSignupBean;
	}

	/**
	 * For UI, it will switch the time-column accordingly
	 * 
	 * @return a boolean value
	 */
	public boolean isShowMyAppointmentTime() {
		if (VIEW_MY_SIGNED_UP.equals(this.viewDateRang))
			return true;

		return false;
	}
	
	
	private boolean userLoggedInStatus = false;
	public boolean isUserLoggedInStatus(){
		if(!userLoggedInStatus){
			if(getSakaiFacade().getCurrentUserId()!=null)
				this.userLoggedInStatus=true;
			else{
				return false;
			}
		}
		return userLoggedInStatus;
	}

	
	/**
	 * @return true if sakai property signup.enableAttendance is true, else will return false
	 */
	public boolean isAttendanceOn() {
			
		if (getSakaiFacade().getServerConfigurationService().getBoolean("signup.enableAttendance", true)) {
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Get a list of instructors, defined as those with a given permission. Format it as a SelectItem list with the
	 * current instructor, if any, at the top
	 * @return
	 */
	public List<SelectItem> getInstructors(SignupMeeting meeting) {
		List<User> users = sakaiFacade.getUsersWithPermission(SakaiFacade.SIGNUP_CREATE_SITE);
		
		List<SelectItem> instructors= new ArrayList<SelectItem>();
		
		//do we have a meeting set?
		//if so get the user and set to top of the list, then remove from the rest of the instructors
		//otherwise, put the current user at the top of the list
		if(meeting != null && StringUtils.isNotBlank(meeting.getCreatorUserId())) {
			User currentInstructor = sakaiFacade.getUser(meeting.getCreatorUserId());
			instructors.add(new SelectItem(currentInstructor.getId(), currentInstructor.getDisplayName() + " (" + currentInstructor.getEid() + ")"));
			users.remove(currentInstructor);
		} else {
			User currentUser = sakaiFacade.getUser(sakaiFacade.getCurrentUserId());
			instructors.add(new SelectItem(currentUser.getId(), currentUser.getDisplayName() + " (" + currentUser.getEid() + ")"));
			users.remove(currentUser);
		}

		//format remaining list of instructors
		for(User u : users) {
			instructors.add(new SelectItem (u.getId(), u.getDisplayName() + " (" + u.getDisplayId() + ")"));
		}
		
		Collections.sort(instructors, SignupSorter.sortSelectItemComparator);
		
		return instructors;
	}
	
	/**
	 * Get the name of the user (instructor) for the given userId. This really just formats a name
	 * @param userId
	 * @return
	 */
	public String getInstructorName(String userId) {
		
		User u = sakaiFacade.getUser(userId);
		if(u == null) {
			return null;
		}
		
		return u.getDisplayName() + " (" + u.getEid() + ")";
	}
	
	/**
	 * Is CSV export enabled?
	 * @return true or false, depending on signup.csv.export.enabled setting.
	 */
	public boolean isCsvExportEnabled() {
		return sakaiFacade.isCsvExportEnabled();
	}
	
	/**
	 * Is the current user allowed to update the site? Used for some permission checks
	 * @return
	 */
	public boolean isCurrentUserAllowedUpdateSite() {
		String currentUserId = sakaiFacade.getCurrentUserId();
		String currentSiteId = sakaiFacade.getCurrentLocationId();
		boolean isAllowedUpdateSite = (sakaiFacade.isAllowedSite(currentUserId, sakaiFacade.SIGNUP_UPDATE_SITE, currentSiteId) 
				|| sakaiFacade.isAllowedSite(currentUserId, sakaiFacade.SIGNUP_CREATE_SITE, currentSiteId));

		return isAllowedUpdateSite;
	}
	
	/**
 	 * UI method to get list of categories for the filter
 	 * First item has null value to signal that it is all categories
 	 * 
 	 * @return list of categories
 	 */
 	public List<SelectItem> getAllCategoriesForFilter(){
 		List<SelectItem> categories = getAllCategories();
 		return categories;
 	}
 	
 	/**
	 * This is a ValueChange Listener to watch the category filter selection by user.
	 * 
	 * @param vce a ValuechangeEvent object.
	 * @return a outcome string.
	 */
	public String processSelectedCategory(ValueChangeEvent vce) {
		String selectedCategory = (String) vce.getNewValue();
		//note that blank values are allowed
		if(!categoryFilter.equals(selectedCategory)){
			setCategoryFilter(selectedCategory);
			setSignupMeetings(null);// reset
		}
		
		return "";
	}
	
	
}
