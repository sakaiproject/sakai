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
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify newly added
 * attendee into the event/meeting
 * </p>
 */
public class AddAttendeeEmail extends SignupEmailBase {

	private final User organizer;

	private final User attendee;

	private final SignupTrackingItem item;

	private final SignupMeeting meeting;

	/**
	 * Constructor
	 * 
	 * @param organizer
	 *            an User, who organizes the event/meeting
	 * @param attendee
	 *            an User, who joins in the event/meeting
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public AddAttendeeEmail(User organizer, User attendee, SignupTrackingItem item, SignupMeeting meeting,
			SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.attendee = attendee;
		this.item = item;
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
		rv.add("Subject: " + rb.getString("subject.new.appointment.A") + space + organizer.getDisplayName() + space + rb.getString("subject.new.appointment.B")+ space +getTime(meeting.getStartTime()).toStringLocalDate());
		rv.add("From: " + organizer.getEmail());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(attendee.getDisplayName()) + "," + newline);
		message.append(newline + makeFirstCapLetter(organizer.getDisplayName()) + space + rb.getString("body.assigned.new.appointment"));
		message.append(newline + newline + rb.getString("body.meetingTopic")+ space + meeting.getTitle());	
			
		message.append(newline + rb.getString("body.timeslot") + space
				+ getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime() + " - "
				+ getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime() + space + rb.getString("body.on")
				+ space + getTime(item.getAddToTimeslot().getStartTime()).toStringLocalDate());
		
		message.append(newline + newline+ rb.getString("body.attendeeCheck.meetingStatus.B"));
		
		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}

}
