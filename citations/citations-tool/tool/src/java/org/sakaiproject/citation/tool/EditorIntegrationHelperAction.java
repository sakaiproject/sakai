/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/citations/trunk/citations-tool/tool/src/java/org/sakaiproject/citation/tool/CitationHelperAction.java $
 * $Id: CitationHelperAction.java 34481 2007-08-27 21:36:06Z dsobiera@indiana.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.citation.tool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Provide support for the Citations editor integration functionality
 */
@Slf4j
public class EditorIntegrationHelperAction extends CitationHelperAction
{
  /**
   * Editor Integration Library Resources Search
   */
  public void doIntegrationSearch(RunData data)
  {
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid());

    log.debug("doIntegrationSearch()");

    setCaller(state, Caller.EDITOR_INTEGRATION);
		doSearchCommon(state, Mode.ERROR);
  }

	/**
	 * Access the SessionState for the current request.  This is a private
	 * session state for this servlet/helper, scoped for the current request.
	 *
	 * @see org.sakaiproject.cheftool.ToolServlet#getState(RunData data)
	 *
	 * @param req The current portlet request.
	 * @return The SessionState objet for the current request.
	 */
	protected SessionState getState(HttpServletRequest req)
	{
		/*
		 * key the state based on the pid, if present. If not we will use the servlet's class name
		 */
		String key = getPid(req);

		if (key == null)
		{
			key = this.toString() + ".";
			log.warn("getState(): using servlet key: " + key);
		}
		/*
		 * Append our full name to form a unique session state key for this helper
		 */
    key += ".org.sakaiproject.citation.tool.EditorIntegrationHelperAction";

		SessionState rv = org.sakaiproject.event.cover.UsageSessionService.getSessionState(key);

		if (rv == null)
		{
		  log.warn("getState(): no state found for key: "
			        +   key
			        +   " "
			        +   req.getPathInfo()
			        +   " "
			        +   req.getQueryString()
			        +   " "
				    	+   req.getRequestURI());
		}
		return rv;
	}

	/**
	 * Add some standard references to the vm context.  Details:
	 *<p>
	 * The tool name (<code>toolName</code> is used to establish the HTML page
	 * title (see <code>#macro (chef_start)</code> in VM_chef_library.vm).
	 *<p>
	 * We set the tool name to reflect the editor integration search window.
	 *
	 * @see org.sakaiproject.vm.VmServlet#setVmStdRef(HttpServletRequest request, HttpServletResponse response)
	 *
	 * @param request The render request.
	 * @param response The render response.
	 */
	protected void setVmStdRef(HttpServletRequest request, HttpServletResponse response)
	{
    /*
     * Establish all of the common definitions
     */
		super.setVmStdRef(request, response);
    /*
     * Set the tool name to reflect our window
     */
    request.setAttribute("toolTitle", getPageTitle());
  }

	/*
	 * Helpers
	 */

  /*
   * Title segment delimiter
   */
  protected static String DELIMITER = " : ";
  /*
   * Default helper name
   */
  protected static String DEFAULT_HELPER_NAME = "Search Library Resources";

	/**
	 * Construct the page title.  This looks like:
	 *<p>
	 *<code>  Sakai-Instance : Site-Name : Tool-Name : Helper-Name  </code>
	 */
  private String getPageTitle()
  {
    StringBuilder   pageTitle   = new StringBuilder();
 		boolean         addedHelper = false;

	  Placement       placement;
	  Site            site;
 		String          sakaiInstance, siteId;

 		/*
 		 * Get the local brand (eg Oncourse, CTools, etc)
 		 */
 		sakaiInstance = ServerConfigurationService.getString("ui.service", "Sakai");
 		if (!isNull(sakaiInstance))
 		{
 		  pageTitle.append(sakaiInstance);
    }
    /*
     * Site name
     */
    placement = ToolManager.getCurrentPlacement();
    site = null;
    try
    {
	    site = SiteService.getSite(placement.getContext());
    }
    catch (Exception ignore) { }

    if (site != null)
    {
      String siteTitle = site.getTitle();

      if (!isNull(siteTitle))
      {
  	    if (pageTitle.length() > 0) pageTitle.append(DELIMITER);
	      pageTitle.append(siteTitle);
	    }
    }
    /*
     * Tool name
     */
    try
    {
      String toolTitle = placement.getTitle();

      if (!isNull(toolTitle))
      {
  	    if (pageTitle.length() > 0) pageTitle.append(DELIMITER);
	      pageTitle.append(toolTitle);
	    }
    }
    catch (Exception ignore) { }
    /*
     * Helper
     */
    try
    {
		  String helperTitle = ToolManager.getCurrentTool().getTitle();

      if (!isNull(helperTitle))
      {
  	    if (pageTitle.length() > 0) pageTitle.append(DELIMITER);
	      pageTitle.append(helperTitle);

        addedHelper = true;
	    }
		}
    catch (Exception ignore) { }
    /*
     * Make sure we have something to display
     */
    if (!addedHelper)
    {
 	    if (pageTitle.length() > 0) pageTitle.append(DELIMITER);
      pageTitle.append(DEFAULT_HELPER_NAME);
    }

    return pageTitle.toString();
  }

  /**
   * Null/empty String?
   * @param string String to verify
   * @return true if so
   */
  private boolean isNull(String string)
  {
    return (string == null) || (string.trim().length() == 0);
  }
}
