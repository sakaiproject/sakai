/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/tool/src/java/org/sakaiproject/signup/tool/jsf/signupFilter.java $
 * $Id: signupFilter.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either exsss or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.jsf;

import java.util.Date;
import java.util.List;

import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;

/**
 * *
 * <p>
 * This class will provide filter logic for Sign-up tool main page.
 * </P>
 * 
 * @author gl256
 * 
 */
public class signupFilter implements SignupBeanConstants {

	private final String currentUserId;

	private final String filterChoice;

	public signupFilter(String currentUserId, String filterChoice) {
		this.currentUserId = currentUserId;
		this.filterChoice = filterChoice;
	}

	/**
	 * Filter out the signupMeeting list according to the filter-choice.
	 * 
	 * @param sMeetings
	 *            a list of SignupMeeting objects
	 */
	public void filterSignupMeetings(List<SignupMeetingWrapper> sMeetingWrps) {
		if (VIEW_MY_SIGNED_UP.equals(filterChoice))
			getMySignedUpOnes(sMeetingWrps);
		else if (VIEW_IMMEDIATE_AVAIL.equals(filterChoice))
			getImmediateAvailOnes(sMeetingWrps);
	}

	private void getImmediateAvailOnes(List<SignupMeetingWrapper> sMeetingWrps) {
		if (sMeetingWrps != null && !sMeetingWrps.isEmpty()) {
			for (int i = sMeetingWrps.size(); i > 0; i--) {
				if ((new Date()).before(sMeetingWrps.get(i - 1).getMeeting().getSignupBegins())
						|| (new Date()).after(sMeetingWrps.get(i - 1).getMeeting().getSignupDeadline())) {
					sMeetingWrps.remove(i - 1);
				}
			}
		}

	}

	private void getMySignedUpOnes(List<SignupMeetingWrapper> sMeetingWrps) {

		if (sMeetingWrps != null && !sMeetingWrps.isEmpty()) {
			for (int i = sMeetingWrps.size(); i > 0; i--) {
				SignupMeetingWrapper wrpOne = sMeetingWrps.get(i - 1);
				List<SignupTimeslot> signupTimeSlots = wrpOne.getMeeting().getSignupTimeSlots();
				boolean found = false;
				for (SignupTimeslot timeslot : signupTimeSlots) {
					List<SignupAttendee> attendees = timeslot.getAttendees();
					for (SignupAttendee attendee : attendees) {
						if (attendee.getAttendeeUserId().equals(currentUserId)) {
							found = true;
							break;
						}
					}
					if (found) {
						/*
						 * set attendee's appointment time frame and set up for
						 * the first schedule if multiple exists
						 */
						if (!wrpOne.isShowMyAppointmentTimeFrame()) {
							wrpOne.setStartTime(timeslot.getStartTime());
							wrpOne.setEndTime(timeslot.getEndTime());
							wrpOne.setShowMyAppointmentTimeFrame(true);
						}
						break;
					}
				}

				if (!found) {
					sMeetingWrps.remove(i - 1);
				}

			}
		}

	}

}
