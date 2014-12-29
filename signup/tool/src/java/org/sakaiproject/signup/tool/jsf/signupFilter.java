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
