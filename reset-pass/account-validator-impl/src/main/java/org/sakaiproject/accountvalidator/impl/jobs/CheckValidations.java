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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.accountvalidator.api.model.ValidationAccount;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Slf4j
public class CheckValidations implements Job {

	@Setter private AccountValidationService avService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private AuthzGroupService authzGroupService;	
	@Setter private EmailTemplateService emailTemplateService;
	@Setter private ServerConfigurationService serverConfigurationService;	
	@Setter public SiteService siteService;
	@Setter private SessionManager sessionManager;
	@Setter private PreferencesService preferencesService;	

	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		//set the user information into the current session
	    Session sakaiSession = sessionManager.getCurrentSession();
	    sakaiSession.setUserId("admin");
	    sakaiSession.setUserEid("admin");

		// check the old property first
		String maxDaysConfig = serverConfigurationService.getString("accountValidator.maxDays");
		if (StringUtils.isNotBlank(maxDaysConfig)) {
			log.warn("accountValidator.maxDays was found. The new property is accountValidator.maxReminderDays");
		}
		// overwrite it with the new property if it exists, default to the old one
		maxDaysConfig = serverConfigurationService.getString("accountValidator.maxReminderDays", maxDaysConfig);
        int maxDays = NumberUtils.toInt(maxDaysConfig, 90);
        
		String maxAttemptsConfig = serverConfigurationService.getString("accountValidator.maxResendAttempts");
        int maxAttempts = NumberUtils.toInt(maxAttemptsConfig, 10);

		StringBuilder usedAccounts = new StringBuilder();
		List<String> oldAccounts = new ArrayList<>();
		// we need sent and resent
		List<ValidationAccount> sent = avService.getValidationAccountsByStatus(ValidationAccount.STATUS_SENT);
		List<ValidationAccount> resent = avService.getValidationAccountsByStatus(ValidationAccount.STATUS_RESENT);

		if (resent != null && !resent.isEmpty()) {
			sent.addAll(resent);
		}

        boolean resendValidations = serverConfigurationService.getBoolean("accountValidator.resendValidations", true);

        Instant maxAge = Instant.now().minus(maxDays, ChronoUnit.DAYS);
		int loggedInAccounts = 0;
		int notLoggedIn = 0;

        for (ValidationAccount account : sent) {
            log.debug("account {} created on {}", account.getUserId(), account.getValidationSent());

            String userSiteId = siteService.getUserSiteId(account.getUserId());
            if (siteService.siteExists(userSiteId)) {
                log.debug("looks like this user logged in!");
                loggedInAccounts++;

                if (resendValidations && account.getValidationsSent() < maxAttempts) {
                    // User logged in, token still valid, haven't hit max attempts -> resend
                    avService.resendValidation(account.getValidationToken());
                } else if (account.getValidationSent().isBefore(maxAge) || account.getValidationsSent() >= maxAttempts) {
                    // Token is too old OR hit max attempts -> expire it
                    account.setStatus(ValidationAccount.STATUS_EXPIRED);
                    account.setValidationReceived(Instant.now());
                    avService.save(account);
                } else if (avService.isTokenExpired(account)) {
                    // Token expired due to password reset timeout -> already handled by calling isTokenExpired()
                    log.debug("token expired for account {}", account);
                } else {
                    // What case gets here?
                    // - resendValidations is FALSE
                    // - validationsSent < maxAttempts
                    // - validation sent is AFTER maxAge (still fresh)
                    // - token is NOT expired
                    log.debug("User {} has logged in but resendValidations is disabled. Token status: {}, sent: {}",
                            account.getUserId(), account.getStatus(), account.getValidationsSent());
                }
                usedAccounts.append(account.getUserId()).append("\n");
            } else {
                // user has never logged in
                log.debug("realm: /site/~{} does not seem to exist", account.getUserId());
                notLoggedIn++;
                if (account.getValidationSent().isBefore(maxAge)) {
                    oldAccounts.add(account.getUserId());
                }
            }
        }
		log.info("users have logged in: {} not logged in: {}", loggedInAccounts, notLoggedIn);
		log.info("we would delete: {} accounts", oldAccounts.size());
        log.debug("users: {}", usedAccounts);
		
		// as potentially a user could have added lots of accounts, we don't want to spam them
		Map<String, List<String>> addedMap = buildAddedMap(oldAccounts);
		
		// now have a map of each user and who they added
		Set<Entry<String,List<String>>> entrySet = addedMap.entrySet();
        for (Entry<String, List<String>> entry : entrySet) {
            String creatorId = entry.getKey();
            try {
                User creator = userDirectoryService.getUser(creatorId);
                Locale locale = preferencesService.getLocale(creatorId);
                List<String> users = entry.getValue();
                StringBuilder userText = new StringBuilder();
                for (String user : users) {
                    try {
                        User u = userDirectoryService.getUser(user);
                        // added the added date 
                        DateTime dt = new DateTime(u.getCreatedDate());
                        DateTimeFormatter fmt = DateTimeFormat.longDate();
                        String str = fmt.withLocale(locale).print(dt);
                        userText.append(u.getEid()).append(" (").append(str).append(")\n");
                        removeCleanUpUser(u.getId());
                    } catch (UserNotDefinedException e) {
                        // this is an orphaned validation token
                        ValidationAccount va = avService.getValidationAccountByUserId(user);
                        avService.deleteValidationAccount(va);
                    }
                }

                List<String> userReferences = new ArrayList<>();
                userReferences.add(creator.getReference());


                Map<String, Object> replacementValues = new HashMap<>();
                replacementValues.put("userList", userText.toString());
                replacementValues.put("creatorName", creator.getDisplayName());
                replacementValues.put("deleteDays", Integer.valueOf(maxDays).toString());
                replacementValues.put("institution", serverConfigurationService.getString("ui.institution"));
                // now we send an email
                String fromEmail = serverConfigurationService.getSmtpFrom();
                emailTemplateService.sendRenderedMessages("validate.deleted", userReferences, replacementValues, fromEmail, fromEmail);
            } catch (UserNotDefinedException e) {
                log.warn(e.toString());
            }
        }
	}

	private void removeCleanUpUser(String id) {
		UserEdit user;
		try {
			Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(id), "site.visit", null);
            for (String group : groups) {
                AuthzGroup azGroup = authzGroupService.getAuthzGroup(group);
                azGroup.removeMember(id);
                authzGroupService.save(azGroup);
            }

            user = userDirectoryService.editUser(id);
            userDirectoryService.removeUser(user);
            ValidationAccount va = avService.getValidationAccountByUserId(id);
            avService.deleteValidationAccount(va);
        } catch (UserNotDefinedException | UserPermissionException | UserLockedException |
                 GroupNotDefinedException | AuthzPermissionException e) {
            log.warn(e.toString());
        }
	}

	private Map<String, List<String>> buildAddedMap(List<String> oldAccounts) {
		
		Map<String, List<String>> ret = new HashMap<>();

        for (String oldAccount : oldAccounts) {
            try {
                User u = userDirectoryService.getUser(oldAccount);
                String createdBy = u.getCreatedBy().getId();
                List<String> l;
                if (ret.containsKey(createdBy)) {
                    l = ret.get(createdBy);
                } else {
                    l = new ArrayList<>();
                }
                l.add(u.getId());
                ret.put(createdBy, l);
            } catch (UserNotDefinedException e) {
                log.warn(e.toString());
            }
        }
		
		return ret;
	}
}
