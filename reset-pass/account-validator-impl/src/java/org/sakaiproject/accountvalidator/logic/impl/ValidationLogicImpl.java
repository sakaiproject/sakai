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

package org.sakaiproject.accountvalidator.logic.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sakaiproject.accountvalidator.logic.ValidationException;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.logic.dao.ValidationDao;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;



public class ValidationLogicImpl implements ValidationLogic {


	private static final String TEMPLATE_KEY_EXISTINGUSER = "validate.existinguser";
	private static final String TEMPLATE_KEY_NEW_USER = "validate.newUser";
	private static final String TEMPLATE_KEY_LEGACYUSER = "validate.legacyuser";
	private static final String TEMPLATE_KEY_PASSWORDRESET = "validate.passwordreset";
	private static final String TEMPLATE_KEY_USERIDUPDATE = "validate.userId.update";
	private static final String TEMPLATE_KEY_DELETED = "validate.deleted";
	private static final String TEMPLATE_KEY_REQUEST_ACCOUNT = "validate.requestAccount";
	private static final String TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET = "acknowledge.passwordReset";
	
	private static final int VALIDATION_PERIOD_MONTHS = -36;
	private static Log log = LogFactory.getLog(ValidationLogicImpl.class);
	
	private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
	
	public void init(){
		log.info("init()");
		//need to populate the templates
		loadTemplate("validate_newUser.xml", TEMPLATE_KEY_NEW_USER);
		loadTemplate("validate_existingUser.xml", TEMPLATE_KEY_EXISTINGUSER);
		loadTemplate("validate_legacyUser.xml", TEMPLATE_KEY_LEGACYUSER);
		loadTemplate("validate_newPassword.xml", TEMPLATE_KEY_PASSWORDRESET);
		loadTemplate("validate_userIdUpdate.xml", TEMPLATE_KEY_USERIDUPDATE);
		loadTemplate("validate_deleted.xml", TEMPLATE_KEY_DELETED);
		loadTemplate("validate_requestAccount.xml", TEMPLATE_KEY_REQUEST_ACCOUNT);
		loadTemplate("acknowledge_passwordReset.xml", TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET);
		
		//seeing the GroupProvider is optional we need to load it here
		if (groupProvider == null) {
			groupProvider = (GroupProvider) ComponentManager.get(GroupProvider.class.getName());
		}
	}
	
	/**
	* Load and register one or more email templates (contained in the given
	* .xml file) with the email template service
	*
	* @param fileName - the name of the .xml file to load
	* @param templateKey - the key (name) of the template to be saved to the service
	*/
	private void loadTemplate(String fileName, String templateKey) {
		//Create the SecurityAdvisor (elevated permissions needed to use EmailTemplateService)
		org.sakaiproject.authz.api.SecurityService securityService = (org.sakaiproject.authz.api.SecurityService) ComponentManager.get( org.sakaiproject.authz.api.SecurityService.class );
		SecurityAdvisor yesMan = new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed( String userId, String function, String reference )
			{
				return SecurityAdvice.ALLOWED;
			}
		};

		try
		{
			// Push the yesMan SA on the stack and perform the necessary actions
			securityService.pushAdvisor( yesMan );

			// Load up the resource as an input stream
			InputStream input = ValidationLogicImpl.class.getClassLoader().getResourceAsStream( fileName );
			if ( input == null )
			{
				log.error( "Could not load resource from '" + fileName + "'. Skipping ..." );
			}
			else
			{
				// Parse the XML, get all the child templates
				Document document = new SAXBuilder().build( input );
				List<?> childTemplates = document.getRootElement().getChildren( "emailTemplate" );
				Iterator<?> iter = childTemplates.iterator();

				// Create and register a template with the service for each one found in the XML file
				while ( iter.hasNext() )
				{
					xmlToTemplate( (Element) iter.next(), templateKey );
				}
			}
		}
		catch ( JDOMException e ) 
		{ 
			log.error( e.getMessage(), e); 
		}
		catch ( IOException e )
		{
			log.error( e.getMessage(), e);
		}

		// Pop the yesMan SA off the stack (remove elevated permissions)
		finally
		{
			securityService.popAdvisor( yesMan );
		}
		
	}

	private IdManager idManager;
	public void setIdManager(IdManager idm) {
		idManager = idm;
	}
	
	private ValidationDao dao;
	public void setDao(ValidationDao dao) {
		this.dao = dao;
	}
	
	private EmailTemplateService emailTemplateService;	
	public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
		this.emailTemplateService = emailTemplateService;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	
	private AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	
	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private GroupProvider groupProvider;
	public void setGroupProvider(GroupProvider groupProvider) {
		this.groupProvider = groupProvider;
	}

	public ValidationAccount getVaLidationAcountById(Long id) {
		Search search = new Search();
		Restriction rest = new Restriction("id", id);
		search.addRestriction(rest);
		List<ValidationAccount> l = dao.findBySearch(ValidationAccount.class, search);
		if (l.size() >0 ) 
			return (ValidationAccount)l.get(0);
		
		return null;
	}

	public ValidationAccount getVaLidationAcountBytoken(String token) {
		
		Search search = new Search();
		Restriction rest = new Restriction("validationToken", token);
		search.addRestriction(rest);
		List<ValidationAccount> l = dao.findBySearch(ValidationAccount.class, search);
		if (l.size() >0 ) 
			return (ValidationAccount)l.get(0);
		
		return null;
	}

	public boolean isAccountValidated(String userId) {
		//this is a basic rule need to account for validations expiring
		log.debug("validating" + userId);
		
		
		ValidationAccount va = this.getVaLidationAcountByUserId(userId);
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.MONTH, VALIDATION_PERIOD_MONTHS);
		//a time validation time in the past
		Date validationDeadline = cal.getTime();
		if (va == null) {
			log.debug("no account found!");
			return false;
		} else {
			if(isTokenExpired(va)) {
				return true;
			}else if (va.getValidationReceived() == null && va.getValidationSent().after(validationDeadline)) {
				log.debug("validation sent still awaiting reply");
				return true;
			} else if (va.getValidationReceived() == null && va.getValidationSent().before(validationDeadline)) {
				log.debug("validation sent but no reply received");
				//what should we do in this case?
				return true;
			}
			log.debug("got an item of staus " + va.getStatus());
			if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
				log.info("account is validated");
				return true;
			}
		}
		
		log.debug("no conditions met assuming account is not validated");
		return false;
	}
	
	public boolean isTokenExpired(ValidationAccount va)
	{
		if (va == null)
		{
			throw new IllegalArgumentException("null ValidationAccount passed to isTokenExpired");
		}
		// check if it's expired in relation to accountValidator.maxPasswordResetMinutes sakai property
		String strMinutes = serverConfigurationService.getString(MAX_PASSWORD_RESET_MINUTES);
		if (strMinutes != null && !strMinutes.isEmpty())
		{
			// this property only applies to validation tokens coming from reset-pass
			if (va.getAccountStatus() != null && va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET))
			{
				try
				{
					// get the time limit and convert to millis
					int minutes = Integer.parseInt(strMinutes);
					long maxMillis = minutes * 60 * 1000;

					// the time when the validation was sent to the email server
					long sentTime = va.getValidationSent().getTime();

					// all calls to setValidationSent use 'new Date()' whose time is equivalent to System.getCurrentTimeMillis(), so we can do this:
					if (System.currentTimeMillis() - sentTime > maxMillis)
					{
						// it's been too long, so invalidate the token and return
						va.setStatus(ValidationAccount.STATUS_EXPIRED);
						Calendar cal = new GregorianCalendar();
						va.setvalidationReceived(cal.getTime());
						dao.save(va);
						return true;
					}
				}
				catch (NumberFormatException nfe)
				{
					log.warn("accountValidator.maxPasswordResetMinutes must be an integer");
				}
			}
		}

		// perhaps accountValidator.maxPasswordResetMinutes wasn't set, in which case a quartz job may have invalidated the token
		return ValidationAccount.STATUS_EXPIRED.equals(va.getStatus());
	}

	public ValidationAccount getVaLidationAcountByUserId(String userId) {
	
		Search search = new Search();
		Restriction rest = new Restriction("userId", userId);
		search.addRestriction(rest);
		List<ValidationAccount> l = dao.findBySearch(ValidationAccount.class, search);
		
		if (l.size() >0 ) 
			return (ValidationAccount)l.get(0);
		
		return null;
	}
	
	
	
	
	public List<ValidationAccount> getValidationAccountsByStatus(Integer status) {
		Search search = new Search();
		Restriction rest = new Restriction("status", status);
		search.addRestriction(rest);
		List<ValidationAccount> l = dao.findBySearch(ValidationAccount.class, search);
		
		if (l.size() >0 ) 
			return l;
		
		return new ArrayList<ValidationAccount>();
	}

	public ValidationAccount createValidationAccount(String userRef) {
		return createValidationAccount(userRef, false);
	}

	public ValidationAccount createValidationAccount(String UserId, boolean newAccount) {
		
		Integer status = ValidationAccount.ACCOUNT_STATUS_EXISITING;
		if (newAccount) {
			status = ValidationAccount.ACCOUNT_STATUS_NEW;
		}
		
		return createValidationAccount(UserId, status);
	}
	
	public ValidationAccount createValidationAccount(String userRef, String newUserId) {
		ValidationAccount account = new ValidationAccount();
		account.setUserId(userRef);
		account.setValidationToken(idManager.createUuid());
		account.setValidationsSent(1);
		account.setAccountStatus(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE);
		if(StringUtils.isNotBlank(newUserId)){
			account.setEid(newUserId);
		}
		sendEmailTemplate(account, newUserId);
		account = saveValidationAccount(account);
		return account;
	}


	public ValidationAccount createValidationAccount(String userRef,
			Integer accountStatus) {
		log.debug("createValidationAccount(" + userRef + ", " + accountStatus);
		
		
		//TODO creating a new Validation should clear old ones for the user
		
		ValidationAccount v = new ValidationAccount();
		v.setUserId(userRef);
		v.setValidationToken(idManager.createUuid());
		v.setValidationsSent(1);
		
		if (accountStatus == null) {
			v.setAccountStatus(ValidationAccount.ACCOUNT_STATUS_NEW);
		} else {
			v.setAccountStatus(accountStatus);
		}
		sendEmailTemplate(v, null);
		
		
		/*if (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == accountStatus.intValue()) {
			//A password reset doesn't invalidate confirmation
			v.setStatus(ValidationAccount.STATUS_CONFIRMED);
		} else { 
			v.setStatus(ValidationAccount.STATUS_SENT);
		}*/
		v = saveValidationAccount(v);
		return v;
	}
	//Set other details for ValidationAccount and save
	private ValidationAccount saveValidationAccount(ValidationAccount account){
		account.setValidationSent(new Date());
		account.setStatus(ValidationAccount.STATUS_SENT);
		String userId = EntityReference.getIdFromRef(account.getUserId());
		try{
			User u = userDirectoryService.getUser(userId);
			if (StringUtils.isNotBlank(u.getFirstName()))
				account.setFirstName(u.getFirstName());

			if (StringUtils.isNotBlank(u.getLastName()))
				account.setSurname(u.getLastName());
		}
		catch(UserNotDefinedException e){
			log.error("No User found for the id " + e.getMessage());

		}
		dao.save(account);
		return account;
	}

	/**
	 * The url to the account validation form varies according to your account status / accountValidator.sendLegacyLinks. This method determines which page to use.
	 * @param accountStatus - the accountStatus of the ValidationAccount
	 * @return a String representing the viewID of the page that should be specified in the URL
	 */
	public String getPageForAccountStatus(Integer accountStatus)
	{
		if (accountStatus == null)
		{
			log.warn("can't determine which account validation page to use - accountStatus is null. Returning the legacy 'validate'");
			return "validate";
		}

		if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT))
		{
			return "requestAccount";
		}

		if (!serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false))
		{
			if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET))
			{
				return "passwordReset";
			}
			else
			{
				return "newUser";
			}
		}
		return "validate";
	}

	private String getTemplateKey(Integer accountStatus) {
		log.info("getTemplateKey( " + accountStatus.intValue());
		
		String templateKey = TEMPLATE_KEY_NEW_USER;
		
		if ( (ValidationAccount.ACCOUNT_STATUS_EXISITING == accountStatus.intValue())) {
			templateKey  = TEMPLATE_KEY_EXISTINGUSER;
		} else if ( (ValidationAccount.ACCOUNT_STATUS_LEGACY == accountStatus.intValue() || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == accountStatus.intValue())) {
			templateKey  = TEMPLATE_KEY_LEGACYUSER;
		} else if ( (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == accountStatus.intValue())) {
			templateKey  = TEMPLATE_KEY_PASSWORDRESET;
		} else if ( (ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE == accountStatus.intValue())) {
			templateKey = TEMPLATE_KEY_USERIDUPDATE;
		} else if ( (ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == accountStatus)) {
			templateKey = TEMPLATE_KEY_REQUEST_ACCOUNT;
		}
		return templateKey;
	}

	
	public void mergeAccounts(String oldUserReference, String newUserReference) throws ValidationException {
		log.debug("merge account: " +  oldUserReference + ", " + newUserReference + ")");
		UserEdit olduser = null;
		try {
			String oldUserId = EntityReference.getIdFromRef(oldUserReference);
			String newuserId = EntityReference.getIdFromRef(newUserReference);
	
			//we need a security advisor
			SecurityAdvisor secAdvice = new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					log.debug("isAllowed( " + userId + ", " + function + ", " + reference);
					if (UserDirectoryService.SECURE_UPDATE_USER_ANY.equals(function)) {
						return SecurityAdvice.ALLOWED;
					} else if (AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP.equals(function)){
						return SecurityAdvice.ALLOWED;
					} else if (UserDirectoryService.SECURE_REMOVE_USER.equals(function)) {
						log.debug("advising user can delete users");
						return SecurityAdvice.ALLOWED;
					} else {
						return SecurityAdvice.NOT_ALLOWED;
					}
				}
			};
			securityService.pushAdvisor(secAdvice);
			log.debug("pushed security avisor: "  + secAdvice);
			olduser = userDirectoryService.editUser(oldUserId);
			
			//get the old users realm memberships
			Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(oldUserReference), "site.visit", null);
			Iterator<String> it = groups.iterator();
			while (it.hasNext()) {
				AuthzGroup group = authzGroupService.getAuthzGroup(it.next());
				Member member = group.getMember(oldUserId);
				//TODO we need to check if olduser and if so resolve the highest role
				Member exisiting = group.getMember(newuserId);
				String preferedRole = member.getRole().getId();
				
				//the groupProvider is optional it may not be set
				if (exisiting != null && groupProvider != null) {
					preferedRole = groupProvider.preferredRole(preferedRole, exisiting.getRole().getId());
				}
				//add the new user, but don't switch their role if they're already a member
				if (group.getMember(newuserId) == null)
				{
					group.addMember(newuserId, preferedRole, true, false);
				}
				//remove the old user
				group.removeMember(oldUserId);
				authzGroupService.save(group);
			}
			
			//remove the old user
			userDirectoryService.removeUser(olduser);
			
		} catch (UserNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UserPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (olduser != null) {
				userDirectoryService.cancelEdit(olduser);
			}
			//throw new ValidationException("don't have persmission", e);
		} catch (UserLockedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GroupNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthzPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			SecurityAdvisor sa = securityService.popAdvisor();
			if (sa == null) {
				log.warn("Something cleared our advisor!");
			}
			
		}
	}

	public void deleteValidationAccount(ValidationAccount toDelete) {
		dao.delete(toDelete);
		
	}

	public void save(ValidationAccount toSave) {
		dao.save(toSave);
		
	}


	private void xmlToTemplate(Element xmlTemplate, String key) {
		String subject = xmlTemplate.getChildText("subject");
		String body = xmlTemplate.getChildText("message");
		String bodyHtml = xmlTemplate.getChildText("messagehtml");
		String locale = xmlTemplate.getChildText("locale");
		String versionString = xmlTemplate.getChildText("version");
		
		
		if (emailTemplateService.getEmailTemplate(key, new Locale(locale)) == null)
		{
			EmailTemplate template = new EmailTemplate();
			template.setSubject(subject);
			template.setMessage(body);
			if (bodyHtml != null) {
				String decodedHtml;
				try {
					decodedHtml = URLDecoder.decode(bodyHtml, "utf8");
				} catch (UnsupportedEncodingException e) {
					decodedHtml = bodyHtml;
					e.printStackTrace();
				}
				template.setHtmlMessage(decodedHtml);
			}
			template.setLocale(locale);
			template.setKey(key);
			template.setVersion(Integer.valueOf(1));//setVersion(versionString != null ? Integer.valueOf(versionString) : Integer.valueOf(0));	// set version
			template.setOwner("admin");
			template.setLastModified(new Date());
			this.emailTemplateService.saveTemplate(template);
			log.info(this + " user notification tempalte " + key + " added");
		}
	}

	public void resendValidation(String token) {
		ValidationAccount account = this.getVaLidationAcountBytoken(token);
		
		if (account ==  null) {
			throw new IllegalArgumentException("no such account: " + token);
		}
		
		
		account.setValidationSent(new Date());
		account.setValidationsSent(account.getValidationsSent() + 1);
		account.setStatus(ValidationAccount.STATUS_RESENT);
		save(account);
		sendEmailTemplate(account,null);
	}
	private void sendEmailTemplate(ValidationAccount account, String newUserId){
		
		//new send the validation
		String userReference = userDirectoryService.userReference(account.getUserId());
		List<String> userIds = new ArrayList<String>();
		List<String> emailAddresses = new ArrayList<String>();
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("validationToken", account.getValidationToken());
		//get the url
		Map<String, String> parameters = new  HashMap<String, String>();
		parameters.put("tokenId", account.getValidationToken());
		
		///we want a direct tool url
		String page = getPageForAccountStatus(account.getAccountStatus());
		String serverUrl = serverConfigurationService.getServerUrl();
		String url = serverUrl + "/accountvalidator/faces/" + page + "?tokenId=" + account.getValidationToken();
		
		
		replacementValues.put("url", url);
		//add some details about the user
		String userId = EntityReference.getIdFromRef(account.getUserId());
		userIds.add(userId);
		String userDisplayName = "";
		String userEid = "";
			
		try {
			User u = userDirectoryService.getUser(userId);
			
			
			userDisplayName = u.getDisplayName();
			
			userEid = u.getEid();
			//information about the user that added them
			User added = u.getCreatedBy();
			replacementValues.put("addedBy", added.getDisplayName());
			replacementValues.put("addedByEmail", added.getEmail());
			if(StringUtils.isNotBlank(newUserId)&& ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE==account.getAccountStatus()) {
				replacementValues.put("newUserId",newUserId);
				emailAddresses.add(newUserId);
			}
			replacementValues.put("displayName", userDisplayName);
			replacementValues.put("userEid", userEid);
			replacementValues.put("supportemail", serverConfigurationService.getString("support.email"));
			replacementValues.put("institution", serverConfigurationService.getString("ui.institution"));
			
		} catch (UserNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		//information about the site(s) they have been added to
		Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(userId, SiteService.SITE_VISIT, null);
		log.debug("got a list of: " + groups.size());
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
				log.debug("adding site: " + s.getTitle());
				sb.append(s.getTitle());
				siteCount++;
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		replacementValues.put("memberSites", sb.toString());
		
		String templateKey = getTemplateKey(account.getAccountStatus());
		RenderedTemplate renderedTemplate = emailTemplateService.getRenderedTemplateForUser(templateKey, userReference, replacementValues);
		emailTemplateService.sendMessage(userIds,emailAddresses, renderedTemplate, serverConfigurationService.getString("support.email"), serverConfigurationService.getString("support.name"));
	}



	
}
