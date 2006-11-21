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
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.portlet.PortletException;
import java.io.IOException;

/**
 *
 */
public class PortletToolRenderService implements ToolRenderService {

    private static final String CONTAINER_PARAM =
        "org.sakaiproject.portal.PORTLET_CONTAINER";

    private PortletStateEncoder portletStateEncoder;

    public PortletStateEncoder getPortletStateEncoder() {
        return portletStateEncoder;
    }

    public void setPortletStateEncoder(PortletStateEncoder portletStateEncoder) {
        this.portletStateEncoder = portletStateEncoder;
    }


    public void preprocess(ToolConfiguration toolConfiguration,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        ServletContext context)
        throws IOException, ToolRenderException {
        String stateParam = request.getParameter(SakaiPortalCallbackService.PORTLET_STATE_QUERY_PARAM);

        Tool tool = toolConfiguration.getTool();
        Placement placement = toolConfiguration;

        if (stateParam != null) {
            PortletState state = portletStateEncoder.decode(stateParam);
            if (state.isAction() && state.getId().equals(tool.getId())) {
                PortletStateAccess.setPortletState(request, state);
                SakaiPortletWindow window = createPortletWindow(tool, placement);
                window.setState(state);
                try {
                    PortletContainer portletContainer = getPortletContainer(context);
                    portletContainer.doAction(window, request, response);
                } catch (PortletException e) {
                    throw new ToolRenderException(e.getMessage(), e);
                } catch (PortletContainerException e) {
                    throw new ToolRenderException(e.getMessage(), e);
                }
            }
        }
    }

    public RenderResult render(ToolConfiguration toolConfiguration,
                       final HttpServletRequest request,
                       final HttpServletResponse response,
                       ServletContext context)
            throws IOException, ToolRenderException {

        Tool tool = toolConfiguration.getTool();

        final SakaiPortletWindow window = createPortletWindow(tool, toolConfiguration);
        PortletState state = PortletStateAccess.getPortletState(request, window.getId().getStringId());
        if(state != null) {
            window.setState(state);
        }
        try {
            final PortletContainer portletContainer = getPortletContainer(context);


            RenderResult result = new RenderResult(){
            	private BufferedServletResponse bufferedResponse = null;
            	private void renderResponse() throws ToolRenderException {
            		if ( bufferedResponse == null ) {
            			bufferedResponse = new BufferedServletResponse(response);
            			try
						{
							portletContainer.doRender(window, request, bufferedResponse);
						}
						catch (PortletException e)
						{
							throw new ToolRenderException(e.getMessage(),e);
						}
						catch (IOException e)
						{
							throw new ToolRenderException(e.getMessage(),e);
						}
						catch (PortletContainerException e)
						{
							throw new ToolRenderException(e.getMessage(),e);
						}	
            		}                	
            	}
				public String getContent() throws ToolRenderException
				{
					renderResponse();
                    return bufferedResponse.getInternalBuffer().getBuffer().toString();
				}
				public String getTitle() throws ToolRenderException
				{
					renderResponse();
			        return PortletAttributesAccess.getPortletAttributes(request, window).getTitle();
				}
            	
            };
            return result;

        } catch (PortletContainerException e) {
            throw new ToolRenderException(e.getMessage(), e);
        }
    }

    private SakaiPortletWindow createPortletWindow(Tool tool, Placement placement) {
//        String contextPath = placement.getContext();
//        String portletName = tool.getId();
        String contextPath = "/testsuite";
        String portletName = "TestPortlet1";
        String windowId = placement.getId();
        return new SakaiPortletWindow(windowId, contextPath, portletName);
    }


    private PortletContainer getPortletContainer(ServletContext context)
    throws PortletContainerException {
        PortletContainer container = (PortletContainer)
            context.getAttribute(CONTAINER_PARAM);

        if(container == null) {
            container = createPortletContainer();
            container.init(context);
            context.setAttribute(CONTAINER_PARAM, container);
        }

        return container;
    }

    private PortletContainer createPortletContainer()
        throws PortletContainerException {
        SakaiPortletContainerServices services = new SakaiPortletContainerServices();
        services.setPortalCallbackService(new SakaiPortalCallbackService());
        services.setPortalContext(new SakaiPortalContext());
        return PortletContainerFactory.getInstance().createContainer("sakai", services);
    }
}
