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
package org.sakaiproject.portal.service;

import java.util.List;
import java.util.Iterator;

import java.io.File;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.spi.optional.PortletRegistryEvent;
import org.apache.pluto.spi.optional.PortletRegistryListener;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.component.cover.ServerConfigurationService;


/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class SakaiPortletRegistryListener implements PortletRegistryListener
{
	private static final Log log = LogFactory.getLog(SakaiPortletRegistryListener.class);

	private PortletContextManager registry;

	private ActiveToolManager activeToolManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.pluto.spi.optional.PortletRegistryListener#portletApplicationRegistered(org.apache.pluto.spi.optional.PortletRegistryEvent)
	 */
	@SuppressWarnings("unchecked")
	public void portletApplicationRegistered(PortletRegistryEvent evt)
	{
		try
		{
			PortletAppDD appDD = evt.getPortletApplicationDescriptor();
			String applicationID = evt.getApplicationId();
			ServletContext portalContext = null;
			InternalPortletContext iPortlet = null;
			for (Iterator<InternalPortletContext> iapps = registry
					.getRegisteredPortletApplications(); iapps.hasNext();)
			{
				InternalPortletContext ipc = iapps.next();
				if (applicationID.equals(ipc.getApplicationId()))
				{
					portalContext = ipc.getServletContext();
					iPortlet = ipc;
				}
			}
			for (Iterator<PortletDD> i = appDD.getPortlets().iterator(); i.hasNext();)
			{
				PortletDD pdd = i.next();
				List<Tool> toolRegs = getRegistrationsForPortlet(pdd, iPortlet, portalContext);
				if ( toolRegs == null )
				{
					PortletTool tool = new PortletTool(pdd, iPortlet, portalContext, null);
					activeToolManager.register(tool, portalContext);
				}
				else
				{
					for ( Iterator<Tool> it = toolRegs.iterator(); it.hasNext(); ) 
					{
						Tool t = it.next();
						PortletTool tool = new PortletTool(pdd, iPortlet, portalContext, t);
						activeToolManager.register(tool, portalContext);
					}
				}

			}
		}
		catch (Exception e)
		{
			log.warn("Failed to register portlets as tools ", e);
		}
	}

	// See if there are any sakai-style tool registrations for this portlet
	public List<Tool> getRegistrationsForPortlet(PortletDD pdd, InternalPortletContext portlet,
			ServletContext portalContext)
	{

		String portletName = pdd.getPortletName();
		String appName = portlet.getApplicationId();

		List<Tool> toolRegs = null;

		// Check sakai.home first
		String homePath = ServerConfigurationService.getSakaiHomePath() + "/portlets/";
		String portletReg = homePath + appName + "/" + portletName + ".xml";

		File toolRegFile = new File(portletReg);
		if (!toolRegFile.canRead())
		{
			portletReg = homePath + portletName + ".xml";
			toolRegFile = new File(portletReg);
		}

		// Attempt to read and parse the registration file
		toolRegs = activeToolManager.parseTools(new File(portletReg));
		if ( toolRegs != null ) 
		{
			log.info("Found "+toolRegs.size()+" Tool(s) to register from="+portletReg);
		}
		
		// If not there - do we have one in the webapp?
		if ( toolRegs == null )
		{
			// See if we have a registration in the portlet itself
			String webappRegPath = "/WEB-INF/sakai/"+portletName+".xml";
			InputStream is = portalContext.getResourceAsStream(webappRegPath);
			if ( is != null ) 
			{
				toolRegs = activeToolManager.parseTools(is);
				if ( toolRegs != null ) 
				{
					log.info("Found "+toolRegs.size()+" Tool(s) to register from="+webappRegPath);
				}
			}
		}

		return toolRegs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.pluto.spi.optional.PortletRegistryListener#portletApplicationRemoved(org.apache.pluto.spi.optional.PortletRegistryEvent)
	 */
	public void portletApplicationRemoved(PortletRegistryEvent arg0)
	{
		// the ToolManager will not allow deregister unfortunately.
	}

	public void init()
	{
		registry = PortletContextManager.getManager();
		registry.addPortletRegistryListener(this);
	}

	public void destroy()
	{
		registry.removePortletRegistryListener(this);
	}

	/**
	 * @return the activeToolManager
	 */
	public ActiveToolManager getActiveToolManager()
	{
		return activeToolManager;
	}

	/**
	 * @param activeToolManager
	 *        the activeToolManager to set
	 */
	public void setActiveToolManager(ActiveToolManager activeToolManager)
	{
		this.activeToolManager = activeToolManager;
	}

}
