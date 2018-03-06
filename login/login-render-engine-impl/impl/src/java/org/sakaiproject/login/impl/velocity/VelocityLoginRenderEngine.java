/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.login.impl.velocity;

import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.login.api.LoginRenderContext;
import org.sakaiproject.login.api.LoginRenderEngine;
import org.sakaiproject.login.api.LoginService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.velocity.util.SLF4JLogChute;

@Slf4j
public class VelocityLoginRenderEngine implements LoginRenderEngine {
	// Member variables
	private List availableLoginSkins;
	
	private boolean debug = false;
	
	private LoginService loginService;
	
	private ServerConfigurationService serverConfigurationService;
	
	private ServletContext context;
	
	private String loginConfig = "loginvelocity.config";
	
	private SessionManager sessionManager;
	
	private boolean styleAble = false;
	
	private boolean styleAbleContentSummary = false;
	
	private VelocityEngine vengine;
	
	// LoginRenderEngine Implementation
	
	public void init() throws Exception {
		/*try
		{
			styleAble = serverConfigurationService.getBoolean("portal.styleable", false);
			styleAbleContentSummary = serverConfigurationService.getBoolean("portal.styleable.contentSummary", false);
		}
		catch (Exception ex)
		{
			log
					.warn("No Server configuration service available, assuming default settings ");
		}*/
		
		if ( sessionManager == null ) {
			log.warn("No session Manager, assuming test mode ");
		}

		vengine = new VelocityEngine();

		vengine.setApplicationAttribute(ServletContext.class.getName(), context);

		vengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, new SLF4JLogChute());

		Properties p = new Properties();
		InputStream in = this.getClass().getResourceAsStream(loginConfig);
		if ( in == null ) {
			throw new RuntimeException("Unable to load configuration " + loginConfig);
		} else {
			log.info("Loaded " + loginConfig);
		}
		p.load(in);
		vengine.init(p);
		availableLoginSkins = new ArrayList();
		Map m = new HashMap();
		m.put("name", "defaultskin");
		m.put("display", "Default");
		availableLoginSkins.add(m);

		vengine.getTemplate("/vm/defaultskin/macros.vm");
	}

	public LoginRenderContext newRenderContext(HttpServletRequest request) {
		VelocityLoginRenderContext rc = new VelocityLoginRenderContext();
		rc.setRenderEngine(this);
		rc.setDebug(debug);

		rc.put("pageSkins", availableLoginSkins);
		String loginSkin = "defaultskin";

		if (request != null)
		{

			HttpSession session = request.getSession();
			loginSkin = (String) session.getAttribute("loginskin");
			String newLoginSkin = request.getParameter("loginskin");
			if (newLoginSkin != null && newLoginSkin.length() > 0)
			{
				session.setAttribute("loginskin", newLoginSkin);
				loginSkin = newLoginSkin;
				log.debug("Set Skin To " + loginSkin);
			}
			else
			{
				if (loginSkin == null || loginSkin.length() == 0)
				{
					loginSkin = "defaultskin";
					session.setAttribute("loginskin", loginSkin);

				}
			}
			rc.put("pageCurrentSkin", loginSkin);
			log.debug("Current Skin is " + loginSkin);
		}
		else
		{
			log.debug("No Request Object Skin is default");
			rc.put("pageCurrentSkin", "defaultskin");
		}

		try
		{
			Properties p = new Properties();
			p.load(this.getClass().getResourceAsStream("/" + loginSkin + "/options.config"));
			rc.setOptions(p);
		}
		catch (Exception ex)
		{
			log.info("No options loaded ", ex);

		}

		return rc;
	}

	public void render(String template, LoginRenderContext rcontext, Writer out)
			throws Exception {
		Context vc = ((VelocityLoginRenderContext) rcontext).getVelocityContext();
		String skin = (String) vc.get("pageCurrentSkin");
		if (skin == null || skin.length() == 0)
		{
			skin = "defaultskin";
		}
		if (!"defaultskin".equals(skin))
		{
			vengine.getTemplate("/vm/" + skin + "/macros.vm");
		}
		vengine.mergeTemplate("/vm/" + skin + "/" + template + ".vm",
				((VelocityLoginRenderContext) rcontext).getVelocityContext(), out);
	}

	public void setupForward(HttpServletRequest req, HttpServletResponse res,
			Placement p, String skin) {
		
		log.error("setupForward not implemented!!! Didn't think I would need this...");
		
	}
	
	// Accessors
	
	public ServletContext getContext()
	{
		return context;
	}

	public void setContext(ServletContext context)
	{
		this.context = context;
	}

	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public LoginService getLoginService() {
		return loginService;
	}

	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}
	


}
