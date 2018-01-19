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

package org.sakaiproject.signup.tool.jsf.organizer.action;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;

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

/**
 * <p>
 * This class will provide business logic for 'move-attendee' action by user.
 * </P>
 */
@Slf4j
public class MoveAttendee extends SignupAction {

	/**
	 * Constructor
	 * 
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 * @param userId
	 *            an unique sakai internal user id.
	 * @param siteId
	 *            an unique sakai site id.
	 */
	public MoveAttendee(String userId, String siteId, SignupMeetingService signupMeetingService) {
		super(userId, siteId, signupMeetingService,true);
	}

	/**
	 * This method perform signup process for adding user into the
	 * event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param currentTimeslot
	 *            a SignupTimeslot object.
	 * @param selectedAttendeeUserId
	 *            an unique sakai internal user id.
	 * @param selectedTimeslotId
	 *            a string value
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void move(SignupMeeting meeting, SignupTimeslot currentTimeslot, String selectedAttendeeUserId,
			String selectedTimeslotId) throws Exception {
		try {
			handleVersion(meeting, currentTimeslot, selectedAttendeeUserId, selectedTimeslotId);
			Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_MOVE_ATTENDEE_L, ToolManager.getCurrentPlacement().getContext() + " meetingId:"
					+ meeting.getId() + this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
			log.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
					+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		}

	}

	private void moveAttendee(SignupMeeting meeting, SignupTimeslot currentTimeslot, String selectedAttendeeUserId,
			String selectedTimeslotId) throws Exception {

		// remove
		SignupAttendee signupAttendee = currentTimeslot.getAttendee(selectedAttendeeUserId);
		if (signupAttendee == null)
			return;
		currentTimeslot.getAttendees().remove(signupAttendee);
		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(signupAttendee, currentTimeslot,
				SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, false);

		// add
		SignupTimeslot addTimeslot = meeting.getTimeslot(Long.parseLong(selectedTimeslotId));
		if (addTimeslot.isCanceled())
			throw new SignupUserActionException(Utilities.rb.getString("timeslot.just.canceled"));

		if (signupAttendee == null || addTimeslot.getAttendee(signupAttendee.getAttendeeUserId()) != null)
			throw new SignupUserActionException(Utilities.rb.getString("failed.move.attendee_due_to_ts_isTaken"));
		addTimeslot.getAttendees().add(signupAttendee);

		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(signupAttendee, addTimeslot,
				SignupEmailFacade.SIGNUP_ATTENDEE_SIGNUP_MOVE, true);

		removeAttendeeFromWaitingList(addTimeslot, signupAttendee);//not removed other waiting list for this guy
		// promote people waiting in line
		promoteAttendeeFromWaitingList(meeting, currentTimeslot);
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
	 * @param selectedAttendeeUserId
	 *            an unique sakai internal user id.
	 * @param selectedTimeslotId
	 *            a string value
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 * 
	 */
	private void handleVersion(SignupMeeting meeting, SignupTimeslot currentTimeslot, String selectedAttendeeUserId,
			String selectedTimeslotId) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				// reset track info
				this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
				this.signupEventTrackingInfo.setMeeting(meeting);
				meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
				currentTimeslot = meeting.getTimeslot(currentTimeslot.getId());
				if (currentTimeslot.getAttendee(selectedAttendeeUserId) == null)
					throw new SignupUserActionException(Utilities.rb
							.getString("failed.move.due_to_attendee_notExisted"));

				moveAttendee(meeting, currentTimeslot, selectedAttendeeUserId, selectedTimeslotId);

				signupMeetingService.updateSignupMeeting(meeting,isOrganizer);
				return;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("failed.move.due_to_attendee_notExisted"));
	}

}
