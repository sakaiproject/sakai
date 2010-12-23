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

import java.util.Collection;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.impl.DbUserService;

/**
 * Some institutions want to have an authentication-only service that authenticates
 * login IDs for local Sakai-maintained user records. (In other words, they do not
 * want to provide user data externally; they only want control over authentication.)
 * 
 * For 2.5.* development, the best way to do this is with the AuthenticatedUserProvider
 * interface. This test code confirms backwards compatibility for legacy UserDirectoryProvider
 * implementations which build this capability on the legacy 
 * authenticateUser(loginId, userEdit, password) method instead.
 * 
 */
public class RequireLocalAccountLegacyAuthenticationTest extends SakaiKernelTestBase {
	private static Log log = LogFactory.getLog(RequireLocalAccountLegacyAuthenticationTest.class);
	
	private UserDirectoryService userDirectoryService;
	private static TestProvider userDirectoryProvider;

	// This service is only used to clear out various caches to make sure
	// we're fetching from the DB.
	private ThreadLocalManager threadLocalManager;
	
	private static String LOCALLY_STORED_EID = "locallystoreduser";
	private static String LOCALLY_STORED_PWD = "locallystoreduser-pwd";
	private static String LOCALLY_STORED_EMAIL = "locallystoreduser@somewhere.edu";
	private static String PROVIDED_EID = "provideduser";
	private static String PROVIDED_PWD = "provideduser-pwd";
	private static String PROVIDED_EMAIL = "provideduser@somewhere.edu";
	
	/**
	 * A complete integration test run is a lot of overhead to take on for
	 * such a small suite of tests. But since the tests rely on being set up with
	 * specially tailored providers, there's not much choice....
	 * 
	 * @throws Exception
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(RequireLocalAccountLegacyAuthenticationTest.class)) {
			protected void setUp() throws Exception {
				if (log.isDebugEnabled()) log.debug("starting setup");
				try {
					oneTimeSetup("disable_user_cache");
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

		User user = dbUserService.addUser(null, LOCALLY_STORED_EID, "J. " + LOCALLY_STORED_EID, "de " + LOCALLY_STORED_EID, 
				LOCALLY_STORED_EMAIL, null, "Guest", null);
		log.debug("addUser eid=" + LOCALLY_STORED_EID + ", id=" + user.getId());
}

	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		threadLocalManager = (ThreadLocalManager)getService(ThreadLocalManager.class.getName());
	}

	public void testWithProvidedUserNotAllowed() throws Exception {
		userDirectoryProvider.setRequireLocalAccount(true);
		
		User user = userDirectoryService.authenticate(LOCALLY_STORED_EID, LOCALLY_STORED_PWD);
		Assert.assertTrue(user.getEmail().equals(LOCALLY_STORED_EMAIL));
		clearUserFromServiceCaches(user.getId());
		user = userDirectoryService.authenticate(PROVIDED_EID, PROVIDED_PWD);
		Assert.assertTrue(user == null);
	}
	
	public void testWithProvidedUserAllowed() throws Exception {
		userDirectoryProvider.setRequireLocalAccount(false);
		
		User user = userDirectoryService.authenticate(LOCALLY_STORED_EID, LOCALLY_STORED_PWD);
		Assert.assertTrue(user.getEmail().equals(LOCALLY_STORED_EMAIL));
		clearUserFromServiceCaches(user.getId());
		user = userDirectoryService.authenticate(PROVIDED_EID, PROVIDED_PWD);
		Assert.assertTrue(user.getEmail().equals(PROVIDED_EMAIL));
		clearUserFromServiceCaches(user.getId());
	}

	/**
	 * WARNING: There seems to be NO easy way to reset the UserDirectoryService MemoryService-managed
	 * cache, and so the only way currently to test for real DB storage is to have this line
	 * in the "sakai.properties" file used for the test:
	 *   cacheMinutes@org.sakaiproject.user.api.UserDirectoryService=0
	 */
	private void clearUserFromServiceCaches(String userId) {
		((DbUserService)userDirectoryService).getIdEidCache().removeAll();
		String ref = "/user/" + userId;
		threadLocalManager.set(ref, null);
	}

	public static class TestProvider implements UserDirectoryProvider {
		private boolean requireLocalAccount = false;

		public boolean authenticateUser(String eid, UserEdit user, String password) {
			return (password.equals(eid + "-pwd"));
		}

		public boolean authenticateWithProviderFirst(String loginId) {
			return false;
		}

		public boolean findUserByEmail(UserEdit edit, String email) {
			return false;
		}

		public boolean getUser(UserEdit user) {
			if (requireLocalAccount) {
				return false;
			} else {
				if (user.getEid().equals(PROVIDED_EID)) {
					user.setEmail(PROVIDED_EMAIL);
					return true;
				} else {
					return false;
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void getUsers(Collection users) {
		}

		public void setRequireLocalAccount(boolean requireLocalAccount) {
			this.requireLocalAccount = requireLocalAccount;
		}
	}

}
