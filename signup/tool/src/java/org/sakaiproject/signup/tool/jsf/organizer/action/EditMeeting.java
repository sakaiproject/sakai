/**********************************************************************************
 * $URL: https://sakai21-dev.its.yale.edu/svn/signup/branches/2-5/tool/src/java/org/sakaiproject/signup/tool/jsf/organizer/action/EditMeeting.java $
 * $Id: EditMeeting.java 2975 2008-04-11 16:01:29Z gl256 $
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
package org.sakaiproject.signup.tool.jsf.organizer.action;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.signup.util.SignupDateFormat;
import org.springframework.dao.OptimisticLockingFailureException;

public class EditMeeting extends SignupAction implements MeetingTypes {

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

	public EditMeeting(String userId, String siteId, SignupMeetingService signupMeetingService, boolean isOrganizer) {
		super(userId, siteId, signupMeetingService, isOrganizer);
		// TODO Auto-generated constructor stub
	}

	public void saveModifiedMeeting(SignupMeeting meeting) throws Exception {

		handleVersion(meeting);
		
		/* give a warning to user and for announcement type no update is required*/
		if (!unlimited && this.maxNumOfAttendees < originalMeetingCopy.getMaxNumberOfAttendees()) {
			Utilities.addMessage(Utilities.rb.getString("max.num_attendee_changed_and_attendee_mayOver_limit_inTS"));
		}
		
		logger.info("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
				+ " - has modified the meeting at meeting time:"
				+ SignupDateFormat.format_date_h_mm_a(meeting.getStartTime()));
		if (getMaxNumOfAttendees() > this.originalMeetingCopy.getMaxNumberOfAttendees())
			logger.info("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
				+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
	}

	/*
	 * give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 */
	public void handleVersion(SignupMeeting meeting) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {

				this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
				this.signupEventTrackingInfo.setMeeting(meeting);

				meeting = prepareModify(meeting);
				signupMeetingService.updateSignupMeeting(meeting, true);
				return;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("edit.failed_due_to_other_updated_db"));
	}

	private SignupMeeting reloadMeeting(SignupMeeting meeting) {
		return signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
	}

	/* put the modification into right place */
	private SignupMeeting prepareModify(SignupMeeting modifyMeeting) throws Exception {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(modifyMeeting.getStartTime());

		SignupMeeting upTodateOrginMeeting = reloadMeeting(modifyMeeting);

		modifyMeeting = checkPreCondition(upTodateOrginMeeting, modifyMeeting);

		List<SignupTimeslot> timeslots = modifyMeeting.getSignupTimeSlots();

		if (modifyMeeting.getMeetingType().equals(INDIVIDUAL)) {
			for (int i = 0; i < this.addMoreTimeslots; i++) {
				SignupTimeslot newTs = new SignupTimeslot();
				newTs.setMaxNoOfAttendees(maxNumOfAttendees);
				newTs.setDisplayAttendees(showAttendeeName);
				timeslots.add(newTs);
			}

			for (SignupTimeslot timeslot : timeslots) {
				timeslot.setMaxNoOfAttendees(maxNumOfAttendees);
				timeslot.setDisplayAttendees(showAttendeeName);
				timeslot.setStartTime(calendar.getTime());
				calendar.add(Calendar.MINUTE, durationOfTslot);
				timeslot.setEndTime(calendar.getTime());
			}
		}

		if (modifyMeeting.getMeetingType().equals(GROUP)) {
			List<SignupTimeslot> signupTimeSlots = modifyMeeting.getSignupTimeSlots();
			SignupTimeslot timeslot = signupTimeSlots.get(0);
			timeslot.setStartTime(modifyMeeting.getStartTime());

			calendar.add(Calendar.MINUTE, getTotalEventDuration());
			timeslot.setEndTime(calendar.getTime());
			timeslot.setDisplayAttendees(showAttendeeName);
			int maxAttendees = (unlimited) ? SignupTimeslot.UNLIMITED : maxNumOfAttendees;
			timeslot.setMaxNoOfAttendees(maxAttendees);
		}

		if (modifyMeeting.getMeetingType().equals(ANNOUNCEMENT)) {
			calendar.add(Calendar.MINUTE, getTotalEventDuration());

		}
		/*
		 * Promoting waiter to attendee status if max size increased and is
		 * allowed
		 */
		if (maxNumOfAttendees > originalMeetingCopy.getMaxNumberOfAttendees()) {
			promotingWaiters(modifyMeeting);
		}

		modifyMeeting.setEndTime(calendar.getTime());

		/* setup signup begin / deadline */
		setSignupBeginDeadlineData(modifyMeeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(),
				getDeadlineTimeType());

		return modifyMeeting;
	}

	/* Promoting all possible waiters to attendee status */
	private void promotingWaiters(SignupMeeting modifiedMeeting) {
		List<SignupTimeslot> timeSlots = modifiedMeeting.getSignupTimeSlots();
		if (timeSlots == null || timeSlots.isEmpty())
			return;

		for (SignupTimeslot timeslot : timeSlots) {
			List attList = timeslot.getAttendees();
			if (attList == null)
				continue;// nobody on wait list for sure

			if (attList.size() < maxNumOfAttendees) {
				int availNum = maxNumOfAttendees - attList.size();
				for (int i = 0; i < availNum; i++) {
					promoteAttendeeFromWaitingList(modifiedMeeting, timeslot);
				}
			}
		}

	}

	/*
	 * Check if there is any update in DB before this one is saved into DB
	 * storage.
	 */
	private SignupMeeting checkPreCondition(SignupMeeting upTodateMeeting, SignupMeeting modifyMeeting)
			throws SignupUserActionException {
		if (!originalMeetingCopy.getTitle().equals(upTodateMeeting.getTitle())
				|| !originalMeetingCopy.getLocation().equals(upTodateMeeting.getLocation())
				|| originalMeetingCopy.getStartTime().getTime() != upTodateMeeting.getStartTime().getTime()
				|| originalMeetingCopy.getEndTime().getTime() != upTodateMeeting.getEndTime().getTime()
				|| originalMeetingCopy.getSignupBegins().getTime() != upTodateMeeting.getSignupBegins().getTime()
				|| originalMeetingCopy.getSignupDeadline().getTime() != upTodateMeeting.getSignupDeadline().getTime()
				/* TODO more case to consider here */
				|| originalMeetingCopy.getNoOfTimeSlots() != upTodateMeeting.getNoOfTimeSlots()
				|| (originalMeetingCopy.getDescription() != null && originalMeetingCopy.getDescription() != null)
				&& (originalMeetingCopy.getDescription().length() != upTodateMeeting.getDescription().length())) {
			throw new SignupUserActionException(Utilities.rb.getString("someone.modified.event.content"));
		}
		/* copy to the new version of meeting instance */
		upTodateMeeting.setTitle(modifyMeeting.getTitle());
		upTodateMeeting.setLocation(modifyMeeting.getLocation());
		upTodateMeeting.setStartTime(modifyMeeting.getStartTime());
		upTodateMeeting.setEndTime(modifyMeeting.getEndTime());
		upTodateMeeting.setSignupBegins(modifyMeeting.getSignupBegins());
		upTodateMeeting.setSignupDeadline(modifyMeeting.getSignupDeadline());
		upTodateMeeting.setDescription(modifyMeeting.getDescription());
		upTodateMeeting.setLocked(modifyMeeting.isLocked());
		upTodateMeeting.setCanceled(modifyMeeting.isCanceled());
		upTodateMeeting.setReceiveEmailByOwner(modifyMeeting.isReceiveEmailByOwner());

		return upTodateMeeting;
	}

	/**
	 * setup the event/meeting's signup begin and deadline time and validate it
	 * too
	 */
	private void setSignupBeginDeadlineData(SignupMeeting meeting, int signupBegin, String signupBeginType,
			int signupDeadline, String signupDeadlineType) throws Exception {
		Date sBegin = Utilities.subTractTimeToDate(meeting.getStartTime(), signupBegin, signupBeginType);
		Date sDeadline = Utilities.subTractTimeToDate(meeting.getEndTime(), signupDeadline, signupDeadlineType);
		
		boolean origSignupStarted = originalMeetingCopy.getSignupBegins().before(new Date());
		if (sBegin.before(new Date()) && !origSignupStarted) {
			// a warning for user
			Utilities.addErrorMessage(Utilities.rb.getString("warning.your.event.singup.begin.time.passed.today.time"));
		}

		meeting.setSignupBegins(sBegin);

		if (sBegin.after(sDeadline))
			throw new SignupUserActionException(Utilities.rb.getString("signup.deadline.is.before.signup.begin"));

		meeting.setSignupDeadline(sDeadline);
	}

	public int getAddMoreTimeslots() {
		return addMoreTimeslots;
	}

	public void setAddMoreTimeslots(int addMoreTimeslots) {
		this.addMoreTimeslots = addMoreTimeslots;
	}

	public int getDeadlineTime() {
		return deadlineTime;
	}

	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	public int getDurationOfTslot() {
		return durationOfTslot;
	}

	public void setDurationOfTslot(int durationOfTslot) {
		this.durationOfTslot = durationOfTslot;
	}

	public int getMaxNumOfAttendees() {
		return maxNumOfAttendees;
	}

	public void setMaxNumOfAttendees(int maxNumOfAttendees) {
		this.maxNumOfAttendees = maxNumOfAttendees;
	}

	public SignupMeeting getOriginalMeetingCopy() {
		return originalMeetingCopy;
	}

	public void setOriginalMeetingCopy(SignupMeeting originalMeetingCopy) {
		this.originalMeetingCopy = originalMeetingCopy;
	}

	public boolean isShowAttendeeName() {
		return showAttendeeName;
	}

	public void setShowAttendeeName(boolean showAttendeeName) {
		this.showAttendeeName = showAttendeeName;
	}

	public int getSignupBegins() {
		return signupBegins;
	}

	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

	public int getTotalEventDuration() {
		return totalEventDuration;
	}

	public void setTotalEventDuration(int totalEventDuration) {
		this.totalEventDuration = totalEventDuration;
	}

	public boolean isUnlimited() {
		return unlimited;
	}

	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

}
