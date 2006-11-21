package org.sakaiproject.portal.render.api;

import org.sakaiproject.site.api.ToolConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * Service responsible for rendering tools within
 * a Sakai portal.
 * 
 */
public interface ToolRenderService {

    /**
     * Perfrorm any preperatory processing
     * for the specified tool.
     *
     * @param toolConfiguration
     * @param request
     * @param response
     * @throws IOException
     * @throws ToolRenderException
     */
    void preprocess(ToolConfiguration toolConfiguration,
                    HttpServletRequest request, HttpServletResponse response,
                    ServletContext context)
        throws IOException, ToolRenderException;

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
                ServletContext context)
        throws IOException, ToolRenderException;


}
