/**********************************************************************************
 * $URL: https://sakai21-dev.its.yale.edu/svn/signup/branches/2-5/impl/src/java/org/sakaiproject/signup/logic/messages/ModifyMeetingEmail.java $
 * $Id: ModifyMeetingEmail.java 2975 2008-04-11 16:01:29Z gl256 $
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
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify participants
 * about the modification event
 * </p>
 */
public class ModifyMeetingEmail extends SignupEmailBase {

	private final SignupMeeting meeting;

	private final User organizer;

	/**
	 * Constructor
	 * 
	 * @param orgainzer
	 *            an User, who organizes the event/meeting
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public ModifyMeetingEmail(User orgainzer, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.organizer = orgainzer;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + rb.getString("subject.meeting.modification.A") + space + organizer.getDisplayName() + space
				+ rb.getString("subject.meeting.modification.B") + space
				+ getTime(meeting.getStartTime()).toStringLocalDate());
		rv.add("From: " + organizer.getEmail());
		rv.add("To: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(newline + rb.getString("body.organizerModified.meeting") + space + makeFirstCapLetter(organizer.getDisplayName()) + ":");
		
		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());
		message.append(newline + rb.getString("body.meeting.time") + space + getTime(meeting.getStartTime()).toStringLocalDate() + space + " -" + space + rb.getString("body.word.from") + space
				+ getTime(meeting.getStartTime()).toStringLocalTime() + space + rb.getString("body.word.to") + space
				+ getTime(meeting.getEndTime()).toStringLocalTime());
		message.append(newline + rb.getString("body.meeting.place") + space + meeting.getLocation());
		
		message.append(newline + newline + meeting.getDescription());
		message.append(newline + rb.getString("body.attendeeCheck.meetingStatus"));

		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}
}
