/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer to notify all potential participants that an
 * event/meeting is created
 * </p>
 */
public class NewMeetingEmail extends SignupEmailBase {

	private final SignupMeeting meeting;

	private final User creator;

	/**
	 * constructor
	 * 
	 * @param creator
	 *            an User, who organizes the event/meeting
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public NewMeetingEmail(User creator, SignupMeeting meeting, SakaiFacade sakaiFacade) {
		this.creator = creator;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + rb.getString("subject.newMeeting") + space + creator.getDisplayName() + space
				+ rb.getString("subject.word.on") + " " + getTime(meeting.getStartTime()).toStringLocalDate());
		rv.add("From: " + creator.getEmail());
		rv.add("To: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append(newline + rb.getString("body.organizerCreate.meeting") + space  
				+ rb.getString("body.word.by") + space + makeFirstCapLetter(creator.getDisplayName()) + rb.getString("body.word.period"));

		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());
		message.append(newline + rb.getString("body.meeting.time") + space + getTime(meeting.getStartTime()).toStringLocalDate() + space + " -" + space + rb.getString("body.word.from") + space
				+ getTime(meeting.getStartTime()).toStringLocalTime() + space + rb.getString("body.word.to") + space
				+ getTime(meeting.getEndTime()).toStringLocalTime());
		message.append(newline + rb.getString("body.meeting.place") + space + meeting.getLocation());
		
		
		if (meeting.getMeetingType().equals(INDIVIDUAL))
			message.append(newline + newline + rb.getString("body.new.inidivual.type.message.A") + space
					+ meeting.getNoOfTimeSlots() + space + rb.getString("body.new.inidivual.type.message.B") + space
					+ getTimeSlotLength(meeting) + space + rb.getString("body.new.inidivual.type.message.C") +space
					+ meeting.getMaxNumberOfAttendees() + space + rb.getString("body.new.inidivual.type.message.D"));
		else if (meeting.getMeetingType().equals(ANNOUNCEMENT))
			message.append(newline + newline + rb.getString("body.new.announce.type.message"));
		else if (meeting.getMeetingType().equals(GROUP) && !isUnlimited(meeting))
			message.append(newline + newline + rb.getString("body.new.group.type.message.A") + space
					+ meeting.getMaxNumberOfAttendees() + space + rb.getString("body.new.inidivual.type.message.BB"));
		else
			message.append(newline + newline + rb.getString("body.new.group.type.message.AA"));
		
		message.append(newline + newline + meeting.getDescription());
		message.append(newline + newline + rb.getString("body.attendeeCheck.meetingStatus"));
		/*footer*/
		message.append(newline + getFooter(newline));

		return message.toString();
	}

	private int getTimeSlotLength(SignupMeeting meeting) {
		List signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots == null || signupTimeSlots.isEmpty())
			return 0;
		SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
		int duration = (int) (ts.getEndTime().getTime() - ts.getStartTime().getTime()) / (1000 * 60);
		return duration;
	}

	private boolean isUnlimited(SignupMeeting meeting) {

		List signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots == null || signupTimeSlots.isEmpty())
			return false;
		SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
		return ts.isUnlimitedAttendee();
	}
}
