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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This class is a wrapper class for SignupMeeting for UI purpose
 * </P>
 */
public class SignupMeetingWrapper implements SignupBeanConstants {

	private SignupMeeting meeting;

	private final String creator;

	private final String currentUserId;

	private boolean selected;

	private SakaiFacade sakaiFacade;

	private boolean subRecurringMeeting;

	private String recurId;

	private int recurEventsSize = 0;

	private String hideStyle = "display: none;";

	private Date startTime;

	private Date endTime;

	private boolean showMyAppointmentTimeFrame = false;

	private String availableStatus = null;

	/**
	 * Constructor
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 * @param creator
	 *            an user display name string.
	 * @param currentUserId
	 *            an unique sakai internal user id(not username).
	 */
	public SignupMeetingWrapper(SignupMeeting signupMeeting, String creator, String currentUserId,
			SakaiFacade sakaiFacade) {
		this.meeting = signupMeeting;
		this.creator = creator;
		this.currentUserId = currentUserId;
		this.sakaiFacade = sakaiFacade;
		this.selected = false;
		this.subRecurringMeeting = false;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a SignupMeeting object.
	 */
	public SignupMeeting getMeeting() {
		return meeting;
	}

	/**
	 * This is a getter for UI
	 * 
	 * @return a creator's display name string.
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an avaiability status string.
	 */
	public String getAvailableStatus() {
		if (this.availableStatus == null) {
			this.availableStatus = retrieveAvailStatus();
		}
		return this.availableStatus;
	}

	/**
	 * It will force to recalculate the current available status.
	 */
	public void resetAvailableStatus() {
		this.availableStatus = null;
	}

	private String retrieveAvailStatus() {
		long curTime = (new Date()).getTime();
		long meetingStartTime = meeting.getStartTime().getTime();
		long meetingEndTime = meeting.getEndTime().getTime();
		long meetingSignupBegin = meeting.getSignupBegins().getTime();
		if (meetingEndTime < curTime)
			return Utilities.rb.getString("event.closed");
		if (meetingEndTime > curTime && meetingStartTime < curTime)
			return Utilities.rb.getString("event.inProgress");

		if (meeting.getMeetingType().equals(SignupMeeting.ANNOUNCEMENT)) {
			return Utilities.rb.getString("event.SignupNotRequire");
		}

		String availableStatus = Utilities.rb.getString("event.unavailable");
		boolean isSignupBegin = true;
		if (meetingSignupBegin > curTime) {
			isSignupBegin = false;
			availableStatus = Utilities.rb.getString("event.Signup.not.started.yet")
					+ " "
					+ getSakaiFacade().getTimeService().newTime(meeting.getSignupBegins().getTime())
							.toStringLocalShortDate();
		}

		boolean isOnWaitingList = false;
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			for (SignupAttendee attendee : attendees) {
				if (attendee.getAttendeeUserId().equals(currentUserId))
					return Utilities.rb.getString("event.youSignedUp");
			}

			List<SignupAttendee> waiters = timeslot.getWaitingList();
			if (!isOnWaitingList) {
				for (SignupAttendee waiter : waiters) {
					if (waiter.getAttendeeUserId().equals(currentUserId)) {
						availableStatus = Utilities.rb.getString("event.youOnWaitList");
						isOnWaitingList = true;
						break;
					}
				}
			}

			int size = (attendees == null) ? 0 : attendees.size();
			if (!isOnWaitingList
					&& isSignupBegin
					&& (size < timeslot.getMaxNoOfAttendees() || timeslot.getMaxNoOfAttendees() == SignupTimeslot.UNLIMITED)) {
				availableStatus = Utilities.rb.getString("event.available");
			}
		}

		return availableStatus;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if one of the timeslot is locked or cancelled.
	 */
	public boolean isAtleastOneTimeslotLockedOrCanceled() {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots == null)
			return false;

		for (SignupTimeslot timeslot : signupTimeSlots) {
			if (timeslot.isCanceled() || timeslot.isLocked())
				return true;
		}

		return false;
	}

	/**
	 * This is a setter.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 */
	public void setMeeting(SignupMeeting meeting) {
		this.meeting = meeting;
		setLastUpdatedTime(new Date().getTime());
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if the event/meeting is selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selected
	 *            a boolean value.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Check if the current data is old and need refreshing.
	 * 
	 * @return true if the current data is old and need refreshing.
	 */
	public boolean isRefresh() {
		if ((new Date()).getTime() - getLastUpdatedTime() > dataRefreshInterval)
			return true;

		return false;
	}

	private long lastUpdatedTime = (new Date()).getTime();

	/**
	 * Get the last updated time for this SignupMeeting object
	 * 
	 * @return a long value (milli seconds)
	 */
	private long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	/**
	 * This is a setter.
	 * 
	 * @param lastUpdatedTime
	 *            a long value for milli seconds.
	 */
	private void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	private SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	/**
	 * It is a UI getter method.
	 * 
	 * @return
	 */
	public boolean isSubRecurringMeeting() {
		return subRecurringMeeting;
	}

	public void setSubRecurringMeeting(boolean subRecurringMeeting) {
		this.subRecurringMeeting = subRecurringMeeting;
	}

	/**
	 * The recurId format is as '294_0' where 294:parent meetingId; _0:first
	 * sub-recurrent meeting
	 * 
	 * @return a String object
	 */
	public String getRecurId() {
		if (meeting.getRecurrenceId() != null && isSubRecurringMeeting())
			return this.recurId;

		return meeting.getRecurrenceId() != null ? meeting.getRecurrenceId().toString() : meeting.getId().toString();
	}

	/*
	 * The recurId format is as '294_0' where 294:parent meetingId; _0:first
	 * sub-recurrent meeting
	 */
	public void setRecurId(String recurId) {
		this.recurId = recurId;
	}

	/**
	 * It's a getter method.
	 * 
	 * @return
	 */
	public String getHideStyle() {
		if (isSubRecurringMeeting())
			return hideStyle;

		return "";
	}

	public void setHideStyle(String hideStyle) {
		this.hideStyle = hideStyle;
	}

	/**
	 * It's a getter method for UI
	 * 
	 * @return true if it's the first one in the table.
	 */
	public boolean isFirstOneRecurMeeting() {
		if (meeting.isRecurredMeeting() && !isSubRecurringMeeting())
			return true;

		return false;
	}

	/**
	 * It's a getter method.
	 * 
	 * @return the size of the recurring meetings.
	 */
	public int getRecurEventsSize() {
		return recurEventsSize;
	}

	public void setRecurEventsSize(int recurEventsSize) {
		this.recurEventsSize = recurEventsSize;
	}

	/**
	 * This is mainly used for showing my appointment time frame at UI.
	 * 
	 * @return a Date object
	 */
	public Date getStartTime() {
		if (!isShowMyAppointmentTimeFrame())
			return meeting.getStartTime();

		return startTime != null ? startTime : meeting.getStartTime();
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * This is mainly used for showing my appointment time frame at UI.
	 * 
	 * @return a Date object
	 */
	public Date getEndTime() {
		if (!isShowMyAppointmentTimeFrame())
			return meeting.getEndTime();

		return endTime != null ? endTime : meeting.getEndTime();
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isShowMyAppointmentTimeFrame() {
		return showMyAppointmentTimeFrame;
	}

	public void setShowMyAppointmentTimeFrame(boolean showMyAppointmentTimeFrame) {
		this.showMyAppointmentTimeFrame = showMyAppointmentTimeFrame;
	}

	private Calendar cal = Calendar.getInstance();

	/**
	 * This will test if the event/meeting is cross days
	 * 
	 * @return true if the event/meeting is cross days
	 */
	public boolean isMyAppointmentCrossDays() {
		cal.setTime(getStartTime());
		int startingDay = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(getEndTime());
		int endingDay = cal.get(Calendar.DAY_OF_YEAR);
		return (startingDay != endingDay);
	}

}
