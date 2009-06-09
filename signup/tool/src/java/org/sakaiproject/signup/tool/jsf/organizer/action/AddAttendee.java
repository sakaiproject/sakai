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

import java.util.List;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupEmailFacade;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * <p>
 * This class will provide business logic for 'add-attendee' action by user.
 * </P>
 */
public class AddAttendee extends SignupAction {

	/**
	 * Constructor
	 * 
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 * @param currentUserId
	 *            an unique sakai internal user id.
	 * @param currentSiteId
	 *            an unique sakai site id.
	 */
	public AddAttendee(SignupMeetingService signupMeetingService, String currentUserId, String currentSiteId,
			boolean isOrganizer) {
		super(currentUserId, currentSiteId, signupMeetingService, isOrganizer);
	}

	/**
	 * This method perform signup process for adding user into the
	 * event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param currentTimeslot
	 *            a SignupTimeslot object.
	 * @param newAttendee
	 *            a SignupAttendee object.
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public SignupMeeting signup(SignupMeeting meeting, SignupTimeslot currentTimeslot, SignupAttendee newAttendee)
			throws Exception {
		try {
			handleVersion(meeting, currentTimeslot, newAttendee);
			/* check if it comes from RESTful case */
			if (ToolManager.getCurrentPlacement() != null) {
				String signupEventType = isOrganizer ? SignupEventTypes.EVENT_SIGNUP_ADD_ATTENDEE_L
						: SignupEventTypes.EVENT_SIGNUP_ADD_ATTENDEE_S;
				Utilities.postEventTracking(signupEventType, ToolManager.getCurrentPlacement().getContext()
						+ " meetingId:" + meeting.getId()
						+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
			}
			logger.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
					+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		} finally {
			meeting = reloadMeeting(meeting.getId());
		}

		return meeting;
	}

	/**
	 * Check if the pre-condition is still satisfied for continuing the update
	 * process after retrieving the up-to-dated data. This process is a
	 * concurrency process.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param currentTimeslot
	 *            a SignupTimeslot object.
	 * @param newAttendee
	 *            a SignupAttendee object.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void actionsForOptimisticVersioning(SignupMeeting meeting, SignupTimeslot currentTimeslot,
			SignupAttendee newAttendee) throws Exception {
		// reset it again
		this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
		this.signupEventTrackingInfo.setMeeting(meeting);

		/*
		 * Instructor or TF can add anyone in regardless of limitation of Max#
		 * attendees or lock status in the timeslot
		 */
		if (!meeting.getPermission().isUpdate()) {
			checkTimeSlotStillAvailable(meeting, currentTimeslot);
			checkForPromotion(meeting, newAttendee);// whether someone else
			// promoted this newAttendee
		}

		addAttendeeToTimeSlot(meeting, currentTimeslot, newAttendee);
		removeNewAttendeeFromWaitingList(meeting, newAttendee);

	}

	/**
	 * Give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param currentTimeslot
	 *            a SignupTimeslot object.
	 * @param currentAttendee
	 *            a SignupAttendee object.
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	private SignupMeeting handleVersion(SignupMeeting meeting, SignupTimeslot currentTimeslot,
			SignupAttendee currentAttendee) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
				actionsForOptimisticVersioning(meeting, currentTimeslot, currentAttendee);
				signupMeetingService.updateSignupMeeting(meeting, isOrganizer);
				return meeting;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.db"));
	}

	private void checkTimeSlotStillAvailable(SignupMeeting meeting, SignupTimeslot currentTimeslot) throws Exception {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		Long changedTimeslotId = currentTimeslot.getId();
		for (SignupTimeslot upTodateTimeslot : signupTimeSlots) {
			if (upTodateTimeslot.getId().equals(changedTimeslotId)) {
				if (upTodateTimeslot.isCanceled())
					throw new SignupUserActionException(Utilities.rb.getString("timeslot.just.canceled"));

				if (upTodateTimeslot.isLocked())
					throw new SignupUserActionException(Utilities.rb.getString("timeslot.just.locked"));

				if (!upTodateTimeslot.isAvailable())
					throw new SignupUserActionException(Utilities.rb.getString("someone.already.taken.theTimeslot"));
			}
		}
	}

	/**
	 * This check required only if version exception is thrown during add signup
	 * to ensure that no one else promoted the current user
	 */
	private void checkForPromotion(SignupMeeting meeting, SignupAttendee newAttendee) throws Exception {
		String attendeeUserId = newAttendee.getAttendeeUserId();
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			for (SignupAttendee attendee : attendees) {
				if (attendee.getAttendeeUserId().equals(attendeeUserId))
					throw new SignupUserActionException(Utilities.rb.getString("you.promoted.to.another.ts_meanwhile"));
			}
		}
	}

	private SignupMeeting reloadMeeting(Long meetingId) {
		SignupMeeting m = signupMeetingService.loadSignupMeeting(meetingId, userId, siteId);
		return m;
	}

	private void addAttendeeToTimeSlot(SignupMeeting meeting, SignupTimeslot selectedTimeSlot,
			SignupAttendee newAttendee) throws Exception {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot upTodateTimeslot : signupTimeSlots) {
			if (selectedTimeSlot.getId().equals(upTodateTimeslot.getId())) {
				List<SignupAttendee> attendees = upTodateTimeslot.getAttendees();
				if (attendees != null && !attendees.isEmpty()) {
					for (SignupAttendee attendee : attendees) {
						if (attendee.getAttendeeUserId().equals(newAttendee.getAttendeeUserId()))
							throw new SignupUserActionException(Utilities.rb.getString("attendee.already.in.timeslot"));
					}
				}

				/* For organizer case and for attendee, it's already safeguarded */
				if (upTodateTimeslot.isCanceled())
					throw new SignupUserActionException(Utilities.rb.getString("timeslot.just.canceled"));

				upTodateTimeslot.getAttendees().add(newAttendee);
				signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(newAttendee, upTodateTimeslot,
						SignupEmailFacade.SIGNUP_ATTENDEE_SIGNUP, true);
			}
		}
	}

	private void removeNewAttendeeFromWaitingList(SignupMeeting meeting, SignupAttendee newAttendee) {
		super.removeAttendeeFromWaitingList(meeting, newAttendee);
	}

}
