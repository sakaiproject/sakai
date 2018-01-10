/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.user.api.User;

/*
 * @author  Peter Liu
 */
@Slf4j
public class EmailDeliverer implements Runnable {

	private List<User> sakaiUsers;
	private final EmailService emailService;
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
			log.warn("The emails may not be sent away due to error: "
					+ e.getMessage());
		} finally {
			if(this.sakaiUsers !=null)
				this.sakaiUsers.clear();
			
			this.sakaiUsers = null;
		}
	}

}
