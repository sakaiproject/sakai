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

package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify participants
 * about the modification event
 * </p>
 */
public class ModifyMeetingEmail extends AllUsersEmailBase {

	private final User organizer;

	private final String emailReturnSiteId;

	/**
	 * Constructor
	 * 
	 * @param orgainzer
	 *            an User, who organizes the event/meeting
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 * @param emailReturnSiteId
	 *            a unique SiteId string
	 */
	public ModifyMeetingEmail(User orgainzer, SignupMeeting meeting, SakaiFacade sakaiFacade, String emailReturnSiteId) {
		this.organizer = orgainzer;
		this.meeting = meeting;
		this.emailReturnSiteId = emailReturnSiteId;
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
		rv.add("To: " + getSakaiFacade().getServerConfigurationService().getString("setup.request","no-reply@" + getSakaiFacade().getServerConfigurationService().getServerName()));

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		Object[] params = new Object[] { getSiteTitleWithQuote(emailReturnSiteId), getServiceName(),
				makeFirstCapLetter(organizer.getDisplayName()) };
		message.append(newline + MessageFormat.format(rb.getString("body.organizerModified.meeting.field"), params));

		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] { getTime(meeting.getStartTime()).toStringLocalDate(),
					getTime(meeting.getStartTime()).toStringLocalTime(),
					getTime(meeting.getEndTime()).toStringLocalTime(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(newline
					+ MessageFormat.format(rb.getString("body.organizer.meeting.timeframe"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe1 = new Object[] { getTime(meeting.getStartTime()).toStringLocalTime(),
					getTime(meeting.getStartTime()).toStringLocalShortDate(),
					getTime(meeting.getEndTime()).toStringLocalTime(),
					getTime(meeting.getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(newline
					+ MessageFormat
							.format(rb.getString("body.organizer.meeting.crossdays.timeframe"), paramsTimeframe1));
		}

		message.append(newline + rb.getString("body.meeting.place") + space + meeting.getLocation());

		if (meeting.getRecurrenceId() != null) {
			message.append(newline + newline + "<b>Attention:</b>");
			String recurFrqs = getRepeatTypeMessage(meeting);

			Object[] paramsRecur = new Object[] { recurFrqs, getTime(meeting.getRepeatUntil()).toStringLocalDate() };
			message.append(newline + "  - "
					+ MessageFormat.format(rb.getString("body.recurrence.meeting.status"), paramsRecur));

			message.append(newline + space + space + rb.getString("body.meeting.recurrences.changed.note"));

			message.append(newline + space + space + rb.getString("body.meeting.recurrences.check.status.note"));

		}

		message.append(newline + newline + meeting.getDescription());
		message.append(newline
				+ newline
				+ MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus"),
						new Object[] { getServiceName() }));

		/* footer */
		message.append(newline + getFooter(newline, emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(organizer.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.meeting.modification.field"), new Object[] {
			organizer.getDisplayName(), getShortSiteTitleWithQuote(emailReturnSiteId), getTime(meeting.getStartTime()).toStringLocalDate(),
			getTime(meeting.getStartTime()).toStringLocalTime(), getAbbreviatedMeetingTitle() });
	}
}
