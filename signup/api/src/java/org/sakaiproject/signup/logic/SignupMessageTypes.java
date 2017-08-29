/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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

package org.sakaiproject.signup.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	
	static final String ATTENDEE_EDIT_COMMENT_NOTE = "send_email_to_notify_the_attendee";
	
	static final String ORGANIZER_EDIT_COMMENT_NOTE = "send_email_to_notify_the_organizers";
	
	//The following three are used to determine who should receive email
	static final String SEND_EMAIL_ALL_PARTICIPANTS = "all";
	
	static final String SEND_EMAIL_ONLY_SIGNED_UP_ATTENDEES = "signup_only";
	
	static final String SEND_EMAIL_ONLY_ORGANIZER_COORDINATORS = "organizers_only";

	//Valid values for selected people 
	static final Set<String> VALID_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY = new HashSet<String>(
			Arrays.asList(SEND_EMAIL_ALL_PARTICIPANTS,SEND_EMAIL_ONLY_SIGNED_UP_ATTENDEES,SEND_EMAIL_ONLY_ORGANIZER_COORDINATORS));


}
