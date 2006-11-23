package org.sakaiproject.portal.render.portlet;

import org.sakaiproject.portal.render.portlet.services.state.PortletStateAccess;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;
import org.sakaiproject.portal.render.portlet.services.state.encode.PortletStateEncoder;
import org.sakaiproject.portal.render.portlet.services.SakaiPortalCallbackService;
import org.sakaiproject.portal.render.portlet.services.SakaiPortletContainerServices;
import org.sakaiproject.portal.render.portlet.services.SakaiPortalContext;
import org.sakaiproject.portal.render.portlet.services.PortletAttributesAccess;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.site.api.ToolConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.portlet.PortletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 *
 */
public class PortletToolRenderService implements ToolRenderService
{

	private static final String CONTAINER_PARAM = "org.sakaiproject.portal.PORTLET_CONTAINER";

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

	private static final Log log = LogFactory.getLog(PortletToolRenderService.class);

	private PortletStateEncoder portletStateEncoder;

	public PortletStateEncoder getPortletStateEncoder()
	{
		return portletStateEncoder;
	}

	public void setPortletStateEncoder(PortletStateEncoder portletStateEncoder)
	{
		this.portletStateEncoder = portletStateEncoder;
	}

	public void preprocess(ToolConfiguration toolConfiguration,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException, ToolRenderException
	{
		String stateParam = request
				.getParameter(SakaiPortalCallbackService.PORTLET_STATE_QUERY_PARAM);

		Tool tool = toolConfiguration.getTool();
		Placement placement = toolConfiguration;

		if (stateParam != null)
		{
			PortletState state = portletStateEncoder.decode(stateParam);
			if (state.isAction() && state.getId().equals(tool.getId()))
			{
				PortletStateAccess.setPortletState(request, state);
				SakaiPortletWindow window = createPortletWindow(tool, placement);
				window.setState(state);
				try
				{
					PortletContainer portletContainer = getPortletContainer(context);
					portletContainer.doAction(window, request, response);
				}
				catch (PortletException e)
				{
					throw new ToolRenderException(e.getMessage(), e);
				}
				catch (PortletContainerException e)
				{
					throw new ToolRenderException(e.getMessage(), e);
				}
			}
		}
	}

	public RenderResult render(ToolConfiguration toolConfiguration,
			final HttpServletRequest request,
			final HttpServletResponse response, ServletContext context)
			throws IOException, ToolRenderException
	{

		Tool tool = toolConfiguration.getTool();

		final SakaiPortletWindow window = createPortletWindow(tool,
				toolConfiguration);
		PortletState state = PortletStateAccess.getPortletState(request, window
				.getId().getStringId());
		if (state != null)
		{
			window.setState(state);
		}
		try
		{
			final PortletContainer portletContainer = getPortletContainer(context);

			RenderResult result = new RenderResult()
			{
				private BufferedServletResponse bufferedResponse = null;

				private void renderResponse() throws ToolRenderException
				{
					if (bufferedResponse == null)
					{
						bufferedResponse = new BufferedServletResponse(response);
						try
						{
							portletContainer.doRender(window, request,
									bufferedResponse);
						}
						catch (PortletException e)
						{
							throw new ToolRenderException(e.getMessage(), e);
						}
						catch (IOException e)
						{
							throw new ToolRenderException(e.getMessage(), e);
						}
						catch (PortletContainerException e)
						{
							throw new ToolRenderException(e.getMessage(), e);
						}
					}
				}

				public String getContent() throws ToolRenderException
				{
					renderResponse();
					return bufferedResponse.getInternalBuffer().getBuffer()
							.toString();
				}

				public String getTitle() throws ToolRenderException
				{
					renderResponse();
					return PortletAttributesAccess.getPortletAttributes(
							request, window).getTitle();
				}

			};
			return result;

		}
		catch (PortletContainerException e)
		{
			throw new ToolRenderException(e.getMessage(), e);
		}
	}

	private SakaiPortletWindow createPortletWindow(Tool tool,
			Placement placement) throws ToolRenderException
	{
		// String contextPath = placement.getContext();
		// String portletName = tool.getId();
		// String contextPath = "/testsuite";
		// String portletName = "TestPortlet1";
		Properties placementProperties = placement.getConfig();
		String contextPath = placementProperties
				.getProperty(TOOL_PORTLET_CONTEXT_PATH);
		String portletName = placementProperties.getProperty(TOOL_PORTLET_NAME);
		if (contextPath == null || contextPath.trim().length() == 0
				|| portletName == null || portletName.trim().length() == 0)
		{
			throw new ToolRenderException("The tool placement does not have a context of a name, "+
					TOOL_PORTLET_CONTEXT_PATH+
					":"+contextPath+"  "+TOOL_PORTLET_NAME+":"+portletName,new Exception());
		}
		String windowId = placement.getId();
		return new SakaiPortletWindow(windowId, contextPath, portletName);
	}

	private PortletContainer getPortletContainer(ServletContext context)
			throws PortletContainerException
	{
		PortletContainer container = (PortletContainer) context
				.getAttribute(CONTAINER_PARAM);

		if (container == null)
		{
			container = createPortletContainer();
			container.init(context);
			context.setAttribute(CONTAINER_PARAM, container);
		}

		return container;
	}

	private PortletContainer createPortletContainer()
			throws PortletContainerException
	{
		SakaiPortletContainerServices services = new SakaiPortletContainerServices();
		services.setPortalCallbackService(new SakaiPortalCallbackService());
		services.setPortalContext(new SakaiPortalContext());
		return PortletContainerFactory.getInstance().createContainer("sakai",
				services);
	}
	
    private boolean isIn168TestMode(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if(session.getAttribute("test168") != null ||
           request.getParameter("test168") != null) {
            request.getSession(true).setAttribute("test168", Boolean.TRUE.toString());
            return true;
        }
        return false;
    }

    private boolean isPortletApplication(ServletContext context, ToolConfiguration configuration)
            throws MalformedURLException {
    	Properties toolProperties = configuration.getConfig();
     	String contextPath = toolProperties.getProperty(TOOL_PORTLET_CONTEXT_PATH);
		String portletName = toolProperties.getProperty(TOOL_PORTLET_NAME);
		if (contextPath == null || contextPath.trim().length() == 0
				|| portletName == null || portletName.trim().length() == 0)
		{
        	log.warn("Tool "+configuration.getToolId()+":"+configuration.getId()+" is not registered as a portlet because portletpath is "+contextPath+" and portlet Name is "+portletName);
			return false;
		}
    	
        ServletContext crossContext = context.getContext(contextPath);
        if ( crossContext == null ||
                crossContext.getResource("/WEB-INF/portlet.xml") == null ) 
        {
        	log.warn("Tool "+configuration.getToolId()+" is registered as a portlet, but the context "+contextPath+" which is "+crossContext+" does not contain a portlet ");
        	return false;
        }
    	log.warn("Tool "+configuration.getToolId()+" is registered as a portlet with the context "+contextPath+" which is "+crossContext+" does not contain a portlet ");
        return true;
    }

	public boolean accept(ToolConfiguration configuration, HttpServletRequest request, HttpServletResponse response, ServletContext context)
	{
		try
		{
			if ( isIn168TestMode(request) ) {
				log.warn("In portlet test mode");
				return true;
			}
			if ( isPortletApplication(context, configuration)) {
				log.warn("Tool "+configuration.getToolId()+" is a portlet");
				return true;				
			}
			log.warn("Tool "+configuration.getToolId()+" is not a portlet");
			return false;
		}
		catch (MalformedURLException e)
		{
			log.error("Failed to render ",e);
			return false;
		}
	}

	public static boolean isPortletTool(ToolConfiguration configuration)
	{
	   	Properties toolProperties = configuration.getConfig();
	   	if ( toolProperties.containsKey(TOOL_PORTLET_CONTEXT_PATH) && 
	   			toolProperties.containsKey(TOOL_PORTLET_NAME) ) {
	   		return true;
	   	}
		return false;
	}

}
