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
 **********************************************************************************/
package org.sakaiproject.signup.logic.messages;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify attendee the
 * cancellation event
 * </p>
 */
public class CancellationEmail extends SignupEmailBase {

	private final SignupTrackingItem item;

	private final SignupMeeting meeting;

	private final User attendee;

	private String organizer;

	/**
	 * constructor
	 * 
	 * @param attendee
	 *            an User, whose appointment has been cancelled in the
	 *            events/meeting
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public CancellationEmail(User attendee, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);

	}

	/**
	 * constructor
	 * 
	 * @param organizer
	 *            an User, whose execute this action
	 * @param attendee
	 *            an User, whose appointment has been cancelled in the
	 *            events/meeting
	 * 
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public CancellationEmail(User organizer, User attendee, SignupTrackingItem item, SignupMeeting meeting,
			SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.organizer = organizer.getDisplayName();
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
		rv.add("Subject: " + rb.getString("subject.Cancel.appointment.A") + space
				+ getTime(meeting.getStartTime()).toStringLocalDate() + space
				+ rb.getString("subject.Cancel.appointment.B") + space
				+ getSakaiFacade().getUserDisplayName(meeting.getCreatorUserId()));
		rv.add("From: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(attendee.getDisplayName()) + "," + newline);
		message.append(newline + makeFirstCapLetter(getOrganizer()) + space
				+ rb.getString("body.organizerCancel.appointment"));
		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());
		message.append(newline + rb.getString("body.timeslot") + space);
		List<SignupTimeslot> removedFromTimeslots = item.getRemovedFromTimeslot();
		if (!removedFromTimeslots.isEmpty()) {
			for (SignupTimeslot timeslot : removedFromTimeslots) {
				message.append(getTime(timeslot.getStartTime()).toStringLocalTime() + " - "
						+ getTime(timeslot.getEndTime()).toStringLocalTime() + space + rb.getString("body.on") + space
						+ getTime(timeslot.getStartTime()).toStringLocalDate() + newline);
			}
		}

		message.append(newline + rb.getString("body.attendeeCheck.meetingStatus.B"));
		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}

	private String getOrganizer() {
		if (this.organizer == null || this.organizer.length() < 1)
			/* creator's name is default one */
			setOrganizer(getSakaiFacade().getUserDisplayName((meeting.getCreatorUserId())));

		return organizer;
	}

	private void setOrganizer(String organizer) {
		this.organizer = organizer;
	}

}
