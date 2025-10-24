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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupTrackingItem;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer to notify attendee that he/she has been
 * swapped with the other one in an event/meeting
 * </p>
 */
public class SwapAttendeeEmail extends TransferEmailBase {

	private final User organizer;
	private final User attendee1;
    private final SignupTrackingItem item;
	private final String emailReturnSiteId;
	private final List<SignupTimeslot> removed;
	private final List<SignupTimeslot> added;

    /**
     * Handle notification when one attendee is swapped with another in an event/meeting.
     *
     * @param organizer   The user who organizes and manages the event/meeting
     * @param attendee1   The first user whose appointment is being swapped
     * @param item        The SignupTrackingItem containing details of the swap transaction
     * @param meeting     The SignupMeeting object representing the meeting being modified
     * @param sakaiFacade The SakaiFacade providing access to Sakai services and utilities
     */
	public SwapAttendeeEmail(User organizer, User attendee1, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.attendee1 = attendee1;
        this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
		removed = item.getRemovedFromTimeslot();
		added = Collections.singletonList(item.getAddToTimeslot());
	}

    @Override
	public List<String> getHeader() {
		List<String> rv = new ArrayList<>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + attendee1.getEmail());

		return rv;
	}

    @Override
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), makeFirstCapLetter(attendee1.getDisplayName())));

		Object[] params = new Object[] {
                makeFirstCapLetter(organizer.getDisplayName()),
				"'" + meeting.getTitle() + "'",
				getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate(),
				getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName()
        };
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizer.change.appointment.part"), params));

		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
					getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(), NEWLINE + StringUtils.SPACE
            };
			message.append(NEWLINE).append(NEWLINE).append(StringUtils.SPACE).append(MessageFormat.format(rb.getString("body.organizer.change.appointment.timeframe"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalShortDate(),
					getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
					getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalShortDate(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalShortDate(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalShortDate(),
                    NEWLINE + StringUtils.SPACE
            };
			message.append(NEWLINE).append(NEWLINE).append(StringUtils.SPACE).append(MessageFormat.format(rb.getString("body.organizer.change.appointment.crossdays.timeframe"), paramsTimeframe));
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
		return MessageFormat.format(rb.getString("subject.organizer.change.appointment.field"),
                getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate(),
                getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
                getAbbreviatedMeetingTitle());
	}
	
	@Override
	public List<SignupTimeslot> getRemoved() {
		return removed;
	}

	@Override
	public List<SignupTimeslot> getAdded() {
		return added;
	}
}
