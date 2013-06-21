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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.util.ExternalTrustedEvidence;

import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class AcountValidationLocator implements BeanLocator  {
	private static Log log = LogFactory.getLog(AcountValidationLocator.class);
	
	public static final String NEW_PREFIX = "new";
	public static final String UNKOWN_PREFIX = "unkown";
	
	private Map<String, Object> delivered = new HashMap<String, Object>();

	//For calculating password entropy
	public static final char[] CHAR_LOWERS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	public static final char[] CHAR_UPPERS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	public static final char[] CHAR_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	public static final char[] CHAR_SPECIALS = { '!', '$', '*', '+', '-', '.', '=', '?', '@', '^', '_', '|', '~' };
	
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
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public Object locateBean(String name) {
		Object togo = delivered.get(name);
		log.debug("Locating ValidationAccount: " + name);
		if (togo == null){
			if(name.startsWith(NEW_PREFIX)){
				togo = new ValidationAccount();
			} else if (name.startsWith(UNKOWN_PREFIX)) { 
				togo = validationLogic.getVaLidationAcountBytoken(name);
			}else {
				//find the bean
				try {
				log.debug("Locating bean: " + name);
				Long id = Long.valueOf(name);
				togo = validationLogic.getVaLidationAcountById(id);
				}
				catch (NumberFormatException nfe) {
					return null;
				}
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
		
		 for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
			 
	          String key = (String) it.next();
	          
	          ValidationAccount item = (ValidationAccount) delivered.get(key);
	           log.debug("Validating Item: " + item.getId() + " for user: " + item.getUserId());
	           String firstName = item.getFirstName();
	           String surname = item.getSurname();
	           log.debug(firstName + " " + surname);
	           log.debug("this is an new item?: " + item.getAccountStatus());
	           try {
	        	String userId = EntityReference.getIdFromRef(item.getUserId());
	        	//we need permission to edit this user
	        	
	        	//if this is an existing user did the password match?
	        	if (ValidationAccount.ACCOUNT_STATUS_EXISITING== item.getAccountStatus() && !validateLogin(userId, item.getPassword())) 
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
				
	        	UserEdit u = userDirectoryService.editUser(userId);
				u.setFirstName(firstName);
				u.setLastName(surname);
				ResourcePropertiesEdit rp = u.getPropertiesEdit();
				DateTime dt = new DateTime();
				DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
				rp.addProperty("AccountValidated", fmt.print(dt));
				
				
				//if this is a new account set the password
				if (ValidationAccount.ACCOUNT_STATUS_NEW == item.getAccountStatus() || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == item.getAccountStatus() || ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == item.getAccountStatus()) {
					if (item.getPassword() == null || !item.getPassword().equals(item.getPassword2())) {
						//Abandon the edit
						userDirectoryService.cancelEdit(u);
						tml.addMessage(new TargettedMessage("validate.passNotMatch", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
						return "error!";
					}

					if (serverConfigurationService.getBoolean("account-validator.validate.passwords", false))
					{
						//verify that the password strength is sufficient

						String displayId = u.getDisplayId();

						int minimumEntropy = serverConfigurationService.getInt("account-validator.minimum.password.entropy", 16);
						if (!passwordIsStrong(item.getPassword(), displayId, minimumEntropy))
						{
							userDirectoryService.cancelEdit(u);
							tml.addMessage(new TargettedMessage("validate.tooWeak", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
							return "error!";
						}
					}

					u.setPassword(item.getPassword());
					
					// Do they have to accept terms and conditions.
					if (!"".equals(serverConfigurationService.getString("account-validator.terms"))) {
						// Check they accepted the terms.
						if (item.getTerms().booleanValue()) {
							u.getPropertiesEdit().addProperty("TermsAccepted", "true");
						} else {
							userDirectoryService.cancelEdit(u);
							tml.addMessage(new TargettedMessage("validate.acceptTerms", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
							return "error!";
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

	        }
		
		return "success";
	}

	/**
	 * To verify that a password's strength is sufficient.
	 * Based on verifyPasswordStrength() in
	 * http://grepcode.com/file/repo1.maven.org/maven2/org.owasp.esapi/esapi/2.0_rc10/org/owasp/esapi/reference/FileBasedAuthenticator.java
	 * @param password
	 * 	The password whose strength is being checked
	 * @param displayId
	 * 	The user's displayId, which the password will be compared against. Skips this check if null
	 * @param minimumEntropy
	 * 	The minimum allowed entropy for the password
	 * @return true if the entropy calculated by this algorithm is greater than minimumEntropy, false otherwise
	 */
	public boolean passwordIsStrong(String password, String displayId, int minimumEntropy)
	{
		if (password == null)
		{
			return false;
		}

		if (displayId != null)
		{
			int length = displayId.length();
			for (int i = 0; i < length -2; i++)
			{
				String sub = displayId.substring(i, i + 3);
				if (password.indexOf(sub) > -1)
				{
					return false;
				}
			}
		}

		// new password must have enough character sets and length
		int charsets = 0;
		for (int i = 0; i < password.length(); i++)
		{
			if (Arrays.binarySearch(CHAR_LOWERS, password.charAt(i)) >= 0)
			{
				charsets++;
				break;
			}
		}
		for (int i = 0; i < password.length(); i++)
		{
			if (Arrays.binarySearch(CHAR_UPPERS, password.charAt(i)) >= 0)
			{
				charsets++;
				break;
			}
		}
		for (int i = 0; i < password.length(); i++)
		{
			if (Arrays.binarySearch(CHAR_DIGITS, password.charAt(i)) >= 0)
			{
				charsets++;
				break;
			}
		}
		for (int i = 0; i < password.length(); i++)
		{
			if (Arrays.binarySearch(CHAR_SPECIALS, password.charAt(i)) >= 0)
			{
				charsets++;
				break;
			}
		}

		// calculate and verify password strength
		int strength = password.length() * charsets;
		if (strength < minimumEntropy)
		{
			return false;
		}

		return true;
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
