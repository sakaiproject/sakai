/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/rwiki/trunk/rwiki-access-tool/src/java/uk/ac/cam/caret/sakai/rwiki/access/WikiAccessServlet.java $ 
 * $Id: WikiAccessServlet.java 51318 2008-08-24 05:28:47Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package uk.ac.cam.caret.sakai.rwiki.access;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
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
 * <p>
 * Wiki extends access: all references are assumed to be under "/wiki/site"
 * </p>
 * 
 * @author Sakai Software Development Team
 */
@Slf4j
public class WikiAccessServlet extends HttpServlet
{

	protected BasicAuth basicAuth = null;


	/**
	 * initialize the AccessServlet servlet
	 * 
	 * @param config
	 *        the servlet config parameter
	 * @exception ServletException
	 *            in case of difficulties
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		basicAuth = new BasicAuth();
		basicAuth.init();
		
	}


	/**
	 * Set active session according to sessionId parameter
	 */
	private void setSession( HttpServletRequest req )
	{
		String sessionId = req.getParameter("session");
		if ( sessionId != null)
		{
			Session session = SessionManager.getSession(sessionId);
			
			if (session != null)
			{
				session.setActive();
				SessionManager.setCurrentSession(session);
			}
		}
	}

	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *			 HttpServletRequest object with the client request
	 * @param res
	 *			 HttpServletResponse object back to the client
	 * @exception ServletException
	 *				  in case of difficulties
	 * @exception IOException
	 *				  in case of difficulties
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		setSession(req);
		dispatch(req, res);
	}
	
	
	/**
	 * handle get and post communication from the user
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 */
	public void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		
		long start = System.currentTimeMillis();

		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		// get the path info
		String path = req.getPathInfo();
		req.setAttribute(Tool.NATIVE_URL, null);
		if (path == null) path = "";

		// pre-process the path
		String origPath = path;
		path = preProcessPath(path, req);
		

		// what is being requested?
		Reference ref = EntityManager.newReference(path);


		
		
		// let the entity producer handle it
		try
		{
			// make sure we have a valid reference with an entity producer we can talk to
			EntityProducer service = ref.getEntityProducer();
			if (service == null) throw new EntityNotDefinedException(ref.getReference());

			// get the producer's HttpAccess helper, it might not support one
			HttpAccess access = service.getHttpAccess();
			if (access == null) throw new EntityNotDefinedException(ref.getReference());

			// let the helper do the work
			access.handleAccess(req, res, ref, null);
		}
		catch (EntityNotDefinedException e)
		{
			// the request was not valid in some way
			sendError(res, HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		catch (EntityPermissionException e)
		{
			// the end user does not have permission - offer a login if there is no user id yet established
			// if not permitted, and the user is the anon user, let them login
			if (SessionManager.getCurrentSessionUserId() == null)
			{
				try {
					doLogin(req, res, origPath);
				} catch ( IOException ioex ) {}
				return;
			}

			// otherwise reject the request
			sendError(res, HttpServletResponse.SC_FORBIDDEN);
		}
		catch (EntityAccessOverloadException e)
		{
			log.info("dispatch(): ref: " + ref.getReference() + e);
			sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
		catch (Throwable e)
		{
			log.warn("dispatch(): exception: ", e);
			sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		finally
		{
			long end = System.currentTimeMillis();

			// log
			if (log.isDebugEnabled())
				log.debug("from:" + req.getRemoteAddr() + " path:" + origPath + " options: " + req.getQueryString()
						+ " time: " + (end-start));
		}
	}

	
	
	/**
	 * respond to an HTTP POST request; only to handle the login process
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		// catch the login helper posts
		String option = req.getPathInfo();
		req.setAttribute(Tool.NATIVE_URL, null);
		String[] parts = option.split("/");
		if ((parts.length == 2) && (("login".equals(parts[1]))))
		{
			doLogin(req, res, null);
		}
		else
		{
			sendError(res, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * Make any changes needed to the path before final "ref" processing.
	 * 
	 * @param path
	 *        The path from the request.
	 * @param req
	 *        The request object.
	 * @return The path to use to make the Reference for further processing.
	 */
	protected String preProcessPath(String path, HttpServletRequest req)
	{
		// everything we work with is down the "content" part of the Sakai access URL space

		// if path is just "/", we don't really know if the request was to .../SERVLET or .../SERVLET/ - we want to preserve the trailing slash
		// the request URI will tell us
		if ("/".equals(path) && !(req.getRequestURI().endsWith("/"))) {
            return "/wiki";
        } else {
			// to relax the URL: allow /wiki/site/siteId and also allow /wiki/siteId
			if (path.startsWith("/site/")) {
                return "/wiki" + path;
            } else {
			    return "/wiki/site" + path;
            }
		}
	}

	/**
	 * Make a redirect to the login url.
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request.
	 * @param res
	 *        HttpServletResponse object back to the client.
	 * @param path
	 *        The current request path, set ONLY if we want this to be where to redirect the user after successfull login
	 * @throws IOException 
	 */
	protected void doLogin(HttpServletRequest req, HttpServletResponse res, String path) throws ToolException, IOException
	{
		// if basic auth is valid do that
		if ( basicAuth.doAuth(req,res) ) {
			//log.info("BASIC Auth Request Sent to the Browser ");
			return;
		} 
		
		
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// set the return path for after login if needed (Note: in session, not tool session, special for Login helper)
		if (path != null)
		{
			// where to go after
			String returnPath  = Web.returnUrl(req, Validator.escapeUrl(path));
			session.setAttribute(Tool.HELPER_DONE_URL, returnPath );
		}

		// check that we have a return path set; might have been done earlier
		if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			log.error("doLogin - proceeding with null HELPER_DONE_URL");
		}

		// map the request to the helper, leaving the path after ".../options" for the helper
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/login";
		tool.help(req, res, context, "/login");
	}

	protected void sendError(HttpServletResponse res, int code)
	{
		try
		{
			res.sendError(code);
		}
		catch (Throwable t)
		{
			log.warn("sendError: " + t);
		}
	}


}
