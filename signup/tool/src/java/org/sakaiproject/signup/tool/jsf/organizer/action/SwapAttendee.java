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
import org.sakaiproject.signup.tool.jsf.organizer.OrganizerSignupMBean;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * <p>
 * This class will provide business logic for 'Swap-attendee' action by user.
 * </P>
 */
public class SwapAttendee extends SignupAction {

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
	public SwapAttendee(String userId, String siteId, SignupMeetingService signupMeetingService) {
		super(userId, siteId, signupMeetingService,true);
	}

	/**
	 * This method perform Replace process for replacing attendee from the
	 * event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param currentTimeslot
	 *            a SignupTimeslot object.
	 * @param selectedAttendeeUserId
	 *            an unique sakai internal user id.
	 * @param attendeeTimeSlotWithId
	 *            a string array with attendee id and timeslot id
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void swapAttendee(SignupMeeting meeting, SignupTimeslot currentTimeslot, String selectedAttendeeUserId,
			String attendeeTimeSlotWithId) throws Exception {
		String[] values = attendeeTimeSlotWithId.split(OrganizerSignupMBean.DELIMITER);

		String swapTimeSlotId = values[0];
		String swapAttendeeId = values[1];
		try {
			handleVersion(meeting, currentTimeslot, selectedAttendeeUserId, swapTimeSlotId, swapAttendeeId);
			Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_SWAP_ATTENDEE_L, ToolManager.getCurrentPlacement().getContext() + " meetingId:"
					+ meeting.getId() + this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
			logger.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
					+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		}

	}

	/**
	 * Give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 */
	private void handleVersion(SignupMeeting meeting, SignupTimeslot currentTimeslot, String selectedAttendeeUserId,
			String swapTimeSlotId, String swapAttendeeId) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				// reset track info
				this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
				this.signupEventTrackingInfo.setMeeting(meeting);
				meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
				currentTimeslot = meeting.getTimeslot(currentTimeslot.getId());

				if (currentTimeslot.getAttendee(selectedAttendeeUserId) == null)
					throw new SignupUserActionException(Utilities.rb
							.getString("failed.swap_due_to_selected_attendee_notExited"));
				if (meeting.getTimeslot(Long.parseLong(swapTimeSlotId)).getAttendee(swapAttendeeId) == null)
					throw new SignupUserActionException(Utilities.rb.getString("failed.swap_due_to_attendee_notExited"));

				swap(meeting, currentTimeslot, selectedAttendeeUserId, swapTimeSlotId, swapAttendeeId);

				signupMeetingService.updateSignupMeeting(meeting,isOrganizer);
				return;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.db"));

	}

	private void swap(SignupMeeting meeting, SignupTimeslot currentTimeslot, String selectedAttendeeUserId,
			String swapTimeSlotId, String swapAttendeeId) throws Exception {
		int currentAttendeeIndex = 0;
		int currentTimeslotIndex = 0;
		int swapTimeslotIndex = 0;
		int swapAttendeeIndex = 0;

		int timeSlotCounter = 0;
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> currentAttendees = timeslot.getAttendees();
			int counter = 0;
			for (SignupAttendee att : currentAttendees) {
				if (att.getAttendeeUserId().equals(selectedAttendeeUserId)
						&& timeslot.getId().equals(currentTimeslot.getId())) {
					currentTimeslotIndex = timeSlotCounter;
					currentAttendeeIndex = counter;
					break;
				}

				if (att.getAttendeeUserId().equals(swapAttendeeId)
						&& timeslot.getId().toString().equals(swapTimeSlotId)) {
					swapTimeslotIndex = timeSlotCounter;
					swapAttendeeIndex = counter;
					break;
				}
				counter++;
			}
			timeSlotCounter++;
		}

		SignupTimeslot currentimeslot = signupTimeSlots.get(currentTimeslotIndex);
		List<SignupAttendee> currentAttendees = currentimeslot.getAttendees();
		SignupAttendee currentAttendee = currentAttendees.get(currentAttendeeIndex);

		SignupTimeslot swapTimeslot = signupTimeSlots.get(swapTimeslotIndex);
		List<SignupAttendee> swapAttendees = swapTimeslot.getAttendees();
		SignupAttendee swapAttendee = swapAttendees.get(swapAttendeeIndex);

		if (currentTimeslot.getAttendee(swapAttendee.getAttendeeUserId()) != null)
			throw new SignupUserActionException(Utilities.rb
					.getString("failed.swap.due_to_swapped_attendee_already_in_ts"));

		if (swapTimeslot.getAttendee(currentAttendee.getAttendeeUserId()) != null)
			throw new SignupUserActionException(Utilities.rb
					.getString("failed.swap.due_to_current_attendee_already_in_swapped_ts"));

		currentAttendees.remove(currentAttendee);
		currentAttendees.add(currentAttendeeIndex, swapAttendee);

		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(currentAttendee, swapTimeslot,
				SignupEmailFacade.SIGNUP_ATTENDEE_SIGNUP_SWAP, true, swapAttendee);
		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(currentAttendee, currentTimeslot,
				SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, false);

		swapAttendees.remove(swapAttendee);
		swapAttendees.add(swapAttendeeIndex, currentAttendee);

		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(swapAttendee, currentTimeslot,
				SignupEmailFacade.SIGNUP_ATTENDEE_SIGNUP_SWAP, true, currentAttendee);
		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(swapAttendee, swapTimeslot,
				SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, false);

		removeAttendeeFromWaitingList(currentTimeslot, swapAttendee);
		removeAttendeeFromWaitingList(swapTimeslot, currentAttendee);
	}

}
