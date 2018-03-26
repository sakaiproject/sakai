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
package org.sakaiproject.accountvalidator.tool.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.user.api.*;
import org.sakaiproject.user.api.UserDirectoryService.PasswordRating;
import org.sakaiproject.util.ExternalTrustedEvidence;
import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class AcountValidationLocator implements BeanLocator  {
	private static Logger log = LoggerFactory.getLogger(AcountValidationLocator.class);
	
	public static final String NEW_PREFIX = "new";
	
	private Map<String, Object> delivered = new HashMap<String, Object>();

	private static final String TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET = "acknowledge.passwordReset";

	private ValidationLogic validationLogic;
	public void setValidationLogic(ValidationLogic vl) {
		validationLogic = vl;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private AuthenticationManager authenticationManager;
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private UsageSessionService usageSessionService;	
	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	private HttpServletRequest httpServletRequest;
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}
	
	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	private DeveloperHelperService developerHelperService;	
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	private EmailTemplateService emailTemplateService;
	public void setEmailTemplateService(EmailTemplateService emailTemplateService)
	{
		this.emailTemplateService = emailTemplateService;
	}

	public Object locateBean(String name) {
		Object togo = delivered.get(name);
		log.debug("Locating ValidationAccount: " + name);
		if (togo == null){
			if(name.startsWith(NEW_PREFIX)){
				togo = new ValidationAccount();
			}else {
				//find the bean
				// always look up by token to prevent sequential guessing
				togo = validationLogic.getVaLidationAcountBytoken(name);
			}
			if (togo != null)
			{
				delivered.put(name, togo);
			}
			delivered.put(name, togo);
		}
		return togo;
	}

	public void saveAll() {
    	log.debug("SaveAll() for " + delivered.size());
        for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
          String key = (String) it.next();
          ValidationAccount item = (ValidationAccount) delivered.get(key);
          /*
          if (key.startsWith(NEW_PREFIX)) {
        	  // could be a special case in some models
          }*/
               	  
          	log.debug("Saving Item: " + item.getId() + " for user: " + item.getUserId());
          	//validationLogic.

        }
	}
	
	public ValidationAccount getAccount() {
		log.debug("getAccount for " + delivered.size());
        for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
          String key = it.next();
          ValidationAccount item = (ValidationAccount) delivered.get(key);
          //this will be a new with only a key set
          ValidationAccount ret = null;
          if (item.getValidationToken() != null) {
        	  ret = validationLogic.getVaLidationAcountBytoken(item.getValidationToken());
        	  if (ret == null) {
        		  Object[] args = new Object[]{ item.getValidationToken()};
        		  tml.addMessage(new TargettedMessage("msg.noSuchValidation", args, TargettedMessage.SEVERITY_ERROR));
        	  }
          } else {
        	  tml.addMessage(new TargettedMessage("msg.noCode", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
          }
          return ret;
        }
		return null;
	}
	
	//TODO the logic should be moved to a service method
	public String validateAccount() {
		log.debug("Validate Account");
		
		List<String> userReferences = new ArrayList<String>();

		 for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
			 
	          String key = (String) it.next();
	          
	          ValidationAccount item = (ValidationAccount) delivered.get(key);

                  // token is the only piece of data we can trust; refresh the item accordingly
                  String token = item.getValidationToken();
                  ValidationAccount fromDb = validationLogic.getVaLidationAcountBytoken(token);
                  String formEid = StringUtils.trimToEmpty(item.getEid());
                  String formFirstName = StringUtils.trimToEmpty(item.getFirstName());
                  String formSurname = StringUtils.trimToEmpty(item.getSurname());
                  String formPw1 = StringUtils.trimToEmpty(item.getPassword());
                  String formPw2 = StringUtils.trimToEmpty(item.getPassword2());
                  boolean formTerms = item.getTerms();
                  item = fromDb;

                  if (ValidationAccount.STATUS_CONFIRMED.equals(item.getStatus()) || ValidationAccount.STATUS_EXPIRED.equals(item.getStatus()))
                  {
                      return "error";
                  }

                  log.debug("Validating Item: " + item.getId() + " for user: " + item.getUserId());

                   int accountStatus = item.getAccountStatus();
                   //names are required in all cases except password resets
                   if (ValidationAccount.ACCOUNT_STATUS_NEW == accountStatus || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == accountStatus || ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == accountStatus)
                   {
                     if (formFirstName.isEmpty())
                     {
                       tml.addMessage(new TargettedMessage("firstname.required", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
                       return "error";
                     }
                     if (formSurname.isEmpty())
                     {
                       tml.addMessage(new TargettedMessage("lastname.required", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
                       return "error";
                     }
                   }

	           log.debug(formFirstName + " " + formSurname);
	           log.debug("this is an new item?: " + item.getAccountStatus());
	           try {
	        	String userId = EntityReference.getIdFromRef(item.getUserId());
	        	//we need permission to edit this user
	        	
	        	//if this is an existing user did the password match?
	        	if (ValidationAccount.ACCOUNT_STATUS_EXISITING == item.getAccountStatus() && !validateLogin(userId, formPw1))
	        	{
	        		tml.addMessage(new TargettedMessage("validate.invalidPassword", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
	        		return "error";
	        	}
				securityService.pushAdvisor(new SecurityAdvisor() {
		            public SecurityAdvice isAllowed(String userId, String function, String reference) {
		              if (function.equals(UserDirectoryService.SECURE_UPDATE_USER_ANY)) {
		                return SecurityAdvice.ALLOWED;
		              } else {
		                return SecurityAdvice.NOT_ALLOWED;
		              }
		            }
		          });

			if (validationLogic.isTokenExpired(item))
			{
				// A TargettedMessage will be displayed by ValidationProducer
				return "error!";
			}
			if(item.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE)) {
				boolean isSuccess = userDirectoryService.updateUserId(userId,formEid);
				item.setEid(formEid);
				if(!isSuccess) {
					tml.addMessage(new TargettedMessage("msg.errUpdate.userId" , new Object[]{formEid}, TargettedMessage.SEVERITY_ERROR));
				}
			}
				
	        	UserEdit u = userDirectoryService.editUser(userId);
				if (isLegacyLinksEnabled() || ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET != accountStatus)
				{
					//We always can change names if legacy links is enabled. Otherwise in the new forms, we can't change names during password resets
					u.setFirstName(formFirstName);
					u.setLastName(formSurname);
					item.setFirstName(formFirstName);
					item.setSurname(formSurname);
				}
				ResourcePropertiesEdit rp = u.getPropertiesEdit();
				DateTime dt = new DateTime();
				DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
				rp.addProperty("AccountValidated", fmt.print(dt));
				
				
				//if this is a new account set the password
				if (ValidationAccount.ACCOUNT_STATUS_NEW == accountStatus || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == accountStatus || ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == accountStatus || ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == accountStatus) {
					if (formPw1 == null || !formPw1.equals(formPw2)) {
						//Abandon the edit
						userDirectoryService.cancelEdit(u);
						tml.addMessage(new TargettedMessage("validate.passNotMatch", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
						return "error!";
					}

					// SAK-22427
					if (userDirectoryService.getPasswordPolicy() != null) {
						PasswordRating rating = userDirectoryService.validatePassword(formPw1, u);
						if (PasswordRating.FAILED.equals(rating))
						{
							userDirectoryService.cancelEdit(u);
							tml.addMessage(new TargettedMessage("validate.password.fail", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
							return "error!";
						}
					}

					u.setPassword(formPw1);

					// Do they have to accept terms and conditions.
					if (!"".equals(serverConfigurationService.getString("account-validator.terms"))) {
						//terms and conditions are only relevant for new accounts (unless we're using the legacy links)
						boolean checkTerms = ValidationAccount.ACCOUNT_STATUS_NEW == accountStatus || isLegacyLinksEnabled();
						if (checkTerms)
						{
							// Check they accepted the terms.
							if (formTerms) {
								u.getPropertiesEdit().addProperty("TermsAccepted", "true");
							} else {
								userDirectoryService.cancelEdit(u);
								tml.addMessage(new TargettedMessage("validate.acceptTerms", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
								return "error!";
							}
						}
					}
				}

				userDirectoryService.commitEdit(u);
				
				//update the Validation object
				item.setvalidationReceived(new Date());
				item.setStatus(ValidationAccount.STATUS_CONFIRMED);
				log.debug("Saving now ...");
				
				//post an event
				developerHelperService.fireEvent("accountvalidation.validated", u.getReference());
				
				validationLogic.save(item);

				userReferences.add(userDirectoryService.userReference(item.getUserId()));
				
				
				//log the user in
				Evidence e = new ExternalTrustedEvidence(u.getEid());
				try {
					Authentication a = authenticationManager.authenticate(e);
					log.debug("authenticated " + a.getEid() + "(" + a.getUid() + ")");
					log.debug("reg: " + httpServletRequest.getRemoteAddr());
					log.debug("user agent: " + httpServletRequest.getHeader("user-agent"));
					usageSessionService.login(a , httpServletRequest);
				} catch (AuthenticationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			} catch (UserNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UserPermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UserLockedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UserAlreadyDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				securityService.popAdvisor();
			}
	           
	          	//validationLogic.

			// Send password reset acknowledgement email for password reset scenarios
			if (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == accountStatus)
			{
				String supportEmail = serverConfigurationService.getString("mail.support");
				Map<String, String> replacementValues = new HashMap<String, String>();
				replacementValues.put("emailSupport", supportEmail);
				emailTemplateService.sendRenderedMessages(TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET, userReferences, replacementValues, supportEmail, supportEmail);
			}
		}
		


		return "success";
	}


	/**
	 * Determines whether account validator sends users to the old validation form or the new ones
	 * @return true when users are sent to the old form
	 */
	private boolean isLegacyLinksEnabled()
	{
		return serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false);
	}


	private boolean validateLogin(String userId, String password) {
		try {
			User u = userDirectoryService.authenticate(userDirectoryService.getUserEid(userId), password);
			if (u != null) {
				return true;
			}
		} catch (UserNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	

	
	
}
