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

package org.sakaiproject.signup.impl.messages;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.user.api.User;


/**
 * An implementation of Runnable that handles asynchronous email delivery to Sakai users.
 * This class encapsulates the email sending functionality using the EmailService.
 */
@Slf4j
public class EmailDeliverer implements Runnable {

	private List<User> sakaiUsers;
	private final EmailService emailService;
	private final List<String> headers;
	private final String message;

    /**
     * Constructs a new EmailDeliverer with the specified parameters.
     *
     * @param sakaiUsers the list of Sakai users to receive the email
     * @param headers the email headers to be used
     * @param message the email message content
     * @param emailService the EmailService instance to send the emails
     */
    public EmailDeliverer(List<User> sakaiUsers, List<String> headers, String message, EmailService emailService) {
		this.sakaiUsers = sakaiUsers;
		this.headers = headers;
		this.message = message;
		this.emailService = emailService;
	}

    /**
     * Executes the email delivery process.
     * Sends emails to all specified users and cleans up resources afterward.
     * Any exceptions during sending are logged as warnings.
     */
    @Override
    public void run() {
		try {
			emailService.sendToUsers(sakaiUsers, headers, message);
		} catch (Exception e) {
			log.warn("The emails may not be sent away due to error: {}", e.toString());
		} finally {
            if (this.sakaiUsers != null) this.sakaiUsers.clear();
            this.sakaiUsers = null;
		}
	}

}
