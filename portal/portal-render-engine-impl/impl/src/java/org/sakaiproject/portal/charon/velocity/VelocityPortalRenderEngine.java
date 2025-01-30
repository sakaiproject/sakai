/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.portal.charon.velocity;

import java.io.IOException;
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

import org.sakaiproject.velocity.util.SLF4JLogChute;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A velocity render engine adapter
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

@Slf4j
public class VelocityPortalRenderEngine implements PortalRenderEngine
{
	private List<Map<String, String>> availablePortalSkins;
	@Setter private ServletContext context;
	@Setter @Getter private boolean debug = false;
	private String defaultSkin = "morpheus";
	@Setter private String portalConfig = "portalvelocity.config";
	@Setter PortalService portalService;
	@Setter ServerConfigurationService serverConfigurationService;
	@Setter SessionManager sessionManager;
	private VelocityEngine vengine;

	public void init() throws Exception
	{
		if (log.isTraceEnabled()) {
			debug = true;
		}
		
		try
		{
			//this variable will decide which templates and configs (bundle) are picked
			defaultSkin = serverConfigurationService.getString("portal.templates", "morpheus");
		}
		catch (Exception ex)
		{
			log.warn("No Server configuration service available, assuming default settings ");
		}
		if ( sessionManager == null ) {
			log.warn("No session Manager, assuming test mode ");
		}

		vengine = new VelocityEngine();

		vengine.setApplicationAttribute(ServletContext.class.getName(), context);
		vengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, new SLF4JLogChute());

		Properties p = new Properties();
		InputStream in = null;
		try {
		in = this.getClass().getResourceAsStream(portalConfig );
		if ( in == null ) {
			throw new RuntimeException("Unable to load configuration "+portalConfig);
		} else {
			log.info("Loaded "+portalConfig);
		}
		p.load(in);
		vengine.init(p);
		availablePortalSkins = new ArrayList<Map<String, String>>();
		Map<String, String> m = new HashMap<String, String>();
		m.put("name", defaultSkin);
		m.put("display", "Default");
		availablePortalSkins.add(m);
		/*
		 * m = new HashMap(); m.put("name", "skintwo"); m.put("display", "Skin
		 * Two"); availablePortalSkins.add(m);
		 */
		}
		catch (IOException e) {
			throw new RuntimeException("Exception encounterd:  " + e, e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					//not much to do
				}
			}
		}

	}

	public PortalRenderContext newRenderContext(HttpServletRequest request)
	{
		VelocityPortalRenderContext rc = new VelocityPortalRenderContext();
		rc.setRenderEngine(this);
		rc.setDebug(debug);
		// this is just for testing, it should be in the path or portal to
		// ensure that the skin remains.

		rc.put("pageSkins", availablePortalSkins);
		String portalSkin = defaultSkin;

		if (request != null)
		{

			HttpSession session = request.getSession();
			portalSkin = (String) session.getAttribute("portalskin");
			String newPortalSkin = request.getParameter("portalskin");
			if (newPortalSkin != null && newPortalSkin.length() > 0)
			{
				session.setAttribute("portalskin", newPortalSkin);
				portalSkin = newPortalSkin;
				log.debug("Set Skin To " + portalSkin);
			}
			else
			{
				if (portalSkin == null || portalSkin.length() == 0)
				{
					portalSkin = defaultSkin;
					session.setAttribute("portalskin", portalSkin);

				}
			}
			rc.put("pageCurrentSkin", portalSkin);
			log.debug("Current Skin is " + portalSkin);
		}
		else
		{
			log.debug("No Request Object Skin is default");
			rc.put("pageCurrentSkin", defaultSkin);
		}

		InputStream stream = null;
		try
		{
			Properties p = new Properties();
			stream = this.getClass().getResourceAsStream(
					"/" + portalSkin + "/options.config");
			p.load(stream);
			rc.setOptions(p);
		}
		catch (Exception ex)
		{
			log.info("No options loaded. Check options.config for " + defaultSkin, ex);

		} 
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					//not much to do in this case
				}
			}
		}

		return rc;
	}

	public void render(String template, PortalRenderContext rcontext, Writer out)
			throws Exception
	{
		if (log.isTraceEnabled()) {
		   log.trace("Portal trace is on, dumping PortalRenderContext to log:\n" + rcontext.dump());
		}
		
		Context vc = ((VelocityPortalRenderContext) rcontext).getVelocityContext();
		String skin = (String) vc.get("pageCurrentSkin");
		if (skin == null || skin.length() == 0)
		{
			skin = defaultSkin;
		}
		vengine.mergeTemplate("/vm/" + skin + "/" + template + ".vm",
				((VelocityPortalRenderContext) rcontext).getVelocityContext(), out);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalRenderEngine#setupForward(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse,
	 *      org.sakaiproject.tool.api.Placement, java.lang.String)
	 */
	public void setupForward(HttpServletRequest req, HttpServletResponse res,
			Placement p, String skin)
	{

		String headJs = (String) req.getAttribute("sakai.html.head.js");
		String headCssToolBase = (String) req.getAttribute("sakai.html.head.css.base");
		String headCssToolSkin = (String) req.getAttribute("sakai.html.head.css.skin");
		String bodyonload = (String) req.getAttribute("sakai.html.body.onload");
		String headCss = headCssToolBase + headCssToolSkin;
		String head = headCss + headJs;

		req.setAttribute("sakai.html.head", head);
		req.setAttribute("sakai.html.head.css", headCss);
		req.setAttribute("sakai.html.head.js", headJs);
		req.setAttribute("sakai.html.head.css.base", headCssToolBase);
		req.setAttribute("sakai.html.head.css.skin", headCssToolSkin);
		req.setAttribute("sakai.html.body.onload", bodyonload);

	}

}
