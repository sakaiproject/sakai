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
package org.sakaiproject.signup.impl.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;

import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupCalendarHelper;
import org.sakaiproject.signup.api.messages.AttendeeComment;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.user.api.User;

public class AttendeeModifiedCommentEmail extends SignupEmailBase {

	private final User modifier;
	private final String emailReturnSiteId;
	private AttendeeComment attendeeComment;

    /**
     * @param modifier a User, who organizes the event/meeting
     * @param meeting a SignupMeeting object
     * @param sakaiFacade a SakaiFacade object
     * @param emailReturnSiteId a unique SiteId string
     */
	public AttendeeModifiedCommentEmail(User modifier, SignupMeeting meeting, SakaiFacade sakaiFacade, String emailReturnSiteId, AttendeeComment attendeeComment) {
		this.modifier = modifier;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = emailReturnSiteId;
		this.attendeeComment = attendeeComment;
		this.modifyComment = true;
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
				makeFirstCapLetter(modifier.getDisplayName()), getSakaiFacade().getUserDisplayLastFirstName(getSakaiFacade().getCurrentUserId())
        };
		message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizer.comment.update"), params));

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
		
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.comment"), makeFirstCapLetter(attendeeComment.getAttendeeComment())));
		
		// footer
		message.append(NEWLINE).append(getFooter(NEWLINE, emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return modifier.getEmail();
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(
                rb.getString("subject.comment.modification.field"),
                getShortSiteTitleWithQuote(emailReturnSiteId), modifier.getDisplayName(), getTime(meeting.getStartTime()).toStringLocalDate(),
                getTime(meeting.getStartTime()).toStringLocalTime());
	}

	@Override
	public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
		// do nothing
		return null;
	}
}
