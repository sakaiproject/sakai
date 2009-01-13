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
package org.sakaiproject.signup.tool.jsf;

import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.util.PlainTextFormat;

/**
 * <p>
 * This class is a wrapper class for SignupAttendee for UI purpose
 * </P>
 */
public class AttendeeWrapper {

	private SignupAttendee signupAttendee;

	private String displayName;

	private int positionIndex = 0;

	private String timeslotPeriod;

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
		if (comment != null && comment.trim().length() > 0)
			return true;
		
		return false;
	}
}
