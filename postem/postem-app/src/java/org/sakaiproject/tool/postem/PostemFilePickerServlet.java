/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.postem;


import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Web;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class PostemFilePickerServlet extends JsfTool
{
  private static final String HELPER_EXT = ".helper";
  
  private static final String HELPER_SESSION_PREFIX = "session.";
  
	protected void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// NOTE: this is a simple path dispatching, taking the path as the view id = jsp file name for the view,
		//       with default used if no path and a path prefix as configured.
		// TODO: need to allow other sorts of dispatching, such as pulling out drill-down ids and making them
		//       available to the JSF

		// build up the target that will be dispatched to
		String target = req.getPathInfo();

		// see if we have a helper request
    if (sendToHelper(req, res, target)) {
       return;
    }
		
		// see if we have a resource request - i.e. a path with an extension, and one that is not the JSF_EXT
		if (isResourceRequest(target))
		{
			// get a dispatcher to the path
			RequestDispatcher resourceDispatcher = getServletContext().getRequestDispatcher(target);
			if (resourceDispatcher != null)
			{
				resourceDispatcher.forward(req, res);
				return;
			}
		}

		if ("Title".equals(req.getParameter("panel")))
		{
			// This allows only one Title JSF for each tool
			target = "/title.jsf";
		}

		else
		{
			ToolSession session = SessionManager.getCurrentToolSession();

			if (target == null || "/".equals(target))
			{
				target = computeDefaultTarget();
				
				// make sure it's a valid path
				if (!target.startsWith("/"))
				{
					target = "/" + target;
				}

				// now that we've messed with the URL, send a redirect to make it official
				res.sendRedirect(Web.returnUrl(req, target));
				return;
			}

			// see if we want to change the specifically requested view
			String newTarget = redirectRequestedTarget(target);
			
			// make sure it's a valid path
			if (!newTarget.startsWith("/"))
			{
				newTarget = "/" + newTarget;
			}

			if (!newTarget.equals(target))
			{
				// now that we've messed with the URL, send a redirect to make it official
				res.sendRedirect(Web.returnUrl(req, newTarget));
				return;
			}
			target = newTarget;

			// store this
			if (m_defaultToLastView)
			{
				session.setAttribute(LAST_VIEW_VISITED, target);
			}
		}

		// add the configured folder root and extension (if missing)
		target = m_path + target;
		
		// add the default JSF extension (if we have no extension)
		int lastSlash = target.lastIndexOf("/");
		int lastDot = target.lastIndexOf(".");
		if (lastDot < 0 || lastDot < lastSlash)
		{
			target += JSF_EXT;
		}
				
		// set the information that can be removed from return URLs
		req.setAttribute(URL_PATH, m_path);
		req.setAttribute(URL_EXT, ".jsp");

		// set the sakai request object wrappers to provide the native, not Sakai set up, URL information
		// - this assures that the FacesServlet can dispatch to the proper view based on the path info
		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		// TODO: Should setting the HTTP headers be moved up to the portal level as well?
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// dispatch to the target
		/*M_log.debug("dispatching path: " + req.getPathInfo() + " to: " + target + " context: "
				+ getServletContext().getServletContextName());*/
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
		dispatcher.forward(req, res);

		// restore the request object
		req.removeAttribute(Tool.NATIVE_URL);
		req.removeAttribute(URL_PATH);
		req.removeAttribute(URL_EXT);
	}

  protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res, String target) throws ToolException {
    String path = req.getPathInfo();
    if (path == null) path = "/";

    // 0 parts means the path was just "/", otherwise parts[0] = "", parts[1] = item id, parts[2] if present is "edit"...
    String[] parts = path.split("/");

    if (parts.length < 2) {
       return false;
    }

    if (!parts[1].endsWith(HELPER_EXT)) {
       return false;
    }

    ToolSession toolSession = SessionManager.getCurrentToolSession();

    Enumeration params = req.getParameterNames();
    while (params.hasMoreElements()) {
       String paramName = (String)params.nextElement();
       if (paramName.startsWith(HELPER_SESSION_PREFIX)) {
          String attributeName = paramName.substring(HELPER_SESSION_PREFIX.length());
          toolSession.setAttribute(attributeName, req.getParameter(paramName));
       }
    }

    // calc helper id
    int posEnd = parts[1].lastIndexOf(".");

    String helperId = target.substring(1, posEnd + 1);
    ActiveTool helperTool = ActiveToolManager.getActiveTool(helperId);

    if (toolSession.getAttribute(helperTool.getId() + Tool.HELPER_DONE_URL) == null && !target.equals("/sakai.filepicker.helper")) {
       toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
             req.getContextPath() + req.getServletPath() + computeDefaultTarget(true));
    }
    
/*comment out for using the global parameter rather than tool-by-tool setting
    SessionState state = UsageSessionService.getSessionState(toolSession.getPlacementId());
		boolean show_other_sites = ServerConfigurationService.getBoolean("syllabus.resources.show_all_collections.helper", true);
		state.setAttribute("resources.allow_user_to_see_all_sites", (new Boolean(show_other_sites)).toString());
		state.setAttribute("resources.user_chooses_to_see_other_sites", (new Boolean(show_other_sites)).toString());
*/
    String context = req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 2);
    String toolPath = Web.makePath(parts, 2, parts.length);
    helperTool.help(req, res, context, toolPath);

    return true; // was handled as helper call
  }

  protected String computeDefaultTarget(boolean lastVisited)
  {
     // setup for the default view as configured
     String target = "/" + m_default;

     // if we are doing lastVisit and there's a last-visited view, for this tool placement / user, use that
     if (lastVisited)
     {
        ToolSession session = SessionManager.getCurrentToolSession();
        String last = (String) session.getAttribute(LAST_VIEW_VISITED);
        if (last != null)
        {
           target = last;
        }
     }

     return target;
  }
}
