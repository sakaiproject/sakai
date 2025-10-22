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

import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupCalendarHelper;
import org.sakaiproject.signup.api.SignupTrackingItem;
import org.sakaiproject.signup.api.messages.SignupTimeslotChanges;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * Class used to send email notifications to attendees when an event/meeting is cancelled.
 * Handles email formatting and delivery for both organizer-initiated and system cancellations.
 * Implements SignupTimeslotChanges to track which timeslots the attendee was removed from.
 */
public class CancellationEmail extends SignupEmailBase implements SignupTimeslotChanges {

	private final SignupTrackingItem item;
	private final User attendee;
	private String organizer;
	private final String emailReturnSiteId;
	private final List<SignupTimeslot> removed;

    /**
     * @param attendee an User, whose appointment has been cancelled in the events/meeting
     * @param item a SignupTrackingItem object
     * @param meeting a SignupMeeting object
     * @param sakaiFacade a SakaiFacade object
	 */
	public CancellationEmail(User attendee, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
		this.cancellation = true;

		removed = item.getRemovedFromTimeslot();
	}

    /**
     * @param organizer an User, whose execute this action
     * @param attendee an User, whose appointment has been cancelled in the events/meeting
     * @param item a SignupTrackingItem object
     * @param meeting a SignupMeeting object
     * @param sakaiFacade a SakaiFacade object
	 */
	public CancellationEmail(User organizer, User attendee, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.organizer = organizer.getDisplayName();
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
		this.cancellation = true;
		
		removed = item.getRemovedFromTimeslot();

	}

    @Override
	public List<String> getHeader() {
		List<String> rv = new ArrayList<>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getServerFromAddress());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

    @Override
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), makeFirstCapLetter(attendee.getDisplayName())));

		Object[] params = new Object[] { makeFirstCapLetter(getOrganizer()), getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizerCancel.appointment.part"), params));
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.meetingTopic.part"), meeting.getTitle()));
		message.append(NEWLINE).append(rb.getString("body.timeslot")).append(StringUtils.SPACE);
		
		List<SignupTimeslot> removedFromTimeslots = item.getRemovedFromTimeslot();
		if (!removedFromTimeslots.isEmpty()) {
			for (SignupTimeslot timeslot : removedFromTimeslots) {
				if (!meeting.isMeetingCrossDays()) {
					Object[] paramsTimeframe = new Object[] {
                            getTime(timeslot.getStartTime()).toStringLocalTime(),
							getTime(timeslot.getEndTime()).toStringLocalTime(),
							getTime(timeslot.getStartTime()).toStringLocalDate(),
							getSakaiFacade().getTimeService().getLocalTimeZone().getID()
                    };
					message.append(MessageFormat.format(rb.getString("body.meeting.timeslot.timeframe"), paramsTimeframe)).append(NEWLINE);
				} else {
					Object[] paramsTimeframe = new Object[] {
                            getTime(timeslot.getStartTime()).toStringLocalTime(),
							getTime(timeslot.getStartTime()).toStringLocalShortDate(),
							getTime(timeslot.getEndTime()).toStringLocalTime(),
							getTime(timeslot.getEndTime()).toStringLocalShortDate(),
							getSakaiFacade().getTimeService().getLocalTimeZone().getID()
                    };
					message.append(MessageFormat.format(rb.getString("body.meeting.crossdays.timeslot.timeframe"), paramsTimeframe)).append(NEWLINE);
				}
			}
		}

		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus.B"), getServiceName()));

		// footer
		message.append(NEWLINE).append(getFooter(NEWLINE, this.emailReturnSiteId));
		return message.toString();
	}

	private String getOrganizer() {
		if (this.organizer == null || this.organizer.isEmpty())
			// creator's name is default one
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
		return MessageFormat.format(rb.getString("subject.Cancel.appointment.field"),
                getTime(meeting.getStartTime()).toStringLocalDate(),
                getSakaiFacade().getUserDisplayName(meeting.getCreatorUserId()),
                getAbbreviatedMeetingTitle());
	}

	@Override
	public List<SignupTimeslot> getRemoved() {
		return removed;
	}

	@Override
	public List<SignupTimeslot> getAdded() {
		return Collections.EMPTY_LIST; // not applicable here
	}

    @Override
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
        List<VEvent> events = new ArrayList<>();
        for (SignupTimeslot timeslot : this.getRemoved()) {
            final VEvent event = timeslot.getVevent();
            if (event != null) {
                calendarHelper.cancelVEvent(event);
                events.add(event);
            }
        }
        return events;
    }

}
