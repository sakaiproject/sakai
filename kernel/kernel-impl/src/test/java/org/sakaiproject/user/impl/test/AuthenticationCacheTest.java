/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
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
*
**********************************************************************************/

package org.sakaiproject.user.impl.test;


import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.impl.AuthenticationCache;
import org.sakaiproject.util.IdPwEvidence;

/**
 *
 */
public class AuthenticationCacheTest extends SakaiKernelTestBase {
	private static Log log = LogFactory.getLog(AuthenticationCacheTest.class);
	private static String[] USER_DATA_1 = {"localonly1user", null, "First", "Last1", "local1@edu", "local1password"};
	private static String[] USER_DATA_2 = {"localonly2user", null, "First", "Last2", "local2@edu", "local2password"};
	private static IdPwEvidence USER_EVIDENCE_1 = new IdPwEvidence(USER_DATA_1[0], USER_DATA_1[5]);

	private AuthenticationManager authenticationManager;
	private AuthenticationCache authenticationCache;
	private UserDirectoryService userDirectoryService;

	
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(AuthenticationCacheTest.class)) {
			protected void setUp() throws Exception {
				if (log.isDebugEnabled()) log.debug("starting setup");
				try {
					oneTimeSetup("AuthenticationCacheTest");
				} catch (Exception e) {
					log.warn(e);
				}
				if (log.isDebugEnabled()) log.debug("finished setup");
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void setUp() throws Exception {
		if (log.isDebugEnabled()) log.debug("Setting up AuthenticationCacheTest");
		authenticationCache = (AuthenticationCache)getService(AuthenticationCache.class.getName());
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		authenticationManager = (AuthenticationManager)getService(AuthenticationManager.class.getName());

		User localUser = userDirectoryService.addUser(USER_DATA_1[1], USER_DATA_1[0],
				USER_DATA_1[2], USER_DATA_1[3], USER_DATA_1[4], USER_DATA_1[5], null, null);
		if (log.isDebugEnabled()) log.debug("Created local user eid=" + localUser.getEid() + ", id=" + localUser.getId());
		USER_DATA_1[1] = localUser.getId();

		localUser = userDirectoryService.addUser(USER_DATA_2[1], USER_DATA_2[0],
				USER_DATA_2[2], USER_DATA_2[3], USER_DATA_2[4], USER_DATA_2[5], null, null);
		if (log.isDebugEnabled()) log.debug("Created local user eid=" + localUser.getEid() + ", id=" + localUser.getId());
		USER_DATA_2[1] = localUser.getId();
	}

	/**
	 * Because a lot of what we have to test in the legacy user provider service involves
	 * irreversible side-effects (such as use of in-memory cache), we can't put much
	 * trust in the "tearDown" approach. Instead, we rely on the "one long
	 * complex test method" approach.
	 */
	public void tearDown() throws Exception {
	}

	public void testAuthenticationCache() throws Exception {
		Assert.assertTrue(authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword()) == null);
		Authentication authentication = authenticationManager.authenticate(USER_EVIDENCE_1);
		if (log.isDebugEnabled()) log.debug("Initial authentication eid=" + authentication.getEid());
		Assert.assertTrue(authentication.getEid().equals(USER_DATA_1[0]));

		// Is the authentication in the cache now?
		authentication = authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword());
		Assert.assertTrue(authentication.getEid().equals(USER_DATA_1[0]));

		// Make sure it gets bumped from the cache when the password is
		// wrong.
		authentication = authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), "WrongPassword");
		Assert.assertTrue(authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword()) == null);
		authentication = authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword());
		Assert.assertTrue(authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword()) == null);
		authenticationManager.authenticate(USER_EVIDENCE_1);
		authentication = authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword());
		Assert.assertTrue(authentication.getEid().equals(USER_DATA_1[0]));

		// Test authentication failure throttle.
		IdPwEvidence badEvidence = new IdPwEvidence(USER_DATA_1[0], "Not the password");
		try {
			authenticationManager.authenticate(badEvidence);
			Assert.fail();
		} catch (AuthenticationException e) {
		}
		try {
			authentication = authenticationCache.getAuthentication(badEvidence.getIdentifier(), badEvidence.getPassword());
			Assert.fail();
		} catch (AuthenticationException e) {
		}

		// Test timeout after 5 seconds.
		int nbrReads = 0;
		long startTime = System.currentTimeMillis();
		authentication = authenticationManager.authenticate(USER_EVIDENCE_1);
		while (nbrReads < 50) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			// See if the record is still in the cache, tickling the idle timeout
			// (if any).
			if (authenticationCache.getAuthentication(USER_EVIDENCE_1.getIdentifier(), USER_EVIDENCE_1.getPassword()) == null) {
				if (log.isDebugEnabled()) log.debug("cache timed out at " + (System.currentTimeMillis()- startTime) + " ms");
				break;
			}
			nbrReads++;
		}
		if (log.isDebugEnabled()) log.debug("Checked cache successfully " + nbrReads + " times before timing out or giving up");
		Assert.assertTrue(nbrReads < 10);
	}

}
