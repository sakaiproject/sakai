/**
 * $Id$
 * $URL$
 * Example.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * Direct servlet allows unfettered access to entity URLs within Sakai, it also handles
 * authentication (login) if required (without breaking an entity URL)<br/>
 * This primarily differs from the access servlet in that it allows posts to work 
 * and removes most of the proprietary checks
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Sakai Software Development Team
 */
public class DirectServlet extends HttpServlet {

   private static Log log = LogFactory.getLog(DirectServlet.class);

   private transient BasicAuth basicAuth;
   private transient EntityRequestHandler entityRequestHandler;

   /**
    * Initializes the servlet<br/>
    * <br/> Note: There is currently no way
    * with the current component manager to check whether it is initialised without causing it to
    * initialise. This method is here as a placeholder to invoke this function when it is available.
    * All members which require the component manager to be initialised should be initialised in
    * this method.
    */
   public void init(ServletConfig config) {
      try {
         basicAuth = new BasicAuth();
         basicAuth.init();
         entityRequestHandler = (EntityRequestHandler) ComponentManager.get("org.sakaiproject.entitybroker.EntityRequestHandler");
         if (entityRequestHandler == null) {
            throw new RuntimeException("FAILED to load EntityRequestHandler");
         }
      } catch (Exception e) {
         throw new IllegalStateException("FAILURE during init direct servlet", e);
      }
   }

   /**
    * Now this will handle all kinds of requests and not just post and get
    */
   @Override
   protected void service(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {
      // force all response encoding to UTF-8 / html by default
      res.setContentType(Formats.HTML_MIME_TYPE);
      res.setCharacterEncoding(Formats.UTF_8);
      // now handle the request
      handleRequest(req, res);
   }

   /**
    * Handle the incoming request (get and post handled in the same way), passes control to the
    * dispatch method or calls the login helper
    * 
    * @param req
    *           (from the client)
    * @param res
    *           (back to the client)
    * @throws ServletException
    * @throws IOException
    */
   private void handleRequest(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {
      // process any login that might be present
      basicAuth.doLogin(req);
      // catch the login helper posts
      String option = req.getPathInfo();
      String[] parts = option.split("/");
      if ((parts.length == 2) && ((parts[1].equals("login")))) {
         doLogin(req, res, null);
      } else {
         dispatch(req, res);
      }
   }

   /**
    * handle all communication from the user not related to login
    * 
    * @param req
    *           (from the client)
    * @param res
    *           (back to the client)
    * @throws ServletException
    */
   public void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException {
      // get the path info
      String path = req.getPathInfo();
      if (path == null) {
         path = "";
      }

      // mark the direct entity request for this session
      SessionManager.getCurrentSession().setAttribute("sakaiEntity-direct", path);

      // this cannot work because the original request data is lost
//      // check for the originalMethod and store it in an attribute
//      if (req.getParameter(ORIGINAL_METHOD) != null) {
//         req.setAttribute(ORIGINAL_METHOD, req.getParameter(ORIGINAL_METHOD));
//      }

      // just handle the request if possible or pass along the failure codes so it can be understood
      try {
         try {
            entityRequestHandler.handleEntityAccess(req, res, path);
         } catch (EntityException e) {
            log.warn("Could not process entity: " + e.entityReference);
            // no longer catching FORBIDDEN or UNAUTHORIZED here
//            if (e.responseCode == HttpServletResponse.SC_UNAUTHORIZED ||
//                  e.responseCode == HttpServletResponse.SC_FORBIDDEN) {
//               throw new SecurityException(e.getMessage(), e);
//            }
            sendError(res, e.responseCode, e.getMessage());
         }
      } catch (SecurityException e) {
         // the end user does not have permission - offer a login if there is no user id yet
         // established,  if not permitted, and the user is the anon user, let them login
         if (SessionManager.getCurrentSessionUserId() == null) {
            log.debug("Attempted to access an entity URL path (" + path
                  + ") for a resource which requires authentication without a session", e);
//            // store the original request type and query string, this is needed because the method gets lost when Sakai handles the login
//            path = path + (req.getQueryString() == null ? "?" : "?"+req.getQueryString()) + ORIGINAL_METHOD + "=" + req.getMethod();
            path = path + (req.getQueryString() == null ? "" : "?"+req.getQueryString()); // preserve the query string
            doLogin(req, res, path);
            return;
         }
         // otherwise reject the request
         String msg = "Security exception accessing entity URL: " + path + " (current user not allowed): " + e.getMessage();
         log.warn(msg);
         sendError(res, HttpServletResponse.SC_FORBIDDEN, msg);
      } catch (Exception e) {
         // all other cases
         String msg = entityRequestHandler.handleEntityError(req, e);
         log.warn(msg, e);
         sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      }

   }

   /**
    * Handle the user authentication (login)
    * 
    * @param req
    *           (from the client)
    * @param res
    *           (back to the client)
    * @param path
    *           current request path, set ONLY if we want this to be where to redirect the user
    *           after a successful login
    * @throws ToolException
    */
   protected void doLogin(HttpServletRequest req, HttpServletResponse res, String path)
         throws ToolException {

      // attempt basic auth first
      try {
         if (basicAuth.doAuth(req, res)) {
            return;
         }
      } catch (IOException ioe) {
         throw new RuntimeException("IO Exception intercepted during logon ", ioe);
      }

      // get the Sakai session (using the cover)
      Session session = SessionManager.getCurrentSession();

      // set the return path for after login if needed
      // (Note: in session, not tool session, special for Login helper)
      boolean helperURLSet = false;
      if (path != null) {
          // defines where to go after login succeeds
          helperURLSet = true;
          String returnURL = Web.returnUrl( req, path );
          String escapedReturnURL = Validator.escapeUrl( returnURL );
          log.info("Direct Login: Setting session ("+session.getId()+") helper URL ("+Tool.HELPER_DONE_URL+") to "+returnURL+" ("+escapedReturnURL+")");
          session.setAttribute(Tool.HELPER_DONE_URL, escapedReturnURL);
      }

      // check that we have a return path set; might have been done earlier
      if (! helperURLSet && session.getAttribute(Tool.HELPER_DONE_URL) == null) {
          session.setAttribute(Tool.HELPER_DONE_URL, "/direct/describe");
          log.warn("doLogin - no HELPER_DONE_URL found, proceeding with default HELPER_DONE_URL: " + "/direct/describe");
      }

      // map the request to the helper, leaving the path after ".../options" for the helper
      ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
      String context = req.getContextPath() + req.getServletPath() + "/login";
      tool.help(req, res, context, "/login");
   }

   /**
    * handles sending back servlet errors to the client
    * 
    * @param res (back to the client)
    * @param code servlet error response code
    * @param message extra info about the error
    */
   protected void sendError(HttpServletResponse res, int code, String message) {
      try {
         res.reset();
         res.sendError(code, message);
      } catch (Exception e) {
         log.warn("Error sending http servlet error code ("+code+") and message ("+message+"): " + e.getMessage(), e);
      }
   }

}
