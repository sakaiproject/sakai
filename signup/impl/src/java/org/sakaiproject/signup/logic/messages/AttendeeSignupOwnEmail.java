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
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used to notify attendee when they signup to a meeting themselves
 * </p>
 */
public class AttendeeSignupOwnEmail extends AttendeeEmailBase {

	private final User attendee;
	private final SignupTimeslot timeslot;

	/**
	 * Constructor
	 * 
	 * @param attendee User who just signed up
	 * @param signupMeeting the SignupMeeting they signed up to
	 * @param timeslot the SignupTimeslot they signed up to
	 * @param sakaiFacade a SakaiFacade object
	 */
	public AttendeeSignupOwnEmail(User attendee, SignupMeeting signupMeeting, SignupTimeslot timeslot, SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.meeting = signupMeeting;
		this.timeslot = timeslot;
		this.setSakaiFacade(sakaiFacade);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
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
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), new Object[] { makeFirstCapLetter(attendee.getDisplayName()) }));

		message.append(newline + newline + MessageFormat.format(rb.getString("body.attendee.signup.own"), new Object[] { getSiteTitleWithQuote(), getServiceName() }));

		message.append(newline + newline + MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
					getTime(timeslot.getEndTime()).toStringLocalTime(),
					getTime(timeslot.getStartTime()).toStringLocalDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.timeslot"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
					getTime(timeslot.getStartTime()).toStringLocalShortDate(),
					getTime(timeslot.getEndTime()).toStringLocalTime(),
					getTime(timeslot.getEndTime()).toStringLocalShortDate(),
					getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.crossdays.timeslot"), paramsTimeframe));

		}
		
		//attendee's comment
		if(timeslot.getAttendee(attendee.getId()) !=null && timeslot.getAttendee(attendee.getId()).getComments() !=null
				&& timeslot.getAttendee(attendee.getId()).getComments().length()> 0
				&& !"&nbsp;".equals(timeslot.getAttendee(attendee.getId()).getComments())){
			message.append(newline + newline + MessageFormat.format(rb.getString("body.ownComment"), new Object[] { 
					timeslot.getAttendee(attendee.getId()).getComments() }));
		}
		
		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return getServerFromAddress();
	}

	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.attendee.signup.own.field"), new Object[] { getAbbreviatedMeetingTitle(), getSiteTitle()});
	}
	
}
