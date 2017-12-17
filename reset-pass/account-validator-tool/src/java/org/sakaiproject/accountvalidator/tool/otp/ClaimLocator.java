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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.accountvalidator.logic.ValidationException;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.model.ValidationClaim;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.IdPwEvidence;

import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

@Slf4j
public class ClaimLocator implements BeanLocator {

	private ValidationLogic validationLogic;
	public void setValidationLogic(ValidationLogic vl) {
		validationLogic = vl;
	}
	
	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	private AuthenticationManager authenticationManager;
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	private UsageSessionService usageSessionService;	
	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}
	
	SessionManager sessionManager;	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private HttpServletRequest httpServletRequest;
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	private DeveloperHelperService developerHelperService;	
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	private Map<String, Object> delivered = new HashMap<String, Object>();
	
	public Object locateBean(String name) {
		Object togo = delivered.get(name);
		if (delivered.containsKey(name)) {
			return delivered.get(name);
		}
		// seeing this is a transient bean its always new
		togo = new ValidationClaim();
		delivered.put(name, togo);
		return togo;
	}

	
	
	public String claimAccount() {
		log.debug("claim account!");
		//does the userName password match?
		ValidationClaim vc = null;
		for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
	          String key = (String) it.next();
	          vc = (ValidationClaim) delivered.get(key);
		}
		if (vc == null) 
			return "error";
		log.debug(vc.getUserEid() + ": " + vc.getPassword1());
		User u = userDirectoryService.authenticate(vc.getUserEid(), vc.getPassword1());
		if (u == null) {
			log.warn("authentification failed for " + vc.getUserEid());
			tml.addMessage(new TargettedMessage("validate.loginFailed",new Object[]{}, TargettedMessage.SEVERITY_ERROR));
			return "error";
		}
		
		ValidationAccount va = validationLogic.getVaLidationAcountBytoken(vc.getValidationToken());
		if (va == null)
		{
			log.warn("Couldn't obtain a ValidationAccount object for token: " + vc.getValidationToken());
			return "error";
		}

		//With sendLegacyLinks disabled, the option to transfer memberships is not available for password resets
		if (!serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false) && (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == va.getStatus() || ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == va.getStatus()))
		{
			log.warn("Was attempting to transfer memberships for a ValidationAccount of status " + va.getStatus());
			return "error";

		}

		String oldUserRef = userDirectoryService.userReference(va.getUserId());

		// don't let them claim the account if their validation token is expired
		if (validationLogic.isTokenExpired(va))
		{
			// a TargettedMessage will be displayed by ValidationProducer
			return "error";
		}
		
		//Try set up the ussersession
		authenticateUser(vc, oldUserRef);
		
		//we can't merge an account into itself
		if (u.getId().equals(va.getUserId())) {
			log.warn("using the same accounts for validation!"); 
			tml.addMessage(new TargettedMessage("validate.sameAccount",new Object[]{}, TargettedMessage.SEVERITY_ERROR));
			return "error";
		}
		

		
		try {
			validationLogic.mergeAccounts(va.getUserId(), u.getReference());
			//delete the token
			validationLogic.deleteValidationAccount(va);
			
			authenticateUser(vc, oldUserRef);
			return "success";
		} catch (ValidationException e2) {
			log.error(e2.getMessage(), e2);
		}
		return "error";
	}

	private void authenticateUser(ValidationClaim vc, String oldUserRef) {
		//log the user in
		Evidence e = new IdPwEvidence(vc.getUserEid(), vc.getPassword1(), httpServletRequest.getRemoteAddr());
		try {
			Authentication a = authenticationManager.authenticate(e);
			log.debug("authenticated " + a.getEid() + "(" + a.getUid() + ")");
			log.debug("reg: " + httpServletRequest.getRemoteAddr());
			log.debug("user agent: " + httpServletRequest.getHeader("user-agent"));
			if (usageSessionService.login(a, httpServletRequest)) {
				log.debug("logged in!");
			}
			
			//post an event
			developerHelperService.fireEvent("accountvalidation.merge", oldUserRef);
	
		} catch (AuthenticationException e1) {
			log.error(e1.getMessage(), e1);
		}
	}
}
