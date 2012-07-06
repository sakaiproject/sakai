/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/impl/src/java/org/sakaiproject/signup/logic/messages/CancellationEmail.java $
 * $Id: CancellationEmail.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
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
import java.util.Collections;
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
public class CancellationEmail extends SignupEmailBase implements SignupTimeslotChanges {

	private final SignupTrackingItem item;

	private final User attendee;

	private String organizer;

	private String emailReturnSiteId;
	
	private List<SignupTimeslot> removed;

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
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
		
		removed = item.getRemovedFromTimeslot();
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
	public CancellationEmail(User organizer, User attendee, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.organizer = organizer.getDisplayName();
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
		
		removed = item.getRemovedFromTimeslot();

	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getServerFromAddress());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), new Object[] { makeFirstCapLetter(attendee.getDisplayName()) }));

		Object[] params = new Object[] { makeFirstCapLetter(getOrganizer()), getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(newline + newline + MessageFormat.format(rb.getString("body.organizerCancel.appointment.part"), params));
		message.append(newline + newline + MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		message.append(newline + rb.getString("body.timeslot") + space);
		
		List<SignupTimeslot> removedFromTimeslots = item.getRemovedFromTimeslot();
		if (!removedFromTimeslots.isEmpty()) {
			for (SignupTimeslot timeslot : removedFromTimeslots) {
				if (!meeting.isMeetingCrossDays()) {
					Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
							getTime(timeslot.getEndTime()).toStringLocalTime(),
							getTime(timeslot.getStartTime()).toStringLocalDate() };
					message.append(MessageFormat.format(rb.getString("body.meeting.timeslot.timeframe"), paramsTimeframe) + newline);
				} else {
					Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
							getTime(timeslot.getStartTime()).toStringLocalShortDate(),
							getTime(timeslot.getEndTime()).toStringLocalTime(),
							getTime(timeslot.getEndTime()).toStringLocalShortDate() };
					message.append(MessageFormat.format(rb.getString("body.meeting.crossdays.timeslot.timeframe"), paramsTimeframe) + newline);

				}
			}
		}

		message.append(newline + newline + MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus.B"), new Object[] { getServiceName() }));

		/* footer */
		message.append(newline + getFooter(newline, this.emailReturnSiteId));
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
	
	@Override
	public String getFromAddress() {
		return getServerFromAddress();
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.Cancel.appointment.field"), new Object[] {
			getTime(meeting.getStartTime()).toStringLocalDate(),
			getSakaiFacade().getUserDisplayName(meeting.getCreatorUserId()) });
	}

	@Override
	public List<SignupTimeslot> getRemoved() {
		return removed;
	}

	@Override
	public List<SignupTimeslot> getAdded() {
		return Collections.EMPTY_LIST; //not applicable here
	}
	

}
