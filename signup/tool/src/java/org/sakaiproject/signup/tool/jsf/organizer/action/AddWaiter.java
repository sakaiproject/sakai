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
import java.util.List;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.signup.util.SignupDateFormat;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * <p>
 * This class will provide business logic for 'add-waiter' action by user.
 * </P>
 */
public class AddWaiter extends SignupAction implements SignupBeanConstants {

	private final String operationType;

	/**
	 * Constructor
	 * 
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 * @param currentUserId
	 *            an unique sakai internal user id.
	 * @param currentSiteId
	 *            an unique sakai site id.
	 * @param operationType
	 *            a string value.
	 */

	public AddWaiter(SignupMeetingService signupMeetingService, String currentUserId, String currentSiteId,
			String operationType, boolean isOrganizer) {
		super(currentUserId, currentSiteId, signupMeetingService, isOrganizer);
		this.operationType = operationType;
	}

	/**
	 * Add attendee to the waiting list in the specific time slot.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param timeSlot
	 *            a SignupTimeslot object.
	 * @param newWaiter
	 *            a SignupAttendee object.
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public SignupMeeting addToWaitingList(SignupMeeting meeting, SignupTimeslot timeSlot, SignupAttendee newWaiter)
			throws Exception {
		try {
			handleVersion(meeting, timeSlot, newWaiter);
			if (ToolManager.getCurrentPlacement() != null) {
				String signupEventType = isOrganizer ? SignupEventTypes.EVENT_SIGNUP_ADD_ATTENDEE_WL_L
						: SignupEventTypes.EVENT_SIGNUP_ADD_ATTENDEE_WL_S;
				Utilities.postEventTracking(signupEventType, ToolManager.getCurrentPlacement().getContext()
						+ " meetingId:" + meeting.getId() + " -added to wlist on TS:"
						+ SignupDateFormat.format_date_h_mm_a(timeSlot.getStartTime()));
			}
			logger.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId + " - has added attendee("
					+ newWaiter.getAttendeeUserId() + ") into waiting list at timeslot started at:"
					+ SignupDateFormat.format_date_h_mm_a(timeSlot.getStartTime()));
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		} finally {
			meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
		}
		return meeting;
	}

	private void prepareAddtoWaitingList(SignupMeeting meeting, SignupTimeslot timeSlot, SignupAttendee newWaiter)
			throws Exception {

		SignupTimeslot currentTimeSlot = timeSlot;
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot upToDateTimeslot : signupTimeSlots) {
			if (currentTimeSlot.getId().equals(upToDateTimeslot.getId())) {
				currentTimeSlot = upToDateTimeslot;

				if (currentTimeSlot.isCanceled())
					throw new SignupUserActionException(Utilities.rb.getString("timeslot.just.canceled"));

				/*
				 * Instructor or TF can add anyone in regardless of limitation
				 * of lock status in the timeslot
				 */
				if (!meeting.getPermission().isUpdate()) {
					if (currentTimeSlot.isLocked())
						throw new SignupUserActionException(Utilities.rb.getString("timeslot.just.locked"));
				}
				break;
			}
		}

		/* make sure the waiter is not already in the same timeslot */
		List<SignupAttendee> attendees = currentTimeSlot.getAttendees();
		if (attendees != null && !attendees.isEmpty()) {
			for (SignupAttendee attendee : attendees) {
				if (attendee.getAttendeeUserId().equals(newWaiter.getAttendeeUserId()))
					throw new SignupUserActionException(Utilities.rb.getString("attendee.already.in.timeslot"));
			}
		}

		/* Meantime, if a spot becomes available again */
		if (attendees == null || attendees.size() < currentTimeSlot.getMaxNoOfAttendees()) {
			if (attendees == null) {
				attendees = new ArrayList<SignupAttendee>();
				currentTimeSlot.setAttendees(attendees);
			}

			attendees.add(newWaiter);

			removeAttendeeFromWaitingList(meeting, newWaiter);
			removeAttendeeFromAttendeesList(meeting, currentTimeSlot, newWaiter);
			return;
		}
		
		/*Case: no waitlist option - for situation: if user has a old meeting instance to get here this far*/
		if(!meeting.isAllowWaitList()){
			throw new SignupUserActionException(Utilities.rb.getString("waitlist.option.just.turn.off"));
		}
		
		List<SignupAttendee> waiters = currentTimeSlot.getWaitingList();

		if (waiters == null) {
			List<SignupAttendee> newWaiters = new ArrayList<SignupAttendee>();
			newWaiters.add(newWaiter);
			currentTimeSlot.setWaitingList(newWaiters);
			return;
		}

		for (SignupAttendee waiter : waiters) {
			if (newWaiter.getAttendeeUserId().equals(waiter.getAttendeeUserId()))
				throw new SignupUserActionException(Utilities.rb.getString("you.already.in_waiting_list"));
		}
		if (ON_TOP_LIST.equals(this.operationType))
			waiters.add(0, newWaiter);// to the Top of the list
		else
			waiters.add(newWaiter);

	}

	/**
	 * 
	 * Check if the pre-condition is still satisfied for continuing the update
	 * process after retrieving the up-to-dated data. This process is a
	 * concurrency process.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param timeSlot
	 *            a SignupTimeslot object.
	 * @param newWaiter
	 *            a SignupAttendee object.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void actionsForOptimisticVersioning(SignupMeeting meeting, SignupTimeslot timeSlot, SignupAttendee newWaiter)
			throws Exception {
		prepareAddtoWaitingList(meeting, timeSlot, newWaiter);
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
}
