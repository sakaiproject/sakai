/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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
package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.signup.logic.Permission;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.tool.jsf.attendee.AttendeeSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.OrganizerSignupMBean;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between main
 * events/meetings list view page:<b>orgSignupMeetings.jsp</b> and backbone
 * system. It provides all the necessary business logic
 * </P>
 */
public class SignupMeetingsBean implements SignupBeanConstants {

	private static Log log = LogFactory.getLog(SignupMeetingsBean.class);

	private UIData meetingTable;

	private String viewDateRang = ALL_FUTURE;// default setting

	private SignupMeetingService signupMeetingService;

	private List<SignupMeetingWrapper> signupMeetings;

	private SakaiFacade sakaiFacade;

	private AttendeeSignupMBean attendeeSignupMBean;

	private OrganizerSignupMBean organizerSignupMBean;

	private SignupSorter signupSorter = new SignupSorter();

	/**
	 * Default Constructro
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
			}

			try {
				signupMeetingService.removeCalendarEvents(meetings);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.removal_failed"));
				log.error(Utilities.rb.getString("error.calendarEvent.removal_failed") + " - " + e.getMessage());
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
		if (permission.isUpdate()) {
			organizerSignupMBean.init(meetingWrapper);
			return ORGANIZER_MEETING_PAGE_URL;
		}

		attendeeSignupMBean.setMeetingWrapper(meetingWrapper);
		try {
			attendeeSignupMBean.updateTimeSlotWrappers(meetingWrapper);
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
		setViewDateRang(viewRange);
		setSignupMeetings(null);// reset

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
			/* sorting according to user's choices on main page */
			getSignupSorter().sort(signupMeetings);

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
		setSignupMeetings(getMeetingWrapper(viewRange));
	}

	private List<SignupMeetingWrapper> getMeetingWrapper(String viewRange) {
		String currentUserId = sakaiFacade.getCurrentUserId();
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
			signupMeetings = signupMeetingService.getSignupMeetings(sakaiFacade.getCurrentLocationId(), currentUserId,
					calendar.getTime(), getUserDefinedDate(Integer.parseInt(viewRange)));
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
		return wrapppers;
	}

	private Date getUserDefinedDate(int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR, 24 * days);
		return cal.getTime();
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
	public boolean isAllowedToCreate() {
		return signupMeetingService.isAllowedToCreateAnyInSite(sakaiFacade.getCurrentUserId(), sakaiFacade
				.getCurrentLocationId());
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

}
