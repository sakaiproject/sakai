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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;

@Slf4j
public class CheckValidations implements Job {

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

	
	private ServerConfigurationService serverConfigurationService;	
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	
	public SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	
	private PreferencesService preferencesService;	
	public void setPreferencesService(PreferencesService preferencesService) {
		this.preferencesService = preferencesService;
	}


	private int maxDays = 90;
	
	public void setMaxDays(int maxDays) {
		this.maxDays = maxDays;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		//set the user information into the current session
	    Session sakaiSession = sessionManager.getCurrentSession();
	    sakaiSession.setUserId("admin");
	    sakaiSession.setUserEid("admin");
		
		
		Calendar cal = new GregorianCalendar();
		// check the old property first
		String maxDaysLocalStr = serverConfigurationService.getString("accountValidator.maxDays", null);
		if (maxDaysLocalStr == null)
		{
			log.warn("accountValidator.maxDays was found. The new property is accountValidator.maxReminderDays");
		}
		// overwrite it with the new property if it exists, default to the old one
		maxDaysLocalStr = serverConfigurationService.getString("accountValidator.maxReminderDays", maxDaysLocalStr);
		if (maxDaysLocalStr == null)
		{
			// neither of the two properties are set, use the default
			maxDaysLocalStr = "" + maxDays;
		}
		try{
			maxDays = Integer.parseInt(maxDaysLocalStr);
		}catch (Exception e) {}
		cal.add(Calendar.DAY_OF_MONTH, (maxDays * -1));
		Date maxAge = cal.getTime();
		int maxAttempts =10;
		String maxAttemptsStr = serverConfigurationService.getString("accountValidator.maxResendAttempts", "" + maxAttempts);
		try{
			maxAttempts = Integer.parseInt(maxAttemptsStr);
		}catch (Exception e) {}
		
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
			log.debug("account " + account.getUserId() + " created on  " + account.getValidationSent());

			//has the user logged in - check for a authz realm


			String userSiteId = siteService.getUserSiteId(account.getUserId());
			if (siteService.siteExists(userSiteId)) {
				log.info("looks like this user logged in!");
				loggedInAccounts++;


				if (account.getValidationsSent().intValue() < maxAttempts
						&& serverConfigurationService.getBoolean(
								"accountValidator.resendValidations", true)) {
						validationLogic.resendValidation(account.getValidationToken());
				} else if (account.getValidationSent().before(maxAge) || account.getValidationsSent().intValue() >= maxAttempts) {
					account.setStatus(ValidationAccount.STATUS_EXPIRED);
					//set the received date so that it will create a new token the next time the user requests a reset
					cal = new GregorianCalendar();
					account.setvalidationReceived(cal.getTime());
					validationLogic.save(account);
				} 
				else if (validationLogic.isTokenExpired(account))
				{
					// Note: ^ isTokenExpired has the side effect of expiring tokens. We are doing this intentionally, so please do not remove this empty 'else if' block.
				} else {
					//TODO What do we do in this case?
				}
				usedAccounts.append(account.getUserId() + "\n");
			} else {
				//user has never logged in
				log.debug("realm: " + "/site/~" + account.getUserId() + " does not seem to exist");
				notLogedIn++;
				if (account.getValidationSent().before(maxAge)) {
					oldAccounts.add(account.getUserId());
				}
			}


		}
		log.info("users have logged in: " + loggedInAccounts + " not logged in: " + notLogedIn);
		log.info("we would delete: " + oldAccounts.size() + " accounts");
		if (log.isDebugEnabled()) {
			log.debug("users:" + usedAccounts.toString());
		}
		
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
				Locale locale = getUserLocale(creatorId);
				List<String> users = entry.getValue();
				StringBuilder userText = new StringBuilder();
				for (int i = 0; i < users.size(); i++) {
					try {
						User u = userDirectoryService.getUser(users.get(i));
						//added the added date 
						DateTime dt = new DateTime(u.getCreatedDate());
						DateTimeFormatter fmt = DateTimeFormat.longDate();
						String str = fmt.withLocale(locale).print(dt);
						userText.append(u.getEid() + " (" + str +")\n");

						removeCleaUpUser(u.getId());
					}
					catch (UserNotDefinedException e) {
						//this is an orphaned validation token
						ValidationAccount va = validationLogic.getVaLidationAcountByUserId(users.get(i));
						validationLogic.deleteValidationAccount(va);
					}
				}

				List<String> userReferences = new ArrayList<String>();
				userReferences.add(creator.getReference());
				
				
				
				Map<String, String> replacementValues = new HashMap<String, String>();
				replacementValues.put("userList", userText.toString());
				replacementValues.put("creatorName", creator.getDisplayName());
				replacementValues.put("deleteDays", Integer.valueOf(maxDays).toString());
				replacementValues.put("institution", serverConfigurationService.getString("ui.institution"));
				//now we send an email
				String fromEmail = serverConfigurationService.getString("accountValidator.checkValidations.fromEmailAddress", serverConfigurationService.getString("mail.support"));
				String fromName = serverConfigurationService.getString("accountValidator.checkValidations.fromEmailName", serverConfigurationService.getString("mail.support"));
				emailTemplateService.sendRenderedMessages("validate.deleted", userReferences, replacementValues, fromEmail, fromName);
				
				
			} catch (UserNotDefinedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void removeCleaUpUser(String id) {
		
		
		
		UserEdit user;
		try {
			Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(id), "site.visit", null);
			Iterator<String> it = groups.iterator();
			while (it.hasNext()) {
				AuthzGroup group = authzGroupService.getAuthzGroup(it.next());
				group.removeMember(id);
				authzGroupService.save(group);
			}
			
			
			user = userDirectoryService.editUser(id);
			userDirectoryService.removeUser(user);
			ValidationAccount va = validationLogic.getVaLidationAcountByUserId(id);
			validationLogic.deleteValidationAccount(va);
		} catch (UserNotDefinedException e) {
			log.error(e.getMessage(), e);
		} catch (UserPermissionException e) {
			log.error(e.getMessage(), e);
		} catch (UserLockedException e) {
			log.error(e.getMessage(), e);
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
		} catch (AuthzPermissionException e) {
			log.error(e.getMessage(), e);
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
				log.error(e.getMessage(), e);
			}
		}
		
		return ret;
	}

	 protected Locale getUserLocale(String userId) {
		   Locale loc = preferencesService.getLocale(userId);
		   //the user has no preference set - get the system default
		   if (loc == null ) {
			   String lang = System.getProperty("user.language");
			   String region = System.getProperty("user.region");

			   if (region != null) {
				   log.debug("getting system locale for: " + lang + "_" + region);
				   loc = new Locale(lang,region);
			   } else { 
				   log.debug("getting system locale for: " + lang );
				   loc = new Locale(lang);
			   }
		   }

		   return loc;
	   }
	
}
