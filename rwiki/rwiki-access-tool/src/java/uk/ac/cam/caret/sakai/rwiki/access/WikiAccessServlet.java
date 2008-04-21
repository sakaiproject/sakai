/**********************************************************************************
 * $URL$ 
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cheftool.VmServlet;
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
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Wiki extends access: all references are assumed to be under "/wiki/site"
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public class WikiAccessServlet extends VmServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(WikiAccessServlet.class);

	/** set to true when init'ed. */
	protected boolean m_ready = false;

	protected BasicAuth basicAuth = null;

	/** delimiter for form multiple values */
	protected static final String FORM_VALUE_DELIMETER = "^";

	/** init thread - so we don't wait in the actual init() call */
	public class WikiServletInit extends Thread
	{
		/**
		 * construct and start the init activity
		 */
		public WikiServletInit()
		{
			m_ready = false;
			start();
		}

		/**
		 * run the init
		 */
		public void run()
		{
			m_ready = true;
		}
	}

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
		startInit();
		basicAuth = new BasicAuth();
		basicAuth.init();
		
	}

	/**
	 * Start the initialization process
	 */
	public void startInit()
	{
		new WikiServletInit();
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
		ParameterParser params = (ParameterParser) req.getAttribute(ATTR_PARAMS);

		// get the path info
		String path = params.getPath();
		if (path == null) path = "";

		if (!m_ready)
		{
			sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}

		// pre-process the path
		String origPath = path;
		path = preProcessPath(path, req);

		// what is being requested?
		Reference ref = EntityManager.newReference(path);

		// get the incoming information
		WikiServletInfo info = newInfo(req);

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
			M_log.info("dispatch(): ref: " + ref.getReference() + e);
			sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
		catch (Throwable e)
		{
			M_log.warn("dispatch(): exception: ", e);
			sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		finally
		{
			// log
			if (M_log.isDebugEnabled())
				M_log.debug("from:" + req.getRemoteAddr() + " path:" + params.getPath() + " options: " + info.optionsString()
						+ " time: " + info.getElapsedTime());
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
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		if ((parts.length == 2) && ((parts[1].equals("login"))))
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
		if ("/".equals(path) && !(req.getRequestURI().endsWith("/"))) return "/wiki/site";

		return "/wiki/site" + path;
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
			//System.err.println("BASIC Auth Request Sent to the Browser ");
			return;
		} 
		
		
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// set the return path for after login if needed (Note: in session, not tool session, special for Login helper)
		if (path != null)
		{
			// where to go after
			session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, Validator.escapeUrl(path)));
		}

		// check that we have a return path set; might have been done earlier
		if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			M_log.warn("doLogin - proceeding with null HELPER_DONE_URL");
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
			M_log.warn("sendError: " + t);
		}
	}

	/** create the info */
	protected WikiServletInfo newInfo(HttpServletRequest req)
	{
		return new WikiServletInfo(req);
	}

	public class WikiServletInfo
	{
		// elapsed time start
		protected long m_startTime = System.currentTimeMillis();

		public long getStartTime()
		{
			return m_startTime;
		}

		public long getElapsedTime()
		{
			return System.currentTimeMillis() - m_startTime;
		}

		// all properties from the request
		protected Properties m_options = null;

		/** construct from the req */
		public WikiServletInfo(HttpServletRequest req)
		{
			m_options = new Properties();
			String type = req.getContentType();

			Enumeration e = req.getParameterNames();
			while (e.hasMoreElements())
			{
				String key = (String) e.nextElement();
				String[] values = req.getParameterValues(key);
				if (values.length == 1)
				{
					m_options.put(key, values[0]);
				}
				else
				{
					StringBuffer buf = new StringBuffer();
					for (int i = 0; i < values.length; i++)
					{
						buf.append(values[i] + FORM_VALUE_DELIMETER);
					}
					m_options.put(key, buf.toString());
				}
			}
		}

		/** return the m_options as a string - obscure any "password" fields */
		public String optionsString()
		{
			StringBuffer buf = new StringBuffer(1024);
			Enumeration e = m_options.keys();
			while (e.hasMoreElements())
			{
				String key = (String) e.nextElement();
				Object o = m_options.getProperty(key);
				if (o instanceof String)
				{
					buf.append(key);
					buf.append("=");
					if (key.equals("password"))
					{
						buf.append("*****");
					}
					else
					{
						buf.append(o.toString());
					}
					buf.append("&");
				}
			}

			return buf.toString();
		}
	}

}
