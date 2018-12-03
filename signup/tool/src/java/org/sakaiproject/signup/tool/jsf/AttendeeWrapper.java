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

import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.tool.util.PlainTextFormat;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

import org.apache.commons.lang3.StringUtils;


/**
 * <p>
 * This class is a wrapper class for SignupAttendee for UI purpose
 * </P>
 */
public class AttendeeWrapper implements Comparable<AttendeeWrapper>{

	private SignupAttendee signupAttendee;

	private String displayName;

	private int positionIndex = 0;

	private String timeslotPeriod;
	
	private String attendeeEmail;

	/**
	 * Constructor
	 * 
	 * @param attendee
	 *            a SignupAttendee object.
	 * @param displayName
	 *            a user display name string value.
	 */
	public AttendeeWrapper(SignupAttendee attendee, String displayName) {
		this.signupAttendee = attendee;
		this.displayName = displayName;

	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a user display name string value.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * This is a setter.
	 * 
	 * @param displayName
	 *            a user display name string value.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * This is a getter method for UI.
	 * @return a SignupAttendee object.
	 */
	public SignupAttendee getSignupAttendee() {
		return signupAttendee;
	}

	public void setSignupAttendee(SignupAttendee signupAttendee) {
		this.signupAttendee = signupAttendee;
	}

	public int getPositionIndex() {
		return positionIndex;
	}

	public void setPositionIndex(int positionIndex) {
		this.positionIndex = positionIndex;
	}

	public String getTimeslotPeriod() {
		return timeslotPeriod;
	}

	public void setTimeslotPeriod(String timeslotPeriod) {
		this.timeslotPeriod = timeslotPeriod;
	}

	private static final int DISPLAY_LENGTH = 100;

	public String getCommentForTooltips() {
		String comment = this.signupAttendee.getComments();
		if (comment == null || comment.trim().length() < 1)
			return "No Comment";

		int totalLength = comment.length();
		int diplayLength = totalLength > DISPLAY_LENGTH ? DISPLAY_LENGTH : totalLength;
		comment = comment.substring(0, diplayLength) + (totalLength > DISPLAY_LENGTH ? "..." : "");
		return PlainTextFormat.convertFormattedHtmlTextToPlaintext(comment);
	}

	public boolean isComment(){
		String comment = this.signupAttendee.getComments();
		return StringUtils.isNotEmpty(comment);
	}
	
	/**
	 * @return the attended
	 */
	public boolean isAttended() {
		return this.signupAttendee.isAttended();
	}

	/**
	 * @param attended 
	 */
	public void setAttended(boolean attended) {
		this.signupAttendee.setAttended(attended);
	}
	
	/**
	 * @return the attendeeEmail
	 */
	public String getAttendeeEmail() {
		
		try
		{
			String userId= getSignupAttendee().getAttendeeUserId();
			User u = UserDirectoryService.getUser(userId);
			attendeeEmail = u.getEmail();
			if ((attendeeEmail != null) && (attendeeEmail.trim().length()) == 0) attendeeEmail = null;			
		}
		catch (UserNotDefinedException e)
		{
			attendeeEmail=null;
		}
		return attendeeEmail;
	}
	
	/**
	 * for sorting purpose. It's according to string alphabetic order. Last name
	 * comes first 
	 */
	@Override
	public int compareTo(AttendeeWrapper attendeeWrapperToCompare) {
		if (attendeeWrapperToCompare == null) {
			return -1;
		}
		if (displayName == null) {
			return -1;
		}
		int value = displayName.compareTo(attendeeWrapperToCompare.getDisplayName());
		if (value != 0) {
			return value;
		}
		return 0;
	}

}
