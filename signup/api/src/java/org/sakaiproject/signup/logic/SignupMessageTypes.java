/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/SignupMessageTypes.java $
 * $Id: SignupMessageTypes.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
package org.sakaiproject.signup.logic;

/**
 * <P>
 * This interface defines the constants of message type for email purpose
 * </P>
 */
public interface SignupMessageTypes {

	final static String SIGNUP_NEW_MEETING = "signup.email.new.meeting";

	final static String SIGNUP_CANCEL_MEETING = "signup.email.cancel.meeting";

	static final String SIGNUP_PRE_ASSIGN = "signup.email.preassign";

	static final String SIGNUP_ORGANIZER_ADD = "signup.email.orgranizer.add";

	static final String SIGNUP_ORGANIZER_REMOVE = "signup.email.orgranizer.remove";

	static final String SIGNUP_ATTENDEE_SIGNUP = "signup.email.attendee.signup";

	static final String SIGNUP_ATTENDEE_SIGNUP_MOVE = "signup.email.attendee.move";

	static final String SIGNUP_ATTENDEE_SIGNUP_REPLACE = "signup.email.attendee.replace";

	static final String SIGNUP_ATTENDEE_SIGNUP_SWAP = "signup.email.attendee.swap";

	static final String SIGNUP_ATTENDEE_CANCEL = "signup.email.attendee.cancel";

	static final String SIGNUP_ATTENDEE_PROMOTE = "signup.email.attedee.promote";

	static final String SIGNUP_MEETING_MODIFIED = "signup.email.meeting.modified";
	
	//The following three are used to determine who should receive email
	static final String SEND_EMAIL_ALL_PARTICIPANTS = "all";
	
	static final String SEND_EMAIL_ONLY_SIGNED_UP_ATTENDEES = "signup_only";
	
	static final String SEND_EMAIL_ONLY_ORGANIZER_COORDINATORS = "organizers_only";

}
