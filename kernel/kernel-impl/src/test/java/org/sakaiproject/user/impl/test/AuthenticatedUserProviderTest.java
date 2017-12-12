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

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.AuthenticatedUserProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.impl.DbUserService;

/**
 *
 */
@Slf4j
public class AuthenticatedUserProviderTest extends SakaiKernelTestBase {
	protected static final String CONFIG = null;

	private static TestProvider userDirectoryProvider;
	private UserDirectoryService userDirectoryService;
	// These services are only used to clear out various caches to make sure
	// we're fetching from the DB.
	private ThreadLocalManager threadLocalManager;

	
	@BeforeClass
	public static void beforeClass() {
		try {
			log.debug("starting oneTimeSetup");
			oneTimeSetup("disable_user_cache");
			oneTimeSetupAfter();
			log.debug("finished oneTimeSetup");
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
		
		// Inject service so that provider can create new user records or search
		// for existing ones.
		userDirectoryProvider.setUserFactory(dbUserService);
		userDirectoryProvider.setUserDirectoryService(dbUserService);
		userDirectoryProvider.setSecurityService((SecurityService)getService(SecurityService.class.getName()));
	}

	@Before
	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		threadLocalManager = (ThreadLocalManager)getService(ThreadLocalManager.class.getName());
	}
	
	@Test
	public void testLocalUserFallThrough() throws Exception {
		User user = userDirectoryService.addUser(null, "local", null, null, null, "localPW", null, null);
		User authUser = userDirectoryService.authenticate("local", "localPW");
		Assert.assertTrue(authUser.getId().equals(user.getId()));
	}
	
	@Test
	public void testExistingProvidedUser() throws Exception {
		User user = userDirectoryService.getUserByEid("provideduser");
		Assert.assertTrue(user != null);
		User authUser = userDirectoryService.authenticate("LOGINprovideduser", "provideduserPW");
		Assert.assertTrue(authUser.getId().equals(user.getId()));
		User failedUser = userDirectoryService.authenticate("LOGINprovideduser", "BadPassword");
		Assert.assertTrue(failedUser == null);
	}
	
	@Test
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
	
	@Test
	public void testProviderCreatedAndUpdatedLocalUsers() throws Exception {
		// Authenticate a user who will then magically appear in the Sakai user tables.
		User providedByAuthn = userDirectoryService.authenticate("LOGINprovidercreated", "providercreatedPW");
		String eid = providedByAuthn.getEid();
		Assert.assertTrue(eid.equals("providercreated"));
		String userId = providedByAuthn.getId();
		
		// This is the tough part: make sure we get the new user from Sakai's user
		// DB rather than any of the three caches.
		clearUserFromServiceCaches(userId);

		providedByAuthn = userDirectoryService.getUserByEid(eid);
		Assert.assertTrue(providedByAuthn.getLastName().equals("Last Name, Sr."));
		
		// Now authenticate the same user and make sure the Sakai-maintained
		// user record was updated.
		providedByAuthn = userDirectoryService.authenticate("LOGINprovidercreated", "providercreatedPW");
		clearUserFromServiceCaches(userId);
		providedByAuthn = userDirectoryService.getUserByEid(eid);
		Assert.assertTrue(providedByAuthn.getLastName().equals("Last Name, Jr."));
	}
	
	/**
	 * WARNING: There seems to be NO easy way to reset the UserDirectoryService MemoryService-managed
	 * cache, and so the only way currently to test for real DB storage is to have this line
	 * in the "sakai.properties" file used for the test:
	 *   cacheMinutes@org.sakaiproject.user.api.UserDirectoryService=0
	 */
	private void clearUserFromServiceCaches(String userId) {
		((DbUserService)userDirectoryService).getIdEidCache().clear();
		String ref = "/user/" + userId;
		threadLocalManager.set(ref, null);
	}
	
	public static class TestProvider implements UserDirectoryProvider, AuthenticatedUserProvider {
		private UserFactory userFactory;
		private UserDirectoryService userDirectoryService;
		private SecurityService securityService;

		public boolean authenticateUser(String eid, UserEdit user, String password) {
			// This should never be called since we implement the new interface.
			throw new RuntimeException("authenticateUser unexpectedly called");
		}

		public boolean authenticateWithProviderFirst(String loginId) {
			return !loginId.equals("loginprovidernotfirst");
		}

		public boolean findUserByEmail(UserEdit edit, String email) {
			return false;
		}

		public boolean getUser(UserEdit user) {
			String eid = user.getEid();
			if (!eid.startsWith("provide")) {
				return false;
			} else if (eid.equals("providercreated")) {
				// It's a little tricky to create a Sakai-stored user record from a
				// provider. When Sakai doesn't find any record of the new EID,
				// it will ask the provider for the corresponding user. If we
				// obligingly fill in the data and hand it over, Sakai will continue
				// to ask us for the provided user from then on. The only way
				// for a provider to force local storage is to deny all
				// knowledge of the user here and then add the Sakai user
				// record later in the authentication logic.
				return false;
			} else {
				return true;
			}
		}

		@SuppressWarnings("unchecked")
		public void getUsers(Collection users) {
		}

		public UserEdit getAuthenticatedUser(String loginId, String password) {
			log.debug("getAuthenticatedUser " + loginId + ", " + password);
			if (!loginId.startsWith("loginprovide")) return null;
			String eid = loginId.substring("login".length());
			if (password.equals(eid + "PW")) {
				if (eid.equals("providercreated")) {
					return createOrUpdateUserAfterAuthentication(eid, password);
				} else {
					// Thoroughly provided user.
					UserEdit user = userFactory.newUser();
					user.setEid(eid);				
					return user;
				}
			} else {
				return null;
			}
		}
		
		/**
		 * Sakai framework APIs don't always clearly distinguish between internal core utility services
		 * (which just do their specialized job) and application-facing support services (which
		 * require authorization checks). UserDirectoryService's user record modification methods
		 * were originally written for application support, and so they check the permissions of the
		 * current user. During the authentication process, there is no current user, and so
		 * if we want to modify the Sakai-stored user record, we need to push a SecurityAdvisor
		 * to bypass the checks. 
		 */
		private UserEdit createOrUpdateUserAfterAuthentication(String eid, String password) {
			// User record is created by provider but stored by core Sakai.
			UserEdit user = null;
			try {
				user = (UserEdit)userDirectoryService.getUserByEid(eid);
				try {
					securityService.pushAdvisor(new SecurityAdvisor() {
						public SecurityAdvice isAllowed(String userId, String function, String reference) {
							if (function.equals(UserDirectoryService.SECURE_UPDATE_USER_ANY)) {
								return SecurityAdvice.ALLOWED;
							} else {
								return SecurityAdvice.NOT_ALLOWED;
							}
						}
					});
					user = userDirectoryService.editUser(user.getId());
					user.setLastName("Last Name, Jr.");
					userDirectoryService.commitEdit(user);
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				} finally {
					securityService.popAdvisor();
				}
			} catch (UserNotDefinedException e) {
				try {
					securityService.pushAdvisor(new SecurityAdvisor() {
						public SecurityAdvice isAllowed(String userId, String function, String reference) {
							if (function.equals(UserDirectoryService.SECURE_ADD_USER)) {
								return SecurityAdvice.ALLOWED;
							} else {
								return SecurityAdvice.NOT_ALLOWED;
							}
						}
					});
					user = (UserEdit)userDirectoryService.addUser(null, eid, "First", "Last Name, Sr.", "eid@somewhere.edu", password, "Student", null);
				} catch (Exception e1) {
					log.warn(e1.getMessage(), e1);
				} finally {
					securityService.popAdvisor();
				}
			}
			return user;			
		}

		public void setUserFactory(UserFactory userFactory) {
			this.userFactory = userFactory;
		}

		public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
			this.userDirectoryService = userDirectoryService;
		}

		public void setSecurityService(SecurityService securityService) {
			this.securityService = securityService;
		}
	}

}
