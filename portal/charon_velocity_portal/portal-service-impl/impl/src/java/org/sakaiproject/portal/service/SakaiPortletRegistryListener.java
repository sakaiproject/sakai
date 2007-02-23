/**
 * 
 */
package org.sakaiproject.portal.service;

import java.util.Iterator;

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

/**
 * @author ieb
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
				PortletTool tool = new PortletTool(pdd, iPortlet, portalContext);
				String portlentName = pdd.getPortletName();
				activeToolManager.register(tool, portalContext);

			}
		}
		catch (Exception e)
		{
			log.warn("Failed to register portlets as tools ", e);
		}
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
