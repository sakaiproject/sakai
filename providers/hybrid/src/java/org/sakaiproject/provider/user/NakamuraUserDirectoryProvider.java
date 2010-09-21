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

import java.net.URI;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.XSakaiToken;

/**
 *
 */
public class NakamuraUserDirectoryProvider implements UserDirectoryProvider {
	private static final Log LOG = LogFactory
			.getLog(NakamuraUserDirectoryProvider.class);
	/**
	 * Key in the ThreadLocalManager for access to the current http request
	 * object.
	 */
	public final static String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";
	private static final String COOKIE_NAME = "SAKAI-TRACKING";
	private static final String ANONYMOUS = "anonymous";
	private static final String THREAD_LOCAL_CACHE_KEY = NakamuraUserDirectoryProvider.class
			.getName()
			+ ".cache";
	public static final String CONFIG_PREFIX = "org.sakaiproject.provider.user.NakamuraUserDirectoryProvider";
	public static final String CONFIG_PRINCIPAL = CONFIG_PREFIX + ".principal";
	public static final String CONFIG_HOST_NAME = CONFIG_PREFIX + ".hostname";
	public static final String CONFIG_VALIDATE_URL = CONFIG_PREFIX
			+ ".validateUrl";

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
	 * Injected ThreadLocalManager
	 */
	private ThreadLocalManager threadLocalManager;

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#authenticateUser(java.lang.String,
	 *      org.sakaiproject.user.api.UserEdit, java.lang.String)
	 */
	public boolean authenticateUser(String eid, UserEdit edit, String password) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("authenticateUser(String " + eid
					+ ", UserEdit edit, String password)");
		}
		if (eid == null) {
			// maybe should throw exception instead?
			// since I assume I am in a chain, I will be quiet about it
			return false;
		}
		final AuthInfo authInfo = getPrincipalLoggedIntoK2(getHttpServletRequest());
		if (authInfo != null) {
			if (eid.equalsIgnoreCase(authInfo.getPrincipal())) {
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
			LOG.debug("findUserByEmail(UserEdit edit, String " + email + ")");
		}
		if (email == null) {
			return false;
		}
		final AuthInfo authInfo = getPrincipalLoggedIntoK2(getHttpServletRequest());
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
		LOG.debug("getUser(UserEdit edit)");
		if (edit == null) {
			return false;
		}
		return this.authenticateUser(edit.getEid(), edit, null);
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#getUsers(java.util.Collection)
	 */
	public void getUsers(Collection<UserEdit> users) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUsers(Collection<UserEdit> " + users + ")");
			LOG.debug("Method is not currently supported");
		}
		// nothing to do here...
		return;
	}

	private AuthInfo getPrincipalLoggedIntoK2(HttpServletRequest request) {
		LOG.debug("getPrincipalLoggedIntoK2(HttpServletRequest request)");
		final Object cache = threadLocalManager.get(THREAD_LOCAL_CACHE_KEY);
		if (cache != null && cache instanceof AuthInfo) {
			LOG.debug("cache hit!");
			return (AuthInfo) cache;
		}
		AuthInfo authInfo = null;
		final String secret = getSecret(request);
		if (secret != null) {
			final DefaultHttpClient http = new DefaultHttpClient();
			try {
				final URI uri = new URI(validateUrl + secret);
				final HttpGet httpget = new HttpGet(uri);
				// authenticate to Nakamura using x-sakai-token mechanism
				final String token = XSakaiToken.createToken(hostname,
						principal);
				httpget.addHeader(XSakaiToken.X_SAKAI_TOKEN_HEADER, token);
				//
				final ResponseHandler<String> responseHandler = new BasicResponseHandler();
				final String responseBody = http.execute(httpget,
						responseHandler);
				authInfo = new AuthInfo(responseBody);
			} catch (HttpResponseException e) {
				// usually a 404 error - could not find cookie / not valid
				if (LOG.isDebugEnabled()) {
					LOG.debug("HttpResponseException: " + e.getMessage() + ": "
							+ e.getStatusCode() + ": " + validateUrl + secret);
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new Error(e);
			} finally {
				http.getConnectionManager().shutdown();
			}
		}

		// cache results in thread local
		threadLocalManager.set(THREAD_LOCAL_CACHE_KEY, authInfo);

		return authInfo;
	}

	private String getSecret(HttpServletRequest req) {
		LOG.debug("getSecret(HttpServletRequest req)");
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

	private HttpServletRequest getHttpServletRequest() {
		LOG.debug("getHttpServletRequest()");
		final HttpServletRequest request = (HttpServletRequest) threadLocalManager
				.get(CURRENT_HTTP_REQUEST);
		if (request == null) {
			throw new IllegalStateException("HttpServletRequest == null");
		}
		return request;
	}

	public void init() {
		LOG.debug("init()");
		validateUrl = ServerConfigurationService.getString(CONFIG_VALIDATE_URL,
				validateUrl);
		principal = ServerConfigurationService.getString(CONFIG_PRINCIPAL,
				principal);
		hostname = ServerConfigurationService.getString(CONFIG_HOST_NAME,
				hostname);
	}

	/**
	 * @param threadLocalManager
	 *            the threadLocalManager to inject
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

	/**
	 * Private class for storing cached results from Nakamura lookup. Use of a
	 * private class will help prevent hijacking of the cache results.
	 */
	final static class AuthInfo {
		private static final Log AILOG = LogFactory.getLog(AuthInfo.class);

		private String principal;
		private String firstName;
		private String lastName;
		private String emailAddress;

		private AuthInfo(String json) {
			if (AILOG.isDebugEnabled()) {
				AILOG.debug("new AuthInfo(String " + json + ")");
			}
			final JSONObject user = JSONObject.fromObject(json).getJSONObject(
					"user");
			final String p = user.getString("principal");
			if (p != null && !"".equals(p) && !ANONYMOUS.equals(p)) {
				principal = p;
			}

			final JSONObject properties = user.getJSONObject("properties");
			firstName = properties.getString("firstName");
			lastName = properties.getString("lastName");
			emailAddress = properties.getString("email");
		}

		/**
		 * @return the givenName
		 */
		String getFirstName() {
			return firstName;
		}

		/**
		 * @return the familyName
		 */
		String getLastName() {
			return lastName;
		}

		/**
		 * @return the emailAddress
		 */
		String getEmailAddress() {
			return emailAddress;
		}

		/**
		 * @return the principal
		 */
		String getPrincipal() {
			return principal;
		}
	}
}
