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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by auto-reminder notification cronJob.
 * </p>
 */
public class AutoReminderEmail extends SignupEmailBase {

	private final User attendee;

	private final SignupTimeslot item;

	private String emailReturnSiteId;
	
	private SimpleDateFormat dateFormat;

	/**
	 * Constructor
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
	public AutoReminderEmail(User attendee, SignupTimeslot item, SignupMeeting meeting, String attendeeSiteId,
			SakaiFacade sakaiFacade) {
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = attendeeSiteId;
		dateFormat = new SimpleDateFormat("", rb.getLocale());
		dateFormat.setTimeZone(sakaiFacade.getTimeService().getLocalTimeZone());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: "
				+ MessageFormat.format(rb.getString("subject.auto.reminder.appointment.field"), new Object[] {getShortWeekDayName(meeting.getStartTime()), getTime(meeting.getStartTime()).toStringLocalDate() }));
		rv.add("From: " + getServiceName() +" <" + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName() + ">");
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
				+ MessageFormat.format(rb.getString("body.auto.reminder.part"), params));
		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getStartTime()).toStringLocalTime(),
					getTime(item.getEndTime()).toStringLocalTime(),
					getTime(item.getStartTime()).toStringLocalDate() };
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.timeslot"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getStartTime()).toStringLocalTime(),
					getTime(item.getStartTime()).toStringLocalShortDate(),
					getTime(item.getEndTime()).toStringLocalTime(),
					getTime(item.getEndTime()).toStringLocalShortDate() };
			message.append(newline
					+ MessageFormat.format(rb.getString("body.attendee.meeting.crossdays.timeslot"), paramsTimeframe));
		}

		message.append(newline
				+ newline
				+ MessageFormat.format(rb.getString("body.auto.reminder.check.meetingStatus"),
						new Object[] { getServiceName() }));

		/* footer */
		message.append(newline + getFooter(newline, this.emailReturnSiteId));
		return message.toString();
	}
	
	private String getShortWeekDayName(Date date){
		dateFormat.applyLocalizedPattern("EEE");
		return dateFormat.format(date);
	}

}
