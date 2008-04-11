/**********************************************************************************
 * $URL: https://sakai21-dev.its.yale.edu/svn/signup/branches/2-5/impl/src/java/org/sakaiproject/signup/logic/messages/AttendeeCancellationEmail.java $
 * $Id: AttendeeCancellationEmail.java 2975 2008-04-11 16:01:29Z gl256 $
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
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by attendee of an event/meeting to notify orgainzer the
 * cancellation event
 * </p>
 */
public class AttendeeCancellationEmail extends SignupEmailBase {

	private final User organizer;

	private final User initiator;

	private final List<SignupTrackingItem> items;

	private final SignupMeeting meeting;

	/**
	 * Constructor
	 * 
	 * @param organizer
	 *            an User, who organizes the event/meeting
	 * @param initiator
	 *            an User, who initialize the events
	 * @param items
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public AttendeeCancellationEmail(User organizer, User initiator, List<SignupTrackingItem> items,
			SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.initiator = initiator;
		this.items = items;
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
		rv.add("Subject: " +rb.getString("subject.Cancel.appointment.A") + space
				+ getTime(meeting.getStartTime()).toStringLocalDate()
				+ rb.getString("subject.Cancel.appointment.B") + space
				+ initiator.getDisplayName());
		rv.add("From: " + initiator.getEmail());
		rv.add("To: " + organizer.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		SignupTrackingItem intiatorItem = null;
		for (SignupTrackingItem item : items) {
			if (item.isInitiator()) {
				intiatorItem = item;
				break;
			}
		}

		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(organizer.getDisplayName()) + "," + newline);
		message.append(newline + makeFirstCapLetter(initiator.getDisplayName()) + space + rb.getString("body.attendee.cancel.appointment"));
		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());
		message.append(newline + rb.getString("body.timeslot") + space );
		/* Currently, we only consider the first one */
		if (intiatorItem.getRemovedFromTimeslot() != null && !intiatorItem.getRemovedFromTimeslot().isEmpty())
			message.append(getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime()
					+ " - "
					+ getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime() + space
					+ rb.getString("body.on")+ space
					+ getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate());

		message.append(newline + getFooter(newline));
		return message.toString();
	}

}
