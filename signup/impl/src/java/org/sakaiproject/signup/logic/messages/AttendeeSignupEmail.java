/**********************************************************************************
 * $URL: https://sakai21-dev.its.yale.edu/svn/signup/branches/2-5/impl/src/java/org/sakaiproject/signup/logic/messages/AttendeeSignupEmail.java $
 * $Id: AttendeeSignupEmail.java 2975 2008-04-11 16:01:29Z gl256 $
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
		rv.add("Subject: " + rb.getString("subject.attendee.signup.A") 
				+ space + getTime(meeting.getStartTime()).toStringLocalDate() + space +  rb.getString("subject.attendee.signup.B") 
				+ space+ currentUser.getDisplayName());
		rv.add("From: " + currentUser.getEmail());
		rv.add("To: " + creator.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(creator.getDisplayName()) + "," + newline);
		message.append(newline + makeFirstCapLetter(currentUser.getDisplayName()) + space + rb.getString("body.attendee.hasSignup"));
		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());			
		message.append(newline + rb.getString("body.timeslot") + space
				+ getTime(timeslot.getStartTime()).toStringLocalTime() 
				+ " - "
				+ getTime(timeslot.getEndTime()).toStringLocalTime() + space + rb.getString("body.on") + space
				+ getTime(timeslot.getStartTime()).toStringLocalDate());
		
		/*footer*/
		message.append(newline + getFooter(newline));
		return message.toString();
	}

}
