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
public class PromoteAttendeeEmail extends SignupEmailBase {

	private final User attendee;

	private final SignupTrackingItem item;

	private final SignupMeeting meeting;

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
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + rb.getString("subject.promote.appointment") + space
				+ getTime(meeting.getStartTime()).toStringLocalDate());
		rv.add("From: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(rb.getString("body.greeting") + space + makeFirstCapLetter(attendee.getDisplayName()) + "," + newline);
		message.append(newline + rb.getString("body.assigned.promote.appointment"));
		message.append(newline + newline + rb.getString("body.meetingTopic") + space + meeting.getTitle());
		message.append(newline + rb.getString("body.timeslot") + space + meeting.getTitle());

		if(!meeting.isMeetingCrossDays())
			message.append(newline + rb.getString("body.timeslot") + space
				+ getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime() + " - "
				+ getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime() + space + rb.getString("body.on")
				+ space + getTime(item.getAddToTimeslot().getStartTime()).toStringLocalDate());
		else
			message.append(newline + rb.getString("body.timeslot") + space
				+ getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime() + ", " + getTime(item.getAddToTimeslot().getStartTime()).toStringLocalShortDate()
				+ "  -  "
				+ getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime() + ", "
				+ getTime(item.getAddToTimeslot().getEndTime()).toStringLocalShortDate());

		/*If you want more detail info, include the following block*/
		/*if (getCancelledSlots() !=null){
			message.append(getCancelledSlots());
		}*/
		
		message.append(newline + newline + rb.getString("body.attendeeCheck.meetingStatus.B"));
		/* footer */
		message.append(newline + getFooter(newline));
		return message.toString();
	}
	
	private String getCancelledSlots(){
		StringBuilder tmp =new StringBuilder();
		List<SignupTimeslot> rmList = item.getRemovedFromTimeslot();
		if (rmList !=null || !rmList.isEmpty()){
			tmp.append(newline + newline + rb.getString("body.cancelled.timeSlots"));
			for (SignupTimeslot rmSlot : rmList) {
				tmp.append(newline + space + space +getSakaiFacade().getTimeService().newTime(rmSlot.getStartTime().getTime()).toStringLocalTime() 
						+ " - "
						+ getSakaiFacade().getTimeService().newTime(rmSlot.getEndTime().getTime()).toStringLocalTime()); 
			}
		}
		
		return tmp.length() < 1? null : tmp.toString();
	}

}
