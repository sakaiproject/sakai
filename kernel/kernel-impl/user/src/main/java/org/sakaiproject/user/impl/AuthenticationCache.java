/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2007 The Regents of the University of California
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

package org.sakaiproject.user.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
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
 * The basic idea comes from the KerberosUserDirectoryProvider, although
 * much of the code is new.
 * <p>
 * There's nothing DAV-specific about this class, and it's also independent of
 * any Sakai classes other than the "Authentication" user ID and EID holder.
 * <p>
 * Three Sakai properties control the cache's behavior:
 * <ul>
 * <li>"maximumSize@org.sakaiproject.user.impl.AuthenticationCache" is the
 * maximum capacity of the cache; a value of 0 disables it.
 * <li>"timeoutMs@org.sakaiproject.user.impl.AuthenticationCache" is the
 * timeout of a cached record in milliseconds. This is NOT an idle
 * timeout, because we want to reduce the risk of OK-ing an out-of-date
 * password.
 * <li>"failureThrottleTimeoutMs@org.sakaiproject.user.impl.AuthenticationCache" 
 * is used to throttle repeated failures with the same ID and password
 * combination. An out-of-date password in a DAV client can result
 * in many consecutive failures in a short period of time, which in turn
 * can trigger bogus security alerts or account lockdowns. If this
 * timeout is greater than 0, then a match on a previous login failure will
 * throw an immediate authentication exception without pestering the provider service.
 * </ul>
 */
public class AuthenticationCache {
	private static final Log log = LogFactory.getLog(AuthenticationCache.class);
	private Map<String, AuthenticationRecord> lruMap;
	private int maximumSize;
	private int timeoutMs;
	private int failureThrottleTimeoutMs;

	/**
	 * Allows easy use in a non-Spring-configured module.
	 */
	public AuthenticationCache(int maximumSize) {
		setMaximumSize(maximumSize);
		init();
	}
	public AuthenticationCache() {
	}

	@SuppressWarnings("unchecked")
	public void init() {
		if (maximumSize > 0) {
			lruMap = Collections.synchronizedMap(new LRUMap(maximumSize));
		}
	}

	/**
	 * @param authenticationId
	 * @param password
	 * @return cached authentication record if successfully fetched; null otherwise
	 * @throws AuthenticationException if authentication failures are being throttled
	 * and this is a repeat ID and password combination
	 */
	public Authentication getAuthentication(String authenticationId, String password) throws AuthenticationException {
		if (!isEnabled()) {
			return null;
		}

		AuthenticationRecord record = lruMap.get(authenticationId);
		if (record != null) {
			boolean isSuccessRecord = (record.authentication != null);
			// Check for timeouts first.
			if ((isSuccessRecord && (System.currentTimeMillis() - record.createTimeInMs) > timeoutMs) ||
					(!isSuccessRecord && (System.currentTimeMillis() - record.createTimeInMs) > failureThrottleTimeoutMs)) {
				if (log.isDebugEnabled()) log.debug("getAuthentication: record for authenticationId=" + authenticationId + " timed out");
				lruMap.remove(authenticationId);
			} else {
				if (MessageDigest.isEqual(record.encodedPassword, getEncrypted(password))) {
					if (isSuccessRecord) {
						if (log.isDebugEnabled()) log.debug("getAuthentication: returning record for authenticationId=" + authenticationId);
						return record.authentication;						
					} else {
						if (log.isDebugEnabled()) log.debug("getAuthentication: replaying authentication failure for authenticationId=" + authenticationId);
						throw new AuthenticationException("repeated invalid login");
					}
				} else {
					// Since the passwords didn't match, we're no longer getting repeats,
					// and so the record should be removed.
					if (log.isDebugEnabled()) log.debug("getAuthentication: record for authenticationId=" + authenticationId + " failed password check");
					lruMap.remove(authenticationId);
				}
			}
		}
		return null;
	}

	public void putAuthentication(String authenticationId, String password, Authentication authentication) {
		if (!isEnabled()) {
			return;
		}
		putAuthenticationRecord(authenticationId, password, authentication);		
	}

	public void putAuthenticationFailure(String authenticationId, String password) {
		if (!isEnabled() || !isFailureThrottleEnabled()) {
			return;
		}
		putAuthenticationRecord(authenticationId, password, null);
	}
	
	private void putAuthenticationRecord(String authenticationId, String password, Authentication authentication) {
		// Don't indefinitely renew the cached record -- we want to force
		// real authentication after the timeout.
		if (lruMap.get(authenticationId) != null) {
			return;
		}

		if (log.isDebugEnabled()) {
			if (authentication != null) 
				log.debug("putAuthenticationRecord for user id=" + authentication.getUid() + ", eid=" + authentication.getEid());
			else
				log.debug("putAuthenticationRecord for failed attempt");
		}
		lruMap.put(authenticationId, new AuthenticationRecord(getEncrypted(password), authentication, System.currentTimeMillis()));		
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
	
	private boolean isEnabled() {
		return (maximumSize > 0);
	}
	
	private boolean isFailureThrottleEnabled() {
		return (failureThrottleTimeoutMs > 0);
	}

	/**
	 * @param maximumSize maximum capacity of the cache before replacing older records
	 */
	public void setMaximumSize(int maximumSize) {
		this.maximumSize = maximumSize;
	}

	/**
	 * @param timeoutMs timeout of a cached authentication in milliseconds
	 */
	public void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	/**
	 * @param failureThrottleTimeoutMs timeout of a cached failed ID and
	 * password combination in milliseconds; used to prevent DAV clients
	 * with out-of-date passwords from swamping authentication services;
	 * 0 to disable
	 */
	public void setFailureThrottleTimeoutMs(int failureThrottleTimeoutMs) {
		this.failureThrottleTimeoutMs = failureThrottleTimeoutMs;
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
