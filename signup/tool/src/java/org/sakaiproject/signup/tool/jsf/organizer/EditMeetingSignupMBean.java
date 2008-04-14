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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.EditMeeting;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * modify meeting page:<b>modifyMeeting.jsp</b> and backbone system.
 * </P>
 */
public class EditMeetingSignupMBean extends SignupUIBaseBean {

	private SignupMeeting signupMeeting;

	private int maxNumOfAttendees;

	private boolean showAttendeeName;

	private int addMoreTimeslots;

	private int durationOfTslot;

	private boolean unlimited;

	private int totalEventDuration;// for group/announcement types

	private SignupMeeting originalMeetingCopy;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;
	
	private EditMeeting editMeeting;

	/**
	 * This method will reset everything to orignal value and also initialize
	 * the value to the variables in this UIBean, which lives in a session
	 * scope.
	 * 
	 */
	public void reset() {
		addMoreTimeslots = 0;
		maxNumOfAttendees = 0;
		showAttendeeName = false;
		sendEmail = true;
		unlimited = false;
		
		editMeeting =null;

		this.signupMeeting = reloadMeeting(meetingWrapper.getMeeting());
		/* for check pre-condition purpose */
		this.originalMeetingCopy = reloadMeeting(meetingWrapper.getMeeting());
		// keep the last version need a deep copy?

		List<SignupTimeslot> signupTimeSlots = getSignupMeeting().getSignupTimeSlots();
		if (signupTimeSlots == null)
			return;

		if (signupTimeSlots != null && !signupTimeSlots.isEmpty()) {
			SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
			int timeSlotDuration = (int) ((ts.getEndTime().getTime() - ts.getStartTime().getTime()) / MINUTE_IN_MILLISEC);
			setDurationOfTslot(timeSlotDuration);
			maxNumOfAttendees = ts.getMaxNoOfAttendees();
			showAttendeeName = ts.isDisplayAttendees();
			unlimited = ts.isUnlimitedAttendee();
			setTotalEventDuration(timeSlotDuration * signupTimeSlots.size());
		} else {// announcement meeting type
			int meetingDuration = (int) ((getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime()
					.getTime()) / MINUTE_IN_MILLISEC);
			setDurationOfTslot(meetingDuration);
			setTotalEventDuration(meetingDuration);

		}
		populateDataForBeginDeadline(this.signupMeeting);

	}

	/* get the relative time out */
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
	 * This is a JSF action call method by UI to modify the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String processSaveModify() {
		try {
			SignupMeeting meeting = getSignupMeeting();
			
			EditMeeting editMeeting = new EditMeeting(getSakaiFacade().getCurrentUserId(),getSakaiFacade().getCurrentLocationId(),getSignupMeetingService(),true);
			/*Pass modified data*/
			editMeeting.setAddMoreTimeslots(getAddMoreTimeslots());
			editMeeting.setSignupBegins(getSignupBegins());
			editMeeting.setSignupBeginsType(getSignupBeginsType());
			editMeeting.setDeadlineTime(getDeadlineTime());
			editMeeting.setDeadlineTimeType(getDeadlineTimeType());
			editMeeting.setDurationOfTslot(getDurationOfTslot());
			editMeeting.setMaxNumOfAttendees(getMaxNumOfAttendees());
			editMeeting.setShowAttendeeName(isShowAttendeeName());
			editMeeting.setOriginalMeetingCopy(this.originalMeetingCopy);
			editMeeting.setUnlimited(isUnlimited());
			editMeeting.setTotalEventDuration(getTotalEventDuration());						
			
			editMeeting.saveModifiedMeeting(meeting);
			
			if (sendEmail) {
				try {
					signupMeetingService.sendEmail(meeting, SIGNUP_MEETING_MODIFIED);
					/*send email to promoted waiter if size increased*/
					signupMeetingService.sendEmailToParticipantsByOrganizerAction(editMeeting.getSignupEventTrackingInfo());
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}

			try {
				signupMeetingService.modifyCalendar(meeting);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.updated_failed"));
				logger.warn(Utilities.rb.getString("error.calendarEvent.updated_failed") + " - " + e.getMessage());
			}

		} catch (PermissionException pe) {
			Utilities.addErrorMessage(Utilities.rb.getString("no.permissoin.do_it"));
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());// TODO need to keep in
														// the same page with
														// new data if db
														// changes
		} catch (Exception e) {
			Utilities.addErrorMessage(Utilities.rb.getString("db.error_or_event.notExisted"));
			logger.warn(Utilities.rb.getString("db.error_or_event.notExisted") + " - " + e.getMessage());
			Utilities.resetMeetingList();
			return MAIN_EVENTS_LIST_PAGE_URL;
		}
		reloadMeetingWrapperInOrganizerPage();

		return ORGANIZER_MEETING_PAGE_URL;
	}
	

	/**
	 * This is for UI-display End time purpose. When user selects a new starting
	 * time, it will refresh the page with new ending time.
	 * 
	 * @param vce
	 *            A ValueChangeEvent object.
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

	private void reloadMeetingWrapperInOrganizerPage() {
		OrganizerSignupMBean bean = (OrganizerSignupMBean) FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().get("OrganizerSignupMBean");
		SignupMeeting meeting = reloadMeeting(meetingWrapper.getMeeting());
		this.meetingWrapper.setMeeting(meeting);
		bean.reset(meetingWrapper);
	}

	private SignupMeeting reloadMeeting(SignupMeeting meeting) {
		return signupMeetingService.loadSignupMeeting(meeting.getId(), sakaiFacade.getCurrentUserId(), sakaiFacade
				.getCurrentLocationId());
	}

	/* overwrite the default one */
	public SignupMeetingWrapper getMeetingWrapper() {
		return meetingWrapper;
	}

	/**
	 * Check if the name of attendees should be made public.
	 * 
	 * @return true if the name of attendees is made public.
	 */
	public boolean isShowAttendeeName() {
		return showAttendeeName;
	}

	/**
	 * This is a setter.
	 * 
	 * @param showAttendeeName
	 *            a boolean value.
	 */
	public void setShowAttendeeName(boolean showAttendeeName) {
		this.showAttendeeName = showAttendeeName;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer number.
	 */
	public int getAddMoreTimeslots() {
		return addMoreTimeslots;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param addMoreTimeslots
	 *            an integer number.
	 */
	public void setAddMoreTimeslots(int addMoreTimeslots) {
		this.addMoreTimeslots = addMoreTimeslots;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer value.
	 */
	public int getDurationOfTslot() {
		return durationOfTslot;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param durationOfTslot
	 *            an integer value.
	 */
	public void setDurationOfTslot(int durationOfTslot) {
		this.durationOfTslot = durationOfTslot;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return an integer number.
	 */
	public int getMaxNumOfAttendees() {
		return maxNumOfAttendees;
	}

	/**
	 * This is a setter.
	 * 
	 * @param maxNumOfAttendees
	 *            an integer number.
	 */
	public void setMaxNumOfAttendees(int maxNumOfAttendees) {
		this.maxNumOfAttendees = maxNumOfAttendees;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a SignupMeeting object.
	 */
	public SignupMeeting getSignupMeeting() {
		return signupMeeting;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 */
	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	/**
	 * Check to see if the attendees are limited in the event/meeting.
	 * 
	 * @return true if the attendees are limited in the event/meeting
	 */
	public boolean isUnlimited() {
		return unlimited;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param unlimited
	 *            a boolean value.
	 */
	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer number.
	 */
	public int getTotalEventDuration() {
		return totalEventDuration;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param totalEventDuration
	 *            an integer number.
	 */
	public void setTotalEventDuration(int totalEventDuration) {
		this.totalEventDuration = totalEventDuration;
	}

	/**
	 * This is a getter method to provide a relative time.
	 * 
	 * @return am integer number.
	 */
	public int getDeadlineTime() {
		return deadlineTime;
	}

	/**
	 * This is a setter.
	 * 
	 * @param deadlineTime
	 *            an integer number, which represents a relative time to meeting
	 *            starting time.
	 */
	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param deadlineTimeType
	 *            an integer number.
	 */
	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer number.
	 */
	public int getSignupBegins() {
		return signupBegins;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param signupBegins
	 *            an integer number.
	 */
	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer number.
	 */
	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param signupBeginsType
	 *            an integer number.
	 */
	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

}
