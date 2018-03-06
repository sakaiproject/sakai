/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.access.tool;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.VmServlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Access is a servlet that provides a portal to entity access by URL for Sakai.<br />
 * The servlet takes the requests and dispatches to the appropriate EntityProducer for the response.<br />
 * Any error handling is done here.<br />
 * If the user has not yet logged in and need to for permission, the login process is handled here, too.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
@Slf4j
public class AccessServlet extends VmServlet
{
	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("access");

	/** stream content requests if true, read all into memory and send if false. */
	protected static final boolean STREAM_CONTENT = true;

	/** The chunk size used when streaming (100k). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

	/** delimiter for form multiple values */
	protected static final String FORM_VALUE_DELIMETER = "^";

	/** set to true when init'ed. */
	protected boolean m_ready = false;

	/** copyright path -- MUST have same value as ResourcesAction.COPYRIGHT_PATH */
	protected static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";

	/** Path used when forcing the user to accept the copyright agreement . */
	protected static final String COPYRIGHT_REQUIRE = Entity.SEPARATOR + "require";

	/** Path used when the user has accepted the copyright agreement . */
	protected static final String COPYRIGHT_ACCEPT = Entity.SEPARATOR + "accept";

	/** Ref accepted, request parameter for COPYRIGHT_ACCEPT request. */
	protected static final String COPYRIGHT_ACCEPT_REF = "ref";

	/** Return URL, request parameter for COPYRIGHT_ACCEPT request. */
	protected static final String COPYRIGHT_ACCEPT_URL = "url";

	/** Session attribute holding copyright-accepted references (a collection of Strings). */
	protected static final String COPYRIGHT_ACCEPTED_REFS_ATTR = "Access.Copyright.Accepted";
	
	protected BasicAuth basicAuth = null;

	protected SecurityService securityService;
	protected EntityManager entityManager;
	protected ActiveToolManager activeToolManager;
	protected SessionManager sessionManager;

	/** init thread - so we don't wait in the actual init() call */
	public class AccessServletInit extends Thread
	{
		/**
		 * construct and start the init activity
		 */
		public AccessServletInit()
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
		// Grab our services
		securityService = ComponentManager.get(SecurityService.class);
		entityManager = ComponentManager.get(EntityManager.class);
		activeToolManager = ComponentManager.get(ActiveToolManager.class);
		sessionManager = ComponentManager.get(SessionManager.class);
	}

	/**
	 * Start the initialization process
	 */
	public void startInit()
	{
		new AccessServletInit();
	}

	/**
	 * respond to an HTTP GET request
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
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// process any login that might be present
		basicAuth.doLogin(req);
		// catch the login helper requests
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			doLogin(req, res, null);
		}	
		else
		{
			dispatch(req, res);
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
		// process any login that might be present
		basicAuth.doLogin(req);
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

		// send the sample copyright screen
		if (COPYRIGHT_PATH.equals(path))
		{
			respondCopyrightAlertDemo(req, res);
			return;
		}

		// send the real copyright screen for some entity (encoded in the request parameter)
		if (COPYRIGHT_REQUIRE.equals(path))
		{
			String acceptedRef = req.getParameter(COPYRIGHT_ACCEPT_REF);
			String returnPath = req.getParameter(COPYRIGHT_ACCEPT_URL);

			Reference aRef = entityManager.newReference(acceptedRef);

			// get the properties - but use a security advisor to avoid needing end-user permission to the resource
			securityService.pushAdvisor(new SecurityAdvisor()
			{
				public SecurityAdvice isAllowed(String userId, String function, String reference)
				{
					return SecurityAdvice.ALLOWED;
				}
			});
			ResourceProperties props = aRef.getProperties();
			securityService.popAdvisor();

			// send the copyright agreement interface
			if (props == null)
			{
				sendError(res, HttpServletResponse.SC_NOT_FOUND);
			}

			setVmReference("validator", new Validator(), req);
			setVmReference("props", props, req);
			setVmReference("tlang", rb, req);

			String acceptPath = Web.returnUrl(req, COPYRIGHT_ACCEPT + "?" + COPYRIGHT_ACCEPT_REF + "=" + Validator.escapeUrl(aRef.getReference()) + "&"
					+ COPYRIGHT_ACCEPT_URL + "=" + Validator.escapeUrl(returnPath));

			setVmReference("accept", acceptPath, req);
			res.setContentType("text/html; charset=UTF-8");
			includeVm("vm/access/copyrightAlert.vm", req, res);
			return;
		}

		// make sure we have a collection for accepted copyright agreements
		Collection accepted = (Collection) sessionManager.getCurrentSession().getAttribute(COPYRIGHT_ACCEPTED_REFS_ATTR);
		if (accepted == null)
		{
			accepted = new Vector();
			sessionManager.getCurrentSession().setAttribute(COPYRIGHT_ACCEPTED_REFS_ATTR, accepted);
		}

		// for accepted copyright, mark it and redirect to the entity's access URL
		if (COPYRIGHT_ACCEPT.equals(path))
		{
			String acceptedRef = req.getParameter(COPYRIGHT_ACCEPT_REF);
			Reference aRef = entityManager.newReference(acceptedRef);

			// save this with the session's other accepted refs
			accepted.add(aRef.getReference());

			// redirect to the original URL
			String returnPath =  Validator.escapeUrl( req.getParameter(COPYRIGHT_ACCEPT_URL) );

			try
			{
				res.sendRedirect(Web.returnUrl(req, returnPath));
			}
			catch (IOException e)
			{
				sendError(res, HttpServletResponse.SC_NOT_FOUND);
			}
			return;
		}

		// pre-process the path
		String origPath = path;
		path = preProcessPath(path, req);

		// what is being requested?
		Reference ref = entityManager.newReference(path);

		// get the incoming information
		AccessServletInfo info = newInfo(req);

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
			access.handleAccess(req, res, ref, accepted);
		}
		catch (EntityNotDefinedException e)
		{
			// the request was not valid in some way
			log.debug("dispatch(): ref: " + ref.getReference(), e);
			sendError(res, HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		catch (EntityPermissionException e)
		{
			// the end user does not have permission - offer a login if there is no user id yet established
			// if not permitted, and the user is the anon user, let them login
			if (sessionManager.getCurrentSessionUserId() == null)
			{
				try {
					doLogin(req, res, origPath);
				} catch ( IOException ioex ) {}
				return;
			}

			// otherwise reject the request
			log.debug("dispatch(): ref: " + ref.getReference(), e);
			sendError(res, HttpServletResponse.SC_FORBIDDEN);
		}

		catch (EntityAccessOverloadException e)
		{
			log.info("dispatch(): ref: " + ref.getReference(), e);
			sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}

		catch (EntityCopyrightException e)
		{
			// redirect to the copyright agreement interface for this entity
			try
			{
				// TODO: send back using a form of the request URL, encoding the real reference, and the requested reference
				// Note: refs / requests with servlet parameters (?x=y...) are NOT supported -ggolden
				String redirPath = COPYRIGHT_REQUIRE + "?" + COPYRIGHT_ACCEPT_REF + "=" + Validator.escapeUrl(e.getReference()) + "&" + COPYRIGHT_ACCEPT_URL
						+ "=" + Validator.escapeUrl(req.getPathInfo());
				res.sendRedirect(Web.returnUrl(req, redirPath));
			}
			catch (IOException ee)
			{
			}
			return;
		}

		catch (Throwable e)
		{
			log.warn("dispatch(): exception: ", e);
			sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		finally
		{
			// log
			if (log.isDebugEnabled())
				log.debug("from:" + req.getRemoteAddr() + " path:" + params.getPath() + " options: " + info.optionsString()
						+ " time: " + info.getElapsedTime());
		}
	}

	/**
	 * Make any changes needed to the path before final "ref" processing.
	 * 
	 * @param path
	 *        The path from the request.
	 * @req The request object.
	 * @return The path to use to make the Reference for further processing.
	 */
	protected String preProcessPath(String path, HttpServletRequest req)
	{
		return path;
	}

	/**
	 * Make the Sample Copyright Alert response.
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request.
	 * @param res
	 *        HttpServletResponse object back to the client.
	 */
	protected void respondCopyrightAlertDemo(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		// the context wraps our real vm attribute set
		ResourceProperties props = new BaseResourceProperties();
		setVmReference("props", props, req);
		setVmReference("validator", new Validator(), req);
		setVmReference("sample", Boolean.TRUE.toString(), req);
		setVmReference("tlang", rb, req);
		res.setContentType("text/html; charset=UTF-8");
		includeVm("vm/access/copyrightAlert.vm", req, res);	
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
			log.info("BASIC Auth Request Sent to the Browser ");
			return;
		} 
		
		
		// if there is a Range: header for partial content and we haven't done basic auth, refuse the request	(SAK-23678)
		if (req.getHeader("Range") != null) {
			sendError(res, HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// get the Sakai session
		Session session = sessionManager.getCurrentSession();

		// set the return path for after login if needed (Note: in session, not tool session, special for Login helper)
		if (path != null)
		{
			// where to go after
			session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, Validator.escapeUrl(path)));
		}

		// check that we have a return path set; might have been done earlier
		if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			log.warn("doLogin - proceeding with null HELPER_DONE_URL");
		}

		// map the request to the helper, leaving the path after ".../options" for the helper
		ActiveTool tool = activeToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/login";
		tool.help(req, res, context, "/login");
	}

	/** create the info */
	protected AccessServletInfo newInfo(HttpServletRequest req)
	{
		return new AccessServletInfo(req);
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

	public class AccessServletInfo
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
		public AccessServletInfo(HttpServletRequest req)
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
					StringBuilder buf = new StringBuilder();
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
			StringBuilder buf = new StringBuilder(1024);
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

	/**
	 * A simple SecurityAdviser that can be used to override permissions on one reference string for one user for one function.
	 */
	public class SimpleSecurityAdvisor implements SecurityAdvisor
	{
		protected String m_userId;

		protected String m_function;

		protected String m_reference;

		public SimpleSecurityAdvisor(String userId, String function, String reference)
		{
			m_userId = userId;
			m_function = function;
			m_reference = reference;
		}

		public SecurityAdvice isAllowed(String userId, String function, String reference)
		{
			SecurityAdvice rv = SecurityAdvice.PASS;
			if (m_userId.equals(userId) && m_function.equals(function) && m_reference.equals(reference))
			{
				rv = SecurityAdvice.ALLOWED;
			}
			return rv;
		}
	}
}
