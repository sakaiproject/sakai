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
package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by attendee of an event/meeting to notify orgainzer the
 * Signed-up event
 * </p>
 */
public class AttendeeSignupEmail extends SignupEmailBase {

	private final User currentUser;

	private final SignupMeeting meeting;

	private final SignupTimeslot timeslot;

	private final User creator;

	/**
	 * Constructor
	 * 
	 * @param creator
	 *            an User, who organizes the event/meeting
	 * @param currentUser
	 *            an User, who will join in the events
	 * @param signupMeeting
	 *            a SignupMeeting object
	 * @param timeslot
	 *            a SignupTimeslot object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public AttendeeSignupEmail(User creator, User currentUser, SignupMeeting signupMeeting, SignupTimeslot timeslot,
			SakaiFacade sakaiFacade) {
		this.creator = creator;
		this.currentUser = currentUser;
		this.meeting = signupMeeting;
		this.timeslot = timeslot;
		this.setSakaiFacade(sakaiFacade);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: "
				+ MessageFormat.format(rb.getString("subject.attendee.signup.field"), new Object[] {
						getTime(meeting.getStartTime()).toStringLocalDate(), currentUser.getDisplayName() }));
		rv.add("From: " + currentUser.getEmail());
		rv.add("To: " + creator.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"),
				new Object[] { makeFirstCapLetter(creator.getDisplayName()) }));

		Object[] params = new Object[] { makeFirstCapLetter(currentUser.getDisplayName()), getSiteTitleWithQuote(),
				getServiceName() };
		message.append(newline + newline + MessageFormat.format(rb.getString("body.attendee.hasSignup.part"), params));

		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
					getTime(timeslot.getEndTime()).toStringLocalTime(),
					getTime(timeslot.getStartTime()).toStringLocalDate() };
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.timeslot"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
					getTime(timeslot.getStartTime()).toStringLocalShortDate(),
					getTime(timeslot.getEndTime()).toStringLocalTime(),
					getTime(timeslot.getEndTime()).toStringLocalShortDate() };
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.crossdays.timeslot"), paramsTimeframe));

		}
		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}

}
