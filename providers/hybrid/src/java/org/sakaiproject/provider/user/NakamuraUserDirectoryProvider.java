/**
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.provider.user;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hybrid.util.NakamuraAuthenticationHelper;
import org.sakaiproject.hybrid.util.NakamuraAuthenticationHelper.AuthInfo;
import org.sakaiproject.hybrid.util.XSakaiToken;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;

/**
 * Authenticates users who have already been authenticated to a Nakamura
 * instance.
 */
public class NakamuraUserDirectoryProvider implements UserDirectoryProvider {
	private static final Log LOG = LogFactory
			.getLog(NakamuraUserDirectoryProvider.class);
	/**
	 * Key in the ThreadLocalManager for access to the current
	 * {@link HttpServletRequest} object.
	 */
	static final String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";
	/**
	 * All sakai.properties settings will be prefixed with this string.
	 */
	public static final String CONFIG_PREFIX = "org.sakaiproject.provider.user.NakamuraUserDirectoryProvider";
	/**
	 * The principal that will be used when connecting to Nakamura REST
	 * end-point. Must have permissions to read /var/cluster/user.cookie.json.
	 * 
	 * @see XSakaiToken#createToken(String, String)
	 */
	public static final String CONFIG_PRINCIPAL = CONFIG_PREFIX + ".principal";
	/**
	 * The hostname we will use to lookup the sharedSecret for access to
	 * validateUrl.
	 * 
	 * @see XSakaiToken#createToken(String, String)
	 */
	public static final String CONFIG_HOST_NAME = CONFIG_PREFIX + ".hostname";
	/**
	 * The Nakamura REST end-point we will use to validate the cookie
	 */
	public static final String CONFIG_VALIDATE_URL = CONFIG_PREFIX
			+ ".validateUrl";

	/**
	 * The Nakamura RESTful service to validate authenticated users. A good
	 * default for common hybrid implementations is supplied.
	 * 
	 * @see #CONFIG_VALIDATE_URL
	 */
	protected String validateUrl = "http://localhost/var/cluster/user.cookie.json?c=";

	/**
	 * The nakamura user that has permissions to GET
	 * /var/cluster/user.cookie.json. A good default for common hybrid
	 * implementations is supplied.
	 * 
	 * @see XSakaiToken#createToken(String, String)
	 * @see #CONFIG_PRINCIPAL
	 */
	protected String principal = "admin";

	/**
	 * The hostname we will use to lookup the sharedSecret for access to
	 * validateUrl. A good default for common hybrid implementations is
	 * supplied.
	 * 
	 * @see XSakaiToken#createToken(String, String)
	 * @see #CONFIG_HOST_NAME
	 */
	protected String hostname = "localhost";

	// dependencies
	ComponentManager componentManager; // injected
	ThreadLocalManager threadLocalManager; // injected
	ServerConfigurationService serverConfigurationService; // injected
	NakamuraAuthenticationHelper nakamuraAuthenticationHelper;

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#authenticateUser(java.lang.String,
	 *      org.sakaiproject.user.api.UserEdit, java.lang.String)
	 */
	public boolean authenticateUser(String eid, UserEdit edit, String password) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("authenticateUser(String " + eid + ", UserEdit " + edit
					+ ", String password)");
		}
		if (eid == null || "null".equalsIgnoreCase(eid) || "".equals(eid)) {
			// maybe should throw exception instead?
			// since I assume I am in a chain, I will be quiet about it
			LOG.debug("eid == null");
			return false;
		}
		final AuthInfo authInfo = nakamuraAuthenticationHelper
				.getPrincipalLoggedIntoNakamura(getHttpServletRequest());
		if (authInfo != null) {
			if (eid.equalsIgnoreCase(authInfo.getPrincipal())) {
				edit.setEid(authInfo.getPrincipal());
				edit.setFirstName(authInfo.getFirstName());
				edit.setLastName(authInfo.getLastName());
				edit.setEmail(authInfo.getEmailAddress());
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#authenticateWithProviderFirst(java.lang.String)
	 */
	public boolean authenticateWithProviderFirst(String eid) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("authenticateWithProviderFirst(String " + eid + ")");
		}
		// What is the best default?
		return false;
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#findUserByEmail(org.sakaiproject.user.api.UserEdit,
	 *      java.lang.String)
	 */
	public boolean findUserByEmail(UserEdit edit, String email) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("findUserByEmail(UserEdit " + edit + ", String " + email
					+ ")");
		}
		if (email == null) {
			LOG.debug("String email == null");
			return false;
		}
		final AuthInfo authInfo = nakamuraAuthenticationHelper
				.getPrincipalLoggedIntoNakamura(getHttpServletRequest());
		if (authInfo != null) {
			if (email.equalsIgnoreCase(authInfo.getEmailAddress())) {
				edit.setEid(authInfo.getPrincipal());
				edit.setFirstName(authInfo.getFirstName());
				edit.setLastName(authInfo.getLastName());
				edit.setEmail(authInfo.getEmailAddress());
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#getUser(org.sakaiproject.user.api.UserEdit)
	 */
	public boolean getUser(UserEdit edit) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUser(UserEdit " + edit + ")");
		}
		/*
		 * For some crazy reason, the not-null String "null" can be passed in
		 * edit.getEid(). Very odd behavior indeed.
		 */
		if (edit != null) {
			final String eid = edit.getEid();
			if (eid != null && !"null".equalsIgnoreCase(eid)) {
				return authenticateUser(edit.getEid(), edit, null);
			}
		}
		LOG.debug("UserEdit edit == null || null eid");
		return false;
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#getUsers(java.util.Collection)
	 */
	public void getUsers(Collection<UserEdit> users) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUsers(Collection<UserEdit> " + users + ")");
		}
		LOG.warn("Method is not currently supported");
		// nothing to do here...
		return;
	}

	/**
	 * Gets a reference to {@link HttpServletRequest} via ThreadLocalManager.
	 * 
	 * @return
	 */
	protected HttpServletRequest getHttpServletRequest() {
		LOG.debug("getHttpServletRequest()");
		final HttpServletRequest request = (HttpServletRequest) threadLocalManager
				.get(CURRENT_HTTP_REQUEST);
		if (request == null) {
			throw new IllegalStateException("HttpServletRequest == null");
		}
		return request;
	}

	/**
	 * Initialize class.
	 */
	public void init() {
		LOG.debug("init()");
		if (componentManager == null) { // may be in a test
			componentManager = org.sakaiproject.component.cover.ComponentManager
					.getInstance();
		}
		validateUrl = serverConfigurationService.getString(CONFIG_VALIDATE_URL,
				validateUrl);
		principal = serverConfigurationService.getString(CONFIG_PRINCIPAL,
				principal);
		hostname = serverConfigurationService.getString(CONFIG_HOST_NAME,
				hostname);
		if (nakamuraAuthenticationHelper == null) {
			nakamuraAuthenticationHelper = new NakamuraAuthenticationHelper(
					componentManager, validateUrl, principal, hostname);
		}
	}

	/**
	 * @param threadLocalManager
	 *            the threadLocalManager to inject
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		LOG.debug("setThreadLocalManager(ThreadLocalManager threadLocalManager)");
		this.threadLocalManager = threadLocalManager;
	}

	/**
	 * @param componentManager
	 *            the componentManager to set
	 */
	public void setComponentManager(ComponentManager componentManager) {
		LOG.debug("setComponentManager(ComponentManager componentManager)");
		this.componentManager = componentManager;
	}

	/**
	 * @param serverConfigurationService
	 *            the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		LOG.debug("setServerConfigurationService(ServerConfigurationService serverConfigurationService)");
		this.serverConfigurationService = serverConfigurationService;
	}
}
