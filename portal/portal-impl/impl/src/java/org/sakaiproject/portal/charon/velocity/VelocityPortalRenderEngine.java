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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

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

	private VelocityEngine vengine;

	private boolean debug = false;

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

	}

	public PortalRenderContext newRenderContext(HttpServletRequest request)
	{
		VelocityPortalRenderContext rc = new VelocityPortalRenderContext();
		rc.setDebug(debug);
		// this is just for testing, it should be in the path or portal to
		// ensure that the skin remains.
		if (request != null)
		{
			String portalSkin = request.getParameter("portalskin");
			if (portalSkin == null || portalSkin.length() == 0)
			{
				portalSkin = "defaultskin";
			}
			rc.put("portalLayoutSkin", portalSkin);
		}
		return rc;
	}

	public void render(String template, PortalRenderContext rcontext, Writer out)
			throws Exception
	{
		Context vc = ((VelocityPortalRenderContext) rcontext)
				.getVelocityContext();
		String skin = (String) vc.get("portalLayoutSkin");
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
