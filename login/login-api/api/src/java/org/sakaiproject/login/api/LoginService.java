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
package org.sakaiproject.login.api;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;


public interface LoginService {
	
	/**
	 * Authenticate the passed credentials
	 * 
	 * @param credentials
	 * @throws LoginException
	 */
	public void authenticate(LoginCredentials credentials) throws LoginException;
	
	/**
	 * Get markup advice to include in the login screen
	 * 
	 * @param credentials
	 * @return
	 */
	public String getLoginAdvice(LoginCredentials credentials);
	
	/**
	 * Get a render engine possibly based on the request
	 * 
	 * @param context -
	 *        the context from whcih to take the render engine.
	 * @param request
	 * @return
	 */
	LoginRenderEngine getRenderEngine(String context, HttpServletRequest request);

	/**
	 * Indicate whether the service has any markup advice to include in the login screen
	 * 
	 * @return true if advice exists, false otherwise
	 */
	public boolean hasLoginAdvice();
	
	/**
	 * Add a render engine to the available render engines.
	 * 
	 * @param context -
	 *        the context to rengister the render engine in, as there may be
	 *        more than one portal in a sakai instance, you need to register the
	 *        render engine against a context. The context should match the
	 *        context used by the portal to retrieve its render engine. This is
	 *        dependant on the Portal implementation details.
	 * @param vengine
	 *        the render engine implementation to register with the portal
	 *        service
	 */
	void addRenderEngine(String context, LoginRenderEngine vengine);

	/**
	 * Remove a render engine from the avaialble render engines
	 * 
	 * @param context -
	 *        the context to deregister the render engine from, as there may be
	 *        more than one portal in a sakai instance, you need to deregister
	 *        the render engine from a context. The context should match the
	 *        context used by the portal to retrieve its render engine. This is
	 *        dependant on the Portal implementation details.
	 * @param vengine
	 */
	void removeRenderEngine(String context, LoginRenderEngine vengine);

	
}
