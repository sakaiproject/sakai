/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.jsf.util;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Customized JsfTool for Samigo - just to workaround the fact that Samigo
 * has the JSF URL mapping "*.faces" hard-coded in several places.  If
 * all instances of "*.faces" were changed to "*.jsf", this class could be removed.
 * </p>
 * 
 */
  public class SamigoJsfTool extends JsfTool {
    private static final String HELPER_EXT = ".helper";
    private static final String HELPER_SESSION_PREFIX = "session.";
    private static Log log = LogFactory.getLog(SamigoJsfTool.class);

    /**
         * Recognize a path that is a resource request. It must have an "extension", i.e. a dot followed by characters that do not include a slash.
	 * 
	 * @param path
	 *        The path to check
	 * @return true if the path is a resource request, false if not.
	 */

	protected boolean isResourceRequest(String path)
	{
	    System.out.println("****0. inside isResourceRequest, path="+path);
		// we need some path
		if ((path == null) || (path.length() == 0)) return false;

		// we need a last dot
		int pos = path.lastIndexOf(".");
		if (pos == -1) return false;

		// we need that last dot to be the end of the path, not burried in the path somewhere (i.e. no more slashes after the last dot)
		String ext = path.substring(pos);
	    System.out.println("****1. inside isResourceRequest, ext="+ext);
		if (ext.indexOf("/") != -1) return false;

		// these are JSF pages, not resources		
		// THESE LINES OF CODE IS THE ONLY REASON THIS CLASS EXISTS!
		if (ext.equals(".jsf")) return false;
		if (ext.equals(".faces")) return false;
		if (path.startsWith("/faces/")) return false;
                if (path.indexOf(".helper") > -1) return false;
		
		// ok, it's a resource request
		return true;
	}

    protected void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
      // NOTE: this is a simple path dispatching, taking the path as the view id = jsp file name for the view,
      //       with default used if no path and a path prefix as configured.

      // build up the target that will be dispatched to
      String target = req.getPathInfo();
      System.out.println("***0. dispatch, target ="+target);

      boolean sendToHelper = sendToHelper(req, res);
      boolean isResourceRequest = isResourceRequest(target);
      System.out.println("***1. dispatch, send to helper ="+sendToHelper);
      System.out.println("***2. dispatch, isResourceRequest ="+ isResourceRequest);

      // see if we have a helper request
      if (sendToHelper) {
        return;
      }

      if (isResourceRequest) {
        // get a dispatcher to the path
        RequestDispatcher resourceDispatcher = getServletContext().getRequestDispatcher(target);
        if (resourceDispatcher != null)  {
          resourceDispatcher.forward(req, res);
          return;
        }
      }

      if (target == null || "/".equals(target)) {
        target = computeDefaultTarget();

        // make sure it's a valid path
        if (!target.startsWith("/")){
          target = "/" + target;
        }

        // now that we've messed with the URL, send a redirect to make it official
        res.sendRedirect(Web.returnUrl(req, target));
        return;
      }

      // see if we want to change the specifically requested view
      String newTarget = redirectRequestedTarget(target);

      // make sure it's a valid path
      if (!newTarget.startsWith("/")){
        newTarget = "/" + newTarget;
      }

      if (!newTarget.equals(target)){
        // now that we've messed with the URL, send a redirect to make it official
        res.sendRedirect(Web.returnUrl(req, newTarget));
        return;
      }
      target = newTarget;

      // store this
      ToolSession toolSession = SessionManager.getCurrentToolSession();
      if (toolSession!=null){
          toolSession.setAttribute(LAST_VIEW_VISITED, target);
      }
        System.out.println("3a. dispatch: toolSession="+toolSession);
        System.out.println("3b. dispatch: target="+target);
        System.out.println("3c. dispatch: lastview?"+m_defaultToLastView);

	  /*
      if (m_defaultToLastView){
      }
	  */



      // add the configured folder root and extension (if missing)
      target = m_path + target;

      // add the default JSF extension (if we have no extension)
      int lastSlash = target.lastIndexOf("/");
      int lastDot = target.lastIndexOf(".");
      if (lastDot < 0 || lastDot < lastSlash){
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
      System.out.println("***4. dispatch, dispatching path: " + req.getPathInfo() + " to: " + target + " context: "
	+ getServletContext().getServletContextName());
      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
      dispatcher.forward(req, res);

      // restore the request object
      req.removeAttribute(Tool.NATIVE_URL);
      req.removeAttribute(URL_PATH);
      req.removeAttribute(URL_EXT);
      
    }


    protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res)
                      throws ToolException {
      String path = req.getPathInfo();
      if (path == null) path = "/";

      // 0 parts means the path was just "/", otherwise parts[0] = "", parts[1] = item id, parts[2] 
      // if present is "edit"...
      String[] parts = path.split("/");

      System.out.println("***a. sendToHelper.partLength="+parts.length);
      String helperPath =null;
      String toolPath=null;

      // e.g. helper url in Samigo can be /jsf/author/item/sakai.filepicker.helper/tool
      //      or /sakai.filepicker.helper 
      if (parts.length > 2){
        System.out.println("***b. sendToHelper.partLength="+parts.length);
        helperPath = parts[parts.length - 2];
        toolPath = parts[parts.length - 1];
      }
      else if (parts.length == 2){
        System.out.println("***c. sendToHelper.partLength="+parts.length);
        helperPath = parts[1];
      }
      else return false;

      if (!helperPath.endsWith(HELPER_EXT)) return false;
      System.out.println("****d. sendToHelper, part #1="+helperPath);
      System.out.println("****e. sendToHelper, part #2="+toolPath);

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
      int posEnd = helperPath.lastIndexOf(".");
      String helperId = helperPath.substring(0, posEnd);
      System.out.println("****f. sendToHelper, helperId="+helperId);
      ActiveTool helperTool = ActiveToolManager.getActiveTool(helperId);

      String url = req.getContextPath() + req.getServletPath();
      if (toolSession.getAttribute(helperTool.getId() + Tool.HELPER_DONE_URL) == null) {
	toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
				 url + computeDefaultTarget(true));
      }

      System.out.println("****g. sendToHelper, url="+url);
      String context = url + "/"+ helperPath;
      System.out.println("****h. sendToHelper, context="+context);
      if (toolPath != null) 
        helperTool.help(req, res, context, "/"+toolPath);
      else
        helperTool.help(req, res, context, "");

      return true; // was handled as helper call
    }

    protected String computeDefaultTarget(boolean lastVisited){
      // setup for the default view as configured
      ToolSession session = SessionManager.getCurrentToolSession();
      String target = "/" + m_default;

      // if we are doing lastVisit and there's a last-visited view, for this tool placement / user, use that
      if (lastVisited)	{
        String last = (String) session.getAttribute(LAST_VIEW_VISITED);
        if (last != null) {
          target = last;
	}
      }
      session.removeAttribute(LAST_VIEW_VISITED);
      System.out.println("***3. computeDefaultTarget()="+target);
      return target;
    }
  }
