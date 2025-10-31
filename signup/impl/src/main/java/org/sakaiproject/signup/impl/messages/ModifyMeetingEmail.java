/**
 * Copyright (c) 2007-2015 The Apereo Foundation
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
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * A class used by event/meeting organizers to send email notifications to participants 
 * when an event has been modified. Extends AllUsersEmailBase to handle email 
 * distribution to all affected users.
 */
public class ModifyMeetingEmail extends AllUsersEmailBase {

	private final User organizer;
	private final String emailReturnSiteId;

    /**
     * @param orgainzer an User, who organizes the event/meeting
     * @param meeting a SignupMeeting object
     * @param sakaiFacade a SakaiFacade object
     * @param emailReturnSiteId a unique SiteId string
	 */
	public ModifyMeetingEmail(User orgainzer, SignupMeeting meeting, SakaiFacade sakaiFacade, String emailReturnSiteId) {
		this.organizer = orgainzer;
		this.meeting = meeting;
		this.emailReturnSiteId = emailReturnSiteId;
		this.setSakaiFacade(sakaiFacade);
	}

    @Override
	public List<String> getHeader() {
		List<String> rv = new ArrayList<>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + getSakaiFacade().getServerConfigurationService().getSmtpFrom());

		return rv;
	}

    @Override
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		Object[] params = new Object[] {
                getSiteTitleWithQuote(emailReturnSiteId),
                getServiceName(),
                makeFirstCapLetter(organizer.getDisplayName())
        };

		message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizerModified.meeting.field"), params));

		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.meetingTopic.part"), meeting.getTitle()));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
                    getTime(meeting.getStartTime()).toStringLocalDate(),
					getTime(meeting.getStartTime()).toStringLocalTime(),
					getTime(meeting.getEndTime()).toStringLocalTime(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()
            };
			message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizer.meeting.timeframe"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe1 = new Object[] {
                    getTime(meeting.getStartTime()).toStringLocalTime(),
					getTime(meeting.getStartTime()).toStringLocalShortDate(),
					getTime(meeting.getEndTime()).toStringLocalTime(),
					getTime(meeting.getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()
            };
			message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizer.meeting.crossdays.timeframe"), paramsTimeframe1));
		}

		message.append(NEWLINE).append(rb.getString("body.meeting.place")).append(StringUtils.SPACE).append(meeting.getLocation());

		if (meeting.getRecurrenceId() != null) {
			message.append(NEWLINE).append(NEWLINE).append("<b>Attention:</b>");
			String recurFrqs = getRepeatTypeMessage(meeting);

			Object[] paramsRecur = new Object[] {
                    recurFrqs,
                    getTime(meeting.getRepeatUntil()).toStringLocalDate()
            };

			message.append(NEWLINE).append("  - ").append(MessageFormat.format(rb.getString("body.recurrence.meeting.status"), paramsRecur));
			message.append(NEWLINE).append(StringUtils.SPACE).append(StringUtils.SPACE).append(rb.getString("body.meeting.recurrences.changed.note"));
			message.append(NEWLINE).append(StringUtils.SPACE).append(StringUtils.SPACE).append(rb.getString("body.meeting.recurrences.check.status.note"));
		}

		message.append(NEWLINE).append(NEWLINE).append(meeting.getDescription());
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus"), getServiceName()));

		// footer
		message.append(NEWLINE).append(getFooter(NEWLINE, emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(organizer.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.meeting.modification.field"),
                organizer.getDisplayName(),
                getShortSiteTitleWithQuote(emailReturnSiteId),
                getTime(meeting.getStartTime()).toStringLocalDate(),
                getTime(meeting.getStartTime()).toStringLocalTime(),
                getAbbreviatedMeetingTitle());
	}
}
