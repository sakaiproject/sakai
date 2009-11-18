/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.signup.logic.Permission;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.tool.jsf.attendee.AttendeeSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.OrganizerSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between main
 * events/meetings list view page:<b>orgSignupMeetings.jsp</b> and backbone
 * system. It provides all the necessary business logic
 * </P>
 */
public class SignupMeetingsBean implements SignupBeanConstants {

	private static Log log = LogFactory.getLog(SignupMeetingsBean.class);

	protected UIData meetingTable;

	protected String viewDateRang = ALL_FUTURE;// default setting

	protected SignupMeetingService signupMeetingService;

	protected List<SignupMeetingWrapper> signupMeetings;

	protected SakaiFacade sakaiFacade;

	protected AttendeeSignupMBean attendeeSignupMBean;

	protected OrganizerSignupMBean organizerSignupMBean;

	private NewSignupMeetingBean newSignupMeetingBean;

	protected SignupSorter signupSorter = new SignupSorter();

	protected boolean showAllRecurMeetings = false;// default

	protected boolean enableExpandOption = false;

	protected List<SelectItem> viewDropDownList;

	protected final String disabledSelectView = "none";

	protected String meetingUnavailableMessages;

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
		List<SignupMeeting> meetings = new ArrayList<SignupMeeting>();

		try {
			for (SignupMeetingWrapper mWrapper : getSignupMeetings()) {
				if (mWrapper.isSelected()) {
					meetings.add(mWrapper.getMeeting());
				}
			}
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
	 * This is a JSF action call method by UI to navigate to view the specific
	 * event/meeting page.
	 * 
	 * @return an action outcome string.
	 */
	public String processSignup() {
		// TODO ??? need to check if we have covered the case for people, who
		// have only view permission; we need
		// to disable everything in that page
		SignupMeetingWrapper meetingWrapper = (SignupMeetingWrapper) meetingTable.getRowData();
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
	 * This is a getter method for UI.
	 * 
	 * @return a list of SignupMeetingWrapper objects.
	 */
	public List<SignupMeetingWrapper> getSignupMeetings() {
		try {
			if (signupMeetings == null || isRefresh()) {
				loadMeetings(getViewDateRang());
				setLastUpdatedTime(new Date().getTime());
			}			

		} catch (Exception e) {
			log.error(Utilities.rb.getString("failed.fetch_allEvents_from_db") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("failed.fetch_allEvents_from_db"));
		}
		return signupMeetings;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of SignupMeetingWrapper objects.
	 */
	// TODO how to handle this more efficiently
	public List<SignupMeetingWrapper> getAllSignupMeetings() {
		try {
			loadMeetings(VIEW_ALL);
		} catch (Exception e) {
			log.error(Utilities.rb.getString("failed.fetch_allEvents_from_db") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("failed.fetch_allEvents_from_db"));
		}
		return signupMeetings;
	}

	private void loadMeetings(String viewRange) {
		List<SignupMeetingWrapper> mWrappers = getMeetingWrapper(viewRange);
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

	private List<SignupMeetingWrapper> getMeetingWrapper(String viewRange) {
		String currentUserId = sakaiFacade.getCurrentUserId();
		if(!isUserLoggedInStatus()){
			/*Let user log-in first*/
			return null;
		}
		
		List<SignupMeeting> signupMeetings = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		if (VIEW_ALL.equals(viewRange)) {
			signupMeetings = signupMeetingService.getAllSignupMeetings(sakaiFacade.getCurrentLocationId(),
					currentUserId);
		} else if (!OLD_DAYS.equals(viewRange)) {
			/* including today's day for search */
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinutes = calendar.get(Calendar.MINUTE);
			calendar.add(Calendar.HOUR, -1 * currentHour);
			calendar.add(Calendar.MINUTE, -1 * currentMinutes);
			String searchDateStr = viewRange;

			if (VIEW_MY_SIGNED_UP.equals(viewRange) || VIEW_IMMEDIATE_AVAIL.equals(viewRange))
				searchDateStr = ALL_FUTURE;

			signupMeetings = signupMeetingService.getSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId,
					calendar.getTime(), Utilities.getUserDefinedDate(Integer.parseInt(searchDateStr)));

		} else if (OLD_DAYS.equals(viewRange)) {
			// calendar.add(Calendar.HOUR, 1 * 24);//exluding today for search
			signupMeetings = signupMeetingService.getSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId,
					calendar.getTime());
		}

		if (signupMeetings == null || signupMeetings.isEmpty())
			return null;

		List<SignupMeetingWrapper> wrapppers = new ArrayList<SignupMeetingWrapper>();
		for (SignupMeeting meeting : signupMeetings) {
			SignupMeetingWrapper wrapper = new SignupMeetingWrapper(meeting, sakaiFacade.getUserDisplayName(meeting
					.getCreatorUserId()), sakaiFacade.getCurrentUserId(), getSakaiFacade());
			wrapppers.add(wrapper);
		}

		/* filter out not-relevant ones */
		signupFilter filter = new signupFilter(currentUserId, viewRange);
		filter.filterSignupMeetings(wrapppers);

		/* show user the option check-box to expand all */
		setEnableExpandOption(false);
		for (SignupMeetingWrapper meetingWrp : wrapppers) {
			if (meetingWrp.getMeeting().isRecurredMeeting())
				setEnableExpandOption(true);
		}

		return wrapppers;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if atleast one of the meeting has delete permission for the
	 *         current user.
	 */
	public boolean isAllowedToDelete() {
		if (getSignupMeetings() == null)
			return false;
		for (SignupMeetingWrapper meetingW : signupMeetings) {
			if (meetingW.getMeeting().getPermission().isDelete())
				return true;
		}

		return false;

	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if atleast one of the meeting has update permission for the
	 *         current user.
	 */
	public boolean isAllowedToUpdate() {
		if (getSignupMeetings() == null)
			return false;
		for (SignupMeetingWrapper meetingW : signupMeetings) {
			if (meetingW.getMeeting().getPermission().isUpdate())
				return true;
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

}
