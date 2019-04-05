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

package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer to notify attendee that he/she has been
 * pre-assigned to an event/meeting
 * </p>
 */
public class OrganizerPreAssignEmail extends AttendeeEmailBase {

	private final User organizer;

	private final SignupTimeslot timeslot;

	private final User user;

	private final String emailReturnSiteId;

	/**
	 * Constructor.
	 * 
	 * @param currentUser
	 *            an User, who organizes the event/meeting.
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 * @param timeslot
	 *            a SignupTimeslot object.
	 * @param user
	 *            an User, who has been pre-assigned to an event/meeting.
	 * @param sakaiFacade
	 *            a SakaiFacade object.
	 * @param emailReturnSiteId
	 *            a unique SiteId string
	 */
	public OrganizerPreAssignEmail(User currentUser, SignupMeeting signupMeeting, SignupTimeslot timeslot, User user,
			SakaiFacade sakaiFacade, String emailReturnSiteId) {
		this.organizer = currentUser;
		this.meeting = signupMeeting;
		this.timeslot = timeslot;
		this.user = user;
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
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"),
				new Object[] { makeFirstCapLetter(user.getDisplayName()) }));
		Object[] params = new Object[] { getSiteTitleWithQuote(emailReturnSiteId), getServiceName(),
				organizer.getDisplayName() };
		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.organizerPreAssign.appointment.part"), params));
		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
					getTime(timeslot.getEndTime()).toStringLocalTime(),
					getTime(timeslot.getStartTime()).toStringLocalDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID() };
			message.append(newline
					+ MessageFormat.format(rb.getString("body.organizer.preassigned.attendee.meeting.timeframe"),
							paramsTimeframe));
		} else {
			Object[] paramsTimeframe1 = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
					getTime(timeslot.getStartTime()).toStringLocalShortDate(),
					getTime(timeslot.getEndTime()).toStringLocalTime(),
					getTime(timeslot.getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID() };
			message.append(newline
					+ MessageFormat.format(rb
							.getString("body.organizer.preassigned.attendee.meeting.crossdays.timeframe"),
							paramsTimeframe1));
		}

		message.append(newline + rb.getString("body.meeting.place") + space + meeting.getLocation());

		/* for recurring meeting */
		if (meeting.isRecurredMeeting()) {
			message.append(newline + rb.getString("body.meeting.recurrence") + space);
			String recurFrqs = getRepeatTypeMessage(meeting);

			Object[] paramsRecur = new Object[] { recurFrqs, getTime(meeting.getRepeatUntil()).toStringLocalDate() };
			message.append(MessageFormat.format(rb.getString("body.recurrence.meeting.status"), paramsRecur));

			if (meeting.isApplyToAllRecurMeetings())
				message.append(newline + newline + rb.getString("body.meeting.assigned.all.recurringMeetings"));
			else
				message.append(newline + newline + rb.getString("body.meeting.assigned.to.firstOne.recurringMeetings"));
		}

		message.append(newline + newline + meeting.getDescription());
		message.append(newline
				+ newline
				+ MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus"),
						new Object[] { getServiceName() }));

		message.append(newline + getFooter(newline, this.emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(organizer.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.organizerPreAssign.appointment.field"), new Object[] {
			organizer.getDisplayName(), getTime(meeting.getStartTime()).toStringLocalDate(), getAbbreviatedMeetingTitle() });
	}

}
