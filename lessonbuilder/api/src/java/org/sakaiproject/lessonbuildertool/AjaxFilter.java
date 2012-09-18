/**********************************************************************************
 * $URL: 
 * $Id: 
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2012 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool;
 
import javax.servlet.*;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.ToolConfiguration; 
import org.sakaiproject.tool.api.Tool;

public class AjaxFilter implements javax.servlet.Filter
{
    private FilterConfig filterConfig;
 
    public void doFilter(ServletRequest request, ServletResponse response,
			 FilterChain chain) 
	throws java.io.IOException, javax.servlet.ServletException
    {
	// rsf does this:
	//Tool tool = (Tool) request.getAttribute("sakai.tool");
	//placement = (Placement) request.getAttribute("sakai.tool.placement");
	// we need to set it.

	String placementId = request.getParameter("placementId");
	ToolConfiguration placement = SiteService.findTool(placementId);
	Tool tool = placement.getTool();

	request.setAttribute("sakai.tool", tool);
	request.setAttribute("sakai.tool.placement", placement);

        chain.doFilter(request, response);
    }
 
    public void init(final FilterConfig filterConfig)
    {
        this.filterConfig = filterConfig;
    }
 
    public void destroy()
    {
        filterConfig = null;
    }
}