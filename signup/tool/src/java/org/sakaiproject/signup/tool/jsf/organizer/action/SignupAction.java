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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.signup.logic.SignupEmailFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;

/**
 * <p>
 * This is a abstract base class, which will provide most commen members and
 * shared methods for children user action classes.
 * </P>
 */
public abstract class SignupAction implements SignupBeanConstants{

	protected final String userId;

	protected final String siteId;

	protected final SignupMeetingService signupMeetingService;

	protected SignupEventTrackingInfo signupEventTrackingInfo;

	protected Log logger = LogFactory.getLog(getClass());

	protected final boolean isOrganizer;

	/**
	 * Constructor
	 * 
	 * @param userId
	 *            an unique sakai internal user id.
	 * @param siteId
	 *            an unique sakai site id.
	 * @param signupMeetingService
	 *            a SignupMeetingService obect.
	 */
	public SignupAction(String userId, String siteId, SignupMeetingService signupMeetingService, boolean isOrganizer) {
		this.userId = userId;
		this.siteId = siteId;
		this.signupMeetingService = signupMeetingService;
		this.isOrganizer = isOrganizer;

		this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
	}

	/**
	 * Remove the attendee from the waiting list in an event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param attendee
	 *            a SignupAttendee object.
	 */
	public void removeAttendeeFromWaitingList(SignupMeeting meeting, SignupAttendee attendee) {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> waiters = timeslot.getWaitingList();
			SignupAttendee waiter = timeslot.getWaiter(attendee.getAttendeeUserId());
			if (waiter != null)
				waiters.remove(waiter);
		}

	}

	/**
	 * Remove the attendee from the waiting list at a specific time slot.
	 * 
	 * @param timeslot
	 *            a SingupTimeslot object.
	 * @param attendee
	 *            a SignupAttendee object.
	 */
	protected void removeAttendeeFromWaitingList(SignupTimeslot timeslot, SignupAttendee attendee) {
		List<SignupAttendee> waiters = timeslot.getWaitingList();
		SignupAttendee waiter = timeslot.getWaiter(attendee.getAttendeeUserId());
		if (waiter != null)
			waiters.remove(waiter);

	}

	/** remove the promoted attendee from all the waiting list */
	protected void promoteAttendeeFromWaitingList(SignupMeeting meeting, SignupTimeslot timeSlot) {

		List<SignupAttendee> attendees = timeSlot.getAttendees();
		/*
		 * if timeslot has more attendees than the max since orgranizer can add
		 * any number of attendees -> then don't promote
		 */
		if (attendees != null && timeSlot.getMaxNoOfAttendees() <= attendees.size())
			return;

		List<SignupAttendee> waitingList = timeSlot.getWaitingList();
		if (waitingList == null || waitingList.isEmpty())
			return;

		SignupAttendee promotedAttendee = waitingList.get(0);

		SignupAttendee att = new SignupAttendee();
		att.setAttendeeUserId(promotedAttendee.getAttendeeUserId());
		att.setComments(promotedAttendee.getComments());
		// TODO: att.setCalendarEventId(promotedAttendee.getCalendarEventId());
		att.setSignupSiteId(promotedAttendee.getSignupSiteId());
		timeSlot.getAttendees().add(att);
		/**
		 * Removing from waiting list first is important. This will prevent
		 * infinite loop
		 */
		removeAttendeeFromWaitingList(meeting, promotedAttendee);

		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(att, timeSlot,
				SignupEmailFacade.SIGNUP_ATTENDEE_PROMOTE, false);

		removeAttendeeFromAttendeesList(meeting, timeSlot, promotedAttendee);

	}

	/**
	 * when one attendee is removed from the Singup list, the people on waiting
	 * list will be promoted into this spot.
	 */
	protected void removeAttendeeFromAttendeesList(SignupMeeting meeting, SignupTimeslot currentTimeslot,
			SignupAttendee attendee) {
		String attendeeUserId = attendee.getAttendeeUserId();
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot upToDateTimeslot : signupTimeSlots) {
			/* prevent from removing this attendee from just promoted spot */
			if (currentTimeslot.getId().equals(upToDateTimeslot.getId()))
				continue;

			List<SignupAttendee> attendees = upToDateTimeslot.getAttendees();
			for (Iterator iter = attendees.iterator(); iter.hasNext();) {
				SignupAttendee att = (SignupAttendee) iter.next();
				if (attendeeUserId.equals(att.getAttendeeUserId())) {
					iter.remove();
					signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(att, upToDateTimeslot,
							SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, false);

					promoteAttendeeFromWaitingList(meeting, upToDateTimeslot);
					break;
				}
			}
		}
	}

	/**
	 * Get the SignupEventTrackingInfo object.
	 * 
	 * @return a SignupEventTrackingInfo object.
	 */
	public SignupEventTrackingInfo getSignupEventTrackingInfo() {
		return signupEventTrackingInfo;
	}

	/**
	 * This is a setter method.
	 * 
	 * @param signupEventTrackingInfo
	 *            a SignupEventTrackingInfo object.
	 */
	public void setSignupEventTrackingInfo(SignupEventTrackingInfo signupEventTrackingInfo) {
		this.signupEventTrackingInfo = signupEventTrackingInfo;
	}

}
