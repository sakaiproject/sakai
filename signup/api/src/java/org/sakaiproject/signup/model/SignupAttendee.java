/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/model/SignupAttendee.java $
 * $Id: SignupAttendee.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
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
package org.sakaiproject.signup.model;

/**
 * <p>
 * This class holds the information for signup attendee. It's mapped directly to
 * the DB storage by Hibernate
 * </p>
 */
public class SignupAttendee {

	/* sakai user id */
	private String attendeeUserId;

	private String signupSiteId;

	private String comments;

	private String calendarEventId;

	private String calendarId;
	
	private String displayName;
	
	private boolean attended;

	
	/**
	 * Constructor
	 * 
	 */
	public SignupAttendee() {
	}

	/**
	 * This is a constructor
	 * 
	 * @param attendeeUserId
	 *            the internal user id (not username)
	 * @param signupSiteId
	 *            a unique id which represents the current site
	 */
	public SignupAttendee(String attendeeUserId, String signupSiteId) {
		this.attendeeUserId = attendeeUserId;
		this.signupSiteId = signupSiteId;
	}

	/**
	 * get the internal user id (not username)
	 */
	public String getAttendeeUserId() {
		return attendeeUserId;
	}

	/**
	 * this is a setter method and it set the internal user id (not username)
	 * 
	 * @param attendeeId
	 *            the internal user id (not username)
	 */
	public void setAttendeeUserId(String attendeeId) {
		this.attendeeUserId = attendeeId;
	}

	/**
	 * get the calendar event Id
	 * 
	 * @return a calendar event Id string
	 */
	public String getCalendarEventId() {
		return calendarEventId;
	}

	/**
	 * this is a setter
	 * 
	 * @param calendarEventId
	 *            a calendar event Id string
	 */
	public void setCalendarEventId(String calendarEventId) {
		this.calendarEventId = calendarEventId;
	}

	/**
	 * get Calendar Id
	 * 
	 * @return a Calendar Id
	 */
	public String getCalendarId() {
		return calendarId;
	}

	/**
	 * this is a setter.
	 * 
	 * @param calendarId
	 *            a Calendar Id
	 */
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	/**
	 * get the comments
	 * 
	 * @return a comment string
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * this is a setter.
	 * 
	 * @param comment
	 *            a comment by user
	 */
	public void setComments(String comment) {
		this.comments = comment;
	}
	
	/**
	 * isAttended
	 * 
	 * @return a boolean
	 */
	public boolean isAttended() {
		return attended;
	}

	/**
	 * this is a setter.
	 * 
	 * @param attended
	 *            boolean of attendance
	 */
	public void setAttended(boolean attended) {
		this.attended = attended;
	}

	/**
	 * get the site Id, which the attendee is in
	 * 
	 * @return a site Id
	 */
	public String getSignupSiteId() {
		return signupSiteId;
	}

	/**
	 * this is a setter.
	 * 
	 * @param signupSiteId
	 *            a site Id
	 */
	public void setSignupSiteId(String signupSiteId) {
		this.signupSiteId = signupSiteId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}	

}
