package org.sakaiproject.portal.render.api;

import org.sakaiproject.site.api.ToolConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * Service responsible for preprocessing and rendering tools
 * within a Sakai portal.
 *
 * @since Sakai 2.2.3
 * @version $Rev$
 */
public interface ToolRenderService
{

    /**
     * Perfrorm any preperatory processing for the specified tool.
     *
     * @param request the servlet request
     * @param response the servlet response.
     * @param context the portal servlet context
     * @return indicates whether or not processing should be continued.
     * @throws IOException if an error occurs during preprocessing.
     */
    boolean preprocess(HttpServletRequest request, HttpServletResponse response,
                    ServletContext context)
        throws IOException;

	/**
	 * Render the tool.
	 * 
	 * @param toolConfiguration
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ToolRenderException
	 */
	RenderResult render(ToolConfiguration toolConfiguration,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException, ToolRenderException;

	/**
	 * The render service will accept responsibility for a tool. This enables a
	 * controller to check if the render service can manage the tool
	 * 
	 * @param configuration tool configuration for the tool in question
	 * @param request
	 * @param response
	 * @param context - this is the servlet context handling the request (ie the portal)
	 * @return
	 */
	boolean accept(ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context);

}
