/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/impl/src/java/org/sakaiproject/signup/logic/messages/AttendeeCancellationEmail.java $
 * $Id: AttendeeCancellationEmail.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
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
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
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
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"),
				new Object[] { makeFirstCapLetter(organizer.getDisplayName()) }));

		Object[] params = new Object[] { makeFirstCapLetter(initiator.getDisplayName()), getSiteTitleWithQuote(),
				getServiceName() };
		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.attendee.cancel.appointment.part"), params));

		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		message.append(newline + rb.getString("body.timeslot") + space);
		/* Currently, we only consider the first one */
		if (intiatorItem.getRemovedFromTimeslot() != null && !intiatorItem.getRemovedFromTimeslot().isEmpty())
			if (!meeting.isMeetingCrossDays()) {
				Object[] paramsTimeframe = new Object[] {
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate() };
				message.append(MessageFormat.format(rb.getString("body.meeting.timeslot.timeframe"), paramsTimeframe));
			} else {
				Object[] paramsTimeframe = new Object[] {
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalShortDate(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalShortDate() };
				message.append(MessageFormat.format(rb.getString("body.meeting.crossdays.timeslot.timeframe"),
						paramsTimeframe));
			}

		message.append(newline + getFooter(newline));

		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return initiator.getEmail();
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.Cancel.appointment.field"), new Object[] {
			getTime(meeting.getStartTime()).toStringLocalDate(), initiator.getDisplayName() });
	}

}
