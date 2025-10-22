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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupCalendarHelper;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * Handles automated reminder email notifications for signup events.
 * This class extends SignupEmailBase to send reminder emails to attendees 
 * about their upcoming scheduled timeslots through a cron job.
 */
public class AutoReminderEmail extends SignupEmailBase {

	private final User attendee;
	private final SignupTimeslot item;
	private final String emailReturnSiteId;
	private SimpleDateFormat dateFormat;

    /**
     * Creates a new AutoReminderEmail instance.
     *
     * @param attendee
     *            an User, who joins in the event/meeting
     * @param item
     *            a SignupTrackingItem object
     * @param meeting
     *            a SignupMeeting object
     * @param sakaiFacade
     *            a SakaiFacade object
	 */
	public AutoReminderEmail(User attendee, SignupTimeslot item, SignupMeeting meeting, String attendeeSiteId, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = attendeeSiteId;
		dateFormat = new SimpleDateFormat("", rb.getLocale());
		dateFormat.setTimeZone(sakaiFacade.getTimeService().getLocalTimeZone());
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

        Object[] params = new Object[] {
                getSiteTitleWithQuote(this.emailReturnSiteId),
                getServiceName()
        };

        message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.auto.reminder.part"), params));
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.meetingTopic.part"), meeting.getTitle()));
		message.append(NEWLINE).append(rb.getString("body.meeting.place")).append(StringUtils.SPACE).append(meeting.getLocation());
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getStartTime()).toStringLocalTime(),
					getTime(item.getEndTime()).toStringLocalTime(),
					getTime(item.getStartTime()).toStringLocalDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.meeting.timeslot"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getStartTime()).toStringLocalTime(),
					getTime(item.getStartTime()).toStringLocalShortDate(),
					getTime(item.getEndTime()).toStringLocalTime(),
					getTime(item.getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.meeting.crossdays.timeslot"), paramsTimeframe));
		}
		
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.auto.reminder.check.meetingStatus"), getServiceName()));

		// footer
		message.append(NEWLINE).append(getFooter(NEWLINE, this.emailReturnSiteId));
		return message.toString();
	}
	
	private String getShortWeekDayName(Date date){
		dateFormat.applyLocalizedPattern("EEE");
		return dateFormat.format(date);
	}
	
	@Override
	public String getFromAddress() {
		return getServerFromAddress();
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.auto.reminder.appointment.field"),
                getShortWeekDayName(meeting.getStartTime()), getTime(meeting.getStartTime()).toStringLocalDate(),
                getTime(item.getStartTime()).toStringLocalTime(), getAbbreviatedMeetingTitle());
	}

    @Override
	public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
		// Handled by cron job, not yet implemented.
		return new ArrayList<>();
	}

}
