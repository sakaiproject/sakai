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
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * Email notification class that handles sending messages to all potential participants 
 * when a new event/meeting is created in the system. The class extends AllUsersEmailBase
 * to provide the base email functionality.
 *
 * <p>This class constructs and formats email notifications containing:
 * <ul>
 *   <li>Meeting details (title, time, location)</li>
 *   <li>Recurrence information for recurring meetings</li>
 *   <li>Time slot details for custom meetings</li>
 *   <li>Participant limits and restrictions</li>
 * </ul>
 *
 * <p>The email content is formatted in HTML and includes customized messaging based on
 * the meeting type (individual, announcement, group etc.).
 */
public class NewMeetingEmail extends AllUsersEmailBase {

	private final User creator;
	private final String emailReturnSiteId;

    /**
     * Creating a new meeting email notification
     *
     * @param creator The user who is organizing/creating the event/meeting
     * @param meeting The SignupMeeting object containing the meeting details
     * @param sakaiFacade The SakaiFacade providing access to Sakai services
     * @param emailReturnSiteId The site ID where the meeting is being created, used for return email address
	 */
	public NewMeetingEmail(User creator, SignupMeeting meeting, SakaiFacade sakaiFacade, String emailReturnSiteId) {
		this.creator = creator;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = emailReturnSiteId;
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
				makeFirstCapLetter(creator.getDisplayName())
        };
		message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizerCreate.meeting.announ"), params));

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

		// for recurring meeting
		if (meeting.isRecurredMeeting()) {
			message.append(NEWLINE).append(rb.getString("body.meeting.recurrence")).append(StringUtils.SPACE);
			String recurFrqs = getRepeatTypeMessage(meeting);

			Object[] paramsRecur = new Object[] {
                    recurFrqs,
                    getTime(meeting.getRepeatUntil()).toStringLocalDate()
            };
			message.append(MessageFormat.format(rb.getString("body.recurrence.meeting.status"), paramsRecur));
		}

        if (meeting.getMeetingType().equals(CUSTOM_TIMESLOTS)) {
            List<SignupTimeslot> tsList = meeting.getSignupTimeSlots();
            message.append(NEWLINE).append(NEWLINE).append(rb.getString("body.meeting.timeslot.detail.title"));
            if (tsList != null) {
                int i = 1;
                for (SignupTimeslot ts : tsList) {
                    if (!meeting.isMeetingCrossDays()) {
                        Object[] oneTsDateParam = new Object[] {
                                getTime(ts.getStartTime()).toStringLocalTime(),
                                getTime(ts.getEndTime()).toStringLocalTime()
                        };
                        message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.custom.defined.meeting.timeslot"), i));
                        message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.custom.defined.meeting.timeslot.timeframe"), oneTsDateParam));
                        message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.custom.defined.meeting.timeslot.max.participants"), ts.getMaxNoOfAttendees()));
                        i++;
                    } else {
                        Object[] oneTsDateParam = new Object[] {
                                getTime(ts.getStartTime()).toStringLocalTime(),
                                getTime(ts.getStartTime()).toStringLocalShortDate(),
                                getTime(ts.getEndTime()).toStringLocalTime(),
                                getTime(ts.getEndTime()).toStringLocalShortDate()
                        };
                        message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.custom.defined.meeting.timeslot"), i));
                        message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.custom.defined.meeting.timeslot.timeframe.crossdays"), oneTsDateParam));
                        message.append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendee.custom.defined.meeting.timeslot.max.participants"), ts.getMaxNoOfAttendees()));
                        i++;
                    }
                }
            }
        } else if (meeting.getMeetingType().equals(INDIVIDUAL)) {
            Object[] params2 = new Object[] {
                    meeting.getNoOfTimeSlots(),
                    getTimeSlotLength(meeting),
                    meeting.getMaxNumberOfAttendees()
            };
            message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.new.inidivual.type.message.detail"), params2));
        } else if (meeting.getMeetingType().equals(ANNOUNCEMENT)) {
            message.append(NEWLINE).append(NEWLINE).append(rb.getString("body.new.announce.type.message"));
        } else if (meeting.getMeetingType().equals(GROUP) && !isUnlimited(meeting)) {
            Object[] params3 = new Object[] {
                    meeting.getMaxNumberOfAttendees()
            };
            message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.new.group.limited.type.message.detail"), params3));
        } else {
            message.append(NEWLINE).append(NEWLINE).append(rb.getString("body.new.group.unlimited.type.message"));
        }

        message.append(NEWLINE).append(NEWLINE).append(meeting.getDescription());
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus"), getServiceName()));

		// footer
		message.append(NEWLINE).append(getFooter(NEWLINE, emailReturnSiteId));
		return message.toString();
	}

	private int getTimeSlotLength(SignupMeeting meeting) {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots == null || signupTimeSlots.isEmpty()) return 0;
		SignupTimeslot ts = signupTimeSlots.get(0);
        return (int) (ts.getEndTime().getTime() - ts.getStartTime().getTime()) / (1000 * 60);
	}

	private boolean isUnlimited(SignupMeeting meeting) {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots == null || signupTimeSlots.isEmpty()) return false;
		SignupTimeslot ts = signupTimeSlots.get(0);
		return ts.isUnlimitedAttendee();
	}

	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(creator.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(
                rb.getString("subject.newMeeting.field"),
                creator.getDisplayName(),
                getTime(meeting.getStartTime()).toStringLocalDate(),
                getAbbreviatedMeetingTitle());
	}
}
