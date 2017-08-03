/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.login.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.login.api.Login;
import org.sakaiproject.login.api.LoginAdvisor;
import org.sakaiproject.login.api.LoginCredentials;
import org.sakaiproject.login.api.LoginRenderEngine;
import org.sakaiproject.login.api.LoginService;

import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.util.IdPwEvidence;


public abstract class LoginServiceComponent implements LoginService {

	protected abstract AuthenticationManager authenticationManager();
	protected abstract ServerConfigurationService serverConfigurationService();
	protected abstract UsageSessionService usageSessionService();
	
	private Map<String, LoginRenderEngine> renderEngines = new ConcurrentHashMap<String, LoginRenderEngine>();
	
	private LoginAdvisor loginAdvisor = null;
	
	public void addRenderEngine(String context, LoginRenderEngine vengine) {
		renderEngines.put(context, vengine);
	}

	public void authenticate(LoginCredentials credentials) throws LoginException {
		LoginAdvisor loginAdvisor = resolveLoginAdvisor();
		
		// Only bother checking login credentials and/or imposing a penalty when the protection level is set
		boolean isAdvisorEnabled = loginAdvisor != null && loginAdvisor.isAdvisorEnabled();
		
		// authenticate
		try
		{
			String eid = credentials.getIdentifier();
			String pw = credentials.getPassword();
			
			boolean isEidEmpty = (eid == null) || (eid.length() == 0);
			boolean isPwEmpty = (pw == null) || (pw.length() == 0);
			
			if (isAdvisorEnabled) {
				if (!loginAdvisor.checkCredentials(credentials)) {
					throw new LoginException(Login.EXCEPTION_INVALID_CREDENTIALS);
				}
			}
			
			if (isEidEmpty || isPwEmpty)
			{
				throw new AuthenticationException("missing-fields");
			}
			
			// Do NOT trim the password, since many authentication systems allow whitespace.
			eid = eid.trim();

			Evidence e = new IdPwEvidence(eid, pw, credentials.getRemoteAddr());

			Authentication a = authenticationManager().authenticate(e);

			// login the user
			if (usageSessionService().login(a, credentials.getRequest()))
			{
				if (isAdvisorEnabled) 
					loginAdvisor.setSuccess(credentials);
			}
			else
			{
				if (isAdvisorEnabled) 
					loginAdvisor.setFailure(credentials);
				throw new LoginException(Login.EXCEPTION_INVALID);
			}
		}
		catch (AuthenticationException ex)
		{
			if (ex.getMessage().equals("missing-fields"))
				throw new LoginException(Login.EXCEPTION_MISSING_CREDENTIALS);
			
			/**
			 * If the Authentication Exception Equals Disabled then 
			 * re throw the message to the top.
			 */
			if (ex.getMessage().equals(Login.EXCEPTION_DISABLED))
				throw new LoginException(Login.EXCEPTION_DISABLED);
			
			boolean isPenaltyImposed = false;
			
			if (isAdvisorEnabled) {
				loginAdvisor.setFailure(credentials);
				isPenaltyImposed = !loginAdvisor.checkCredentials(credentials);
			} 
			
			if (isPenaltyImposed)
				throw new LoginException(Login.EXCEPTION_INVALID_WITH_PENALTY);
			else
				throw new LoginException(Login.EXCEPTION_INVALID);
		}
		
	}
	
	public String getLoginAdvice(LoginCredentials credentials) {
		LoginAdvisor loginAdvisor = resolveLoginAdvisor();
		
		// Only bother checking login credentials and/or imposing a penalty when the protection level is set
		boolean isAdvisorEnabled = loginAdvisor != null && loginAdvisor.isAdvisorEnabled();
		
		if (isAdvisorEnabled) {
			return loginAdvisor.getLoginAdvice(credentials);
		}
		
		return "";
	}
	
	public LoginRenderEngine getRenderEngine(String context, HttpServletRequest request)
	{
		// at this point we ignore request but we might use ut to return more
		// than one render engine

		if (context == null || context.length() == 0)
		{
			context = Login.DEFAULT_LOGIN_CONTEXT;
		}

		return (LoginRenderEngine) renderEngines.get(context);
	}
	
	public boolean hasLoginAdvice() {
		LoginAdvisor loginAdvisor = resolveLoginAdvisor();
		
		return loginAdvisor != null && loginAdvisor.isAdvisorEnabled();
	}
	
	public void removeRenderEngine(String context, LoginRenderEngine vengine) {
		renderEngines.remove(context);
	}

	private LoginAdvisor resolveLoginAdvisor() {
		if (loginAdvisor == null) {
			loginAdvisor = (LoginAdvisor)ComponentManager.get(LoginAdvisor.class);
		}
		
		return loginAdvisor;
	}
	
}
