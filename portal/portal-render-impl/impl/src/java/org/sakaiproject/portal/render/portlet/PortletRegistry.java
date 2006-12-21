package org.sakaiproject.portal.render.portlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.portlet.services.SakaiPortletContainerServices;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;

/**
 * A cache of all portlets windows.
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public class PortletRegistry
{

	/**
	 * Map of all portlet windows within the portal.
	 */
	private Map<String, SakaiPortletWindow> portletWindows;

	public PortletRegistry()
	{
		this.portletWindows = new HashMap<String, SakaiPortletWindow>();
	}

	/**
	 * Retrieve the portlet window for the specified placement.
	 * 
	 * @param placement
	 * @return
	 * @throws ToolRenderException
	 */
	public SakaiPortletWindow getOrCreatePortletWindow(Placement placement)
			throws ToolRenderException
	{
		if (!portletWindows.containsKey(placement.getId()))
		{
			createPortletWindow(placement);
		}
		return getPortletWindow(placement.getId());
	}

	/**
	 * Retrieve the PortletWindow for the given id.
	 * 
	 * @param placementId
	 * @return
	 */
	public SakaiPortletWindow getPortletWindow(String placementId)
	{
		return (SakaiPortletWindow) portletWindows.get(placementId);
	}

	private void createPortletWindow(Placement placement)
			throws ToolRenderException
	{
		SakaiPortletConfig pc = new SakaiPortletConfig(placement);
		if ( ! pc.isPortletConfig() ) {
			return;
		}
		String windowId = placement.getId();
		SakaiPortletWindow window = new SakaiPortletWindow(windowId,
				pc.contextPath, pc.portletName);
		portletWindows.put(windowId, window);
	}

	public boolean isPortletApplication(Placement placement)
	{
		SakaiPortletConfig pc = new SakaiPortletConfig(placement);
		return pc.isPortletConfig();
	}

	public class SakaiPortletConfig
	{
		private String portletName = null;
		private String contextPath = null;
		private boolean portlet = false;

		public SakaiPortletConfig(Placement placement)
		{
			if (placement == null)
			{
				return;
			}
			
			Properties toolProperties = placement.getPlacementConfig();
			if (toolProperties != null)
			{
				contextPath = toolProperties
						.getProperty(PortalService.TOOL_PORTLET_CONTEXT_PATH);
				portletName = toolProperties
						.getProperty(PortalService.TOOL_PORTLET_NAME);
			}
			Properties configProperties = placement.getConfig();
			if (configProperties != null)
			{
				if (isEmpty(contextPath))
				{
					contextPath = configProperties
							.getProperty(PortalService.TOOL_PORTLET_CONTEXT_PATH);
				}
				if (isEmpty(portletName))
				{
					portletName = configProperties
							.getProperty(PortalService.TOOL_PORTLET_NAME);
				}
			}
			portlet =  !(isEmpty(contextPath) || isEmpty(portletName));

		}
		public boolean isPortletConfig() {
			return portlet;
		}

	}

	private static boolean isEmpty(String string)
	{
		return string == null || string.trim().length() == 0;

	}

	public void reset(ToolConfiguration configuration)
	{
		portletWindows.remove(configuration.getId());
	}
}
