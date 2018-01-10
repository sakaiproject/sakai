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

import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;

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

/**
 * <p>
 * This class will provide business logic for 'Remove-attendee' action by user.
 * </P>
 */
@Slf4j
public class RemoveWaiter implements SignupBeanConstants {

	private static final int MAX_NUMBER_OF_RETRY = 20;

	private final SignupMeetingService signupMeetingService;

	private final String currentUserId;

	private final String currentSiteId;

	private final boolean isOrganizer;

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
	public RemoveWaiter(SignupMeetingService signupMeetingService, String currentUserId, String currentSiteId,
			String operationType, boolean isOrganizer) {
		this.signupMeetingService = signupMeetingService;
		this.currentUserId = currentUserId;
		this.currentSiteId = currentSiteId;
		this.isOrganizer = isOrganizer;
	}

	private SignupMeeting reloadMeeting(Long meetingId) {
		SignupMeeting m = signupMeetingService.loadSignupMeeting(meetingId, currentUserId, currentSiteId);
		return m;
	}

	/**
	 * This method perform Remove process for removing attendee from the waiting
	 * list in the event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param timeslot
	 *            a SignupTimeslot object.
	 * @param waiter
	 *            a SignupAttendee object.
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public SignupMeeting removeFromWaitingList(SignupMeeting meeting, SignupTimeslot timeslot, SignupAttendee waiter)
			throws Exception {
		try {

			handleVersion(meeting, timeslot, waiter);
			if (ToolManager.getCurrentPlacement() != null) {
				String signupEventType = isOrganizer ? SignupEventTypes.EVENT_SIGNUP_REMOVE_ATTENDEE_WL_L
						: SignupEventTypes.EVENT_SIGNUP_REMOVE_ATTENDEE_WL_S;
				Utilities.postEventTracking(signupEventType, ToolManager.getCurrentPlacement().getContext()
						+ " meetingId:" + meeting.getId() + " -removed from wlist on TS:"
						+ SignupDateFormat.format_date_h_mm_a(timeslot.getStartTime()));
			}
			log.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + currentUserId
					+ " - has removed attendee(userId):" + waiter.getAttendeeUserId() + " from waiting list"
					+ " at timeslot started at:" + SignupDateFormat.format_date_h_mm_a(timeslot.getStartTime()));
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		} finally {
			meeting = reloadMeeting(meeting.getId());
		}
		// TODO calendar event id;
		return meeting;

	}

	/**
	 * Give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 */
	private void handleVersion(SignupMeeting meeting, SignupTimeslot timeslot, SignupAttendee waiter) throws Exception {
		boolean success = false;
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				meeting = reloadMeeting(meeting.getId());
				prepareRemoveFromWaitingList(meeting, timeslot, waiter);
				signupMeetingService.updateSignupMeeting(meeting, isOrganizer);
				success = true;
				break; // add attendee is successful
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		if (!success)
			throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.db"));

	}

	private void prepareRemoveFromWaitingList(SignupMeeting meeting, SignupTimeslot timeSlot,
			SignupAttendee removedWaiter) throws Exception {

		SignupTimeslot currentTimeSlot = timeSlot;
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot upToDateTimeslot : signupTimeSlots) {
			if (currentTimeSlot.getId().equals(upToDateTimeslot.getId())) {
				currentTimeSlot = upToDateTimeslot;
				break;
			}
		}
		boolean found = false;
		List<SignupAttendee> waiters = currentTimeSlot.getWaitingList();
		if (waiters != null) {
			for (Iterator iter = waiters.iterator(); iter.hasNext();) {
				SignupAttendee waiter = (SignupAttendee) iter.next();
				if (removedWaiter.getAttendeeUserId().equals(waiter.getAttendeeUserId())) {
					iter.remove();
					found = true;
				}
			}
			if (!found) {
				throw new SignupUserActionException(Utilities.rb
						.getString("someone.already.removed_or_promoted_attendee"));
			}
		}

	}

}
