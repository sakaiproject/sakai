/**
 * $Id$
 * $URL$
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.impl.jobs;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

@Slf4j
public class CheckAccountsJob implements Job {

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService uds) {
		userDirectoryService = uds;
	}

	private ValidationLogic validationLogic;
	public void setValidationLogic(ValidationLogic vl) {
		validationLogic = vl;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		try {
			//get all Guest Users
			List<User> users = userDirectoryService.getUsers();
		
			for (int i =0; i < users.size(); i++ ) {
				User u = (User)users.get(i);
				if ("guest".equals(u.getType())) {
					if (!validationLogic.isAccountValidated(u.getReference())){
						log.info("found unvalidated account: " + u.getEid() + "(" + u.getId() + ")");
						ValidationAccount va = validationLogic.createValidationAccount(u.getReference(), ValidationAccount.ACCOUNT_STATUS_LEGACY);
						log.info("sent validation token of " + va.getValidationToken());
					}
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
