/**
 * $Id$
 * $URL$
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

package org.sakaiproject.user.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;

/**
 * Because DAV clients do not understand the concept of secure sessions, a DAV
 * user will end up asking Sakai to re-authenticate them for every action.
 * To ease the overhead, this class checks a size-limited timing-out cache
 * of one-way encrypted successful authentication IDs and passwords.
 * <p>
 * There's nothing DAV-specific about this class, and it's also independent of
 * any Sakai classes other than the "Authentication" user ID and EID holder.
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class AuthenticationCache {
	private static final Log log = LogFactory.getLog(AuthenticationCache.class);

	private Cache authCache = null;

	/**
	* The central cache object, should be injected
	*/
	public void setAuthCache(Cache authCache) {
		this.authCache = authCache;
		if (log.isDebugEnabled() && (authCache != null)) log.debug("authCache timeToLiveSeconds=" + authCache.getTimeToLiveSeconds() + ", timeToIdleSeconds=" + authCache.getTimeToIdleSeconds());
	}

	public Authentication getAuthentication(String authenticationId, String password)
			throws AuthenticationException {
		Authentication auth = null;
		try {
			AuthenticationRecord record = (AuthenticationRecord) authCache.get(authenticationId).getObjectValue();
			if (MessageDigest.isEqual(record.encodedPassword, getEncrypted(password))) {
				if (record.authentication == null) {
					if (log.isDebugEnabled()) log.debug("getAuthentication: replaying authentication failure for authenticationId=" + authenticationId);
					throw new AuthenticationException("repeated invalid login");
				} else {
					if (log.isDebugEnabled()) log.debug("getAuthentication: returning record for authenticationId=" + authenticationId);
					auth = record.authentication;
				}
			} else {
				// Since the passwords didn't match, we're no longer getting repeats,
				// and so the record should be removed.
				if (log.isDebugEnabled()) log.debug("getAuthentication: record for authenticationId=" + authenticationId + " failed password check");
				authCache.remove(authenticationId);
			}
		} catch (NullPointerException e) {
			// this is ok and generally expected to indicate the value is not in the cache
			auth = null;
		}
		return auth;
	}

	public void putAuthentication(String authenticationId, String password, Authentication authentication) {
		putAuthenticationRecord(authenticationId, password, authentication);
	}

	public void putAuthenticationFailure(String authenticationId, String password) {
		putAuthenticationRecord(authenticationId, password, null);
	}

	protected void putAuthenticationRecord(String authenticationId, String password,
			Authentication authentication) {
		if (authCache.isKeyInCache(authenticationId)) {
			// Don't indefinitely renew the cached record -- we want to force
			// real authentication after the timeout.
		} else {
			authCache.put( new Element(authenticationId,
					new AuthenticationRecord(getEncrypted(password), authentication, System.currentTimeMillis()) ) );
		}
	}

	private byte[] getEncrypted(String plaintext) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA");
			messageDigest.update(plaintext.getBytes("UTF-8"));
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			// This seems highly unlikely.
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @deprecated No longer used. Use standard cache settings instead.
	 * @param maximumSize maximum capacity of the cache before replacing older records
	 */
	public void setMaximumSize(int maximumSize) {
		if (log.isWarnEnabled()) log.warn("maximumSize property set but no longer used; should switch to maxElementsInMemory property instead");
	}

	/**
	 * @deprecated No longer used. Use standard cache settings instead.
	 * @param timeoutMs timeout of a cached authentication in milliseconds
	 */
	public void setTimeoutMs(int timeoutMs) {
		if (log.isWarnEnabled()) log.warn("timeoutMs property set but no longer used; should switch to timeToLive seconds property instead");
	}

	/**
	 * @deprecated No longer used. Use standard cache settings instead.
	 * @param failureThrottleTimeoutMs timeout of a cached failed ID and
	 * password combination in milliseconds; used to prevent DAV clients
	 * with out-of-date passwords from swamping authentication services.
	 */
	public void setFailureThrottleTimeoutMs(int failureThrottleTimeoutMs) {
		if (log.isWarnEnabled()) log.warn("failureThrottleTimeoutMs property set but no longer used; should switch to timeToLive seconds property instead");
	}

	static class AuthenticationRecord {
		byte[] encodedPassword;
		Authentication authentication;	// Null for failed authentication
		long createTimeInMs;

		public AuthenticationRecord(byte[] encodedPassword, Authentication authentication, long createTimeInMs) {
			this.encodedPassword = encodedPassword;
			this.authentication = authentication;
			this.createTimeInMs = createTimeInMs;
		}
	}

}
