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
 * <p>
 * This class is used by organizer of an event/meeting to notify newly added
 * attendee into the event/meeting
 * </p>
 */
public class AddAttendeeEmail extends AttendeeEmailBase {

	private final User organizer;
	private final User attendee;
	private final SignupTrackingItem item;
	private final String emailReturnSiteId;

    /**
     * Creates an email notification for a newly added attendee
     *
     * @param organizer the user who organizes the event/meeting
     * @param attendee the user who joins in the event/meeting
     * @param item contains tracking information for the signup action
     * @param meeting the signup meeting details
     * @param sakaiFacade provides access to Sakai services
	 */
	public AddAttendeeEmail(User organizer, User attendee, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
	}

    @Override
	public List<String> getHeader() {
		List<String> rv = new ArrayList<>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

    @Override
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), makeFirstCapLetter(attendee.getDisplayName())));

		Object[] params = new Object[] { makeFirstCapLetter(organizer.getDisplayName()),
				getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.assigned.new.appointment.part"), params));
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.meetingTopic.part"), meeting.getTitle()));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()
            };
			message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.meeting.timeslot"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalShortDate(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()
            };
			message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.meeting.crossdays.timeslot"), paramsTimeframe));
		}

		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus.B"), getServiceName()));

		// footer
		message.append(NEWLINE).append(getFooter(NEWLINE, this.emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(organizer.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.new.appointment.field"),
                organizer.getDisplayName(), getTime(meeting.getStartTime()).toStringLocalDate(),
                getAbbreviatedMeetingTitle());
	}

}
