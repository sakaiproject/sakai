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
package org.sakaiproject.signup.tool.jsf.organizer.action;

import java.util.ArrayList;
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

/**
 * <p>
 * This class will provide business logic for modifying meeting action
 * by organizer.
 * </P>
 */
public class EditMeeting extends SignupAction implements MeetingTypes {

	private int maxNumOfAttendees;

	private boolean showAttendeeName;

	private int currentNumberOfSlots;

	private int timeSlotDuration;

	private boolean unlimited;

	// private int totalEventDuration;// for group/announcement types

	private SignupMeeting originalMeetingCopy;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;

	private boolean convertToNoRecurrent;

	private List<SignupMeeting> savedMeetings;

	private String recurringType = null;

	// may not used
	private Date firstRecurMeetingModifyDate = null;

	private Date lastRecurMeetingModifyDate = null;

	public EditMeeting(String userId, String siteId, SignupMeetingService signupMeetingService, boolean isOrganizer) {
		super(userId, siteId, signupMeetingService, isOrganizer);
		// TODO Auto-generated constructor stub
	}

	public void saveModifiedMeeting(SignupMeeting meeting) throws Exception {
		handleVersion(meeting);

		/*
		 * give a warning to user and for announcement type no update is
		 * required
		 */
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
				/*
				 * TODO current eventTracking is not working for multiple
				 * recurring meetings. It's a bit more complex and need modify
				 * the SignupEventTrackingInfoImpl object or other way.
				 * Currently, no email are sent out to newly promoted people for
				 * multiple recurring meetings.
				 */
				this.signupEventTrackingInfo.setMeeting(meeting);

				List<SignupMeeting> sMeetings = prepareModify(meeting);
				signupMeetingService.updateSignupMeetings(sMeetings, true);
				setSavedMeetings(sMeetings);// for email & postCalendar purpose
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
	private List<SignupMeeting> prepareModify(SignupMeeting modifyMeeting) throws Exception {
		List<SignupMeeting> upTodateOrginMeetings = null;
		List<SignupMeeting> newlyModifyMeetings = new ArrayList<SignupMeeting>();

		Long recurrenceId = modifyMeeting.getRecurrenceId();
		if (recurrenceId != null && recurrenceId.longValue() > 0 && !isConvertToNoRecurrent()) {
			/* only update the future recurring meeting now now today */
			upTodateOrginMeetings = signupMeetingService.getRecurringSignupMeetings(siteId, userId, recurrenceId,
					new Date());
			retrieveRecurrenceData(upTodateOrginMeetings);
		} else {
			SignupMeeting upTodateOrginMeeting = reloadMeeting(modifyMeeting);
			upTodateOrginMeetings = new ArrayList<SignupMeeting>();
			upTodateOrginMeetings.add(upTodateOrginMeeting);
		}

		/*
		 * Since recurring meetings are identical by title, location, etc. only
		 * one will be checked here.
		 */
		/*
		 * If someone has changed it before you,it should be caught by versionId
		 * since meetings are saved as a bulk.
		 */
		SignupMeeting upToDateMatchOne = getCorrespondingMeeting(upTodateOrginMeetings, modifyMeeting);
		checkPreCondition(upToDateMatchOne);

		Calendar calendar = Calendar.getInstance();
		for (SignupMeeting upTodateOrginMeeting : upTodateOrginMeetings) {

			SignupMeeting newlyModifyMeeting = upTodateOrginMeeting;

			/*
			 * set event starting time for calendar due to multiple-recurring
			 * meetings
			 */
			long startTimeChangeDiff = modifyMeeting.getStartTime().getTime()
					- getOriginalMeetingCopy().getStartTime().getTime();
			calendar.setTimeInMillis(upTodateOrginMeeting.getStartTime().getTime() + startTimeChangeDiff);

			/* pass user changes */
			passModifiedValues(modifyMeeting, calendar, newlyModifyMeeting);

			newlyModifyMeetings.add(newlyModifyMeeting);
		}

		return newlyModifyMeetings;
	}

	private void passModifiedValues(SignupMeeting modifyMeeting, Calendar calendar, SignupMeeting newlyModifyMeeting)
			throws Exception {
		newlyModifyMeeting.setTitle(modifyMeeting.getTitle());
		newlyModifyMeeting.setLocation(modifyMeeting.getLocation());
		newlyModifyMeeting.setStartTime(calendar.getTime());
		newlyModifyMeeting.setDescription(modifyMeeting.getDescription());
		newlyModifyMeeting.setLocked(modifyMeeting.isLocked());
		newlyModifyMeeting.setCanceled(modifyMeeting.isCanceled());
		newlyModifyMeeting.setReceiveEmailByOwner(modifyMeeting.isReceiveEmailByOwner());

		if (newlyModifyMeeting.getMeetingType().equals(INDIVIDUAL)) {
			List<SignupTimeslot> timeslots = newlyModifyMeeting.getSignupTimeSlots();
			/* add increased time slots */
			int newAddTs = getCurrentNumberOfSlots() - timeslots.size();
			for (int i = 0; i < newAddTs; i++) {
				SignupTimeslot newTs = new SignupTimeslot();
				newTs.setMaxNoOfAttendees(maxNumOfAttendees);
				newTs.setDisplayAttendees(showAttendeeName);
				timeslots.add(newTs);
			}

			for (SignupTimeslot timeslot : timeslots) {
				timeslot.setMaxNoOfAttendees(maxNumOfAttendees);
				timeslot.setDisplayAttendees(showAttendeeName);
				timeslot.setStartTime(calendar.getTime());
				calendar.add(Calendar.MINUTE, timeSlotDuration);
				timeslot.setEndTime(calendar.getTime());
			}
		}

		if (newlyModifyMeeting.getMeetingType().equals(GROUP)) {
			List<SignupTimeslot> signupTimeSlots = newlyModifyMeeting.getSignupTimeSlots();
			SignupTimeslot timeslot = signupTimeSlots.get(0);
			timeslot.setStartTime(newlyModifyMeeting.getStartTime());

			calendar.add(Calendar.MINUTE, getTimeSlotDuration());
			timeslot.setEndTime(calendar.getTime());
			timeslot.setDisplayAttendees(showAttendeeName);
			int maxAttendees = (unlimited) ? SignupTimeslot.UNLIMITED : maxNumOfAttendees;
			timeslot.setMaxNoOfAttendees(maxAttendees);
		}

		if (newlyModifyMeeting.getMeetingType().equals(ANNOUNCEMENT)) {
			calendar.add(Calendar.MINUTE, getTimeSlotDuration());

		}
		/*
		 * Promoting waiter to attendee status if max size increased and is
		 * allowed
		 */
		if (maxNumOfAttendees > originalMeetingCopy.getMaxNumberOfAttendees()) {
			promotingWaiters(newlyModifyMeeting);
		}

		// set up meeting end time
		newlyModifyMeeting.setEndTime(calendar.getTime());

		/* setup sign-up begin / deadline */
		setSignupBeginDeadlineData(newlyModifyMeeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(),
				getDeadlineTimeType());

		/* This can happen only once to promote recurring meeting to stand-alone */
		if (isConvertToNoRecurrent()) {
			newlyModifyMeeting.setRecurrenceId(null);
		}

		if (newlyModifyMeeting.getRecurrenceId() != null) {
			newlyModifyMeeting.setRepeatType(getRecurringType());
			newlyModifyMeeting.setRepeatUntil(getLastRecurMeetingModifyDate());
		}
	}

	private SignupMeeting getCorrespondingMeeting(List<SignupMeeting> upTodateOrginMeetings, SignupMeeting modifyMeeting) {
		for (SignupMeeting sm : upTodateOrginMeetings) {
			if (sm.getId().equals(modifyMeeting.getId()))
				return sm;
		}
		return null;
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
	private void checkPreCondition(SignupMeeting upTodateMeeting) throws SignupUserActionException {
		if (upTodateMeeting == null
				|| !originalMeetingCopy.getTitle().equals(upTodateMeeting.getTitle())
				|| !originalMeetingCopy.getLocation().equals(upTodateMeeting.getLocation())
				|| originalMeetingCopy.getStartTime().getTime() != upTodateMeeting.getStartTime().getTime()
				|| originalMeetingCopy.getEndTime().getTime() != upTodateMeeting.getEndTime().getTime()
				|| originalMeetingCopy.getSignupBegins().getTime() != upTodateMeeting.getSignupBegins().getTime()
				|| originalMeetingCopy.getSignupDeadline().getTime() != upTodateMeeting.getSignupDeadline().getTime()
				/* TODO more case to consider here */
				|| !((originalMeetingCopy.getRecurrenceId() == null && upTodateMeeting.getRecurrenceId() == null) || (originalMeetingCopy
						.getRecurrenceId() != null && originalMeetingCopy.getRecurrenceId().equals(
						upTodateMeeting.getRecurrenceId())))
				|| originalMeetingCopy.getNoOfTimeSlots() != upTodateMeeting.getNoOfTimeSlots()
				|| !((originalMeetingCopy.getDescription() == null && upTodateMeeting.getDescription() == null) || (originalMeetingCopy
						.getDescription() != null && upTodateMeeting.getDescription() != null)
						&& (originalMeetingCopy.getDescription().length() == upTodateMeeting.getDescription().length()))) {
			throw new SignupUserActionException(Utilities.rb.getString("someone.modified.event.content"));
		}
	}

	/**
	 * setup the event/meeting's signup begin and deadline time and validate it
	 * too
	 */
	private void setSignupBeginDeadlineData(SignupMeeting meeting, int signupBegin, String signupBeginType,
			int signupDeadline, String signupDeadlineType) throws Exception {
		Date sBegin = Utilities.subTractTimeToDate(meeting.getStartTime(), signupBegin, signupBeginType);
		Date sDeadline = Utilities.subTractTimeToDate(meeting.getEndTime(), signupDeadline, signupDeadlineType);

		boolean origSignupStarted = originalMeetingCopy.getSignupBegins().before(new Date());// ????
		/* TODO have to pass it in?? */
		if (sBegin.before(new Date()) && !origSignupStarted) {
			// a warning for user
			Utilities.addErrorMessage(Utilities.rb.getString("warning.your.event.singup.begin.time.passed.today.time"));
		}

		meeting.setSignupBegins(sBegin);

		if (sBegin.after(sDeadline))
			throw new SignupUserActionException(Utilities.rb.getString("signup.deadline.is.before.signup.begin"));

		meeting.setSignupDeadline(sDeadline);
	}

	public int getCurrentNumberOfSlots() {
		return currentNumberOfSlots;
	}

	public void setCurrentNumberOfSlots(int currentNumberOfSlots) {
		this.currentNumberOfSlots = currentNumberOfSlots;
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

	public int getTimeSlotDuration() {
		return timeSlotDuration;
	}

	public void setTimeSlotDuration(int timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
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

	/*
	 * public int getTotalEventDuration() { return totalEventDuration; }
	 * 
	 * public void setTotalEventDuration(int totalEventDuration) {
	 * this.totalEventDuration = totalEventDuration; }
	 */

	public boolean isUnlimited() {
		return unlimited;
	}

	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	public boolean isConvertToNoRecurrent() {
		return convertToNoRecurrent;
	}

	public void setConvertToNoRecurrent(boolean convertToNoRecurrent) {
		this.convertToNoRecurrent = convertToNoRecurrent;
	}

	public List<SignupMeeting> getSavedMeetings() {
		return savedMeetings;
	}

	private void setSavedMeetings(List<SignupMeeting> savedMeetings) {
		this.savedMeetings = savedMeetings;
	}

	private void retrieveRecurrenceData(List<SignupMeeting> upTodateOrginMeetings) {
		setFirstRecurMeetingModifyDate(null);
		setLastRecurMeetingModifyDate(null);
		if (upTodateOrginMeetings == null || upTodateOrginMeetings.isEmpty())
			return;

		setFirstRecurMeetingModifyDate(upTodateOrginMeetings.get(0).getStartTime());
		/* in case: it's the last one */
		setLastRecurMeetingModifyDate(upTodateOrginMeetings.get(0).getStartTime());

		int listSize = upTodateOrginMeetings.size();
		if (listSize > 1) {
			setLastRecurMeetingModifyDate(upTodateOrginMeetings.get(listSize - 1).getStartTime());
			Calendar calFirst = Calendar.getInstance();
			Calendar calSecond = Calendar.getInstance();
			/*
			 * we can only get approximate estimation by assuming it's a
			 * succession
			 */
			calFirst.setTime(upTodateOrginMeetings.get(listSize - 2).getStartTime());
			calFirst.set(Calendar.SECOND, 0);
			calFirst.set(Calendar.MILLISECOND, 0);
			calSecond.setTime(upTodateOrginMeetings.get(listSize - 1).getStartTime());
			calSecond.set(Calendar.SECOND, 0);
			calSecond.set(Calendar.MILLISECOND, 0);
			int tmp = calSecond.get(Calendar.DATE);
			int daysDiff = (int) (calSecond.getTimeInMillis() - calFirst.getTimeInMillis()) / DAY_IN_MILLISEC;
			if (daysDiff == perDay)
				setRecurringType(DAILY);
			else if (daysDiff == perWeek)
				setRecurringType(WEEKLY);
			else if (daysDiff == perBiweek)
				setRecurringType(BIWEEKLY);
		}
	}

	private Date getFirstRecurMeetingModifyDate() {
		return firstRecurMeetingModifyDate;
	}

	private void setFirstRecurMeetingModifyDate(Date firstRecurMeetingModifyDate) {
		this.firstRecurMeetingModifyDate = firstRecurMeetingModifyDate;
	}

	private Date getLastRecurMeetingModifyDate() {
		return lastRecurMeetingModifyDate;
	}

	private void setLastRecurMeetingModifyDate(Date lastRecurMeetingModifyDate) {
		this.lastRecurMeetingModifyDate = lastRecurMeetingModifyDate;
	}

	private String getRecurringType() {
		return recurringType;
	}

	private void setRecurringType(String recurringType) {
		this.recurringType = recurringType;
	}

}
