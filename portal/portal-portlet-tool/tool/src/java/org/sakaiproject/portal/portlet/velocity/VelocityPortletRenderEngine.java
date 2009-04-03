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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.portlet.velocity;

import java.io.Writer;
import java.util.List;
import java.util.Properties;

import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.sakaiproject.portal.portlet.PortletRenderContext;
import org.sakaiproject.portal.portlet.PortletRenderEngine;

/**
 * A velocity render engine adapter
 * 
 * @author ieb
 */
public class VelocityPortletRenderEngine implements PortletRenderEngine
{
	private static final Log log = LogFactory
			.getLog(VelocityPortletRenderEngine.class);

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
						"portletvelocity.config"));
		vengine.init(p);

	}

	public PortletRenderContext newRenderContext(RenderRequest request)
	{
		VelocityPortletRenderContext rc = new VelocityPortletRenderContext();
		rc.setDebug(debug);
		return rc;
	}

	public void render(String template, PortletRenderContext rcontext, Writer out)
			throws Exception
	{
		Context vc = ((VelocityPortletRenderContext) rcontext)
				.getVelocityContext();
		vengine.getTemplate("defaultskin/macros.vm");
		vengine.mergeTemplate("defaultskin/" + template + ".vm",
				((VelocityPortletRenderContext) rcontext).getVelocityContext(),
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
