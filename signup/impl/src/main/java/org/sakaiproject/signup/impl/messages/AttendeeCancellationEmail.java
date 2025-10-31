/**
 * Copyright (c) 2007-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.impl.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupTrackingItem;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * Handles email notifications sent to event/meeting organizers when
 * an attendee cancels their attendance. This notification informs the organizer  
 * about which attendee cancelled and from which timeslot.
 */
public class AttendeeCancellationEmail extends OrganizerEmailBase {

	private final User organizer;
	private final User initiator;
	private final List<SignupTrackingItem> items;

    /**
     * Initialize attendee cancellation email object
     *
     * @param organizer   an User, who organizes the event/meeting
     * @param initiator   an User, who initialize the events
     * @param items       a SignupTrackingItem object
     * @param meeting     a SignupMeeting object
     * @param sakaiFacade a SakaiFacade object
	 */
	public AttendeeCancellationEmail(User organizer, User initiator, List<SignupTrackingItem> items, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.initiator = initiator;
		this.items = items;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
	}

    @Override
	public List<String> getHeader() {
		List<String> rv = new ArrayList<>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + organizer.getEmail());

		return rv;
	}

    @Override
	public String getMessage() {
		SignupTrackingItem intiatorItem = null;
		for (SignupTrackingItem item : items) {
			if (item.isInitiator()) {
				intiatorItem = item;
				break;
			}
		}

		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), makeFirstCapLetter(organizer.getDisplayName())));

		Object[] params = new Object[] { makeFirstCapLetter(initiator.getDisplayName()), getSiteTitleWithQuote(), getServiceName() };
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.cancel.appointment.part"), params));

		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		message.append(NEWLINE).append(rb.getString("body.timeslot")).append(StringUtils.SPACE);
		// Currently, we only consider the first one
		if (intiatorItem.getRemovedFromTimeslot() != null && !intiatorItem.getRemovedFromTimeslot().isEmpty())
			if (!meeting.isMeetingCrossDays()) {
				Object[] paramsTimeframe = new Object[] {
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate(),
						getSakaiFacade().getTimeService().getLocalTimeZone().getID()
                };
				message.append(MessageFormat.format(rb.getString("body.meeting.timeslot.timeframe"), paramsTimeframe));
			} else {
				Object[] paramsTimeframe = new Object[] {
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalShortDate(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
						getTime(intiatorItem.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalShortDate(),
						getSakaiFacade().getTimeService().getLocalTimeZone().getID()
                };
				message.append(MessageFormat.format(rb.getString("body.meeting.crossdays.timeslot.timeframe"), paramsTimeframe));
			}

		message.append(NEWLINE).append(getFooter(NEWLINE));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(initiator.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(
                rb.getString("subject.Cancel.appointment.field"),
                getTime(meeting.getStartTime()).toStringLocalDate(),
                initiator.getDisplayName(),
                getAbbreviatedMeetingTitle());
	}

}
