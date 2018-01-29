/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.user.impl.test;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.impl.AuthenticationCache;
import org.sakaiproject.util.IdPwEvidence;

@Slf4j
public class AuthenticationCacheTest extends SakaiKernelTestBase {
	private static String[] USER_DATA_1 = {"localonly1user", null, "First", "Last1", "local1@edu", "local1password"};
	private static IdPwEvidence USER_EVIDENCE_1 = new IdPwEvidence(USER_DATA_1[0], USER_DATA_1[5], null);
	private static String[] USER_DATA_2 = {"localonly2user", null, "First", "Last2", "local2@edu", "local2password"};
	private AuthenticationManager authenticationManager;
	private AuthenticationCache authenticationCache;
	private UserDirectoryService userDirectoryService;

	
	@BeforeClass
	public static void beforeClass() {
		try {
			log.debug("starting oneTimeSetup");
			oneTimeSetup("AuthenticationCacheTest");
			log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Before
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

	@Test
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
		IdPwEvidence badEvidence = new IdPwEvidence(USER_DATA_1[0], "Not the password", null);
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
        /* removed test that was testing if caching works */
	}

}
