/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/messages/SignupEmailNotification.java $
 * $Id: SignupEmailNotification.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
package org.sakaiproject.signup.logic.messages;

import java.util.List;

import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;

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
	 * provide eamil message body
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

}
