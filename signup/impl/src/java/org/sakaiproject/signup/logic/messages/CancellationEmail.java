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
import java.util.Collections;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify attendee the
 * cancellation event
 * </p>
 */
public class CancellationEmail extends SignupEmailBase implements SignupTimeslotChanges {

	private final SignupTrackingItem item;

	private final User attendee;

	private String organizer;

	private String emailReturnSiteId;
	
	private List<SignupTimeslot> removed;

	/**
	 * constructor
	 * 
	 * @param attendee
	 *            an User, whose appointment has been cancelled in the
	 *            events/meeting
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
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
	 * constructor
	 * 
	 * @param organizer
	 *            an User, whose execute this action
	 * @param attendee
	 *            an User, whose appointment has been cancelled in the
	 *            events/meeting
	 * 
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
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

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getServerFromAddress());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"), new Object[] { makeFirstCapLetter(attendee.getDisplayName()) }));

		Object[] params = new Object[] { makeFirstCapLetter(getOrganizer()), getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(newline + newline + MessageFormat.format(rb.getString("body.organizerCancel.appointment.part"), params));
		message.append(newline + newline + MessageFormat.format(rb.getString("body.meetingTopic.part"), new Object[] { meeting.getTitle() }));
		message.append(newline + rb.getString("body.timeslot") + space);
		
		List<SignupTimeslot> removedFromTimeslots = item.getRemovedFromTimeslot();
		if (!removedFromTimeslots.isEmpty()) {
			for (SignupTimeslot timeslot : removedFromTimeslots) {
				if (!meeting.isMeetingCrossDays()) {
					Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
							getTime(timeslot.getEndTime()).toStringLocalTime(),
							getTime(timeslot.getStartTime()).toStringLocalDate(),
							getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
					message.append(MessageFormat.format(rb.getString("body.meeting.timeslot.timeframe"), paramsTimeframe) + newline);
				} else {
					Object[] paramsTimeframe = new Object[] { getTime(timeslot.getStartTime()).toStringLocalTime(),
							getTime(timeslot.getStartTime()).toStringLocalShortDate(),
							getTime(timeslot.getEndTime()).toStringLocalTime(),
							getTime(timeslot.getEndTime()).toStringLocalShortDate(),
							getSakaiFacade().getTimeService().getLocalTimeZone().getID()};
					message.append(MessageFormat.format(rb.getString("body.meeting.crossdays.timeslot.timeframe"), paramsTimeframe) + newline);

				}
			}
		}

		message.append(newline + newline + MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus.B"), new Object[] { getServiceName() }));

		/* footer */
		message.append(newline + getFooter(newline, this.emailReturnSiteId));
		return message.toString();
	}

	private String getOrganizer() {
		if (this.organizer == null || this.organizer.length() < 1)
			/* creator's name is default one */
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
		return MessageFormat.format(rb.getString("subject.Cancel.appointment.field"), new Object[] {
			getTime(meeting.getStartTime()).toStringLocalDate(), getSakaiFacade().getUserDisplayName(meeting.getCreatorUserId()), getAbbreviatedMeetingTitle() });
	}

	@Override
	public List<SignupTimeslot> getRemoved() {
		return removed;
	}

	@Override
	public List<SignupTimeslot> getAdded() {
		return Collections.EMPTY_LIST; //not applicable here
	}

    /**
     * {@inheritDoc}
     */
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
        List<VEvent> events = new ArrayList<VEvent>();
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
