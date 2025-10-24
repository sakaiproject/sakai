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

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.Permission;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupEventTypes;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.sakaiproject.signup.api.model.SignupAttachment;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.tool.jsf.attendee.AttendeeSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.AttendanceSignupBean;
import org.sakaiproject.signup.tool.jsf.organizer.OrganizerSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.user.api.User;

import static org.sakaiproject.signup.api.SignupConstants.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between main
 * events/meetings list view page:<b>orgSignupMeetings.jsp</b> and backbone
 * system. It provides all the necessary business logic
 * </P>
 */
@Slf4j
@NoArgsConstructor
public class SignupMeetingsBean implements SignupBeanConstants {

    @Setter protected SakaiFacade sakaiFacade;
    @Setter protected SignupMeetingService signupMeetingService;

    @Getter @Setter protected String categoryFilter = CATERGORY_FILER_ALL; // default setting
    @Setter @Getter protected String viewDateRang = ALL_FUTURE; // default setting
    @Setter @Getter protected UIData meetingTable;
    @Setter protected List<SignupMeetingWrapper> signupMeetings;
    @Setter @Getter protected AttendeeSignupMBean attendeeSignupMBean;
    @Setter @Getter protected OrganizerSignupMBean organizerSignupMBean;
	@Getter @Setter protected AttendanceSignupBean attendanceSignupBean;
    @Setter @Getter private NewSignupMeetingBean newSignupMeetingBean;
    @Setter @Getter protected SignupSorter signupSorter = new SignupSorter();
	protected Boolean showAllRecurMeetings = null;
    @Setter @Getter protected boolean enableExpandOption = false;
	protected List<SelectItem> viewDropDownList;
	protected final String disabledSelectView = "none";
	protected String meetingUnavailableMessages;
	protected Boolean categoriesExist = null;
	protected Boolean locationsExist = null;
    private long lastUpdatedLocTime = 0;
    private List<SelectItem> allLocations = null;
    private long lastUpdatedCatTime = 0;
    private List<SelectItem> allCategories = null;
    @Getter private CreateSitesGroups createSitesGroups = null;
    private boolean userLoggedInStatus = false;

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
		Set<SignupMeeting> meetingsSet = new HashSet<>();

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
			
			List<SignupMeeting> meetings = new ArrayList<>(meetingsSet);
			
			signupMeetingService.removeMeetings(meetings);
			/* record the logs of the removed meetings */
			for (SignupMeeting meeting : meetings) {
                log.info("Meeting Name: {} - UserId: {} - has removed the meeting at meeting startTime: {}",
                        meeting.getTitle(),
                        sakaiFacade.getCurrentUserId(),
                        sakaiFacade.getTimeService().newTime(meeting.getStartTime().getTime()).toStringLocalFull());
				Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_MTNG_REMOVE, sakaiFacade.getToolManager().getCurrentPlacement().getContext(),
						meeting.getId(), meeting.getTitle(), "at startTime:" + sakaiFacade.getTimeService().newTime(meeting.getStartTime().getTime())
						.toStringLocalFull());

			}

			try {
				signupMeetingService.removeCalendarEvents(meetings);
			} catch (Exception e) {
				log.error("{}, {}", Utilities.rb.getString("error.calendarEvent.removal_failed"), e.toString());
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.removal_failed"));
			}
			// cleanup attachments in contentHS
			for (SignupMeeting m : meetings) {
				List<SignupAttachment> attachs= m.getSignupAttachments();
				if(attachs !=null){
					for (SignupAttachment attach : attachs) {
						getNewSignupMeetingBean().getAttachmentHandler().removeAttachmentInContentHost(attach);
					}
				}
			}

		} catch (Exception e) {
			log.error("{}, {}", Utilities.rb.getString("Failed.remove.event"), e.toString());
			Utilities.addErrorMessage(Utilities.rb.getString("Failed.remove.event"));
		}
		signupMeetings = null;// TODO:do it more efficiently

		return MAIN_EVENTS_LIST_PAGE_URL;
	}

    public List<SelectItem> getAllLocations() {
        long currentTime = Instant.now().toEpochMilli();

        // Use optional to better handle null cases and avoid null checks
        if (allLocations == null || currentTime - lastUpdatedLocTime > 1000) {
            List<SelectItem> locations = new ArrayList<>();

            // Use Optional to handle potential null result
            Optional<List<String>> allLocsOptional = Optional.ofNullable(signupMeetingService.getAllLocations(sakaiFacade.getCurrentLocationId()));

            // Process locations if present using streams
            allLocsOptional.ifPresent(allLocs ->
                    locations.addAll(
                            allLocs.stream()
                                    .filter(StringUtils::isNotBlank)
                                    .map(SelectItem::new)
                                    .toList()
                    )
            );

            // Use boolean primitives instead of Boolean objects
            locationsExist = !locations.isEmpty();

            lastUpdatedLocTime = currentTime;
            allLocations = locations;
        }
    
        return allLocations;
    }

    public List<SelectItem> getAllCategories() {
        long currentTime = Instant.now().toEpochMilli();

        // Use optional to better handle null cases and avoid null checks
        if (allCategories == null || currentTime - lastUpdatedCatTime > 1000) {
            List<SelectItem> categories = new ArrayList<>();

            // Use Optional to handle potential null result 
            Optional<List<String>> allCatsOptional = Optional.ofNullable(
                    signupMeetingService.getAllCategories(sakaiFacade.getCurrentLocationId()));

            // Process categories if present using streams
            allCatsOptional.ifPresent(allCats ->
                    categories.addAll(
                            allCats.stream()
                                    .filter(StringUtils::isNotBlank)
                                    .map(SelectItem::new)
                                    .toList()
                    )
            );

            // Use boolean primitive instead of Boolean object
            this.categoriesExist = !categories.isEmpty();

            lastUpdatedCatTime = currentTime;
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
		SignupMeetingWrapper meetingWrapper;
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
		setShowAllRecurMeetings(expandAllEvents);
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
            log.error("{}  - {}", Utilities.rb.getString("failed.fetch_allEvents_from_db"), e.toString());
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
            log.error("{}: {}", Utilities.rb.getString("failed.fetch_allEvents_from_db"), e.toString());
			Utilities.addErrorMessage(Utilities.rb.getString("failed.fetch_allEvents_from_db"));
		}
		return signupMeetings;
	}

    /**
     * Loads signup meetings based on the specified view range and category filter and updates the bean state.
     * The meetings are filtered according to user permissions and view preferences.
     *
     * @param viewRange The time range filter to apply when retrieving meetings ("all", "future", etc)
     * @param categoryFilter Optional category name to filter meetings by. If null or empty, no category filtering is applied
	 */
	private void loadMeetings(String viewRange, String categoryFilter) {
				
		List<SignupMeetingWrapper> mWrappers = getMeetingWrappers(viewRange, categoryFilter);
		if (!isShowAllRecurMeetings()) {
			markingRecurMeetings(mWrappers);
		}
		setSignupMeetings(mWrappers);
	}

	private void markingRecurMeetings(List<SignupMeetingWrapper> smList) {
		if (smList == null || smList.isEmpty()) return;

		/*
		 * Assume that the list is already sorted by Date (default Date sorting
		 * by sql-query)
		 */
		for (int i = 0; i < smList.size(); i++) {
			SignupMeetingWrapper smWrapper = smList.get(i);
			Long firstRecurId = smWrapper.getMeeting().getRecurrenceId();
			if (firstRecurId != null && firstRecurId >= 0 && !smWrapper.isSubRecurringMeeting()) {
				int index = 0;
				for (int j = i + 1; j < smList.size(); j++) {
					SignupMeetingWrapper nextOne = smList.get(j);
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
		
		List<SignupMeeting> signupMeetings;
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

		} else {
			// calendar.add(Calendar.HOUR, 1 * 24);//exluding today for search
			signupMeetings = signupMeetingService.getSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId, calendar.getTime());
		}

		if (signupMeetings == null || signupMeetings.isEmpty()) {
			return null;
		}
		
		//SIGNUP-173 filter list by categoryFilter 
		//if no category, add them all
		List<SignupMeeting> filteredCategorySignupMeetings = new ArrayList<>();
		if(StringUtils.isNotBlank(categoryFilter) && !StringUtils.equals(CATERGORY_FILER_ALL,categoryFilter)) {
			for(SignupMeeting s: signupMeetings) {
				if(StringUtils.equals(s.getCategory(), categoryFilter)) {
					filteredCategorySignupMeetings.add(s);
				}
			}
		} else {
			filteredCategorySignupMeetings.addAll(signupMeetings);
		}

		List<SignupMeetingWrapper> wrappers = new ArrayList<>();
		for (SignupMeeting meeting : filteredCategorySignupMeetings) {
			SignupMeetingWrapper wrapper = new SignupMeetingWrapper(meeting, sakaiFacade.getUserDisplayName(meeting
					.getCreatorUserId()), sakaiFacade.getCurrentUserId(), sakaiFacade);
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
		if (sakaiFacade.isAllowedSite(sakaiFacade.getCurrentUserId(), SIGNUP_DELETE_SITE, sakaiFacade.getCurrentLocationId())) {
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
		if (sakaiFacade.isAllowedSite(sakaiFacade.getCurrentUserId(), SIGNUP_UPDATE_SITE, sakaiFacade.getCurrentLocationId())) {
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
		if (this.categoriesExist == null) getAllCategories();
		return this.categoriesExist;
	}
	
	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if we have categories already
	 *       
	 */
	public boolean isLocationsAvailable() {
		if (this.locationsExist == null) getAllLocations();
		return this.locationsExist;
	}

	/**
	 * This provides information of user's View selection for UI
	 * 
	 * @return true if user has select 'all future meetings' or 'all' for view
	 */
	public boolean isSelectedViewFutureMeetings() {
        return getViewDateRang().equals(ALL_FUTURE);
	}

	/**
	 * This provides information of user's View selection for UI
	 * 
	 * @return true if user has select 'all meetings' or 'all' for view
	 */
	public boolean isSelectedViewAllMeetings() {
        return getViewDateRang().equals(VIEW_ALL);
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if atleast one of the meeting has create permission for the
	 *         current user.
	 */
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

    /* The following methods provide data auto-refresh for current page */
	private long lastUpdatedTime = new Date().getTime();

	private boolean isRefresh() {
        return Instant.now().toEpochMilli() - lastUpdatedTime > dataRefreshInterval;
    }

	private void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

    /**
	 * This is a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isShowAllRecurMeetings() {
		if (showAllRecurMeetings == null) {
            showAllRecurMeetings = sakaiFacade.getServerConfigurationService().getBoolean("signup.showAllRecurMeetings.default", false);
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

	/**
	 * This is a getter method which provide current Iframe id for refresh
	 * IFrame purpose.
	 * 
	 * @return a String
	 */
	public String getIframeId() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return (String) request.getAttribute("sakai.tool.placement.id");
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
		List<SelectItem> viewDrpDwnList = new ArrayList<>();

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
        return getViewDateRang().equals(VIEW_IMMEDIATE_AVAIL);
	}

	public boolean isSelectedViewMySignedUp() {
        return getViewDateRang().equals(VIEW_MY_SIGNED_UP);
	}

	private void setMeetingUnavailableMessages(String meetingUnavailableMessages) {
		this.meetingUnavailableMessages = meetingUnavailableMessages;
	}

    /**
	 * For UI, it will switch the time-column accordingly
	 * 
	 * @return a boolean value
	 */
	public boolean isShowMyAppointmentTime() {
        return VIEW_MY_SIGNED_UP.equals(this.viewDateRang);
    }

    public boolean isUserLoggedInStatus() {
        if (!userLoggedInStatus) {
            userLoggedInStatus = sakaiFacade.getCurrentUserId() != null;
            return userLoggedInStatus;
        }
        return true;
    }

    /**
	 * @return true if sakai property signup.enableAttendance is true, else will return false
	 */
	public boolean isAttendanceOn() {
        return sakaiFacade.getServerConfigurationService().getBoolean("signup.enableAttendance", true);
	}

    /**
     * Gets a formatted list of instructors who have site-level signup creation permission.
     * The list is sorted alphabetically and formatted as SelectItems for use in UI dropdowns.
     *
     * @param meeting The SignupMeeting object used to determine the current instructor. 
     *               If null, the current user will be used as the default instructor.
     * @return A List of SelectItems where:
     *         - The first item is either the meeting's creator or current user
     *         - Remaining items are all other users with signup creation permission
     *         - Each item contains the user's ID as value and "displayName (eid)" as label
     *         - The list is sorted alphabetically (excluding first item)
	 */
    public List<SelectItem> getInstructors(SignupMeeting meeting) {
        List<User> users = sakaiFacade.getUsersWithPermission(SIGNUP_CREATE_SITE);
        List<SelectItem> instructors = new ArrayList<>();

        // Get appropriate instructor based on meeting or current user
        User primaryInstructor = Optional.ofNullable(meeting)
                .filter(m -> StringUtils.isNotBlank(m.getCreatorUserId()))
                .map(m -> sakaiFacade.getUser(m.getCreatorUserId()))
                .orElseGet(() -> sakaiFacade.getUser(sakaiFacade.getCurrentUserId()));

        // Add primary instructor at the top
        instructors.add(new SelectItem(primaryInstructor.getId(), getFormattedName(primaryInstructor)));
        users.remove(primaryInstructor);

        // Add remaining instructors
        instructors.addAll(users.stream()
                .map(u -> new SelectItem(u.getId(), getFormattedName(u)))
                .sorted(SignupSorter.sortSelectItemComparator)
                .toList());

        return instructors;
    }

    /**
     * Gets a formatted display name for a given user ID (instructor). 
     * The name is formatted as "displayName (eid)".
     *
     * @param user The user whose display name is to be formatted.
     * @return Formatted string containing user's display name and EID in parentheses,
     *         or null if the user cannot be found
   */
	public String getFormattedName(User user) {
        return user == null ? null : String.format("%s (%s)", user.getDisplayName(), user.getEid());
    }
	
	/**
	 * Is CSV export enabled?
	 * @return true or false, depending on signup.csv.export.enabled setting.
	 */
	public boolean isCsvExportEnabled() {
		return sakaiFacade.isCsvExportEnabled();
	}

    /**
     * Checks if the current user has site-level update permissions for doing certain operations.
     *
     * @return true if the current user has either site-level signup update permission (signup.site.update) 
     *         or site-level signup create permission (signup.site.create), false otherwise
	 */
	public boolean isCurrentUserAllowedUpdateSite() {
		String currentUserId = sakaiFacade.getCurrentUserId();
		String currentSiteId = sakaiFacade.getCurrentLocationId();
        return sakaiFacade.isAllowedSite(currentUserId, SIGNUP_UPDATE_SITE, currentSiteId)
                || sakaiFacade.isAllowedSite(currentUserId, SIGNUP_CREATE_SITE, currentSiteId);
	}
	
	/**
 	 * UI method to get list of categories for the filter
 	 * First item has null value to signal that it is all categories
 	 * 
 	 * @return list of categories
 	 */
 	public List<SelectItem> getAllCategoriesForFilter(){
        return getAllCategories();
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
