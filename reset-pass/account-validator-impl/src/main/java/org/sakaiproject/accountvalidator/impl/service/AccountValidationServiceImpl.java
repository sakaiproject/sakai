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
package org.sakaiproject.accountvalidator.impl.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.sakaiproject.accountvalidator.api.exception.ValidationException;
import org.sakaiproject.accountvalidator.api.model.ValidationAccount;
import org.sakaiproject.accountvalidator.api.repository.ValidationAccountRepository;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AccountValidationService using Spring's repository pattern.
 * This service handles all business logic for account validation operations.
 */
@Slf4j
@Transactional
public class AccountValidationServiceImpl implements AccountValidationService {

	private static final String TEMPLATE_KEY_EXISTING_USER = "validate.existinguser";
	private static final String TEMPLATE_KEY_NEW_USER = "validate.newUser";
	private static final String TEMPLATE_KEY_LEGACY_USER = "validate.legacyuser";
	private static final String TEMPLATE_KEY_PASSWORD_RESET = "validate.passwordreset";
	private static final String TEMPLATE_KEY_USERID_UPDATE = "validate.userId.update";
	private static final String TEMPLATE_KEY_DELETED = "validate.deleted";
	private static final String TEMPLATE_KEY_REQUEST_ACCOUNT = "validate.requestAccount";
	private static final String TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET = "acknowledge.passwordReset";

	private static final int VALIDATION_PERIOD_MONTHS = -36;

	private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
	private static final int MAX_PASSWORD_RESET_MINUTES_DEFAULT = 60;

	@Setter private IdManager idManager;
	@Setter private ValidationAccountRepository repository;
	@Setter private EmailTemplateService emailTemplateService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private AuthzGroupService authzGroupService;
	@Setter private SiteService siteService;
	@Setter private DeveloperHelperService developerHelperService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SecurityService securityService;
	@Setter private GroupProvider groupProvider;
	@Setter private ResourceLoader resourceLoader;

    public AccountValidationServiceImpl() {
        this.resourceLoader = new ResourceLoader();
    }

    public void init() {
		log.info("init()");

		// Need to populate the templates
		ClassLoader loader = AccountValidationServiceImpl.class.getClassLoader();
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_newUser.xml"), TEMPLATE_KEY_NEW_USER);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_existingUser.xml"), TEMPLATE_KEY_EXISTING_USER);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_legacyUser.xml"), TEMPLATE_KEY_LEGACY_USER);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_newPassword.xml"), TEMPLATE_KEY_PASSWORD_RESET);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_userIdUpdate.xml"), TEMPLATE_KEY_USERID_UPDATE);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_deleted.xml"), TEMPLATE_KEY_DELETED);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("validate_requestAccount.xml"), TEMPLATE_KEY_REQUEST_ACCOUNT);
		emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream("acknowledge_passwordReset.xml"), TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET);
	}

	@Override
	public ValidationAccount getValidationAccountById(Long id) {
		return repository.findById(id).orElse(null);
	}

	@Override
	public ValidationAccount getValidationAccountBytoken(String token) {
		return repository.findByValidationToken(token).orElse(null);
	}

	@Override
    public boolean isAccountValidated(String userId) {
        log.debug("validating {}", userId);

        ValidationAccount va = this.getValidationAccountByUserId(userId);
        if (va == null) {
            log.debug("no account found!");
            return false;
        }

        if (isTokenExpired(va)) {
            return false;
        }

        if (va.getValidationReceived() == null) {
            if (va.getValidationSent() != null) {
                Calendar cal = new GregorianCalendar();
                cal.add(Calendar.MONTH, VALIDATION_PERIOD_MONTHS); // 36 months ago
                Date validationDeadline = cal.getTime();

                if (va.getValidationSent().before(validationDeadline)) {
                    log.debug("validation sent but expired - no reply received within {} months", Math.abs(VALIDATION_PERIOD_MONTHS));
                } else {
                    log.debug("validation sent still awaiting reply");
                }
            } else {
                log.debug("validation sent date is null");
            }
            return false;
        }

        log.debug("got an item of status {}", va.getStatus());
        if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
            log.info("account is validated");
            return true;
        }

        log.debug("no conditions met assuming account is not validated");
        return false;
    }

    @Override
    public boolean isTokenExpired(ValidationAccount va) {
        if (va == null) {
            throw new IllegalArgumentException("null ValidationAccount passed to isTokenExpired");
        }

        // Check if the account is already marked as expired
        if (ValidationAccount.STATUS_EXPIRED.equals(va.getStatus())) {
            return true;
        }

        // Expiry validation only applies to validation tokens coming from reset-pass
        if (va.getAccountStatus() != null && va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET)) {
            if (va.getValidationSent() == null) {
                return false;
            }

            // Check if it's expired in relation to accountValidator.maxPasswordResetMinutes sakai property
            int minutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);

            // Get the time limit and convert to millis
            long maxMillis = minutes * 60L * 1000L;

            // The time when the validation was sent to the email server
            long sentTime = va.getValidationSent().getTime();

            // Check if token has expired
            if (System.currentTimeMillis() - sentTime > maxMillis) {
                // It's been too long, so invalidate the token and return
                va.setStatus(ValidationAccount.STATUS_EXPIRED);
                va.setValidationReceived(new Date());
                repository.save(va);
                return true;
            }
        }

        return false;
    }

	@Override
	public ValidationAccount getValidationAccountByUserId(String userId) {
		return repository.findByUserId(userId).orElse(null);
	}

	@Override
	public List<ValidationAccount> getValidationAccountsByStatus(Integer status) {
		return repository.findByStatus(status);
	}

	@Override
	public ValidationAccount createValidationAccount(String userRef) {
		return createValidationAccount(userRef, false);
	}

	@Override
	public ValidationAccount createValidationAccount(String userId, boolean newAccount) {
        return createValidationAccount(userId, newAccount ? ValidationAccount.ACCOUNT_STATUS_NEW : ValidationAccount.ACCOUNT_STATUS_EXISTING);
	}

	@Override
	public ValidationAccount createValidationAccount(String userRef, String newUserId) {
		ValidationAccount account = new ValidationAccount();
		account.setUserId(userRef);
		account.setValidationToken(idManager.createUuid());
		account.setValidationsSent(1);
		account.setAccountStatus(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE);
		if (StringUtils.isNotBlank(newUserId)) {
			account.setEid(newUserId);
		}
		sendEmailTemplate(account, newUserId);
		return saveValidationAccount(account);
	}

	@Override
	public ValidationAccount createValidationAccount(String userRef, Integer accountStatus) {
		log.debug("create validation account ref [{}], status [{}]", userRef, accountStatus);

		// TODO creating a new Validation should clear old ones for the user

		ValidationAccount v = new ValidationAccount();
		v.setUserId(userRef);
		v.setValidationToken(idManager.createUuid());
		v.setValidationsSent(1);

        v.setAccountStatus(accountStatus == null ? ValidationAccount.ACCOUNT_STATUS_NEW : accountStatus);
		sendEmailTemplate(v, null);

		return saveValidationAccount(v);
	}

	private String getFormattedExpirationMinutes() {
		int expirationMinutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);
		Period period = new Period(expirationMinutes * 60 * 1000L);
		PeriodFormatter periodFormatter = PeriodFormat.wordBased(resourceLoader.getLocale());
		return periodFormatter.print(period);
	}

	// Set other details for ValidationAccount and save
	private ValidationAccount saveValidationAccount(ValidationAccount account) {
		account.setValidationSent(new Date());
		account.setStatus(ValidationAccount.STATUS_SENT);
		String userId = EntityReference.getIdFromRef(account.getUserId());
		try {
			User u = userDirectoryService.getUser(userId);
            account.setFirstName(StringUtils.isNotBlank(u.getFirstName()) ? u.getFirstName() : null);
            account.setSurname(StringUtils.isNotBlank(u.getLastName()) ? u.getLastName() : null);
		} catch (UserNotDefinedException e) {
			log.warn("No User found for the id [{}], {}", userId, e.toString());
		}
		return repository.save(account);
	}

	/**
	 * The url to the account validation form varies according to your account status / accountValidator.sendLegacyLinks. This method determines which page to use.
	 * @param accountStatus - the accountStatus of the ValidationAccount
	 * @return a String representing the viewID of the page that should be specified in the URL
	 */
	public String getPageForAccountStatus(Integer accountStatus) {
		if (accountStatus == null) {
			log.warn("can't determine which account validation page to use - accountStatus is null. Returning the legacy 'validate'");
			return "validate";
		}

		if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT)) {
			return "requestAccount";
		}

		if (!serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false)) {
			if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET)) {
				return "passwordReset";
			} else {
				return "newUser";
			}
		}
		return "validate";
	}

    private String getTemplateKey(Integer accountStatus) {
        log.debug("retrieve template with account status [{}])", accountStatus);
        if (accountStatus == null) return TEMPLATE_KEY_NEW_USER;
        return switch (accountStatus) {
            case ValidationAccount.ACCOUNT_STATUS_EXISTING -> TEMPLATE_KEY_EXISTING_USER;
            case ValidationAccount.ACCOUNT_STATUS_LEGACY, ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS -> TEMPLATE_KEY_LEGACY_USER;
            case ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET -> TEMPLATE_KEY_PASSWORD_RESET;
            case ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE -> TEMPLATE_KEY_USERID_UPDATE;
            case ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT -> TEMPLATE_KEY_REQUEST_ACCOUNT;
            default -> TEMPLATE_KEY_NEW_USER;
        };
    }

	@Override
	public void mergeAccounts(String oldUserReference, String newUserReference) throws ValidationException {
		log.debug("merge account: {}, {})", oldUserReference, newUserReference);
		UserEdit olduser = null;

 		// We need a security advisor
 		SecurityAdvisor secAdvice = (String userId, String function, String reference) -> {
 			log.debug("isAllowed({}, {}, {})", userId, function, reference);
				if (UserDirectoryService.SECURE_UPDATE_USER_ANY.equals(function)) {
					return SecurityAdvice.ALLOWED;
				} else if (AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP.equals(function)) {
					return SecurityAdvice.ALLOWED;
				} else if (UserDirectoryService.SECURE_REMOVE_USER.equals(function)) {
					log.debug("advising user can delete users");
					return SecurityAdvice.ALLOWED;
				} else {
					return SecurityAdvice.NOT_ALLOWED;
				}
 		};

		try {
     		securityService.pushAdvisor(secAdvice);
 	    	log.debug("pushed security advisor: {}", secAdvice);

			String oldUserId = EntityReference.getIdFromRef(oldUserReference);
			String newuserId = EntityReference.getIdFromRef(newUserReference);

			olduser = userDirectoryService.editUser(oldUserId);

			// Get the old users realm memberships
			Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(
                    EntityReference.getIdFromRef(oldUserReference), "site.visit", null);

            for (String s : groups) {
                AuthzGroup group = authzGroupService.getAuthzGroup(s);
                Member member = group.getMember(oldUserId);
                // TODO we need to check if olduser and if so resolve the highest role
                Member exisiting = group.getMember(newuserId);
                String preferedRole = member.getRole().getId();

                // The groupProvider is optional it may not be set
                if (exisiting != null && groupProvider != null) {
                    preferedRole = groupProvider.preferredRole(preferedRole, exisiting.getRole().getId());
                }
                // Add the new user, but don't switch their role if they're already a member
                if (group.getMember(newuserId) == null) {
                    group.addMember(newuserId, preferedRole, true, false);
                }
                // Remove the old user
                group.removeMember(oldUserId);
                authzGroupService.save(group);
            }

			// Remove the old user
			userDirectoryService.removeUser(olduser);

		} catch (Exception e) {
            log.error("Failed to merge accounts old [{}], new [{}]", oldUserReference, newUserReference, e);
			if (olduser != null) {
				userDirectoryService.cancelEdit(olduser);
			}
            throw new ValidationException("Failed to merge accounts", e);
		} finally {
			SecurityAdvisor sa = securityService.popAdvisor(secAdvice);
			if (sa == null) {
				log.warn("Something cleared our advisor!");
			}
		}
	}

	@Override
	public void deleteValidationAccount(ValidationAccount toDelete) {
		repository.delete(toDelete);
	}

	@Override
	public void save(ValidationAccount toSave) {
        toSave.setFirstName(StringUtils.isNotBlank(toSave.getFirstName()) ? toSave.getFirstName() : null);
        toSave.setSurname(StringUtils.isNotBlank(toSave.getSurname()) ? toSave.getSurname() : null);
		repository.save(toSave);
	}

	@Override
	public void resendValidation(String token) {
		ValidationAccount account = this.getValidationAccountBytoken(token);

		if (account == null) {
			throw new IllegalArgumentException("no such account: " + token);
		}

		account.setValidationSent(new Date());
		account.setValidationsSent(account.getValidationsSent() + 1);
		account.setStatus(ValidationAccount.STATUS_RESENT);
		save(account);
		sendEmailTemplate(account, null);
	}

	private void sendEmailTemplate(ValidationAccount account, String newUserId) {
		// Now send the validation
		String userReference = userDirectoryService.userReference(account.getUserId());
		List<String> userIds = new ArrayList<>();
		List<String> emailAddresses = new ArrayList<>();
		Map<String, Object> replacementValues = new HashMap<>();
		replacementValues.put("validationToken", account.getValidationToken());
		// We want a direct tool url
		String page = getPageForAccountStatus(account.getAccountStatus());
		String serverUrl = serverConfigurationService.getServerUrl();
		String url = serverUrl + "/accountvalidator/" + page + "?tokenId=" + account.getValidationToken();

		replacementValues.put("expireTime", getFormattedExpirationMinutes());

		replacementValues.put("url", url);
		// Add some details about the user
		String userId = EntityReference.getIdFromRef(account.getUserId());
		userIds.add(userId);
		String userDisplayName;
		String userEid;

		try {
			User u = userDirectoryService.getUser(userId);

			userDisplayName = u.getDisplayName();

			userEid = u.getEid();
			// Information about the user that added them
			User added = u.getCreatedBy();
			replacementValues.put("addedBy", added.getDisplayName());
			replacementValues.put("addedByEmail", added.getEmail());
			if (StringUtils.isNotBlank(newUserId) && ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE == account.getAccountStatus()) {
				replacementValues.put("newUserId", newUserId);
				emailAddresses.add(newUserId);
			}
			replacementValues.put("displayName", userDisplayName);
			replacementValues.put("userEid", userEid);
			replacementValues.put("supportemail", serverConfigurationService.getString("mail.support"));
			replacementValues.put("institution", serverConfigurationService.getString("ui.institution"));

 	} catch (UserNotDefinedException e) {
 		log.error("No user with ID = {}", userId, e);
		}

 	// Information about the site(s) they have been added to
 	Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(userId, SiteService.SITE_VISIT, null);
 	log.debug("got a list of: {}", groups.size());
		Iterator<String> itg = groups.iterator();
		StringBuilder sb = new StringBuilder();
		int siteCount = 0;
		while (itg.hasNext()) {
			String groupRef = itg.next();
			String siteId = developerHelperService.getLocationIdFromRef(groupRef);
			try {
				Site s = siteService.getSite(siteId);
 			if (siteCount > 0) {
 				sb.append(", ");
 			}
 			log.debug("adding site: {}", s.getTitle());
				sb.append(s.getTitle());
				siteCount++;
 		} catch (IdUnusedException e) {
 			log.error("No site with id = {}", siteId, e);
			}
		}
		replacementValues.put("memberSites", sb.toString());

		String templateKey = getTemplateKey(account.getAccountStatus());
		RenderedTemplate renderedTemplate = emailTemplateService.getRenderedTemplateForUser(templateKey, userReference, replacementValues);
		emailTemplateService.sendMessage(
                userIds, emailAddresses, renderedTemplate, serverConfigurationService.getSmtpFrom(),
                serverConfigurationService.getString("mail.support.name", serverConfigurationService.getString("support.name")));
	}
}
