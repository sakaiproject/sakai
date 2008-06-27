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
 * This class is used by organizer to notify attendee that he/she has been
 * swapped with the other one in an event/meeting
 * </p>
 */
public class SwapAttendeeEmail extends SignupEmailBase {

	private final User organizer;

	private final User attendee1;

	private final User attendee2;

	private final SignupMeeting meeting;

	private final SignupTrackingItem item;

	/**
	 * construtor
	 * 
	 * @param organizer
	 *            an User, who organizes the event/meeting
	 * @param attendee1
	 *            an User, whose appointment has been swapped
	 * @param attendee2
	 *            an User, whose appointment has been swapped
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public SwapAttendeeEmail(User organizer, User attendee1, User attendee2, SignupTrackingItem item,
			SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.attendee1 = attendee1;
		this.attendee2 = attendee2;
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
		rv.add("Subject: " + rb.getString("subject.organizer.change.appointment.A") + space
				+ getTime(meeting.getStartTime()).toStringLocalDate() + space
				+ getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime() + space
				+ rb.getString("subject.organizer.change.appointment.B"));
		rv.add("From: " + organizer.getEmail());
		rv.add("To: " + attendee1.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(attendee1.getDisplayName()) + "," + newline);
		message.append(newline + makeFirstCapLetter(organizer.getDisplayName()) + space + rb.getString("body.organizer.change.appointment")
				+ space + "'" + meeting.getTitle() + "'" + space + rb.getString("body.on") + space
				+ getTime(item.getAddToTimeslot().getStartTime()).toStringLocalDate());

		if(!meeting.isMeetingCrossDays()){
			message.append(newline + space + rb.getString("body.word.cap.from") + space
				+ getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime() + " - "
				+ getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime());
			message.append(newline + space + rb.getString("body.word.cap.to") + space
				+ getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime() + " - "
				+ getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime());
		}else{
			message.append(newline + space + rb.getString("body.word.cap.from") + space
				+ getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime() +", " +getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalShortDate()
				+ "  -  "
				+ getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime() + ", " + getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalShortDate());
			message.append(newline + space + rb.getString("body.word.cap.to") + space
				+ getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime() +", " + getTime(item.getAddToTimeslot().getStartTime()).toStringLocalShortDate() 
				+ "  -  "
				+ getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime() + ", " + getTime(item.getAddToTimeslot().getEndTime()).toStringLocalShortDate());
		}

		message.append(newline + newline + rb.getString("body.attendeeCheck.meetingStatus.B"));
		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}

}
