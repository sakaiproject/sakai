package org.sakaiproject.portal.render.portlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerFactory;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.render.portlet.services.SakaiPortalCallbackService;
import org.sakaiproject.portal.render.portlet.services.SakaiPortalContext;
import org.sakaiproject.portal.render.portlet.services.SakaiPortletContainerServices;
// TODO: Review by David
import org.sakaiproject.portal.render.portlet.servlet.SakaiServletActionRequest;
import org.sakaiproject.portal.render.portlet.servlet.SakaiServletRequest;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;
import org.sakaiproject.portal.render.portlet.services.state.PortletStateAccess;
import org.sakaiproject.portal.render.portlet.services.state.PortletStateEncoder;
import org.sakaiproject.portal.render.portlet.servlet.BufferedServletResponse;
import org.sakaiproject.site.api.ToolConfiguration;

import javax.portlet.PortletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 */
public class PortletToolRenderService implements ToolRenderService {

    /**
     * Log instance used for all instances of this service.
     */
    private static final Log LOG =
        LogFactory.getLog(PortletToolRenderService.class);


    /**
     * Portlet Container instance used by this
     * service.
     */
    private PortletContainer container;

    /**
     *Portlet Registry used by this service.
     */
    private PortletRegistry registry = new PortletRegistry();


    private PortletStateEncoder portletStateEncoder;

    public PortletStateEncoder getPortletStateEncoder() {
        return portletStateEncoder;
    }

    public void setPortletStateEncoder(PortletStateEncoder portletStateEncoder) {
        this.portletStateEncoder = portletStateEncoder;
    }


    public boolean preprocess(HttpServletRequest request,
                              HttpServletResponse response,
                              ServletContext context)
        throws IOException {


        String stateParam = request.getParameter(SakaiPortalCallbackService.PORTLET_STATE_QUERY_PARAM);

        // If there is not state parameter, short circuit
        if (stateParam == null) {
            return true;
        }

        PortletState state = portletStateEncoder.decode(stateParam);
        PortletStateAccess.setPortletState(request, state);

        if (LOG.isDebugEnabled()) {
            LOG.debug("New Portlet State retrieved for Tool '"+state.getId()+".");
        }

        if (state.isAction()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Processing action for placement id " + state.getId());
            }

            PortletStateAccess.setPortletState(request, state);
            SakaiPortletWindow window =
                isIn168TestMode(request) ?
                    createPortletWindow(state.getId()) :
                    registry.getPortletWindow(state.getId());
            window.setState(state);

            try {
                PortletContainer portletContainer = getPortletContainer(context);

                // TODO: Review by David - I nned to make sure that I fully understand
                // Understand why we go through this fuss to make parameters come from state
                // rather than the request object - thus breaking everything - Chuck
                // Note that in Pluto - they do no such wrapping or parameter faking
                // "pluto-portal-driver/src/main/java/org/apache/pluto/driver/PortalDriverServlet.java" line 118 of 192 -
                // This is related to making the user-added parameters direct on the URL modifications
                // in the state encoding process.

                portletContainer.doAction(window, new SakaiServletActionRequest(request, state), response);

                // portletContainer.doAction(window, request, response);
            } catch (PortletException e) {
                throw new ToolRenderException(e.getMessage(), e);
            } catch (PortletContainerException e) {
                throw new ToolRenderException(e.getMessage(), e);
            } finally {
                state.setAction(false);
            }
            // TODO: This was false - but makes no sense to be false - check with David - /Chuck
            return true;
        }
        return true;
    }

    public RenderResult render(ToolConfiguration toolConfiguration,
                               final HttpServletRequest request,
                               final HttpServletResponse response,
                               ServletContext context)
        throws IOException {

        final SakaiPortletWindow window = isIn168TestMode(request) ?
            createPortletWindow(toolConfiguration.getId()) :
            registry.getOrCreatePortletWindow(toolConfiguration);

        PortletState state =
            PortletStateAccess.getPortletState(request, window.getId().getStringId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved PortletState from request cache.  Applying to window.");
        }


        if (state == null) {
            state = new PortletState(window.getId().getStringId());
            PortletStateAccess.setPortletState(request, state);
        }
        
        window.setState(state);


        try {

            // TODO: Review David
            final HttpServletRequest req = new SakaiServletRequest(request, state);
            final PortletContainer portletContainer = getPortletContainer(context);

            return new RenderResult() {
                private BufferedServletResponse bufferedResponse = null;

                private void renderResponse() throws ToolRenderException {
                    if (bufferedResponse == null) {
                        bufferedResponse = new BufferedServletResponse(response);
                        try {
                            // TODO: Review David
                            portletContainer.doRender(window, req, bufferedResponse);
                            // portletContainer.doRender(window, request, bufferedResponse);
                        }
                        catch (PortletException e) {
                            throw new ToolRenderException(e.getMessage(), e);
                        }
                        catch (IOException e) {
                            throw new ToolRenderException(e.getMessage(), e);
                        }
                        catch (PortletContainerException e) {
                            throw new ToolRenderException(e.getMessage(), e);
                        }
                    }
                }

                public String getContent() throws ToolRenderException {
                    renderResponse();
                    return bufferedResponse.getInternalBuffer().getBuffer().toString();
                }

                public String getTitle() throws ToolRenderException {
                    renderResponse();
                    // TODO: Review David
                    return PortletStateAccess.getPortletState(req, window.getId().getStringId()).getTitle();
                    // return PortletStateAccess.getPortletState(request, window.getId().getStringId()).getTitle();
                }

            };

        } catch (PortletContainerException e) {
            throw new ToolRenderException(e.getMessage(), e);
        }
    }

    private SakaiPortletWindow createPortletWindow(String windowId) {
        String contextPath = "/rsf";
        String portletName = "numberguess";
        return new SakaiPortletWindow(windowId, contextPath, portletName);
    }


    private PortletContainer getPortletContainer(ServletContext context)
        throws PortletContainerException {
        if (container == null) {
            container = createPortletContainer();
            container.init(context);
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

    private static boolean isIn168TestMode(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if(session.getAttribute("test168") != null ||
           request.getParameter("test168") != null) {
            request.getSession(true).setAttribute("test168", Boolean.TRUE.toString());
            return true;
        }
        return false;
    }

    private boolean isPortletApplication(ServletContext context, ToolConfiguration configuration)
        throws ToolRenderException, MalformedURLException {
        SakaiPortletWindow window = registry.getOrCreatePortletWindow(configuration);
        if ( window == null ) {
        	return false;
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Checking context for potential portlet ");
        }
        ServletContext crossContext = context.getContext(window.getContextPath());
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Got servlet context as  "+crossContext);
        	LOG.debug("Getting Context for path "+window.getContextPath());
        	LOG.debug("Base Path "+crossContext.getRealPath("/"));
        	LOG.debug("Context Name "+crossContext.getServletContextName());
        	LOG.debug("Server Info "+crossContext.getServerInfo());
        	LOG.debug("      and it is a portlet ? :"+(crossContext.getResource("/WEB-INF/portlet.xml") != null));
        }
        return crossContext.getResource("/WEB-INF/portlet.xml") != null;
    }

	public boolean accept(ToolConfiguration configuration,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext context) {
		try
		{
			if ( isIn168TestMode(request) ) {
				LOG.warn("In portlet test mode");
				return true;
			}
			if ( isPortletApplication(context, configuration)) {
        			if (LOG.isDebugEnabled()) {
					LOG.debug("Tool "+configuration.getToolId()+" is a portlet");
				}
				return true;
			}
        		if (LOG.isDebugEnabled()) {
				LOG.debug("Tool "+configuration.getToolId()+" is not a portlet");
			}
			return false;
		}
		catch (MalformedURLException e)
		{
			LOG.error("Failed to render ",e);
			return false;
		} catch (ToolRenderException e) {
            LOG.error("Failed to render ",e);
            return false;
        }
    }

	public void reset(ToolConfiguration configuration)
	{
		registry.reset(configuration);
	}

	
}
