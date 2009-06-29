/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * RequestFilter Filters all requests to Sakai tools. It is responsible for keeping the Sakai session, done using a cookie to the
 * end user's browser storing the user's session id.
 */
public class RequestFilter implements Filter
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(RequestFilter.class);

	/** The name of the cookie we use to keep sakai session. */
	public static final String SESSION_COOKIE = "JSESSIONID";

	/** The request attribute name used to store the Sakai session. */
	public static final String ATTR_SESSION = "sakai.session";

	/** The request attribute name used to ask the RequestFilter to output
	 * a client cookie at the end of the request cycle. */
	public static final String ATTR_SET_COOKIE = "sakai.set.cookie";
	
	/** The request attribute name (and value) used to indicated that the request has been filtered. */
	public static final String ATTR_FILTERED = "sakai.filtered";

	/** The request attribute name (and value) used to indicated that file uploads have been parsed. */
	public static final String ATTR_UPLOADS_DONE = "sakai.uploads.done";

	/** The request attribute name (and value) used to indicated that character encoding has been set. */
	public static final String ATTR_CHARACTER_ENCODING_DONE = "sakai.character.encoding.done";

	/** The request attribute name used to indicated that the *response* has been redirected. */
	public static final String ATTR_REDIRECT = "sakai.redirect";

	/** The request parameter name used to indicated that the request is automatic, not from a user action. */
	public static final String PARAM_AUTO = "auto";

	/** Config parameter to control http session handling. */
	public static final String CONFIG_SESSION = "http.session";

	/** Config parameter to control remote user handling. */
	public static final String CONFIG_REMOTE_USER = "remote.user";

	/** Config parameter to control tool placement URL en/de-coding. */
	public static final String CONFIG_TOOL_PLACEMENT = "tool.placement";

	/** Config parameter to control whether to set the character encoding on the request. Default is true. */
	public static final String CONFIG_CHARACTER_ENCODING_ENABLED = "encoding.enabled";

	/** Config parameter which to control character encoding to apply to the request. Default is UTF-8. */
	public static final String CONFIG_CHARACTER_ENCODING = "encoding";

	/**
	 * Config parameter to control whether the request filter parses file uploads. Default is true. If false, the tool will need to
	 * provide its own upload filter that executes BEFORE the Sakai request filter.
	 */
	public static final String CONFIG_UPLOAD_ENABLED = "upload.enabled";

	/**
	 * Config parameter to control the maximum allowed upload size (in MEGABYTES) from the browser.<br />
	 * If defined on the filter, overrides the system property. Default is 1 (one megabyte).<br />
	 * This is an aggregate limit on the sum of all files included in a single request.<br />
	 * Also used as a per-request request parameter, encoded in the URL, to set the max for that particular request.
	 */
	public static final String CONFIG_UPLOAD_MAX = "upload.max";

	/**
	 * System property to control the maximum allowed upload size (in MEGABYTES) from the browser. Default is 1 (one megabyte). This
	 * is an aggregate limit on the sum of all files included in a single request.
	 */
	public static final String SYSTEM_UPLOAD_MAX = "sakai.content.upload.max";

	/**
	 * System property to control the maximum allowed upload size (in MEGABYTES) from any other method - system wide, request
	 * filter, or per-request.
	 */
	public static final String SYSTEM_UPLOAD_CEILING = "sakai.content.upload.ceiling";

	/**
	 * Config parameter (in bytes) to control the threshold at which to store uploaded files on-disk (temporarily) instead of
	 * in-memory. Default is 1024 bytes.
	 */
	public static final String CONFIG_UPLOAD_THRESHOLD = "upload.threshold";

	/**
	 * Config parameter to continue (or abort, if false) upload field processing if there's a file upload max size exceeded
	 * exception.
	 */
	protected static final String CONFIG_CONTINUE = "upload.continueOverMax";

	/**
	 * Config parameter to treat the max upload size as for the individual files in the request (or, if false, for the entire
	 * request).
	 */
	protected static final String CONFIG_MAX_PER_FILE = "upload.maxPerFile";

	/**
	 * Config parameter that specifies the absolute path of a temporary directory in which to store file uploads. Default is the
	 * servlet container temporary directory. Note that this is TRANSIENT storage, used by the commons-fileupload API. The files
	 * must be renamed or otherwise processed (by the tool through the commons-fileupload API) in order for the data to become
	 * permenant.
	 */
	public static final String CONFIG_UPLOAD_DIR = "upload.dir";

	/** System property to control the temporary directory in which to store file uploads. */
	public static final String SYSTEM_UPLOAD_DIR = "sakai.content.upload.dir";

	/** Config parameter to set the servlet context for context based session (overriding the servlet's context name). */
	public static final String CONFIG_CONTEXT = "context";

	/** sakaiHttpSession setting for don't do anything. */
	protected final static int CONTAINER_SESSION = 0;

	/** sakaiHttpSession setting for use the sakai wide session. */
	protected final static int SAKAI_SESSION = 1;

	/** sakaiHttpSession setting for use the context session. */
	protected final static int CONTEXT_SESSION = 2;

	/** sakaiHttpSession setting for use the tool session, in any, else context. */
	protected final static int TOOL_SESSION = 3;

	/** Key in the ThreadLocalManager for binding our remoteUser preference. */
	protected final static String CURRENT_REMOTE_USER = "org.sakaiproject.util.RequestFilter.remote_user";

	/** Key in the ThreadLocalManager for binding our http session preference. */
	protected final static String CURRENT_HTTP_SESSION = "org.sakaiproject.util.RequestFilter.http_session";

	/** Key in the ThreadLocalManager for binding our context id. */
	protected final static String CURRENT_CONTEXT = "org.sakaiproject.util.RequestFilter.context";

	/** Key in the ThreadLocalManager for access to the current http request object. */
	public final static String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";

	/** Key in the ThreadLocalManager for access to the current http response object. */
	public final static String CURRENT_HTTP_RESPONSE = "org.sakaiproject.util.RequestFilter.http_response";

	/** Key in the ThreadLocalManager for access to the current servlet context. */
	public final static String CURRENT_SERVLET_CONTEXT = "org.sakaiproject.util.RequestFilter.servlet_context";

	/** The "." character */
	protected static final String DOT = ".";

	/** The name of the system property that will be used when setting the value of the session cookie. */
	protected static final String SAKAI_SERVERID = "sakai.serverId";

	/** If true, we deliver the Sakai wide session as the Http session for each request. */
	protected int m_sakaiHttpSession = TOOL_SESSION;

	/** If true, we deliver the Sakai end user enterprise id as the remote user in each request. */
	protected boolean m_sakaiRemoteUser = true;

	/** If true, we encode / decode the tool placement using the a URL parameter. */
	protected boolean m_toolPlacement = true;

	/** Our contex (i.e. servlet context) id. */
	protected String m_contextId = null;

	protected String m_characterEncoding = "UTF-8";

	protected boolean m_characterEncodingEnabled = true;

	protected boolean m_uploadEnabled = true;

	protected long m_uploadMaxSize = 1L * 1024L * 1024L;

	protected long m_uploadCeiling = 1L * 1024L * 1024L;

	protected int m_uploadThreshold = 1024;

	protected String m_uploadTempDir = null;

	protected boolean m_displayModJkWarning = true;

	/** Default is to abort further upload processing if the max is exceeded. */
	protected boolean m_uploadContinue = false;

	/** Default is to treat the m_uploadMaxSize as for the entire request, not per file. */
	protected boolean m_uploadMaxPerFile = false;

	/** The servlet context for the filter. */
	protected ServletContext m_servletContext = null;
	
	/** Is this a Terracotta clustered environment? */
	protected boolean TERRACOTTA_CLUSTER = false;

	/**
	 * Wraps a request object so we can override some standard behavior.
	 */
	public class WrappedRequest extends HttpServletRequestWrapper
	{
		/** The Sakai session. */
		protected Session m_session = null;

		/** Our contex (i.e. servlet context) id. */
		protected String m_contextId = null;

		public WrappedRequest(Session s, String contextId, HttpServletRequest req)
		{
			super(req);
			m_session = s;
			m_contextId = contextId;

			if (m_toolPlacement)
			{
				extractPlacementFromParams();
			}
		}

		public String getRemoteUser()
		{
			// use the "current" setting for this
			boolean remoteUser = ((Boolean) ThreadLocalManager.get(CURRENT_REMOTE_USER)).booleanValue();

			if (remoteUser && (m_session != null) && (m_session.getUserEid() != null))
			{
				return m_session.getUserEid();
			}

			return super.getRemoteUser();
		}

		public HttpSession getSession()
		{
			return getSession(true);
		}

		public HttpSession getSession(boolean create)
		{
			HttpSession rv = null;

			// use the "current" settings for this
			int curHttpSession = ((Integer) ThreadLocalManager.get(CURRENT_HTTP_SESSION)).intValue();
			String curContext = (String) ThreadLocalManager.get(CURRENT_CONTEXT);

			switch (curHttpSession)
			{
				case CONTAINER_SESSION:
				{
					rv = super.getSession(create);
					break;
				}

				case SAKAI_SESSION:
				{
					rv = (HttpSession) m_session;
					break;
				}

				case CONTEXT_SESSION:
				{
					rv = (HttpSession) m_session.getContextSession(curContext);
					break;
				}

				case TOOL_SESSION:
				{
					rv = (HttpSession) SessionManager.getCurrentToolSession();
					if (rv == null)
					{
						rv = (HttpSession) m_session.getContextSession(curContext);
					}
					break;
				}
			}

			return rv;
		}

		/**
		 * Pull the specially encoded tool placement id from the request parameters.
		 */
		protected void extractPlacementFromParams()
		{
			String placementId = getParameter(Tool.PLACEMENT_ID);
			if (placementId != null)
			{
				setAttribute(Tool.PLACEMENT_ID, placementId);
			}
		}
	}

	/**
	 * Wraps a response object so we can override some standard behavior.
	 */
	public class WrappedResponse extends HttpServletResponseWrapper
	{
		/** The request. */
		protected HttpServletRequest m_req = null;

		/** Wrapped Response * */
		protected HttpServletResponse m_res = null;

		public WrappedResponse(Session s, HttpServletRequest req, HttpServletResponse res)
		{
			super(res);

			m_req = req;
			m_res = res;
		}

		public String encodeRedirectUrl(String url)
		{
			return rewriteURL(url);
		}

		public String encodeRedirectURL(String url)
		{
			return rewriteURL(url);
		}

		public String encodeUrl(String url)
		{
			return rewriteURL(url);
		}

		public String encodeURL(String url)
		{
			return rewriteURL(url);
		}

		public void sendRedirect(String url) throws IOException
		{
			url = rewriteURL(url);
			m_req.setAttribute(ATTR_REDIRECT, url);
			super.sendRedirect(url);
		}

		/**
		 * Rewrites the given URL to insert the current tool placement id, if any, as the start of the path
		 * 
		 * @param url
		 *        The url to rewrite.
		 */
		protected String rewriteURL(String url)
		{
			if (m_toolPlacement)
			{
				// if we have a tool placement to add, add it
				String placementId = (String) m_req.getAttribute(Tool.PLACEMENT_ID);
				if (placementId != null)
				{
					// compute the URL root "back" to this servlet context (rel and full)
					StringBuilder full = new StringBuilder();
					full.append(m_req.getScheme());
					full.append("://");
					full.append(m_req.getServerName());
					if (((m_req.getServerPort() != 80) && (!m_req.isSecure()))
							|| ((m_req.getServerPort() != 443) && (m_req.isSecure())))
					{
						full.append(":");
						full.append(m_req.getServerPort());
					}

					StringBuilder rel = new StringBuilder();
					rel.append(m_req.getContextPath());

					full.append(rel.toString());

					// if we match the fullUrl, or the relUrl, assume that this is a URL back to this servlet
					if ((url.startsWith(full.toString()) || url.startsWith(rel.toString())))
					{
						// put the placementId in as a parameter
						StringBuilder newUrl = new StringBuilder(url);
						if (url.indexOf('?') != -1)
						{
							newUrl.append('&');
						}
						else
						{
							newUrl.append('?');
						}
						newUrl.append(Tool.PLACEMENT_ID);
						newUrl.append("=");
						newUrl.append(placementId);
						url = newUrl.toString();
					}
				}
			}

			// Chain back so the wrapped response can encode the URL futher if needed
			// this is necessary for WSRP support.
			if (m_res != null) url = m_res.encodeURL(url);

			return url;
		}
	}

	/**
	 * Request wrapper that exposes the parameters parsed from the multipart/mime file upload (along with parameters from the
	 * request).
	 */
	static class WrappedRequestFileUpload extends HttpServletRequestWrapper
	{
		private Map map;

		/**
		 * Constructs a wrapped response that exposes the given map of parameters.
		 * 
		 * @param req
		 *        The request to wrap.
		 * @param paramMap
		 *        The parameters to expose.
		 */
		public WrappedRequestFileUpload(HttpServletRequest req, Map paramMap)
		{
			super(req);
			map = paramMap;
		}

		public Map getParameterMap()
		{
			return map;
		}

		public String[] getParameterValues(String name)
		{
			String[] ret = null;
			Map map = getParameterMap();
			return (String[]) map.get(name);
		}

		public String getParameter(String name)
		{
			String[] params = getParameterValues(name);
			if (params == null) return null;
			return params[0];
		}

		public Enumeration getParameterNames()
		{
			Map map = getParameterMap();
			return Collections.enumeration(map.keySet());
		}
	}

	/**
	 * Take this filter out of service.
	 */
	public void destroy()
	{
	}

	/**
	 * Filter a request / response.
	 */
	public void doFilter(ServletRequest requestObj, ServletResponse responseObj, FilterChain chain) throws IOException,
			ServletException
	{
		StringBuffer sb = null;
		long startTime = System.currentTimeMillis();

		// bind some preferences as "current"
		Boolean curRemoteUser = (Boolean) ThreadLocalManager.get(CURRENT_REMOTE_USER);
		Integer curHttpSession = (Integer) ThreadLocalManager.get(CURRENT_HTTP_SESSION);
		String curContext = (String) ThreadLocalManager.get(CURRENT_CONTEXT);
		ServletRequest curRequest = (ServletRequest) ThreadLocalManager.get(CURRENT_HTTP_REQUEST);
		ServletResponse curResponse = (ServletResponse) ThreadLocalManager.get(CURRENT_HTTP_RESPONSE);
		boolean cleared = false;

		// keep track of temp files with this request that need to be deleted on the way out
		List<FileItem> tempFiles = new ArrayList<FileItem>();

		try
		{
			ThreadLocalManager.set(CURRENT_REMOTE_USER, Boolean.valueOf(m_sakaiRemoteUser));
			ThreadLocalManager.set(CURRENT_HTTP_SESSION, Integer.valueOf(m_sakaiHttpSession));
			ThreadLocalManager.set(CURRENT_CONTEXT, m_contextId);

			// make the servlet context available
			ThreadLocalManager.set(CURRENT_SERVLET_CONTEXT, m_servletContext);

			// we are expecting HTTP stuff
			if (!((requestObj instanceof HttpServletRequest) && (responseObj instanceof HttpServletResponse)))
			{
				// if not, just pass it through
				chain.doFilter(requestObj, responseObj);
				return;
			}

			HttpServletRequest req = (HttpServletRequest) requestObj;
			HttpServletResponse resp = (HttpServletResponse) responseObj;

			// check on file uploads and character encoding BEFORE checking if
			// this request has already been filtered, as the character encoding
			// and file upload handling are configurable at the tool level.
			// so the 2nd invokation of the RequestFilter (at the tool level)
			// may actually cause character encoding and file upload parsing
			// to happen.

			// handle character encoding
			handleCharacterEncoding(req, resp);

			// handle file uploads
			req = handleFileUpload(req, resp, tempFiles);

			// if we have already filtered this request, pass it on
			if (req.getAttribute(ATTR_FILTERED) != null)
			{
				// set the request and response for access via the thread local
				ThreadLocalManager.set(CURRENT_HTTP_REQUEST, req);
				ThreadLocalManager.set(CURRENT_HTTP_RESPONSE, resp);

				chain.doFilter(req, resp);
			}

			// filter the request
			else
			{
				if (M_log.isDebugEnabled())
				{
					sb = new StringBuffer("http-request: ");
					sb.append(req.getMethod());
					sb.append(" ");
					sb.append(req.getRequestURL());
					if (req.getQueryString() != null)
					{
						sb.append("?");
						sb.append(req.getQueryString());
					}
					M_log.debug(sb);
				}
								
				try
				{
					// mark the request as filtered to avoid re-filtering it later in the request processing
					req.setAttribute(ATTR_FILTERED, ATTR_FILTERED);

					// some useful info
					ThreadLocalManager.set(ServerConfigurationService.CURRENT_SERVER_URL, serverUrl(req));

					// make sure we have a session
					Session s = assureSession(req, resp);

					// pre-process request
					req = preProcessRequest(s, req);

					// detect a tool placement and set the current tool session
					detectToolPlacement(s, req);

					// pre-process response
					resp = preProcessResponse(s, req, resp);

					// set the request and response for access via the thread local
					ThreadLocalManager.set(CURRENT_HTTP_REQUEST, req);
					ThreadLocalManager.set(CURRENT_HTTP_RESPONSE, resp);

					// set the portal into thread local
					if (m_contextId != null && m_contextId.length() > 0)
					{
						ThreadLocalManager.set(ServerConfigurationService.CURRENT_PORTAL_PATH, "/" + m_contextId);
					}

					// Only synchronize on session for Terracotta. See KNL-218, KNL-75.
					if (TERRACOTTA_CLUSTER) {
						synchronized(s) {
							// Pass control on to the next filter or the servlet
							chain.doFilter(req, resp);
	
							// post-process response
							postProcessResponse(s, req, resp);
						}
					} else {
						// Pass control on to the next filter or the servlet
						chain.doFilter(req, resp);

						// post-process response
						postProcessResponse(s, req, resp);						
					}
			
					// Output client cookie if requested to do so
					if (s != null && req.getAttribute(ATTR_SET_COOKIE) != null) {
						
						// check for existing cookie
						String suffix = getCookieSuffix();
						Cookie c = findCookie(req, SESSION_COOKIE, suffix);

						// the cookie value we need to use
						String sessionId = s.getId() + DOT + suffix;

						// set the cookie if necessary
						if ((c == null) || (!c.getValue().equals(sessionId))) {
							c = new Cookie(SESSION_COOKIE, sessionId);
							c.setPath("/");
							c.setMaxAge(-1);
							if (req.isSecure() == true)
							{
								c.setSecure(true);
							}
							resp.addCookie(c);
						}
					}

					
				}
				catch (RuntimeException t)
				{
					M_log.warn("", t);
					throw t;
				}
				catch (IOException ioe)
				{
					M_log.warn("", ioe);
					throw ioe;
				}
				catch (ServletException se)
				{
					M_log.warn("", se);
					throw se;
				}
				finally
				{
					// clear any bound current values
					ThreadLocalManager.clear();
					cleared = true;
				}
			}
			
		}
		finally
		{
			if (!cleared)
			{
				// restore the "current" bindings
				ThreadLocalManager.set(CURRENT_REMOTE_USER, curRemoteUser);
				ThreadLocalManager.set(CURRENT_HTTP_SESSION, curHttpSession);
				ThreadLocalManager.set(CURRENT_CONTEXT, curContext);
				ThreadLocalManager.set(CURRENT_HTTP_REQUEST, curRequest);
				ThreadLocalManager.set(CURRENT_HTTP_RESPONSE, curResponse);
			}

			// delete any temp files
			deleteTempFiles(tempFiles);

			if (M_log.isDebugEnabled() && sb != null)
			{
				long elapsedTime = System.currentTimeMillis() - startTime;
				M_log.debug("request timing (ms): " + elapsedTime + " for " + sb);
			}
		}
		
	}

	/**
	 * If any of these files exist, delete them.
	 * 
	 * @param tempFiles
	 *        The file items to delete.
	 */
	protected void deleteTempFiles(List<FileItem> tempFiles)
	{
		for (FileItem item : tempFiles)
		{
			item.delete();
		}
	}

	/**
	 * Place this filter into service.
	 * 
	 * @param filterConfig
	 *        The filter configuration object
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{
		// capture the servlet context for later user
		m_servletContext = filterConfig.getServletContext();

		if (filterConfig.getInitParameter(CONFIG_SESSION) != null)
		{
			String s = filterConfig.getInitParameter(CONFIG_SESSION);
			if ("container".equalsIgnoreCase(s))
			{
				m_sakaiHttpSession = CONTAINER_SESSION;
			}
			else if ("sakai".equalsIgnoreCase(s))
			{
				m_sakaiHttpSession = SAKAI_SESSION;
			}
			else if ("context".equalsIgnoreCase(s))
			{
				m_sakaiHttpSession = CONTEXT_SESSION;
			}
			else if ("tool".equalsIgnoreCase(s))
			{
				m_sakaiHttpSession = TOOL_SESSION;
			}
			else
			{
				M_log.warn("invalid " + CONFIG_SESSION + " setting (" + s + "): not one of container, sakai, context, tool");
			}
		}

		if (filterConfig.getInitParameter(CONFIG_REMOTE_USER) != null)
		{
			m_sakaiRemoteUser = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_REMOTE_USER)).booleanValue();
		}

		if (filterConfig.getInitParameter(CONFIG_TOOL_PLACEMENT) != null)
		{
			m_toolPlacement = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_TOOL_PLACEMENT)).booleanValue();
		}

		if (filterConfig.getInitParameter(CONFIG_CONTEXT) != null)
		{
			m_contextId = filterConfig.getInitParameter(CONFIG_CONTEXT);
		}
		else
		{
			m_contextId = m_servletContext.getServletContextName();
			if (m_contextId == null)
			{
				m_contextId = toString();
			}
		}

		if (filterConfig.getInitParameter(CONFIG_CHARACTER_ENCODING) != null)
		{
			m_characterEncoding = filterConfig.getInitParameter(CONFIG_CHARACTER_ENCODING);
		}

		if (filterConfig.getInitParameter(CONFIG_CHARACTER_ENCODING_ENABLED) != null)
		{
			m_characterEncodingEnabled = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_CHARACTER_ENCODING_ENABLED))
					.booleanValue();
		}

		if (filterConfig.getInitParameter(CONFIG_UPLOAD_ENABLED) != null)
		{
			m_uploadEnabled = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_UPLOAD_ENABLED)).booleanValue();
		}

		// get the maximum allowed upload size from the system property - use if not overriden, and also use as the ceiling if that
		// is not defined.
		if (System.getProperty(SYSTEM_UPLOAD_MAX) != null)
		{
			m_uploadMaxSize = Long.valueOf(System.getProperty(SYSTEM_UPLOAD_MAX)).longValue() * 1024L * 1024L;
			m_uploadCeiling = m_uploadMaxSize;
		}

		// if the maximum allowed upload size is configured on the filter, it overrides the system property
		if (filterConfig.getInitParameter(CONFIG_UPLOAD_MAX) != null)
		{
			m_uploadMaxSize = Long.valueOf(filterConfig.getInitParameter(CONFIG_UPLOAD_MAX)).longValue() * 1024L * 1024L;
		}

		// get the upload max ceiling that limits any other upload max, if defined
		if (System.getProperty(SYSTEM_UPLOAD_CEILING) != null)
		{
			m_uploadCeiling = Long.valueOf(System.getProperty(SYSTEM_UPLOAD_CEILING)).longValue() * 1024L * 1024L;
		}

		// get the system wide settin, if present, for the temp dir
		if (System.getProperty(SYSTEM_UPLOAD_DIR) != null)
		{
			m_uploadTempDir = System.getProperty(SYSTEM_UPLOAD_DIR);
		}

		// override with our configuration for temp dir, if set
		if (filterConfig.getInitParameter(CONFIG_UPLOAD_DIR) != null)
		{
			m_uploadTempDir = filterConfig.getInitParameter(CONFIG_UPLOAD_DIR);
		}

		if (filterConfig.getInitParameter(CONFIG_UPLOAD_THRESHOLD) != null)
		{
			m_uploadThreshold = Integer.valueOf(filterConfig.getInitParameter(CONFIG_UPLOAD_THRESHOLD)).intValue();
		}

		if (filterConfig.getInitParameter(CONFIG_CONTINUE) != null)
		{
			m_uploadContinue = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_CONTINUE)).booleanValue();
		}

		if (filterConfig.getInitParameter(CONFIG_MAX_PER_FILE) != null)
		{
			m_uploadMaxPerFile = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_MAX_PER_FILE)).booleanValue();
		}

		// Note: if set to continue processing max exceeded uploads, we only support per-file max, not overall max
		if (m_uploadContinue && !m_uploadMaxPerFile)
		{
			M_log.warn("overridding " + CONFIG_MAX_PER_FILE + " setting: must be 'true' with " + CONFIG_CONTINUE + " ='true'");
			m_uploadMaxPerFile = true;
		}
		
		String clusterTerracotta = System.getProperty("sakai.cluster.terracotta");
		TERRACOTTA_CLUSTER = "true".equals(clusterTerracotta);
		
	}

	/**
	 * If setting character encoding is enabled for this filter, and there isn't already a character encoding on the request, then
	 * set the encoding.
	 */
	protected void handleCharacterEncoding(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException
	{
		if (m_characterEncodingEnabled && req.getCharacterEncoding() == null && m_characterEncoding != null
				&& m_characterEncoding.length() > 0 && req.getAttribute(ATTR_CHARACTER_ENCODING_DONE) == null)
		{
			req.setAttribute(ATTR_CHARACTER_ENCODING_DONE, ATTR_CHARACTER_ENCODING_DONE);
			req.setCharacterEncoding(m_characterEncoding);
		}
	}

	/**
	 * if the filter is configured to parse file uploads, AND the request is multipart (typically a file upload), then parse the
	 * request.
	 * 
	 * @return If there is a file upload, and the filter handles it, return the wrapped request that has the results of the parsed
	 *         file upload. Parses the files using Apache commons-fileuplaod. Exposes the results through a wrapped request. Files
	 *         are available like: fileItem = (FileItem) request.getAttribute("myHtmlFileUploadId");
	 */
	protected HttpServletRequest handleFileUpload(HttpServletRequest req, HttpServletResponse resp, List<FileItem> tempFiles)
			throws ServletException, UnsupportedEncodingException
	{
		if (!m_uploadEnabled || !ServletFileUpload.isMultipartContent(req) || req.getAttribute(ATTR_UPLOADS_DONE) != null)
		{
			return req;
		}

		// mark that the uploads have been parsed, so they aren't parsed again on this request
		req.setAttribute(ATTR_UPLOADS_DONE, ATTR_UPLOADS_DONE);

		// Result - map that will be created of request parameters,
		// parsed parameters, and uploaded files
		Map map = new HashMap();

		// parse using commons-fileupload

		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// set the factory parameters: the temp dir and the keep-in-memory-if-smaller threshold
		if (m_uploadTempDir != null) factory.setRepository(new File(m_uploadTempDir));
		if (m_uploadThreshold > 0) factory.setSizeThreshold(m_uploadThreshold);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// set the encoding
		String encoding = req.getCharacterEncoding();
		if (encoding != null && encoding.length() > 0) upload.setHeaderEncoding(encoding);

		// set the max upload size
		long uploadMax = -1;
		if (m_uploadMaxSize > 0) uploadMax = m_uploadMaxSize;

		// check for request-scoped override to upload.max (value in megs)
		String override = req.getParameter(CONFIG_UPLOAD_MAX);
		if (override != null)
		{
			try
			{
				// get the max in bytes
				uploadMax = Long.parseLong(override) * 1024L * 1024L;
			}
			catch (NumberFormatException e)
			{
				M_log.warn(CONFIG_UPLOAD_MAX + " set to non-numeric: " + override);
			}
		}

		// limit to the ceiling
		if (uploadMax > m_uploadCeiling)
		{
			M_log.warn("Upload size exceeds ceiling: " + ((uploadMax / 1024L) / 1024L) + " > "
					+ ((m_uploadCeiling / 1024L) / 1024L) + " megs");

			uploadMax = m_uploadCeiling;
		}

		// to let commons-fileupload throw the exception on over-max, and also halt full processing of input fields
		if (!m_uploadContinue)
		{
			// TODO: when we switch to commons-fileupload 1.2
			// // either per file or overall request, as configured
			// if (m_uploadMaxPerFile)
			// {
			// upload.setFileSizeMax(uploadMax);
			// }
			// else
			// {
			// upload.setSizeMax(uploadMax);
			// }

			upload.setSizeMax(uploadMax);
		}

		try
		{
			// parse multipart encoded parameters
			boolean uploadOk = true;
			List list = upload.parseRequest(req);
			for (int i = 0; i < list.size(); i++)
			{
				FileItem item = (FileItem) list.get(i);

				if (item.isFormField())
				{
					String str = item.getString(encoding);

					Object obj = map.get(item.getFieldName());
					if (obj == null)
					{
						map.put(item.getFieldName(), new String[]
						{
							str
						});
					}
					else if (obj instanceof String[])
					{
						String[] old_vals = (String[]) obj;
						String[] values = new String[old_vals.length + 1];
						for (int i1 = 0; i1 < old_vals.length; i1++)
						{
							values[i1] = old_vals[i1];
						}
						values[values.length - 1] = str;
						map.put(item.getFieldName(), values);
					}
					else if (obj instanceof String)
					{
						String[] values = new String[2];
						values[0] = (String) obj;
						values[1] = str;
						map.put(item.getFieldName(), values);
					}
				}
				else
				{
					// collect it for delete at the end of the request
					tempFiles.add(item);

					// check the max, unless we are letting commons-fileupload throw exception on max exceeded
					// Note: the continue option assumes the max is per-file, not overall.
					if (m_uploadContinue && (item.getSize() > uploadMax))
					{
						uploadOk = false;

						M_log.info("Upload size limit exceeded: " + ((uploadMax / 1024L) / 1024L));

						req.setAttribute("upload.status", "size_limit_exceeded");
						// TODO: for 1.2 commons-fileupload, switch this to a FileSizeLimitExceededException
						req.setAttribute("upload.exception", new FileUploadBase.SizeLimitExceededException("", item.getSize(),
								uploadMax));
						req.setAttribute("upload.limit", Long.valueOf((uploadMax / 1024L) / 1024L));
					}
					else
					{
						req.setAttribute(item.getFieldName(), item);
					}
				}
			}

			// unless we had an upload file that exceeded max, set the upload status to "ok"
			if (uploadOk)
			{
				req.setAttribute("upload.status", "ok");
			}
		}
		catch (FileUploadBase.SizeLimitExceededException ex)
		{
			M_log.info("Upload size limit exceeded: " + ((upload.getSizeMax() / 1024L) / 1024L));

			// DON'T throw an exception, instead note the exception
			// so that the tool down-the-line can handle the problem
			req.setAttribute("upload.status", "size_limit_exceeded");
			req.setAttribute("upload.exception", ex);
			req.setAttribute("upload.limit", Long.valueOf((upload.getSizeMax() / 1024L) / 1024L));
		}
		// TODO: put in for commons-fileupload 1.2
		// catch (FileUploadBase.FileSizeLimitExceededException ex)
		// {
		// M_log.info("Upload size limit exceeded: " + ((upload.getFileSizeMax() / 1024L) / 1024L));
		//
		// // DON'T throw an exception, instead note the exception
		// // so that the tool down-the-line can handle the problem
		// req.setAttribute("upload.status", "size_limit_exceeded");
		// req.setAttribute("upload.exception", ex);
		// req.setAttribute("upload.limit", new Long((upload.getFileSizeMax() / 1024L) / 1024L));
		// }
		catch (FileUploadException ex)
		{
			M_log.info("Unexpected exception in upload parsing", ex);
			req.setAttribute("upload.status", "exception");
			req.setAttribute("upload.exception", ex);
		}

		// add any parameters that were in the URL - make sure to get multiples
		for (Enumeration e = req.getParameterNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			String[] values = req.getParameterValues(name);
			map.put(name, values);
		}

		// return a wrapped response that exposes the parsed parameters and files
		return new WrappedRequestFileUpload(req, map);
	}

	/**
	 * Make sure we have a Sakai session.
	 * 
	 * @param req
	 *        The request object.
	 * @param res
	 *        The response object.
	 * @return The Sakai Session object.
	 */
	protected Session assureSession(HttpServletRequest req, HttpServletResponse res)
	{
		Session s = null;
		String sessionId = null;
		boolean allowSetCookieEarly = true;
		Cookie c = null;
		
		// automatic, i.e. not from user activite, request?
		boolean auto = req.getParameter(PARAM_AUTO) != null;

		String suffix = getCookieSuffix();

		// try finding a non-cookie session based on the remote user / principal
		// Note: use principal instead of remote user to avoid any possible confusion with the remote user set by single-signon
		// auth.
		// Principal is set by our Dav interface, which this is designed to cover. -ggolden
		
		Principal principal = req.getUserPrincipal();

		if ((principal != null) && (principal.getName() != null))
		{
			// set our session id to the remote user id
			sessionId = SessionManager.makeSessionId(req, principal);

			// don't supply this cookie to the client
			allowSetCookieEarly = false;
			
			// find the session
			s = SessionManager.getSession(sessionId);

			// if not found, make a session for this user
			if (s == null)
			{
				s = SessionManager.startSession(sessionId);
			}
			
			// Make these sessions expire after 10 minutes
			s.setMaxInactiveInterval(10*60);
		}

		// if no principal, check request parameter and cookie
		if (sessionId == null || s == null)
		{
			sessionId = req.getParameter(ATTR_SESSION);

			// find our session id from our cookie
			c = findCookie(req, SESSION_COOKIE, suffix);

			if (sessionId == null && c != null)
			{
				// get our session id
				sessionId = c.getValue();
			}

			if (sessionId != null)
			{
				// remove the server id suffix
				final int dotPosition = sessionId.indexOf(DOT);
				if (dotPosition > -1)
				{
					sessionId = sessionId.substring(0, dotPosition);
				}
				if (M_log.isDebugEnabled())
				{
					M_log.debug("assureSession found sessionId in cookie: " + sessionId);
				}

				// find the session
				s = SessionManager.getSession(sessionId);
			}
		}

		// if found and not automatic, mark it as active
		if ((s != null) && (!auto))
		{
			synchronized(s) {
				s.setActive();
			}
		}

		// if missing, make one
		if (s == null)
		{
			s = SessionManager.startSession();

			// if we have a cookie, but didn't find the session and are creating a new one, mark this
			if (c != null)
			{
				ThreadLocalManager.set(SessionManager.CURRENT_INVALID_SESSION, SessionManager.CURRENT_INVALID_SESSION);
			}
		}

		// put the session in the request attribute
		req.setAttribute(ATTR_SESSION, s);

		// set this as the current session
		SessionManager.setCurrentSession(s);
		
		// Now that we know the session exists, regardless of whether it's new or not, lets see if there
		// is a UsageSession.  If so, we want to check it's serverId
		UsageSession us = null;
		// FIXME synchronizing on a changing value is a bad practice plus it is possible for s to be null according to the visible code -AZ
		synchronized(s) {
			us = (UsageSession)s.getAttribute(UsageSessionService.USAGE_SESSION_KEY);
			if (us != null) {
				// check the server instance id
				ServerConfigurationService configService = org.sakaiproject.component.cover.ServerConfigurationService.getInstance();
				String serverInstanceId = configService.getServerIdInstance();
				if ((serverInstanceId != null) && (!serverInstanceId.equals(us.getServer()))) {
					// Log that the UsageSession server value is changing
					M_log.info("UsageSession: Server change detected: Old Server=" + us.getServer() +
							"    New Server=" + serverInstanceId);
					// set the new UsageSession server value
					us.setServer(serverInstanceId);
				}
			}
		}

		// if we had a cookie and we have no session, clear the cookie TODO: detect closed session in the request
		if ((s == null) && (c != null))
		{
			// remove the cookie
			c = new Cookie(SESSION_COOKIE, "");
			c.setPath("/");
			c.setMaxAge(0);
			res.addCookie(c);
		}

		// if we have a session and had no cookie,
		// or the cookie was to another session id, set the cookie
		if ((s != null) && allowSetCookieEarly)
		{
			// the cookie value we need to use
			sessionId = s.getId() + DOT + suffix;

			if ((c == null) || (!c.getValue().equals(sessionId)))
			{
				// set the cookie
				c = new Cookie(SESSION_COOKIE, sessionId);
				c.setPath("/");
				c.setMaxAge(-1);
				if (req.isSecure() == true)
				{
					c.setSecure(true);
				}
				res.addCookie(c);
			}
		}

		return s;
	}

	/**
	 * Detect a tool placement from the URL, and if found, setup the placement attribute and current tool session based on that id.
	 * 
	 * @param s
	 *        The sakai session.
	 * @param req
	 *        The request, already prepared with the placement id if any.
	 * @return The tool session.
	 */
	protected ToolSession detectToolPlacement(Session s, HttpServletRequest req)
	{
		// skip if so configured
		if (this.m_toolPlacement == false) return null;

		ToolSession toolSession = null;
		String placementId = (String) req.getParameter(Tool.PLACEMENT_ID);
		if (placementId != null)
		{
			toolSession = s.getToolSession(placementId);

			// put the session in the request attribute
			req.setAttribute(Tool.TOOL_SESSION, toolSession);

			// set as the current tool session
			SessionManager.setCurrentToolSession(toolSession);

			// put the placement id in the request attribute
			req.setAttribute(Tool.PLACEMENT_ID, placementId);
		}

		return toolSession;
	}

	/**
	 * Pre-process the request, returning a possibly wrapped req for further processing.
	 * 
	 * @param s
	 *        The Sakai Session.
	 * @param req
	 *        The request object.
	 * @return a possibly wrapped and possibly new request object for further processing.
	 */
	protected HttpServletRequest preProcessRequest(Session s, HttpServletRequest req)
	{
		req = new WrappedRequest(s, m_contextId, req);

		return req;
	}

	/**
	 * Pre-process the response, returning a possibly wrapped res for further processing.
	 * 
	 * @param s
	 *        The Sakai Session.
	 * @param req
	 *        The request object.
	 * @param res
	 *        The response object.
	 * @return a possibly wrapped and possibly new response object for further processing.
	 */
	protected HttpServletResponse preProcessResponse(Session s, HttpServletRequest req, HttpServletResponse res)
	{
		res = new WrappedResponse(s, req, res);

		return res;
	}

	/**
	 * Post-process the response.
	 * 
	 * @param s
	 *        The Sakai Session.
	 * @param req
	 *        The request object.
	 * @param res
	 *        The response object.
	 */
	protected void postProcessResponse(Session s, HttpServletRequest req, HttpServletResponse res)
	{
	}

	/**
	 * Find a cookie by this name from the request; one with a value that has the specified suffix.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param name
	 *        The cookie name
	 * @param suffix
	 *        The suffix string to find at the end of the found cookie value.
	 * @return The cookie of this name in the request, or null if not found.
	 */
	protected Cookie findCookie(HttpServletRequest req, String name, String suffix)
	{
		Cookie[] cookies = req.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(name))
				{
					// If this is NOT a terracotta cluster environment
					// and the suffix passed in to this method is not null
					// then only match the cookie if the end of the cookie
					// value is equal to the suffix passed in.
					if (TERRACOTTA_CLUSTER || ((suffix == null) || cookies[i].getValue().endsWith(suffix)))
					{
						return cookies[i];
					}
				}
			}
		}

		return null;
	}

	/**
	 * Compute the URL that would return to this server based on the current request. Note: this method is a duplicate of one in the
	 * util/Web.java
	 * 
	 * @param req
	 *        The request.
	 * @return The URL back to this server based on the current request.
	 */
	public static String serverUrl(HttpServletRequest req)
	{
		String transport = null;
		int port = 0;
		boolean secure = false;

		// if force.url.secure is set (to a https port number), use https and this port
		String forceSecure = System.getProperty("sakai.force.url.secure");
		if (forceSecure != null)
		{
			transport = "https";
			port = Integer.parseInt(forceSecure);
			secure = true;
		}

		// otherwise use the request scheme and port
		else
		{
			transport = req.getScheme();
			port = req.getServerPort();
			secure = req.isSecure();
		}

		StringBuilder url = new StringBuilder();
		url.append(transport);
		url.append("://");
		url.append(req.getServerName());
		if (((port != 80) && (!secure)) || ((port != 443) && secure))
		{
			url.append(":");
			url.append(port);
		}

		return url.toString();
	}
	
	/**
	 * Get cookie suffix from the serverId.
	 * We can't do this at init time as it might not have been set yet (sakai hasn't started).
	 * @return The cookie suffix to use.
	 */
	private String getCookieSuffix()
	{
		// compute the session cookie suffix, based on this configured server id
		String suffix = System.getProperty(SAKAI_SERVERID);
		if ((suffix == null) || (suffix.length() == 0))
		{
			if (m_displayModJkWarning)
			{
				M_log.warn("no sakai.serverId system property set - mod_jk load balancing will not function properly");
			}
			m_displayModJkWarning = false;
			suffix = "sakai";
		}
		return suffix;
	}

}
