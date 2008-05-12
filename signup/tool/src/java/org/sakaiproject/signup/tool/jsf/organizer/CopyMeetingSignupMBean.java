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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.jsf.organizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * copy meeting page:<b>copyMeeting.jsp</b> and backbone system.
 * </P>
 */
public class CopyMeetingSignupMBean extends SignupUIBaseBean {

	private SignupMeeting signupMeeting;

	private boolean keepAttendees;

	private int durationOfTimeSlot;

	private int maxNumOfAttendees;

	private int totalEventDuration;// for group/announcement types

	private boolean unlimited = false;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;

	/**
	 * this reset information which contains in this UIBean lived in a session
	 * scope
	 * 
	 */
	public void reset() {
		keepAttendees = false;
		sendEmail = DEFAULT_SEND_EMAIL;
		this.signupMeeting = signupMeetingService.loadSignupMeeting(meetingWrapper.getMeeting().getId(), sakaiFacade
				.getCurrentUserId(), sakaiFacade.getCurrentLocationId());

		List<SignupTimeslot> signupTimeSlots = signupMeeting.getSignupTimeSlots();

		if (signupTimeSlots != null && !signupTimeSlots.isEmpty()) {
			SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
			int timeSlotDuration = (int) ((ts.getEndTime().getTime() - ts.getStartTime().getTime()) / MINUTE_IN_MILLISEC);
			setDurationOfTimeSlot(timeSlotDuration);
			maxNumOfAttendees = ts.getMaxNoOfAttendees();
			this.unlimited = ts.isUnlimitedAttendee();
			setTotalEventDuration(timeSlotDuration * signupTimeSlots.size());
		} else {// announcement meeting type
			int meetingDuration = (int) ((signupMeeting.getEndTime().getTime() - signupMeeting.getStartTime().getTime()) / MINUTE_IN_MILLISEC);
			setDurationOfTimeSlot(meetingDuration);
			setTotalEventDuration(meetingDuration);
		}

		populateDataForBeginDeadline(this.signupMeeting);

	}

	/* process the relative time for Signup begin/deadline */
	private void populateDataForBeginDeadline(SignupMeeting sMeeting) {
		long signupBeginsTime = sMeeting.getSignupBegins() == null ? new Date().getTime() : sMeeting.getSignupBegins()
				.getTime();
		long signupDeadline = sMeeting.getSignupDeadline() == null ? new Date().getTime() : sMeeting
				.getSignupDeadline().getTime();

		/* get signup begin & deadline relative time in minutes */
		long signupBeginBeforMeeting = (sMeeting.getStartTime().getTime() - signupBeginsTime) / MINUTE_IN_MILLISEC;
		long signupDeadLineBeforMeetingEnd = (sMeeting.getEndTime().getTime() - signupDeadline) / MINUTE_IN_MILLISEC;

		this.signupBeginsType = Utilities.getTimeScaleType(signupBeginBeforMeeting);
		this.signupBegins = Utilities.getRelativeTimeValue(signupBeginsType, signupBeginBeforMeeting);

		this.deadlineTimeType = Utilities.getTimeScaleType(signupDeadLineBeforMeetingEnd);
		this.deadlineTime = Utilities.getRelativeTimeValue(deadlineTimeType, signupDeadLineBeforMeetingEnd);

	}

	/**
	 * Just to overwrite the parent one
	 */
	public SignupMeetingWrapper getMeetingWrapper() {
		return meetingWrapper;
	}

	/**
	 * This is a JSF action call method by UI to copy the event/meeting into a
	 * new one
	 * 
	 * @return an action outcome string
	 */
	// TODO: what to do if timeslot is locked or canceled
	public String processSaveCopy() {
		SignupMeeting sMeeting = getSignupMeeting();
		try {
			prepareCopy(sMeeting);
			this.signupMeetingService.saveMeeting(sMeeting, sakaiFacade.getCurrentUserId());
			Utilities.resetMeetingList();

			logger.info("Meeting Name:" + sMeeting.getTitle() + " - UserId:" + sakaiFacade.getCurrentUserId()
					+ " - has copied to create a new meeting at meeting startTime:"
					+ getSakaiFacade().getTimeService().newTime(sMeeting.getStartTime().getTime()).toStringLocalFull());

			try {
				if (sendEmail)
					signupMeetingService.sendEmail(sMeeting, SIGNUP_NEW_MEETING);

				signupMeetingService.sendEmail(sMeeting, SIGNUP_PRE_ASSIGN);
			} catch (Exception e) {
				logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
				Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
			}

			try {
				signupMeetingService.postToCalendar(sMeeting);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.posted_failed"));
				logger.warn(Utilities.rb.getString("error.calendarEvent.posted_failed") + " - " + e.getMessage());
			}

		} catch (PermissionException e) {
			logger.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("no.permission_create_event"));
			return ORGANIZER_MEETING_PAGE_URL;
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
			return "";
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
			return ORGANIZER_MEETING_PAGE_URL;
		}

		return MAIN_EVENTS_LIST_PAGE_URL;
	}

	private void prepareCopy(SignupMeeting meeting) throws Exception {

		meeting.setId(null);// to save as new meeting in db
		List<SignupTimeslot> timeslots = meeting.getSignupTimeSlots();

		boolean lockOrCanceledTimeslot = false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(meeting.getStartTime());

		/* Announcement type */
		if (isAnnouncementType() || timeslots == null || timeslots.isEmpty()) {
			calendar.add(Calendar.MINUTE, getTotalEventDuration());
			meeting.setMeetingType(ANNOUNCEMENT);
			meeting.setSignupTimeSlots(null);
		} else {
			for (SignupTimeslot timeslot : timeslots) {
				lockOrCanceledTimeslot = copyTimeslot(meeting, lockOrCanceledTimeslot, calendar, timeslot);
			}

			if (lockOrCanceledTimeslot)
				Utilities.addMessage(Utilities.rb.getString("warning.some_timeslot_may_locked_canceled"));
		}

		meeting.setEndTime(calendar.getTime());

		/* setup signup begin / deadline */
		setSignupBeginDeadlineData(meeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(),
				getDeadlineTimeType());

		copySites(meeting);

	}

	/**
	 * This is for UI-display End time purpose. When user selects a new starting
	 * time, it will refresh the page with new ending time
	 * 
	 * @param vce
	 *            A ValueChangeEvent object
	 * @return an action outcome stirng
	 */
	public String processEventEndTime(ValueChangeEvent vce) {
		Date newStartTime = (Date) vce.getNewValue();
		Calendar cal = Calendar.getInstance();
		cal.setTime(newStartTime);
		cal.add(Calendar.MINUTE, getTotalEventDuration());
		this.signupMeeting.setEndTime(cal.getTime());
		return "";
	}

	private boolean copyTimeslot(SignupMeeting meeting, boolean lockOrCanceledTimeslot, Calendar calendar,
			SignupTimeslot timeslot) {

		/* to save as new TimeSlot in db */
		timeslot.setId(null);

		/* Never copy wait list people */
		timeslot.setWaitingList(null);

		if (timeslot.isCanceled() || timeslot.isLocked())
			lockOrCanceledTimeslot = true;
		if (!this.keepAttendees)
			timeslot.setAttendees(null);// clear up
		else {
			// TODO: should we copy the command, calendar id need to be
			// copied, calendar
			List<SignupAttendee> attendees = new ArrayList<SignupAttendee>(timeslot.getAttendees());
			for (SignupAttendee attendee : attendees) {

				// attendee.setComments(null);
				attendee.setCalendarEventId(null);
				attendee.setCalendarId(null);
			}
			timeslot.setAttendees(attendees);

		}

		timeslot.setStartTime(calendar.getTime());
		calendar.add(Calendar.MINUTE, durationOfTimeSlot);
		timeslot.setEndTime(calendar.getTime());

		return lockOrCanceledTimeslot;
	}

	private void copySites(SignupMeeting meeting) {
		List<SignupSite> sites = meeting.getSignupSites();
		if (sites != null && !sites.isEmpty()) {
			for (SignupSite site : sites) {
				site.setId(null);
				site.setCalendarEventId(null);
				site.setCalendarId(null);
				List<SignupGroup> grps = new ArrayList<SignupGroup>(site.getSignupGroups());// copy
				for (SignupGroup group : grps) {
					group.setCalendarId(null);
					group.setCalendarEventId(null);
				}
				site.setSignupGroups(grps);
			}
		}
	}

	/**
	 * check if the attendees in the event/meeting should be copied along with
	 * it
	 * 
	 * @return true if the attendees in the event/meeting is copied along with
	 *         it
	 */
	public boolean isKeepAttendees() {
		return keepAttendees;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param keepAttendees
	 */
	public void setKeepAttendees(boolean keepAttendees) {
		this.keepAttendees = keepAttendees;
	}

	/**
	 * this is a getter method
	 * 
	 * @return an integer value
	 */
	public int getDurationOfTimeSlot() {
		return durationOfTimeSlot;
	}

	/**
	 * this is a setter
	 * 
	 * @param durationOfTslot
	 *            an integer value
	 */
	public void setDurationOfTimeSlot(int durationOfTslot) {
		this.durationOfTimeSlot = durationOfTslot;
	}

	/**
	 * this is a getter method
	 * 
	 * @return an integer number
	 */
	public int getMaxNumOfAttendees() {
		return maxNumOfAttendees;
	}

	/**
	 * this is a setter
	 * 
	 * @param maxNumOfAttendees
	 *            an integer number
	 */
	public void setMaxNumOfAttendees(int maxNumOfAttendees) {
		this.maxNumOfAttendees = maxNumOfAttendees;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return a SignupMeeting object
	 */
	public SignupMeeting getSignupMeeting() {
		return signupMeeting;
	}

	/**
	 * this is a setter
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object
	 */
	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	/**
	 * check to see if the attendees are limited in the event/meeting
	 * 
	 * @return true if the attendees are limited in the event/meeting
	 */
	public boolean isUnlimited() {
		return unlimited;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param unlimited
	 *            a boolean value
	 */
	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return an integer number
	 */
	public int getTotalEventDuration() {
		return totalEventDuration;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param totalEventDuration
	 *            an integer number
	 */
	public void setTotalEventDuration(int totalEventDuration) {
		this.totalEventDuration = totalEventDuration;
	}

	/**
	 * this is a getter method to provide a relative time
	 * 
	 * @return am integer number
	 */
	public int getDeadlineTime() {
		return deadlineTime;
	}

	/**
	 * this is a setter
	 * 
	 * @param deadlineTime
	 *            an integer number, which represents a relative time to meeting
	 *            starting time
	 */
	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return a string value
	 */
	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param deadlineTimeType
	 *            an integer number
	 */
	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return an integer number
	 */
	public int getSignupBegins() {
		return signupBegins;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param signupBegins
	 *            an integer number
	 */
	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return an integer number
	 */
	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param signupBeginsType
	 *            an integer number
	 */
	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

}
