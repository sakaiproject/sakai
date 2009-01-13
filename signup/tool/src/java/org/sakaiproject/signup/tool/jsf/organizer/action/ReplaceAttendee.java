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
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * <p>
 * This class will provide business logic for 'Replace-attendee' action by user.
 * </P>
 */
public class ReplaceAttendee extends SignupAction {

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
	public ReplaceAttendee(String userId, String siteId, SignupMeetingService signupMeetingService) {
		super(userId, siteId, signupMeetingService, true);
	}

	/**
	 * This method perform Replace process for replacing attendee from the
	 * event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param currentTimeslot
	 *            a SignupTimeslot object.
	 * @param toBeReplacedUserId
	 *            an unique sakai internal user id.
	 * @param replacerUserId
	 *            an unique sakai internal user id.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void replace(SignupMeeting meeting, SignupTimeslot currentTimeslot, String toBeReplacedUserId,
			String replacerUserId, List<SignupUser> allSignupUsers) throws Exception {

		SignupAttendee replacer = new SignupAttendee(replacerUserId, SignupUIBaseBean.getAttendeeMainActiveSiteId(
				replacerUserId, allSignupUsers, this.siteId));

		try {
			handleVersion(meeting, currentTimeslot, toBeReplacedUserId, replacer);
			Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_REPLACE_ATTENDEE_L, ToolManager
					.getCurrentPlacement().getContext()
					+ " meetingId:" + meeting.getId() + this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
			logger.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
					+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		}

	}

	private void replace(SignupMeeting meeting, SignupTimeslot currentTimeslot, String toBeReplacedUserId,
			SignupAttendee replacer) throws Exception {
		List<SignupAttendee> attendees = currentTimeslot.getAttendees();
		int count = 0;
		if (currentTimeslot.getAttendee(replacer.getAttendeeUserId()) != null)
			throw new SignupUserActionException(Utilities.rb.getString("failed.replaced_due_to_already_in_ts"));

		for (Iterator iter = attendees.iterator(); iter.hasNext();) {
			SignupAttendee att = (SignupAttendee) iter.next();
			if (toBeReplacedUserId.equals(att.getAttendeeUserId())) {
				attendees.remove(att);
				attendees.add(count, replacer);
				signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(replacer, currentTimeslot,
						SignupEmailFacade.SIGNUP_ATTENDEE_SIGNUP_REPLACE, true, att);
				signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(att, currentTimeslot,
						SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, false);
				/* remove from any Wait List if any */
				removeAttendeeFromWaitingList(meeting, replacer);
				break;
			}
			count++;
		}
	}

	/**
	 * Give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 */
	private void handleVersion(SignupMeeting meeting, SignupTimeslot currentTimeslot, String toBeReplacedUserId,
			SignupAttendee newAttendee) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
				this.signupEventTrackingInfo.setMeeting(meeting);
				meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
				currentTimeslot = meeting.getTimeslot(currentTimeslot.getId());
				if (currentTimeslot.getAttendee(toBeReplacedUserId) == null)
					throw new SignupUserActionException(Utilities.rb
							.getString("failed.replaced_due_to_attendee_notExisted_in_ts"));

				replace(meeting, currentTimeslot, toBeReplacedUserId, newAttendee);

				signupMeetingService.updateSignupMeeting(meeting, isOrganizer);
				return;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("failed.replace.someone.already.updated.db"));
	}

}
