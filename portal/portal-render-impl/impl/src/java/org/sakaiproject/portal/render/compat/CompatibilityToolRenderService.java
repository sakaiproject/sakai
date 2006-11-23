package org.sakaiproject.portal.render.compat;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Render serivice used to support both Portlet and
 * iframe based tools.
 */
public class CompatibilityToolRenderService implements ToolRenderService {

    private static final Log LOG =
            LogFactory.getLog(CompatibilityToolRenderService.class);


    private List renderServices = null;


    public void preprocess(ToolConfiguration configuration,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext context)
            throws IOException, ToolRenderException {
    	for ( Iterator i = renderServices.iterator(); i.hasNext(); ) {
    		ToolRenderService trs = (ToolRenderService) i.next();
    		if ( trs.accept(configuration, request, response, context) ) {
  			  	LOG.warn("Preprocessing with "+trs);
    			trs.preprocess(configuration, request, response, context) ;
    			return;
    		}
        }
    	throw new ToolRenderException("No available Tool Render Service for the tool "+configuration.getToolId());
    }

    public RenderResult render(ToolConfiguration configuration,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       ServletContext context)
            throws IOException, ToolRenderException {

    	for ( Iterator i = renderServices.iterator(); i.hasNext(); ) {
    		ToolRenderService trs = (ToolRenderService) i.next();
    		if ( trs.accept(configuration, request, response, context) ) {
    			  LOG.warn("Rendering with "+trs);
    			  return trs.render(configuration, request, response, context);
            }		
    	}
    	throw new ToolRenderException("No available Tool Render Service for the tool "+configuration.getToolId());
    }

	public boolean accept(ToolConfiguration configuration, HttpServletRequest request, HttpServletResponse response, ServletContext context)
	{
		for ( Iterator i = renderServices.iterator(); i.hasNext(); ) {
    		ToolRenderService trs = (ToolRenderService) i.next();
    		if ( trs.accept(configuration, request, response, context) ){
    			return true;
    		}
        }
		return false;
	}

	/**
	 * @return the renderServices
	 */
	public List getRenderServices()
	{
		return renderServices;
	}

	/**
	 * @param renderServices the renderServices to set
	 */
	public void setRenderServices(List renderServices)
	{
		this.renderServices = renderServices;
	}

}
