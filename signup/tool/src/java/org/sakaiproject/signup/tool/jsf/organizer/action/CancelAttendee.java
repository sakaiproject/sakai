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

import java.util.Iterator;
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
 * This class will provide business logic for 'Cancel-attendee' action by user.
 * </P>
 */
public class CancelAttendee extends SignupAction {

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
	public CancelAttendee(SignupMeetingService signupMeetingService, String currentUserId, String currentSiteId,
			boolean isOrganizer) {
		super(currentUserId, currentSiteId, signupMeetingService, isOrganizer);
	}

	/**
	 * This method perform signup process for adding user into the
	 * event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param timeSlot
	 *            a SignupTimeslot object.
	 * @param attendee
	 *            a SignupAttendee object.
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public SignupMeeting cancelSignup(SignupMeeting meeting, SignupTimeslot timeSlot, SignupAttendee attendee)
			throws Exception {
		try {
			handleVersion(meeting, timeSlot, attendee);
			if (ToolManager.getCurrentPlacement() != null) {
				String signupEventType = isOrganizer ? SignupEventTypes.EVENT_SIGNUP_REMOVE_ATTENDEE_L
						: SignupEventTypes.EVENT_SIGNUP_REMOVE_ATTENDEE_S;
				Utilities.postEventTracking(signupEventType, ToolManager.getCurrentPlacement().getContext()
						+ " meetingId:" + meeting.getId()
						+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
			}
			logger.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
					+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		} finally {
			meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
		}
		// TODO calendar event id;
		return meeting;
	}

	/**
	 * Check if the pre-condition is still satisfied for continuing the update
	 * process after retrieving the up-to-dated data. This process is a
	 * concurrency process.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param timeslot
	 *            a SignupTimeslot object.
	 * @param attendee
	 *            a SignupAttendee object.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void actionsForOptimisticVersioning(SignupMeeting meeting, SignupTimeslot timeslot, SignupAttendee attendee)
			throws Exception {
		/* reset it again */
		this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
		this.signupEventTrackingInfo.setMeeting(meeting);

		prepareCancelAttendee(meeting, timeslot, attendee);
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

	private void prepareCancelAttendee(SignupMeeting meeting, SignupTimeslot currentTimeSlot,
			SignupAttendee removedAttendee) throws SignupUserActionException {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		/* make sure to get latest version copy */
		for (Iterator iter = signupTimeSlots.iterator(); iter.hasNext();) {
			SignupTimeslot upToDateTS = (SignupTimeslot) iter.next();
			if (upToDateTS.getId().equals(currentTimeSlot.getId())) {
				currentTimeSlot = upToDateTS;
				break;
			}

		}

		boolean foundUser = false;
		List<SignupAttendee> attendees = currentTimeSlot.getAttendees();
		for (Iterator iter = attendees.iterator(); iter.hasNext();) {
			SignupAttendee attendee = (SignupAttendee) iter.next();
			if (removedAttendee.getAttendeeUserId().equals(attendee.getAttendeeUserId())) {
				iter.remove();
				signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(attendee, currentTimeSlot,
						SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, true);
				foundUser = true;
				break;
			}
		}
		if (!foundUser) {
			throw new SignupUserActionException(Utilities.rb.getString("someone.already.remove_you"));
		}
		/*
		 * promote a new attendee from the waiting list on the same timeslot if
		 * any
		 */
		promoteAttendeeFromWaitingList(meeting, currentTimeSlot);
	}

}
