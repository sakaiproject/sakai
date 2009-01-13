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

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.signup.util.SignupDateFormat;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * <p>
 * This class will provide business logic for 'lock or unlock a timeslot' action
 * by user.
 * </P>
 */
public class LockUnlockTimeslot extends SignupAction {

	private SignupMeeting meeting;

	private SignupTimeslot timeslot;

	/**
	 * /** Construtcor
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param timeslot
	 *            a SignupTimeslot object.
	 * @param userId
	 *            an unique sakai internal user id.
	 * @param siteId
	 *            an unique sakai site id.
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 */
	public LockUnlockTimeslot(SignupMeeting meeting, SignupTimeslot timeslot, String userId, String siteId,
			SignupMeetingService signupMeetingService) {
		super(userId, siteId, signupMeetingService,true);
		this.meeting = meeting;
		this.timeslot = timeslot;
	}

	/**
	 * This method performs lock or unlock process for a specific time slot in
	 * the event/meeting.
	 * 
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public SignupMeeting lockOrUnlock() throws Exception {
		boolean lockAction = !timeslot.isLocked();
		try {
			handleVersion(meeting, timeslot, lockAction);
			String signupEventType=timeslot.isLocked()? SignupEventTypes.EVENT_SIGNUP_MTNG_TS_UNLOCK : SignupEventTypes.EVENT_SIGNUP_MTNG_TS_LOCK;
			Utilities.postEventTracking(signupEventType, ToolManager.getCurrentPlacement().getContext() + " meetingId:"
					+ meeting.getId() + " on the TS:"
						+ SignupDateFormat.format_date_h_mm_a(timeslot.getStartTime()));
			logger.debug("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId + " - has "
					+ (lockAction ? "locked" : "unlocked") + " the timeslot started at:"
					+ SignupDateFormat.format_date_h_mm_a(timeslot.getStartTime()));
		} catch (PermissionException pe) {
			throw new SignupUserActionException(Utilities.rb.getString("no.permissoin.do_it"));
		} finally {
			meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
		}

		return meeting;
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
	 * @param lockAction
	 *            a boolean value
	 * @return a SignupMeeting object, which is a refreshed updat-to-date data.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	private SignupMeeting handleVersion(SignupMeeting meeting, SignupTimeslot currentTimeslot, boolean lockAction)
			throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
				currentTimeslot = meeting.getTimeslot(currentTimeslot.getId());
				if (currentTimeslot.isLocked() == lockAction)
					throw new SignupUserActionException(Utilities.rb
							.getString("someone.already.changed.ts.lock_status"));

				currentTimeslot.setLocked(lockAction);

				signupMeetingService.updateSignupMeeting(meeting,isOrganizer);
				return meeting;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}

		throw new SignupUserActionException(Utilities.rb.getString("failed.lock_or_unlock_ts"));
	}

}
