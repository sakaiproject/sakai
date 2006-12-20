package org.sakaiproject.portal.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortletApplicationDescriptor;
import org.sakaiproject.portal.api.PortletDescriptor;


public class PlacementPortlet extends GenericPortlet {

	private PortletRenderEngine rengine;
	private PortalService portalService;

	@Override
	public void init(PortletConfig config) throws PortletException
	{
		
		
		super.init(config);
        // this should be a spring bean, but for the moment I dont want to bind
        // to spring.
        String renderEngineClass = config.getInitParameter("renderEngineImpl");
        if (renderEngineClass == null || renderEngineClass.trim().length() == 0) {
            renderEngineClass = PortletRenderEngine.DEFAULT_RENDER_ENGINE;
        }

        try {
            Class c = Class.forName(renderEngineClass);
            rengine = (PortletRenderEngine) c.newInstance();
            rengine.init();
        }
        catch (Exception e) {
            throw new PortletException("Failed to start velocity ", e);
        }
        portalService = org.sakaiproject.portal.api.cover.PortalService.getInstance();
	}

    public void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
   
    	PortletRenderContext rcontext = rengine.newRenderContext(request);
    	rcontext.put("test", "testing");    	
    	rcontext.put("portletapplications", getPortletApplications(response));
    	try
		{
    		response.setContentType("text/html");
			rengine.render("view", rcontext,response.getWriter());
		}
		catch (Exception e)
		{
			throw new PortletException("Failed to render view template ",e);
		}
    }

    protected void doEdit(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
    	PortletRenderContext rcontext = rengine.newRenderContext(request);
    	rcontext.put("test", "testing");    	
    	rcontext.put("portletapplications", getPortletApplications(response));
    	
    	try
		{
    		response.setContentType("text/html");
			rengine.render("edit", rcontext,response.getWriter());
		}
		catch (Exception e)
		{
			throw new PortletException("Failed to render edit template ",e);
		}
    }

    private List<Map> getPortletApplications(RenderResponse response) throws PortletModeException
	{
    	List<Map> portletApps = new ArrayList<Map>();
    	for ( Iterator<PortletApplicationDescriptor> i = portalService.getRegisteredApplications(); i.hasNext();) {
    		PortletApplicationDescriptor portletApplication = i.next();
    		Map<String, Object> portletApp = new HashMap<String, Object>();    		
    		portletApp.put("name", portletApplication.getApplicationContext());
    		List<Map> portlets = new ArrayList<Map>();
    		for ( Iterator<PortletDescriptor> pai  = portletApplication.getPortlets(); pai.hasNext(); ) {
    			PortletDescriptor pd = pai.next();
    			Map<String, String> portletDesc = new HashMap<String, String>();
    			portletDesc.put("name", pd.getPortletName());
    			PortletURL purl = response.createActionURL();
    			purl.setParameter("context", portletApplication.getApplicationContext());
    			purl.setParameter("portlet", pd.getPortletId());
    			purl.setPortletMode(PortletMode.VIEW);
    			portletDesc.put("url",purl.toString());
    			portlets.add(portletDesc);
    		}
    		portletApp.put("portlets", portlets);
    	}
    	return portletApps;
    	
	}

	protected void doHelp(RenderRequest request, RenderResponse response)
    throws PortletException, IOException {
    	PortletRenderContext rcontext = rengine.newRenderContext(request);
    	rcontext.put("test", "testing");    	
    	try
		{
    		response.setContentType("text/html");
			rengine.render("help", rcontext,response.getWriter());
		}
		catch (Exception e)
		{
			throw new PortletException("Failed to render help template ",e);
		}
    }
}
