/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/restful/SignupParticipant.java $
 * $Id: SignupParticipant.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
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
 **********************************************************************************/
package org.sakaiproject.signup.restful;

/**
 * <p>
 * This class holds the information of sign-up attendee. It's a wrapper class
 * for RESTful case
 * </p>
 */
public class SignupParticipant {

	/* sakai user id */
	private String attendeeUserId;

	private String signupSiteId;

	private String comments;

	private String displayName;
	
	private boolean attended;

	

	/**
	 * Constructor
	 * 
	 */
	public SignupParticipant() {
	}

	/**
	 * This is a constructor
	 * 
	 * @param attendeeUserId
	 *            the internal user id (not username)
	 * @param signupSiteId
	 *            a unique id which represents the current site
	 */
	public SignupParticipant(String attendeeUserId, String signupSiteId) {
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	 * @return the attended
	 */
	public boolean isAttended() {
		return attended;
	}

	/**
	 * @param attended the attended to set
	 */
	public void setAttended(boolean attended) {
		this.attended = attended;
	}

}
