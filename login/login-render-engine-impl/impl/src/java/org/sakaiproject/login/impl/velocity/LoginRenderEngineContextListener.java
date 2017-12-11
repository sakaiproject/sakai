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
package org.sakaiproject.login.impl.velocity;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.login.api.Login;
import org.sakaiproject.login.cover.LoginService;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Knock off on {@link org.sakaiproject.portal.charon.velocity.PortalRenderEngineContextListener}
 * 
 * @author jrenfro
 */
@Slf4j
public class LoginRenderEngineContextListener implements ServletContextListener {
	private VelocityLoginRenderEngine vengine;
	
	public void contextDestroyed(ServletContextEvent sce) {
		LoginService.getInstance().removeRenderEngine(Login.DEFAULT_LOGIN_CONTEXT, vengine);
	}

	public void contextInitialized(ServletContextEvent event) {
		try
		{
			vengine = new VelocityLoginRenderEngine();
			vengine.setContext(event.getServletContext());
			vengine.setServerConfigurationService(ServerConfigurationService.getInstance());
			vengine.setSessionManager(SessionManager.getInstance());
			vengine.init();
			vengine.setLoginService(LoginService.getInstance());
			LoginService.getInstance().addRenderEngine(Login.DEFAULT_LOGIN_CONTEXT, vengine);
		}
		catch (Exception ex)
		{
			log.error("Failed to register render engine with the login service, this is probably fatal ", ex);
		}
	}

}
