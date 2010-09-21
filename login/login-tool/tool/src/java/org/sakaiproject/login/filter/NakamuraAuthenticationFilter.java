/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/branches/SAK-17223/login-tool/tool/src/java/org/sakaiproject/login/filter/NakamuraAuthenticationFilter.java $
 * $Id: NakamuraAuthenticationFilter.java 82350 2010-09-17 15:01:25Z lance@indiana.edu $
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

package org.sakaiproject.login.filter;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.util.XSakaiToken;

/**
 * 
 */
public class NakamuraAuthenticationFilter implements Filter {
	private static final Log LOG = LogFactory
			.getLog(NakamuraAuthenticationFilter.class);
	private static final String COOKIE_NAME = "SAKAI-TRACKING";
	private static final String ANONYMOUS = "anonymous";
	private SessionManager sessionManager;
	private UserDirectoryService userDirectoryService;
	private UsageSessionService usageSessionService;
	private EventTrackingService eventTrackingService;
	private AuthzGroupService authzGroupService;
	private SecureRandom random = new SecureRandom();

	/**
	 * Filter will be bypassed unless enabled; see sakai.properties:
	 * login.k2.authentication = true
	 */
	protected boolean filterEnabled = false;

	public static final String CONFIG_PREFIX = "org.sakaiproject.login.filter.NakamuraAuthenticationFilter";
	public static final String CONFIG_ENABLED = CONFIG_PREFIX + ".enabled";
	public static final String CONFIG_PRINCIPAL = CONFIG_PREFIX + ".principal";
	public static final String CONFIG_HOST_NAME = CONFIG_PREFIX + ".hostname";
	public static final String CONFIG_VALIDATE_URL = CONFIG_PREFIX
			+ ".validateUrl";
	public static final String CONFIG_AUTO_PROVISION_USER = CONFIG_PREFIX
			+ ".autoProvisionUser";

	/**
	 * The Nakamura RESTful service to validate authenticated users
	 */
	protected String validateUrl = "http://localhost/var/cluster/user.cookie.json?c=";

	/**
	 * The nakamura user that has permissions to GET
	 * /var/cluster/user.cookie.json.
	 */
	protected String principal = "admin";

	/**
	 * The hostname we will use to lookup the sharedSecret for access to
	 * validateUrl.
	 */
	protected String hostname = "localhost";

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("doFilter(ServletRequest " + servletRequest
					+ ", ServletResponse " + servletResponse + ", FilterChain "
					+ chain + ")");
		}
		if (filterEnabled && servletRequest instanceof HttpServletRequest) {
			final HttpServletRequest request = (HttpServletRequest) servletRequest;
			final HttpServletResponse response = (HttpServletResponse) servletResponse;

			final List<Object> list = getPrincipalLoggedIntoK2(request);
			final Principal principal = (Principal) list.get(0);
			if (principal != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Authenticated to K2 proceeding with chain: "
							+ principal.getName());
				}
				chain.doFilter(new K2HttpServletRequestWrapper(request,
						principal), servletResponse);
				return;
			} else {
				LOG.debug("NOT authenticated to K2.");
				if (!response.isCommitted()) {
					// TODO redirect to K2 login URL instead of 403
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				} else {
					// what to do here?
					throw new Error(
							"response.isCommitted() && response.sendError(HttpServletResponse.SC_FORBIDDEN)");
				}
			}
		} else { // not enabled or not HttpServletRequest - just proceed with
			// chain
			chain.doFilter(servletRequest, servletResponse);
			return;
		}
	}

	private String getSecret(HttpServletRequest req) {
		String secret = null;
		final Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (COOKIE_NAME.equals(cookie.getName())) {
					secret = cookie.getValue();
				}
			}
		}
		return secret;
	}

	private List<Object> getPrincipalLoggedIntoK2(HttpServletRequest request) {
		Principal principal = null;
		JSONObject jsonObject = null;
		final String secret = getSecret(request);
		if (secret != null) {
			DefaultHttpClient http = new DefaultHttpClient();
			try {
				URI uri = new URI(validateUrl + secret);
				HttpGet httpget = new HttpGet(uri);
				// authenticate to Nakamura using x-sakai-token mechanism
				final String token = XSakaiToken.createToken(hostname,
						this.principal);
				httpget.addHeader(XSakaiToken.X_SAKAI_TOKEN_HEADER, token);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = http.execute(httpget, responseHandler);
				jsonObject = JSONObject.fromObject(responseBody);
				String p = jsonObject.getJSONObject("user").getString(
						"principal");
				if (p != null && !"".equals(p) && !ANONYMOUS.equals(p)) {
					// only if not null and not "anonymous"
					principal = new K2Principal(p);
				}
			} catch (HttpResponseException e) {
				// usually a 404 error - could not find cookie / not valid
				if (LOG.isDebugEnabled()) {
					LOG.debug("HttpResponseException: " + e.getMessage() + ": "
							+ e.getStatusCode() + ": " + validateUrl + secret);
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			} finally {
				http.getConnectionManager().shutdown();
			}
		}

		List<Object> list = new ArrayList<Object>(2);
		list.add(0, principal);
		list.add(1, jsonObject);
		return list;
	}

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.debug("init(FilterConfig filterConfig)");
		filterEnabled = ServerConfigurationService.getBoolean(CONFIG_ENABLED,
				filterEnabled);
		if (filterEnabled) {
			LOG.info("NakamuraAuthenticationFilter ENABLED.");
			validateUrl = ServerConfigurationService.getString(
					CONFIG_VALIDATE_URL, validateUrl);
			LOG.info("vaildateUrl=" + validateUrl);
			principal = ServerConfigurationService.getString(CONFIG_PRINCIPAL,
					principal);
			LOG.info("principal=" + principal);
			hostname = ServerConfigurationService.getString(CONFIG_HOST_NAME,
					hostname);
			LOG.info("hostname=" + hostname);

			// make sure container.login is turned on as well
			boolean containerLogin = ServerConfigurationService.getBoolean(
					"container.login", false);
			if (!containerLogin) {
				throw new IllegalStateException(
						"container.login must be enabled in sakai.properties!");
			}
			// what about top.login = false ?

			sessionManager = (SessionManager) ComponentManager
					.get(SessionManager.class);
			if (sessionManager == null) {
				throw new IllegalStateException("SessionManager == null");
			}
			userDirectoryService = (UserDirectoryService) ComponentManager
					.get(UserDirectoryService.class);
			if (userDirectoryService == null) {
				throw new IllegalStateException("UserDirectoryService == null");
			}
			usageSessionService = (UsageSessionService) ComponentManager
					.get(UsageSessionService.class);
			if (usageSessionService == null) {
				throw new IllegalStateException("UsageSessionService == null");
			}
			eventTrackingService = (EventTrackingService) ComponentManager
					.get(EventTrackingService.class);
			if (eventTrackingService == null) {
				throw new IllegalStateException("EventTrackingService == null");
			}
			authzGroupService = (AuthzGroupService) ComponentManager
					.get(AuthzGroupService.class);
			if (authzGroupService == null) {
				throw new IllegalStateException("AuthzGroupService == null");
			}
		}
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// nothing to do here
	}

	public static class K2Principal implements Principal {
		private String name = null;

		public K2Principal(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof Principal) {
				return name.equals(((Principal) obj).getName());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return name;
		}

	}

	public static class K2HttpServletRequestWrapper extends
			HttpServletRequestWrapper implements HttpServletRequest {

		private final Principal principal;

		public K2HttpServletRequestWrapper(HttpServletRequest request,
				Principal principal) {
			super(request);
			this.principal = principal;
		}

		/**
		 * @see javax.servlet.http.HttpServletRequestWrapper#getRemoteUser()
		 */
		@Override
		public String getRemoteUser() {
			return principal != null ? this.principal.getName() : null;
		}

		/**
		 * @see javax.servlet.http.HttpServletRequestWrapper#getUserPrincipal()
		 */
		@Override
		public Principal getUserPrincipal() {
			return this.principal;
		}

		/**
		 * @see javax.servlet.http.HttpServletRequestWrapper#isUserInRole(java.lang.String)
		 */
		@Override
		public boolean isUserInRole(String role) {
			// not needed for this filter
			return false;
		}

	}

}
