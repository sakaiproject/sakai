/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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

package org.sakaiproject.signup.logic.messages;

import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * <P>
 * This is an interface for email
 * </P>
 */
public interface SignupEmailNotification {

	/**
	 * provide email header information
	 * 
	 * @return a list of email header information strings
	 */
	List<String> getHeader();

	/**
	 * provide email message body
	 * 
	 * @return a message body string
	 */
	String getMessage();
	
	/**
	 * Provide the from address. Cannot always rely on the 'From:' header.
	 * 
	 * @return the email address
	 */
	String getFromAddress();
	
	/**
	 * Provide the subject. Cannot always rely on the 'Subject:' header.
	 * 
	 * @return the subject
	 */
	String getSubject();
	
	/**
	 * Add ability to get SignupMeeting that is the parent of this email
	 * @return
	 */
	SignupMeeting getMeeting();	

	/**
	 * Generate a list of VEvent objects to be converted to ICal and
	 * attached to the email.
	 * @param user The User performing the action
	 * @return The list of events to be attached to this email
	 */
	List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper);

	/**
	 * Does this type of email represent a cancellation?
	 * @return true if it is a cancellation, false otherwise
	 */
	boolean isCancellation();
	
	/**
	 * this type means that it will not generate ICS file
	 * @return
	 */
	boolean isModifyComment();

}
