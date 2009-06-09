/**********************************************************************************
 * $URL$
 * $Id$
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
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify participants
 * about the modification event
 * </p>
 */
public class ModifyMeetingEmail extends SignupEmailBase {

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
		rv.add("Subject: "
				+ MessageFormat.format(rb.getString("subject.meeting.modification.field"), new Object[] {
						organizer.getDisplayName(), getTime(meeting.getStartTime()).toStringLocalDate() }));
		rv.add("From: " + organizer.getEmail());
		rv.add("To: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());

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
					getTime(meeting.getEndTime()).toStringLocalTime() };
			message.append(newline
					+ MessageFormat.format(rb.getString("body.organizer.meeting.timeframe"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe1 = new Object[] { getTime(meeting.getStartTime()).toStringLocalTime(),
					getTime(meeting.getStartTime()).toStringLocalShortDate(),
					getTime(meeting.getEndTime()).toStringLocalTime(),
					getTime(meeting.getEndTime()).toStringLocalShortDate() };
			message.append(newline
					+ MessageFormat
							.format(rb.getString("body.organizer.meeting.crossdays.timeframe"), paramsTimeframe1));
		}

		message.append(newline + rb.getString("body.meeting.place") + space + meeting.getLocation());

		if (meeting.getRecurrenceId() != null) {
			message.append(newline + newline + "<b>Attention:</b>");
			String recurFrqs = "";
			if (DAILY.equals(meeting.getRepeatType()))
				recurFrqs = rb.getString("body.meeting.repeatDaily");
			else if (WEEKLY.equals(meeting.getRepeatType()))
				recurFrqs = rb.getString("body.meeting.repeatWeekly");
			else if (BIWEEKLY.equals(meeting.getRepeatType()))
				recurFrqs = rb.getString("body.meeting.repeatBiWeekly");
			else
				recurFrqs = rb.getString("body.meeting.unknown.repeatType");

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
}
