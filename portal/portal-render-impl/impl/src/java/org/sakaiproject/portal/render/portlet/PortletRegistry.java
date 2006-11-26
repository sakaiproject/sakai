package org.sakaiproject.portal.render.portlet;

import org.apache.pluto.PortletWindow;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.site.api.ToolConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A cache of all portlets windows.
 *
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public class PortletRegistry {

    /**
     * this is the property in the tool config that defines the portlet context
     * of tool. At the moment we assume that this is in the read-only properties
     * of the tool, but there could be a generic tool placement that enabled any
     * portlet to be mounted
     */
    public static final String TOOL_PORTLET_CONTEXT_PATH = "portlet-context";

    /**
     * this is the property in the tool config that defines the name of the
     * portlet
     */
    public static final String TOOL_PORTLET_NAME = "portlet-name";

    /**
     * Map of all portlet windows within the portal.
     */
    private Map portletWindows;

    public PortletRegistry() {
        this.portletWindows = new HashMap();
    }

    /**
     * Retrieve the portlet window for the specified placement.
     * @param placement
     * @return
     * @throws ToolRenderException
     */
    public SakaiPortletWindow getOrCreatePortletWindow(Placement placement)
        throws ToolRenderException {
        if (!portletWindows.containsKey(placement.getId())) {
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
    public SakaiPortletWindow getPortletWindow(String placementId) {
        return (SakaiPortletWindow) portletWindows.get(placementId);
    }

    private void createPortletWindow(Placement placement) throws ToolRenderException {
        Properties placementProperties = placement.getConfig();
        String contextPath = placementProperties.getProperty(TOOL_PORTLET_CONTEXT_PATH);
        String portletName = placementProperties.getProperty(TOOL_PORTLET_NAME);

        if (isEmpty(contextPath) || isEmpty(portletName)) {
            throw new ToolRenderException("The tool placement does not have a context of a name, " +
                TOOL_PORTLET_CONTEXT_PATH +
                ":" + contextPath + "  " + TOOL_PORTLET_NAME + ":" + portletName, new Exception());
        }
        String windowId = placement.getId();
        PortletWindow window = new SakaiPortletWindow(windowId, contextPath, portletName);
        portletWindows.put(windowId, window);
    }

    static boolean isPortletApplication(ToolConfiguration configuration) {
        Properties toolProperties = configuration.getConfig();
        if (toolProperties.containsKey(TOOL_PORTLET_CONTEXT_PATH) &&
            toolProperties.containsKey(TOOL_PORTLET_NAME)) {
            return true;
        }
        return false;
    }

    private boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;

    }
}

