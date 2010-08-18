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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.impl.jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class CheckValidations implements Job {


	private static Log log = LogFactory.getLog(CheckValidations.class);


	private ValidationLogic validationLogic;
	public void setValidationLogic(ValidationLogic vl) {
		validationLogic = vl;
	}

	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService uds) {
		userDirectoryService = uds;
	}
	
	

	private AuthzGroupService authzGroupService;	
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	
	
	private EmailTemplateService emailTemplateService;
	public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
		this.emailTemplateService = emailTemplateService;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -31);
		Date maxAge = cal.getTime();
		int maxAttempts =10;

		StringBuilder usedAccounts = new StringBuilder();
		List<String> oldAccounts = new ArrayList<String>();
		//we need sent and resent
		List<ValidationAccount> list = validationLogic.getValidationAccountsByStatus(ValidationAccount.STATUS_SENT);
		List<ValidationAccount> list2 = validationLogic.getValidationAccountsByStatus(ValidationAccount.STATUS_RESENT);

		if (list2 != null) {
			list.addAll(list2);
		}

		int loggedInAccounts = 0;
		int notLogedIn = 0;

		for (int i = 0; i < list.size(); i++) {
			ValidationAccount account = list.get(i);
			log.info("account " + account.getUserId() + " created on  " + account.getValidationSent());

			//has the user logged in - check for a authz realm
			try {
				AuthzGroup group = authzGroupService.getAuthzGroup("/site/~" + account.getUserId());
				log.info("looks like this user logged in!");
				loggedInAccounts++;

				
				if (account.getValidationSent().before(maxAge) && account.getValidationsSent().intValue() <= maxAttempts) {
					validationLogic.resendValidation(account.getValidationToken());
					usedAccounts.append(account.getUserId() + "\n");
				} else if (account.getValidationsSent().intValue() > maxAttempts) {
					//TODO What do we do in this case?
				}

			} catch (GroupNotDefinedException e) {

				log.info("realm: " + "/site/~" + account.getUserId() + " does not seem to exist");
				notLogedIn++;
				if (account.getValidationSent().before(maxAge)) {
					oldAccounts.add(account.getUserId());
				}
			}


		}
		log.info("users have logged in: " + loggedInAccounts + " not logged in: " + notLogedIn);
		log.info("we would delete: " + oldAccounts.size() + " accounts");
		log.info("users:" + usedAccounts.toString());
		
		//as potentially a user could have added lots of accounts we don't want to spam them
		Map<String, List<String>> addedMap = buildAddedMap(oldAccounts);
		
		//Ok now we have a map of each user and who they added
		Set<Entry<String,List<String>>> entrySet = addedMap.entrySet();
		Iterator<Entry<String,List<String>>> it = entrySet.iterator();
		while (it.hasNext()) {
			Entry<String,List<String>> entry = it.next();
			String creatorId = entry.getKey();
			try {
				User creator = userDirectoryService.getUser(creatorId);
				List<String> users = entry.getValue();
				StringBuilder userText = new StringBuilder();
				for (int i = 0; i < users.size(); i++) {
					User u = userDirectoryService.getUser(users.get(i));
					userText.append(u.getEid() + "\n");
					
					//TODO we need to remove the user from realms and delete the token and user
				}
				
				List<String> userReferences = new ArrayList<String>();
				userReferences.add(creator.getReference());
				
				Map<String, String> replacementValues = new HashMap<String, String>();
				replacementValues.put("userList", userText.toString());
				
				//now we send an email
				emailTemplateService.sendRenderedMessages("validation.deleted", userReferences, replacementValues, "help@vula.uct.ac.za", "Vula Help");
				
				
			} catch (UserNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

	private Map<String, List<String>> buildAddedMap(List<String> oldAccounts) {
		
		Map<String, List<String>> ret = new HashMap<String, List<String>>();
		
		for (int i =0; i < oldAccounts.size(); i++) {
			try {
				User u = userDirectoryService.getUser(oldAccounts.get(i));
				String createdBy = u.getCreatedBy().getId();
				if (ret.containsKey(createdBy)) {
					List<String> l = ret.get(createdBy);
					l.add(u.getId());
					ret.put(createdBy, l);
				} else {
					List<String> l = new ArrayList<String>();
					l.add(u.getId());
					ret.put(createdBy, l);
				}
			} catch (UserNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		return ret;
	}

}
