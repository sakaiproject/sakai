/**********************************************************************************
 * $URL: https://sakai-svn.its.yale.edu/svn/signup/branches/2-6-dev/impl/src/java/org/sakaiproject/signup/logic/messages/EmailDeliverer.java $
 * $Id: EmailDeliverer.java 4456 2009-09-28 20:51:21Z gl256 $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.user.api.User;

/*
 * @author  Peter Liu
 */

public class EmailDeliverer implements Runnable {

	private List<User> sakaiUsers;
	private final EmailService emailService;
	private Log logger = LogFactoryImpl.getLog(getClass());
	private final List<String> headers;
	private final String message;

	public EmailDeliverer(List<User> sakaiUsers, List<String> headers,
			String message, EmailService emailService) {
		this.sakaiUsers = sakaiUsers;
		this.headers = headers;
		this.message = message;
		this.emailService = emailService;
	}

	public void run() {
		try {
			emailService.sendToUsers(sakaiUsers, headers, message);
		} catch (Exception e) {
			logger.warn("The emails may not be sent away due to error: "
					+ e.getMessage());
		} finally {
			if(this.sakaiUsers !=null)
				this.sakaiUsers.clear();
			
			this.sakaiUsers = null;
		}
	}

}
