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

import java.util.Collection;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.user.api.AuthenticatedUserProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;

/**
 *
 */
public class AuthenticatedUserProviderTest extends SakaiTestBase {
	private static Log log = LogFactory.getLog(AuthenticatedUserProviderTest.class);
	
	private UserDirectoryService userDirectoryService;
	private static TestProvider userDirectoryProvider;
	
	/**
	 * A complete integration test run is a lot of overhead to take on for
	 * such a small suite of tests. But since the tests rely on being set up with
	 * specially tailored providers, there's not much choice....
	 * 
	 * @throws Exception
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(AuthenticatedUserProviderTest.class)) {
			protected void setUp() throws Exception {
				if (log.isDebugEnabled()) log.debug("starting setup");
				try {
					oneTimeSetup();
					oneTimeSetupAfter();
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
	
	private static void oneTimeSetupAfter() throws Exception {
		userDirectoryProvider = new TestProvider();
		
		// This is a workaround until we can make it easier to load sakai.properties
		// for specific integration tests.
		DbUserService dbUserService = (DbUserService)getService(UserDirectoryService.class.getName());
		dbUserService.setProvider(userDirectoryProvider);
		
		// Inject service so that provider can create new user records or search
		// for existing ones.
		userDirectoryProvider.setUserFactory(dbUserService);
	}

	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
	}
	
	public void testLocalUserFallThrough() throws Exception {
		User user = userDirectoryService.addUser(null, "local", null, null, null, "localPW", null, null);
		User authUser = userDirectoryService.authenticate("local", "localPW");
		Assert.assertTrue(authUser.getId().equals(user.getId()));
	}
	
	public void testExistingProvidedUser() throws Exception {
		User user = userDirectoryService.getUserByEid("provideduser");
		Assert.assertTrue(user != null);
		User authUser = userDirectoryService.authenticate("LOGINprovideduser", "provideduserPW");
		Assert.assertTrue(authUser.getId().equals(user.getId()));
		User failedUser = userDirectoryService.authenticate("LOGINprovideduser", "BadPassword");
		Assert.assertTrue(failedUser == null);
	}
	
	public void testNewProvidedUsers() throws Exception {
		User providedByAuthn = userDirectoryService.authenticate("LOGINprovidedauthn", "providedauthnPW");
		Assert.assertTrue(providedByAuthn != null);
		User providedByDelayedAuthn = userDirectoryService.authenticate("LOGINprovidernotfirst", "providernotfirstPW");
		Assert.assertTrue(providedByDelayedAuthn != null);
		User user = userDirectoryService.getUserByEid("providedauthn");
		Assert.assertTrue(user.getId().equals(providedByAuthn.getId()));
		user = userDirectoryService.getUserByEid("providernotfirst");
		Assert.assertTrue(user.getId().equals(providedByDelayedAuthn.getId()));
	}
	
	public static class TestProvider implements UserDirectoryProvider, AuthenticatedUserProvider {
		private UserFactory userFactory;

		public boolean authenticateUser(String eid, UserEdit user, String password) {
			// This should never be called since we implement the new interface.
			throw new RuntimeException("authenticateUser unexpectedly called");
		}

		public boolean authenticateWithProviderFirst(String loginId) {
			return !loginId.equals("LOGINprovidernotfirst");
		}

		public boolean findUserByEmail(UserEdit edit, String email) {
			return false;
		}

		public boolean getUser(UserEdit user) {
			String eid = user.getEid();
			return (eid.startsWith("provide"));
		}

		public void getUsers(Collection users) {
		}

		public UserEdit getAuthenticatedUser(String loginId, String password) {
			log.debug("getAuthenticatedUser " + loginId + ", " + password);
			if (!loginId.startsWith("LOGINprovide")) return null;
			String eid = loginId.substring("LOGIN".length());
			if (password.equals(eid + "PW")) {
				UserEdit user = userFactory.newUser();
				user.setEid(eid);
				return user;
			} else {
				return null;
			}
		}

		public void setUserFactory(UserFactory userFactory) {
			this.userFactory = userFactory;
		}
	}

}
