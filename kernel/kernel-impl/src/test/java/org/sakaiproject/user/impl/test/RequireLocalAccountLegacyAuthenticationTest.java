/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.event.api.EventTrackingService;
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
@Slf4j
public class RequireLocalAccountLegacyAuthenticationTest extends SakaiKernelTestBase {
	private static TestProvider userDirectoryProvider;
	private static String LOCALLY_STORED_EID = "locallystoreduser";
	private static String LOCALLY_STORED_PWD = "locallystoreduser-pwd";
	private static String LOCALLY_STORED_EMAIL = "locallystoreduser@somewhere.edu";
	private static String PROVIDED_EID = "provideduser";
	private static String PROVIDED_PWD = "provideduser-pwd";
	private static String PROVIDED_EMAIL = "provideduser@somewhere.edu";
	private UserDirectoryService userDirectoryService;
	// This service is only used to clear out various caches to make sure
	// we're fetching from the DB.
	private ThreadLocalManager threadLocalManager;
	private EventTrackingService eventTrackingService;
	
	/**
	 * A complete integration test run is a lot of overhead to take on for
	 * such a small suite of tests. But since the tests rely on being set up with
	 * specially tailored providers, there's not much choice....
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() {
		try {
			if (log.isDebugEnabled()) log.debug("starting setup");
			oneTimeSetup("disable_user_cache");
			oneTimeSetupAfter();
			if (log.isDebugEnabled()) log.debug("finished setup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
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

	@Before
	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		threadLocalManager = (ThreadLocalManager)getService(ThreadLocalManager.class.getName());
		eventTrackingService = (EventTrackingService)getService(EventTrackingService.class);
	}

	@Test
	public void testWithProvidedUserRequired() throws Exception {
		userDirectoryProvider.setRequireLocalAccount(true);
		
		User user = userDirectoryService.authenticate(LOCALLY_STORED_EID, LOCALLY_STORED_PWD);
		Assert.assertTrue(user.getEmail().equals(LOCALLY_STORED_EMAIL));
		clearUserFromServiceCaches(user.getId());
		user = userDirectoryService.authenticate(PROVIDED_EID, PROVIDED_PWD);
		Assert.assertTrue(user == null);
	}
	
	@Test
	public void testWithProvidedUserGood() throws Exception {
		userDirectoryProvider.setRequireLocalAccount(false);
		
		User user = userDirectoryService.authenticate(LOCALLY_STORED_EID, LOCALLY_STORED_PWD);
		Assert.assertTrue(user.getEmail().equals(LOCALLY_STORED_EMAIL));
		clearUserFromServiceCaches(user.getId());
		user = userDirectoryService.authenticate(PROVIDED_EID, PROVIDED_PWD);
		Assert.assertTrue(user.getEmail().equals(PROVIDED_EMAIL));
		clearUserFromServiceCaches(user.getId());
	}

	private void clearUserFromServiceCaches(String userId) throws SecurityException {
		((DbUserService)userDirectoryService).getIdEidCache().clear();
		String ref = "/user/" + userId;
		threadLocalManager.set(ref, null);
		// Clear all caches, as it's a test its easier todo this than
		// set up thread to be a super user which is needed for the MemoryService.resetCachers()
		eventTrackingService.post(eventTrackingService.newEvent("memory.reset", "", true));
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

		@SuppressWarnings("rawtypes")
		public void getUsers(Collection users) {
		}

		public void setRequireLocalAccount(boolean requireLocalAccount) {
			this.requireLocalAccount = requireLocalAccount;
		}
	}

}
