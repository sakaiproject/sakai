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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.math.NumberUtils;

import org.sakaiproject.cluster.api.ClusterNode;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.cluster.api.ClusterService.Status;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ClosingException;
import org.sakaiproject.tool.api.RebuildBreakdownService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;


/**
 * RequestFilter Filters all requests to Sakai tools. It is responsible for keeping the Sakai session, done using a cookie to the
 * end user's browser storing the user's session id.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class RequestFilter implements Filter
{
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
	/** Config parameter to control whether to check the request principal before any cookie to establish a session */
	public static final String CONFIG_SESSION_AUTH = "sakai.session.auth";
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
	/** Key in the ThreadLocalManager for access to the current http request object. */
	public final static String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";
	/** Key in the ThreadLocalManager for access to the current http response object. */
	public final static String CURRENT_HTTP_RESPONSE = "org.sakaiproject.util.RequestFilter.http_response";
	/** Key in the ThreadLocalManager for access to the current servlet context. */
	public final static String CURRENT_SERVLET_CONTEXT = "org.sakaiproject.util.RequestFilter.servlet_context";
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
	/** sakaiHttpSession setting for don't do anything. */
	protected final static int CONTAINER_SESSION = 0;
	/** sakaiHttpSession setting for use the sakai wide session. */
	protected final static int SAKAI_SESSION = 1;
	/** sakaiHttpSession setting for use the context session. */
	protected final static int CONTEXT_SESSION = 2;
	/** sakaiHttpSession setting for use the tool session, in any, else context. */
	protected final static int TOOL_SESSION = 3;
	/** If true, we deliver the Sakai wide session as the Http session for each request. */
	protected int m_sakaiHttpSession = TOOL_SESSION;
	/** Key in the ThreadLocalManager for binding our remoteUser preference. */
	protected final static String CURRENT_REMOTE_USER = "org.sakaiproject.util.RequestFilter.remote_user";
	/** Key in the ThreadLocalManager for binding our http session preference. */
	protected final static String CURRENT_HTTP_SESSION = "org.sakaiproject.util.RequestFilter.http_session";
	/** Key in the ThreadLocalManager for binding our context id. The servlet context is stored against this. */
	protected final static String CURRENT_CONTEXT = "org.sakaiproject.util.RequestFilter.context";
	/** The "." character */
	protected static final String DOT = ".";

	/** The name of the system property that will be used when setting the value of the session cookie. */
	protected static final String SAKAI_SERVERID = "sakai.serverId";

	/** The name of the system property that will be used when setting the name of the session cookie. */
	protected static final String SAKAI_COOKIE_NAME = "sakai.cookieName";

	/** The name of the system property that will be used when setting the domain of the session cookie. */
	protected static final String SAKAI_COOKIE_DOMAIN = "sakai.cookieDomain";

	/** The name of the Sakai property to disable setting the HttpOnly attribute on the cookie (if false). */
	protected static final String SAKAI_COOKIE_HTTP_ONLY = "sakai.cookieHttpOnly";

	/** The name of the Sakai property to set the SameSite attribute on the cookie. "lax" is the default. */
	protected static final String SAKAI_COOKIE_SAME_SITE = "sakai.cookieSameSite";

	/** The name of the Sakai property to set the X-UA Compatible header
	 */
	protected static final String SAKAI_UA_COMPATIBLE = "sakai.X-UA-Compatible";
	
	/** The name of the Sakai property to allow passing a session id in the ATTR_SESSION request parameter */
	protected static final String SAKAI_SESSION_PARAM_ALLOW = "session.parameter.allow";
	
	/** The tools allowed as lti provider **/
	protected static final String SAKAI_BLTI_PROVIDER_TOOLS = "basiclti.provider.allowedtools";

	/** The name of the Skaia property to say we should redirect to another node when in shutdown */
	protected static final String SAKAI_CLUSTER_REDIRECT_RANDOM = "cluster.redirect.random.node";

	/** If true, we deliver the Sakai end user enterprise id as the remote user in each request. */
	protected boolean m_sakaiRemoteUser = true;

	/** If true, we encode / decode the tool placement using the a URL parameter. */
	protected boolean m_toolPlacement = true;

	/** Our contex (i.e. servlet context) id. */
	protected String m_contextId = null;

	protected String m_characterEncoding = "UTF-8";

	protected boolean m_characterEncodingEnabled = true;

	protected boolean m_uploadEnabled = true;

	protected boolean m_checkPrincipal = false; 
	
	protected long m_uploadMaxSize = 1L * 1024L * 1024L;

	protected long m_uploadCeiling = 1L * 1024L * 1024L;

	protected int m_uploadThreshold = 1024;

	protected String m_uploadTempDir = null;

	protected boolean m_displayModJkWarning = true;

	protected boolean m_redirectRandomNode = true;

	/** Default is to abort further upload processing if the max is exceeded. */
	protected boolean m_uploadContinue = false;

	/** Default is to treat the m_uploadMaxSize as for the entire request, not per file. */
	protected boolean m_uploadMaxPerFile = false;

	/** The servlet context for the filter. */
	protected ServletContext m_servletContext = null;
	
	/** Is this a Terracotta clustered environment? */
	protected boolean TERRACOTTA_CLUSTER = false;

	/** Allow setting the cookie in a request parameter */
	protected boolean m_sessionParamAllow = false;
                                                                                                             
    /** The name of the cookie we use to keep sakai session. */                                            
    protected String cookieName = "JSESSIONID";                                                            
                                                                                                              
    protected String cookieDomain = null; 

    private ThreadLocalManager threadLocalManager;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
	private RebuildBreakdownService rebuildBreakdownService;

	public RequestFilter() {
		threadLocalManager = ComponentManager.get(ThreadLocalManager.class);
		sessionManager = ComponentManager.get(SessionManager.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		rebuildBreakdownService = ComponentManager.get(RebuildBreakdownService.class);
	}

	/** Set the HttpOnly attribute on the cookie */
	protected boolean m_cookieHttpOnly = true;
	/** Set the SameSite attribute on the cookie */
	protected String m_cookieSameSite = "lax";

	protected String m_UACompatible = null;
            
	protected boolean isLTIProviderAllowed = false;
	// knl-640
	private String chsDomain;
	private String appUrl;
	private String chsUrl;
	private boolean useContentHostingDomain;
	private String [] contentPaths;
	private String [] loginPaths;
	private String [] contentExceptions;

	/**
	 * Compute the URL that would return to this server based on the current request.
	 *
	 * Note: this method is used by the one in /sakai-kernel-util/src/main/java/org/sakaiproject/util/Web.java
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
		int forceSecureInt = NumberUtils.toInt(forceSecure);
		if (forceSecureInt > 0 && forceSecureInt <= 65535) {
			transport = "https";
			port = forceSecureInt;
			secure = true;
		} else {
	        // otherwise use the request scheme and port
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
	 * Take this filter out of service.
	 */
	public void destroy()
	{
	}

	private boolean startsWithAny(String source, String[] toMatch) {
		for (String test: toMatch) {
			if (source.startsWith(test)) {
				return true;
			}
		}
		return false;
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
		Boolean curRemoteUser = (Boolean) threadLocalManager.get(CURRENT_REMOTE_USER);
		Integer curHttpSession = (Integer) threadLocalManager.get(CURRENT_HTTP_SESSION);
		String curContext = (String) threadLocalManager.get(CURRENT_CONTEXT);
		ServletRequest curRequest = (ServletRequest) threadLocalManager.get(CURRENT_HTTP_REQUEST);
		ServletResponse curResponse = (ServletResponse) threadLocalManager.get(CURRENT_HTTP_RESPONSE);
		boolean cleared = false;

		// keep track of temp files with this request that need to be deleted on the way out
		List<FileItem> tempFiles = new ArrayList<FileItem>();

		try
		{
			threadLocalManager.set(CURRENT_REMOTE_USER, Boolean.valueOf(m_sakaiRemoteUser));
			threadLocalManager.set(CURRENT_HTTP_SESSION, Integer.valueOf(m_sakaiHttpSession));
			threadLocalManager.set(CURRENT_CONTEXT, m_contextId);

			// make the servlet context available
			threadLocalManager.set(CURRENT_SERVLET_CONTEXT, m_servletContext);

			// we are expecting HTTP stuff
			if (!((requestObj instanceof HttpServletRequest) && (responseObj instanceof HttpServletResponse)))
			{
				// if not, just pass it through
				chain.doFilter(requestObj, responseObj);
				return;
			}

			HttpServletRequest req = (HttpServletRequest) requestObj;
			HttpServletResponse resp = (HttpServletResponse) responseObj;

			// knl-640
			// The AppDomain should reject:
			// 1) all GET URL's starting with contentPaths
			//
			// The FileDomain should only accept:
			// 1) any URL's in loginPath. We have to accept POST methods here
			//    as well so folks can log in on this node.
			// 2) any GET URL's from contentPaths (POST's any other methods not
			//    allowed.
			if (useContentHostingDomain) {
				String requestURI = req.getRequestURI();
				if(req.getQueryString() != null) requestURI += "?" + req.getQueryString();
				if (startsWithAny(requestURI, contentPaths) && "GET".equalsIgnoreCase(req.getMethod())) {
					if  (!req.getServerName().equals(chsDomain) && !(startsWithAny(requestURI, contentExceptions))) {
						resp.sendRedirect(chsUrl+requestURI);
						return;
					}
				}
				else {
					if (req.getServerName().equals(chsDomain) &&
						!(startsWithAny(requestURI, contentPaths) && !"GET".equalsIgnoreCase(req.getMethod())) &&
						!(startsWithAny(requestURI, loginPaths))) {
						resp.sendRedirect(appUrl+requestURI);
						return;
					}
				}
			}

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
				threadLocalManager.set(CURRENT_HTTP_REQUEST, req);
				threadLocalManager.set(CURRENT_HTTP_RESPONSE, resp);

				chain.doFilter(req, resp);
			}

			// filter the request
			else
			{
				log.debug("http-request: {} {}?{}", req.getMethod(), req.getRequestURL(), req.getQueryString());

				try
				{
					// mark the request as filtered to avoid re-filtering it later in the request processing
					req.setAttribute(ATTR_FILTERED, ATTR_FILTERED);

					// some useful info
					threadLocalManager.set(ServerConfigurationService.CURRENT_SERVER_URL, serverUrl(req));

					// make sure we have a session
					Session s = assureSession(req, resp);

					// pre-process request
					req = preProcessRequest(s, req);

					// detect a tool placement and set the current tool session
					detectToolPlacement(s, req);

					// pre-process response
					resp = preProcessResponse(s, req, resp);

					// set the request and response for access via the thread local
					threadLocalManager.set(CURRENT_HTTP_REQUEST, req);
					threadLocalManager.set(CURRENT_HTTP_RESPONSE, resp);

					// set the portal into thread local
					if (m_contextId != null && m_contextId.length() > 0)
					{
						threadLocalManager.set(ServerConfigurationService.CURRENT_PORTAL_PATH, "/" + m_contextId);
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
						Cookie c = findCookie(req, cookieName, suffix);

						// the cookie value we need to use
						String sessionId = s.getId() + DOT + suffix;

						// set the cookie if necessary
						if ((c == null) || (!c.getValue().equals(sessionId))) {
							c = new Cookie(cookieName, sessionId);
							c.setPath("/");
							c.setMaxAge(-1);
							if (cookieDomain != null)
							{
								c.setDomain(cookieDomain);
							}
							if (req.isSecure() == true)
							{
								c.setSecure(true);
							}
							addCookie(resp, c);
						}
					}


				}
				catch (ClosingException se) {
					closingRedirect(req, resp);
				}
				catch (RuntimeException t)
				{
					log.error("", t);
					throw t;
				}
				catch (IOException ioe)
				{
					log.error("", ioe);
					throw ioe;
				}
				catch (ServletException se)
				{
					log.error(se.getMessage(), se);
					throw se;
				}
				finally
				{
					// clear any bound current values
					threadLocalManager.clear();
					cleared = true;
				}
			}

		}
		finally
		{
			if (!cleared)
			{
				// restore the "current" bindings
				threadLocalManager.set(CURRENT_REMOTE_USER, curRemoteUser);
				threadLocalManager.set(CURRENT_HTTP_SESSION, curHttpSession);
				threadLocalManager.set(CURRENT_CONTEXT, curContext);
				threadLocalManager.set(CURRENT_HTTP_REQUEST, curRequest);
				threadLocalManager.set(CURRENT_HTTP_RESPONSE, curResponse);
			}

			// delete any temp files
			deleteTempFiles(tempFiles);

			if (log.isDebugEnabled() && sb != null)
			{
				long elapsedTime = System.currentTimeMillis() - startTime;
				log.debug("request timing (ms): " + elapsedTime + " for " + sb);
			}
		}
	}

	/**
	 * This is called when a request is made to a node that is in the process of closing down
	 * and so we don't want to allow new session to be created.
	 * @param req The servlet request.
	 * @param res The servlet response.
	 */
	protected void closingRedirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// We should avoid redirecting on non get methods as the body will be lost.
		if (!"GET".equals(req.getMethod())) {
			log.warn("Non GET request for "+ req.getPathInfo());
		}

		// We could check that we aren't in a redirect loop here, but if the load balancer doesn't know that
		// a node is no longer responding to new sessions it may still be sending it new clients, and so after
		// a couple of redirects it should hop off this node.
		String value = getRedirectNode();
		// set the cookie
		Cookie c = new Cookie(cookieName, value);
		c.setPath("/");
		// Delete the cookie
		c.setMaxAge(0);
		if (cookieDomain != null)
		{
			c.setDomain(cookieDomain);
		}
		if (req.isSecure() == true)
		{
			c.setSecure(true);
		}
		addCookie(res, c);

		// We want the non-decoded ones so we don't have to re-encode.
		StringBuilder url = new StringBuilder(req.getRequestURI());
		if (req.getQueryString() != null) {
			url.append("?").append(req.getQueryString());
		}
		res.sendRedirect(url.toString());
	}

	/**
	 * This looks to find a node to redirect to or if it can't find one it just empties the cookie
	 * so the load balancer chooses.
	 * @return The cookie value for a different node.
	 */
	protected String getRedirectNode() {
		if (m_redirectRandomNode) {
			ClusterService clusterService = (ClusterService) ComponentManager.get(ClusterService.class);
			Map<String, ClusterNode> nodes = clusterService.getServerStatus();
			// There may be more than one node listed for each node ID, just list the latest ones.
			Map<String, ClusterNode> latestNodes = new HashMap<>();
			for (ClusterNode node: nodes.values()) {
				ClusterNode latest = latestNodes.get(node.getServerId());
				if (latest == null || latest.getUpdated().after(node.getUpdated())) {
					latestNodes.put(node.getServerId(), node);
				}
			}
			// This node shouldn't ever be included but it's better safe than sorry.
			latestNodes.remove(System.getProperty(SAKAI_SERVERID));
			// Remove all the non-running servers.
			List<String> activeServers = new ArrayList<>(latestNodes.size());
			for (ClusterNode node : latestNodes.values()) {
				if (Status.RUNNING.equals(node.getStatus())) {
					activeServers.add(node.getServerId());
				}
			}
			// Pick a random remaining server if we have one.
			if (!(activeServers.isEmpty())) {
				Random random = new Random();
				int i = random.nextInt(activeServers.size());
				String serverId = activeServers.get(i);
				return DOT + serverId;
			}
		}
		return "";
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

		// Requesting the ServerConfigurationService here also triggers the promotion of certain
		// sakai.properties settings to system properties - see SakaiPropertyPromoter()


		// knl-640
		appUrl = serverConfigurationService.getString("serverUrl", null);
		chsDomain = serverConfigurationService.getString("content.chs.serverName", null);
		chsUrl = serverConfigurationService.getString("content.chs.serverUrl", null);
		useContentHostingDomain = serverConfigurationService.getBoolean("content.separateDomains", false);
		contentPaths = serverConfigurationService.getStrings("content.chs.urlprefixes");
		if (contentPaths == null) {
			contentPaths = new String[] { "/access/", "/web/"};
		}
		loginPaths = serverConfigurationService.getStrings("content.login.urlprefixes");
		if (loginPaths == null) {
			loginPaths = new String[] { "/access/login", "/sakai-login-tool", "/access/require", "/access/accept" };
		}
		contentExceptions = serverConfigurationService.getStrings("content.chsexception.urlprefixes");
		if (contentExceptions == null) {
			// add in default exceptions here, if desired
			contentExceptions = new String[] { "/access/calendar/", "/access/citation/export_ris_sel/", "/access/citation/export_ris_all/" };
		}

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
				log.warn("invalid " + CONFIG_SESSION + " setting (" + s + "): not one of container, sakai, context, tool");
			}
		}

		if (filterConfig.getInitParameter(CONFIG_REMOTE_USER) != null)
		{
			m_sakaiRemoteUser = Boolean.valueOf(filterConfig.getInitParameter(CONFIG_REMOTE_USER)).booleanValue();
		}

		if (filterConfig.getInitParameter(CONFIG_SESSION_AUTH) != null)
		{
			m_checkPrincipal= "basic".equals(filterConfig.getInitParameter(CONFIG_SESSION_AUTH));
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
			// This is a little confusing as we're taking a display name and using it as an ID.
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
			m_uploadMaxSize = Long.valueOf(System.getProperty(SYSTEM_UPLOAD_MAX).trim()).longValue() * 1024L * 1024L;
			m_uploadCeiling = m_uploadMaxSize;
		}

		// if the maximum allowed upload size is configured on the filter, it overrides the system property
		if (filterConfig.getInitParameter(CONFIG_UPLOAD_MAX) != null)
		{
			m_uploadMaxSize = Long.valueOf(filterConfig.getInitParameter(CONFIG_UPLOAD_MAX).trim()).longValue() * 1024L * 1024L;
		}

		// get the upload max ceiling that limits any other upload max, if defined
		if (System.getProperty(SYSTEM_UPLOAD_CEILING) != null)
		{
			m_uploadCeiling = Long.valueOf(System.getProperty(SYSTEM_UPLOAD_CEILING).trim()).longValue() * 1024L * 1024L;
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
			log.warn("overridding " + CONFIG_MAX_PER_FILE + " setting: must be 'true' with " + CONFIG_CONTINUE + " ='true'");
			m_uploadMaxPerFile = true;
		}

		String clusterTerracotta = System.getProperty("sakai.cluster.terracotta");
		TERRACOTTA_CLUSTER = "true".equals(clusterTerracotta);

		// retrieve the configured cookie name, if any
		if (System.getProperty(SAKAI_COOKIE_NAME) != null)
		{
			cookieName = System.getProperty(SAKAI_COOKIE_NAME);
		}

		// retrieve the configured cookie domain, if any
		if (System.getProperty(SAKAI_COOKIE_DOMAIN) != null)
		{
			cookieDomain = System.getProperty(SAKAI_COOKIE_DOMAIN);
		}

		m_sessionParamAllow = serverConfigurationService.getBoolean(SAKAI_SESSION_PARAM_ALLOW, false);

		// retrieve option to enable or disable cookie HttpOnly
		m_cookieHttpOnly = serverConfigurationService.getBoolean(SAKAI_COOKIE_HTTP_ONLY, true);
		// retrieve option to enable or disable cookie SameSite
		m_cookieSameSite = serverConfigurationService.getString(SAKAI_COOKIE_SAME_SITE, null);

		m_UACompatible = serverConfigurationService.getString(SAKAI_UA_COMPATIBLE, null);

		isLTIProviderAllowed = (serverConfigurationService.getString(SAKAI_BLTI_PROVIDER_TOOLS,null)!=null);

		m_redirectRandomNode = serverConfigurationService.getBoolean(SAKAI_CLUSTER_REDIRECT_RANDOM, true);

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
				log.warn(CONFIG_UPLOAD_MAX + " set to non-numeric: " + override);
			}
		}

		// limit to the ceiling
		if (uploadMax > m_uploadCeiling)
		{
			/**
			 * KNL-602 This is the expected behaviour of the request filter honouring the globaly configured
			 * value -DH
			 */
			log.debug("Upload size exceeds ceiling: " + ((uploadMax / 1024L) / 1024L) + " > "
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

						log.info("Upload size limit exceeded: " + ((uploadMax / 1024L) / 1024L));

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
			log.info("Upload size limit exceeded: " + ((upload.getSizeMax() / 1024L) / 1024L));

			// DON'T throw an exception, instead note the exception
			// so that the tool down-the-line can handle the problem
			req.setAttribute("upload.status", "size_limit_exceeded");
			req.setAttribute("upload.exception", ex);
			req.setAttribute("upload.limit", Long.valueOf((upload.getSizeMax() / 1024L) / 1024L));
		}
		// TODO: put in for commons-fileupload 1.2
		// catch (FileUploadBase.FileSizeLimitExceededException ex)
		// {
		// log.info("Upload size limit exceeded: " + ((upload.getFileSizeMax() / 1024L) / 1024L));
		//
		// // DON'T throw an exception, instead note the exception
		// // so that the tool down-the-line can handle the problem
		// req.setAttribute("upload.status", "size_limit_exceeded");
		// req.setAttribute("upload.exception", ex);
		// req.setAttribute("upload.limit", new Long((upload.getFileSizeMax() / 1024L) / 1024L));
		// }
		catch (FileUploadException ex)
		{
			log.info("Unexpected exception in upload parsing", ex);
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

		// automatic, i.e. not from user activity, request?
		boolean auto = req.getParameter(PARAM_AUTO) != null;

		// session id provided in a request parameter?
		boolean reqsession = m_sessionParamAllow && req.getParameter(ATTR_SESSION) != null;

		String suffix = getCookieSuffix();

		// try finding a non-cookie session based on the remote user / principal
		// Note: use principal instead of remote user to avoid any possible confusion with the remote user set by single-signon
		// auth.
		// Principal is set by our Dav interface, which this is designed to cover. -ggolden

		Principal principal = req.getUserPrincipal();

		if (m_checkPrincipal && (principal != null) && (principal.getName() != null))
		{
			// set our session id to the remote user id
			sessionId = sessionManager.makeSessionId(req, principal);

			// don't supply this cookie to the client
			allowSetCookieEarly = false;

			// find the session
			s = sessionManager.getSession(sessionId);

			// if not found, make a session for this user
			if (s == null)
			{
				s = sessionManager.startSession(sessionId);
			}

			// Make these sessions expire after 10 minutes
			s.setMaxInactiveInterval(10*60);
		}

		// if no principal, check request parameter and cookie
		if (sessionId == null || s == null)
		{
			if (m_sessionParamAllow) {
				sessionId = req.getParameter(ATTR_SESSION);
			}

			// find our session id from our cookie
			c = findCookie(req, cookieName, suffix);

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
				if (log.isDebugEnabled())
				{
					log.debug("assureSession found sessionId in cookie: " + sessionId);
				}

				// find the session
				s = sessionManager.getSession(sessionId);
			}

			// ignore the session id provided in a request parameter
			// if the session is not authenticated
			if (reqsession && s != null && s.getUserId() == null) {
				s = null;
			}
		}

		// if found and not automatic, mark it as active
		if ((s != null) && (!auto))
		{
			synchronized(s) {
				s.setActive();
			}
		}
		if (s == null && sessionId != null) {
			// check to see if this session has already been built.  If not, rebuild

			if (rebuildBreakdownService != null) {
				s = sessionManager.startSession(sessionId);
				if (!rebuildBreakdownService.rebuildSession(s)) {
					s.invalidate();
					s = null;
				}
			}
		}

		// if missing, make one
		if (s == null)
		{
			s = sessionManager.startSession();

			// if we have a cookie, but didn't find the session and are creating a new one, mark this
			if (c != null)
			{
				threadLocalManager.set(SessionManager.CURRENT_INVALID_SESSION, SessionManager.CURRENT_INVALID_SESSION);
			}
		}

		// put the session in the request attribute
		req.setAttribute(ATTR_SESSION, s);

		// set this as the current session
		sessionManager.setCurrentSession(s);

		// Now that we know the session exists, regardless of whether it's new or not, lets see if there
		// is a UsageSession.  If so, we want to check it's serverId
		UsageSession us = null;
		// FIXME synchronizing on a changing value is a bad practice plus it is possible for s to be null according to the visible code -AZ
		synchronized(s) {
			us = (UsageSession)s.getAttribute(UsageSessionService.USAGE_SESSION_KEY);
			if (us != null) {
				// check the server instance id
				String serverInstanceId = serverConfigurationService.getServerIdInstance();
				if ((serverInstanceId != null) && (!serverInstanceId.equals(us.getServer()))) {
					// Logger that the UsageSession server value is changing
					log.info("UsageSession: Server change detected: Old Server=" + us.getServer() +
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
			c = new Cookie(cookieName, "");
			c.setPath("/");
			c.setMaxAge(0);
			if (cookieDomain != null)
			{
				c.setDomain(cookieDomain);
			}
			addCookie(res, c);
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
				c = new Cookie(cookieName, sessionId);
				c.setPath("/");
				c.setMaxAge(-1);
				if (cookieDomain != null)
				{
					c.setDomain(cookieDomain);
				}
				if (req.isSecure() == true)
				{
					c.setSecure(true);
				}
				addCookie(res, c);
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
			sessionManager.setCurrentToolSession(toolSession);

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
		//Set response headers SAK-20058
		if (m_UACompatible != null) {
			res.setHeader("X-UA-Compatible",m_UACompatible);
		}

		if (!isLTIProviderAllowed && (!useContentHostingDomain || !req.getServerName().equals(chsDomain))) {
			res.setHeader("X-Frame-Options", "SAMEORIGIN");
		}

		UsageSession us = (UsageSession)s.getAttribute(UsageSessionService.USAGE_SESSION_KEY);
		if (us != null) {
			res.setHeader("X-Sakai-Session",us.getId());
		}

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
		if (rebuildBreakdownService != null) {
		    rebuildBreakdownService.storeSession(s, req);
		}
	}

	/**
	 * isSessionClusteringEnabled() checks if session information is clustered.
	 * Clustering can be either through Terracotta clustering or through
	 * RebuildBreakdownService session clustering
	 * @return true if sessionClustering is enabled
	 */
	private boolean isSessionClusteringEnabled()
	{
	    return TERRACOTTA_CLUSTER || rebuildBreakdownService != null && rebuildBreakdownService.isSessionHandlingEnabled();
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
					if (isSessionClusteringEnabled() || ((suffix == null) || cookies[i].getValue().endsWith(suffix)))
					{
						return cookies[i];
					}
				}
			}
		}

		return null;
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
				log.warn("no sakai.serverId system property set - mod_jk load balancing will not function properly");
			}
			m_displayModJkWarning = false;
			suffix = "sakai";
		}
		return suffix;
	}
	
	protected void addCookie(HttpServletResponse res, Cookie cookie) {

		if (!m_cookieHttpOnly) {
			// Use the standard servlet mechanism for setting the cookie
			res.addCookie(cookie);
		} else {
			// Set the cookie manually

			StringBuffer sb = new StringBuffer();

			ServerCookie.appendCookieValue(sb, cookie.getVersion(), cookie.getName(), cookie.getValue(),
					cookie.getPath(), cookie.getDomain(), cookie.getComment(),
					cookie.getMaxAge(), cookie.getSecure(), m_cookieHttpOnly, m_cookieSameSite);

			res.addHeader("Set-Cookie", sb.toString());
		}
		return;
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
			boolean remoteUser = ((Boolean) threadLocalManager.get(CURRENT_REMOTE_USER)).booleanValue();

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
			int curHttpSession = ((Integer) threadLocalManager.get(CURRENT_HTTP_SESSION)).intValue();
			String curContext = (String) threadLocalManager.get(CURRENT_CONTEXT);

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
					rv = (HttpSession) sessionManager.getCurrentToolSession();
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
}
