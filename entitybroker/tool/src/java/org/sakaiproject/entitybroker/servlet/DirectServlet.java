/**
 * DirectServlet.java - created by someone on 31 May 2007
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
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.Web;

/**
 * Direct servlet allows unfettered access to entity URLs within Sakai, it also handles
 * authentication (login) if required (without breaking an entity URL)<br/> This primarily differs
 * from the access servlet in that it allows posts to work and removes most of the proprietary
 * checks
 * 
 * @author someone crazy... quit looking at me!
 * @author Sakai Software Development Team
 */
public class DirectServlet extends HttpServlet {

   private static Log log = LogFactory.getLog(DirectServlet.class);

   /**
    * set to true when initialization complete
    */
   private boolean initComplete = false;

   private BasicAuth basicAuth;

   private HttpServletAccessProviderManager accessProviderManager;

   private EntityBroker entityBroker;

   /**
    * Checks dependencies and loads/inits them if needed<br/> <br/> Note: There is currently no way
    * with the current component manager to check whether it is initialised without causing it to
    * initialise. This method is here as a placeholder to invoke this function when it is available.
    * All members which require the component manager to be initialised should be initialised in
    * this method.
    */
   private void checkDependencies() {
      try {
         basicAuth = new BasicAuth();
         basicAuth.init();
         accessProviderManager = (HttpServletAccessProviderManager) ComponentManager
               .get("org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager");
         entityBroker = (EntityBroker) ComponentManager
               .get("org.sakaiproject.entitybroker.EntityBroker");
         if (accessProviderManager != null || entityBroker != null) {
            initComplete = true;
         }
      } catch (Exception e) {
         log.error("Error initialising DirectServlet", e);
      }
   }

   /**
    * Initialises the servlet
    */
   public void init(ServletConfig config) {
      checkDependencies();
   }

   /**
    * Now this will handle all kinds of requests and not just post and get
    */
   @Override
   protected void service(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {
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

      if (!initComplete) {
         sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
         return;
      }

      // logically, we only want to let this request continue on if the entity exists AND
      // there is an http access provider to handle it AND the user can access it
      // (there is some auth completed already or no auth is required)
      try {
         EntityReference ref = entityBroker.parseReference(path);
         if (ref == null || !entityBroker.entityExists(ref.toString())) {
            log.warn("Attempted to access an entity URL path (" + path + ") for an entity ("
                  + ref.toString() + ") that does not exist");
            sendError(res, HttpServletResponse.SC_NOT_FOUND);
         } else {
            HttpServletAccessProvider accessProvider = accessProviderManager
                  .getProvider(ref.prefix);
            if (accessProvider == null) {
               log.warn("Attempted to access an entity URL path ("
                           + path + ") for an entity (" + ref.toString()
                           + ") when there is no HttpServletAccessProvider to handle the request for prefix ("
                           + ref.prefix + ")");
               sendError(res, HttpServletResponse.SC_NOT_FOUND);
            } else {
               ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
               try {
                  ClassLoader newClassLoader = accessProvider.getClass().getClassLoader();
                  // check to see if this access provider reports the correct classloader
                  if (accessProvider instanceof ClassLoaderReporter) {
                     newClassLoader = ((ClassLoaderReporter) accessProvider).getSuitableClassLoader();
                  }
                  Thread.currentThread().setContextClassLoader(newClassLoader);
                  // send request to the access provider which will route it on to the correct entity world
                  accessProvider.handleAccess(req, res, ref);
               } finally {
                  Thread.currentThread().setContextClassLoader(currentClassLoader);
               }
            }
         }
      } catch (SecurityException e) {
         // the end user does not have permission - offer a login if there is no user id yet
         // established,
         // if not permitted, and the user is the anon user, let them login
         if (SessionManager.getCurrentSessionUserId() == null) {
            log.debug("Attempted to access an entity URL path (" + path
                  + ") for a resource which requires authentication without a session", e);
            doLogin(req, res, path);
         }
         // otherwise reject the request
         sendError(res, HttpServletResponse.SC_FORBIDDEN);
      } catch (Exception e) {
         // all other cases
         log.warn("dispatch(): exception: ", e);
         sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
      if (path != null) {
         // defines where to go after login succeeds
         session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, path));
      }

      // check that we have a return path set; might have been done earlier
      if (session.getAttribute(Tool.HELPER_DONE_URL) == null) {
         log.warn("doLogin - proceeding with null HELPER_DONE_URL");
      }

      // map the request to the helper, leaving the path after ".../options" for
      // the helper
      ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
      String context = req.getContextPath() + req.getServletPath() + "/login";
      tool.help(req, res, context, "/login");
   }

   /**
    * handles sending back servlet errors to the client
    * 
    * @param res
    *           (back to the client)
    * @param code
    *           servlet error response code
    */
   protected void sendError(HttpServletResponse res, int code) {
      try {
         res.sendError(code);
      } catch (Throwable t) {
         log.warn(t.getMessage(), t);
      }
   }

}
