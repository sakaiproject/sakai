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

package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by the Signup tool to notify attendee that he/she has
 * promoted from waiting list
 * </p>
 */
public class PromoteAttendeeEmail extends AttendeeEmailBase {

	private final User attendee;

	private final SignupTrackingItem item;

	private String emailReturnSiteId;

	/**
	 * constructor
	 * 
	 * @param attendee
	 *            an User, who has promoted
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public PromoteAttendeeEmail(User attendee, SignupTrackingItem item, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
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
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"),
				new Object[] { makeFirstCapLetter(attendee.getDisplayName()) }));

		Object[] params = new Object[] { getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.assigned.promote.appointment.part"), params));

		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));

		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.timeslot"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalShortDate(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.crossdays.timeslot"), paramsTimeframe));

		}
		/* If you want more detail info, include the following block */
		/*
		 * if (getCancelledSlots() !=null){ message.append(getCancelledSlots()); }
		 */

		message.append(newline
				+ newline
				+ MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus.B"),
						new Object[] { getServiceName() }));
		/* footer */
		message.append(newline + getFooter(newline, this.emailReturnSiteId));
		return message.toString();
	}

	private String getCancelledSlots() {
		StringBuilder tmp = new StringBuilder();
		List<SignupTimeslot> rmList = item.getRemovedFromTimeslot();
		if (rmList != null || !rmList.isEmpty()) {
			tmp.append(newline + newline + rb.getString("body.cancelled.timeSlots"));
			for (SignupTimeslot rmSlot : rmList) {
				tmp.append(newline
						+ space
						+ space
						+ getSakaiFacade().getTimeService().newTime(rmSlot.getStartTime().getTime())
								.toStringLocalTime() + " - "
						+ getSakaiFacade().getTimeService().newTime(rmSlot.getEndTime().getTime()).toStringLocalTime());
			}
		}

		return tmp.length() < 1 ? null : tmp.toString();
	}
	
	@Override
	public String getFromAddress() {
		return getServerFromAddress();
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.promote.appointment.field"), new Object[] { getTime(
				meeting.getStartTime()).toStringLocalDate(), getAbbreviatedMeetingTitle() });
	}

}
