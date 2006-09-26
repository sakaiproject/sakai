/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.portal.charon.velocity;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.sakaiproject.portal.charon.PortalRenderContext;
import org.sakaiproject.portal.charon.PortalRenderEngine;

/**
 * A velocity render engine adapter
 * 
 * @author ieb
 */
public class VelocityPortalRenderEngine implements PortalRenderEngine
{
	private static final Log log = LogFactory
			.getLog(VelocityPortalRenderEngine.class);

	private VelocityEngine vengine;

	private boolean debug = false;

	private List availablePortalSkins;

	public void init() throws Exception
	{
		vengine = new VelocityEngine();

		vengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		vengine
				.setProperty("runtime.log.logsystem.log4j.category",
						"ve.portal");
		Properties p = new Properties();
		p
				.load(this.getClass().getResourceAsStream(
						"portalvelocity.properties"));
		vengine.init(p);
		availablePortalSkins = new ArrayList();
		Map m = new HashMap();
		m.put("name", "defaultskin");
		m.put("display", "Default");
		availablePortalSkins.add(m);
		m = new HashMap();
		m.put("name", "skintwo");
		m.put("display", "Skin Two");
		availablePortalSkins.add(m);

	}

	public PortalRenderContext newRenderContext(HttpServletRequest request)
	{
		VelocityPortalRenderContext rc = new VelocityPortalRenderContext();
		rc.setDebug(debug);
		// this is just for testing, it should be in the path or portal to
		// ensure that the skin remains.

		rc.put("pageSkins", availablePortalSkins);
		String portalSkin = "defaultskin";

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
					portalSkin = "defaultskin";
					session.setAttribute("portalskin", portalSkin);

				}
			}
			rc.put("pageCurrentSkin", portalSkin);
			log.debug("Current Skin is " + portalSkin);
		}
		else
		{
			log.debug("No Request Object Skin is default");
			rc.put("pageCurrentSkin", "defaultskin");
		}

		try
		{
			Properties p = new Properties();
			p.load(this.getClass().getResourceAsStream(
					"/"+portalSkin + "/options.properties"));
			rc.setOptions(p);
		}
		catch (Exception ex)
		{
			log.info("No options loaded ",ex);

		}

		return rc;
	}

	public void render(String template, PortalRenderContext rcontext, Writer out)
			throws Exception
	{
		Context vc = ((VelocityPortalRenderContext) rcontext)
				.getVelocityContext();
		String skin = (String) vc.get("pageCurrentSkin");
		if (skin == null || skin.length() == 0)
		{
			skin = "defaultskin";
		}
		vengine.getTemplate("defaultskin/macros.vm");
		vengine.getTemplate(skin + "/macros.vm");
		vengine.mergeTemplate(skin + "/" + template + ".vm",
				((VelocityPortalRenderContext) rcontext).getVelocityContext(),
				out);

	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

}
