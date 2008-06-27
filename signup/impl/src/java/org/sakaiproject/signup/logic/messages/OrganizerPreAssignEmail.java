/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer to notify attendee that he/she has been
 * pre-assigned to an event/meeting
 * </p>
 */
public class OrganizerPreAssignEmail extends SignupEmailBase {

	private final User organizer;

	private final SignupMeeting meeting;

	private final SignupTimeslot timeslot;

	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param currentUser
	 *            an User, who organizes the event/meeting.
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 * @param timeslot
	 *            a SignupTimeslot object.
	 * @param user
	 *            an User, who has been pre-assigned to an event/meeting.
	 * @param sakaiFacade
	 *            a SakaiFacade object.
	 */
	public OrganizerPreAssignEmail(User currentUser, SignupMeeting signupMeeting, SignupTimeslot timeslot, User user,
			SakaiFacade sakaiFacade) {
		this.organizer = currentUser;
		this.meeting = signupMeeting;
		this.timeslot = timeslot;
		this.user = user;
		this.setSakaiFacade(sakaiFacade);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + rb.getString("subject.organizerPreAssign.appointment") + space + organizer.getDisplayName() + space + rb.getString("subject.word.on")+ space +getTime(meeting.getStartTime()).toStringLocalDate());
		rv.add("From: " + organizer.getEmail());
		rv.add("To: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(user.getDisplayName()) + "," + newline);
		message.append(newline + rb.getString("body.organizerPreAssign.appointment") + space + organizer.getDisplayName() + rb.getString("body.word.period"));
		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());
		if(!meeting.isMeetingCrossDays())
			message.append(newline + rb.getString("body.meeting.time") + space + getTime(timeslot.getStartTime()).toStringLocalTime() + " - "
				+ getTime(timeslot.getEndTime()).toStringLocalTime() + space + rb.getString("body.on") + space
				+ getTime(timeslot.getStartTime()).toStringLocalDate());
		else
			message.append(newline + rb.getString("body.meeting.time") + space + getTime(timeslot.getStartTime()).toStringLocalTime() +", " 
					+ getTime(timeslot.getStartTime()).toStringLocalShortDate() + space + " - " + space
					+ getTime(timeslot.getEndTime()).toStringLocalTime() + ", " + getTime(timeslot.getEndTime()).toStringLocalShortDate());
		
		message.append(newline + rb.getString("body.meeting.place") + space + meeting.getLocation());
		message.append(newline + newline + meeting.getDescription());
		message.append(newline + newline + rb.getString("body.attendeeCheck.meetingStatus"));
		
		message.append(newline + getFooter(newline));
		return message.toString();
	}

}
