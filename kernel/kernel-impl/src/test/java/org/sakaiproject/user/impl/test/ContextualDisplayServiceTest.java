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

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.impl.DbUserService;

/**
 * Test that the User Directory Service is correctly enabling context-specific display IDs
 * and display names.
 */
@Slf4j
public class ContextualDisplayServiceTest extends SakaiKernelTestBase {
	private static final String FIRST_NAME = "Alice";
	// Also use EIDs as last names.
	private static final String NOT_IN_SITE_USER_EID = "not_in_site_user";
	private static final String UNADVISED_USER_EID = "unadvised_user";
	private static final String DISPLAY_ADVISED_USER_EID = "display_advised_user";
	private static final String DISPLAY_ADVISED_SUFFIX = "-ADVISED";
	private static final String STANDARD_SITE_NAME = "standard_site";
	private static final String CONTEXTUAL_SITE_NAME = "contextual_site";

	private UserDirectoryService userDirectoryService;
	private SiteService siteService;
	private AuthzGroupService authzGroupService;
	
	// Declared static since we won't be able to find it as a Spring bean.
	private static ContextualUserDisplayService contextualUserDisplayService;
	
	private static String standardSiteUid;
	private static String contextualSiteUid;
	private static String currentSiteUid;

	@BeforeClass
	public static void beforeClass() {
		try {
            log.debug("starting oneTimeSetup");
			oneTimeSetup();
			oneTimeSetupAfter();
            log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	// A full test requires both a display-advising User Directory Provider and a
	// contextual display provider. An inter-component integration test would manage
	// via normal Sakai configuration files. For a kernel-internal test, it needs
	// to be handled less realistically within a single classloader and by explicit injection
	// through Java code.
	
	// TODO Make it easier to handle test-specific providers in the kernel.
	private static void oneTimeSetupAfter() throws Exception {
		TestDisplayAdvisorProvider userDirectoryProvider = new TestDisplayAdvisorProvider();
		contextualUserDisplayService = new TestContextualUserDisplayService();
		
		DbUserService dbUserService = (DbUserService)getService(UserDirectoryService.class);
		dbUserService.setProvider(userDirectoryProvider);
		dbUserService.setContextualUserDisplayService(contextualUserDisplayService);
	}
	
	@Before
	public void setUp() throws Exception {
		userDirectoryService = getService(UserDirectoryService.class);
		siteService = getService(SiteService.class);
		authzGroupService = getService(AuthzGroupService.class);
		
		actAsUserEid("admin");
		
		// Add test users.
		addUserWithEid(NOT_IN_SITE_USER_EID);
		addUserWithEid(UNADVISED_USER_EID);
		addUserWithEid(DISPLAY_ADVISED_USER_EID);
		
		// Add test sites.
		Site site = siteService.addSite(STANDARD_SITE_NAME, "project");
		siteService.save(site);
		standardSiteUid = site.getReference();
		
		site = siteService.addSite(CONTEXTUAL_SITE_NAME, "project");
		siteService.save(site);
		contextualSiteUid = site.getReference();
	}
	
	@Test
	public void testContextualDisplayProvision() throws Exception {
		User unadvisedUser = userDirectoryService.getUserByEid(UNADVISED_USER_EID);
		User displayAdvisedUser = userDirectoryService.getUserByEid(DISPLAY_ADVISED_USER_EID);
		User notInSiteUser = userDirectoryService.getUserByEid(NOT_IN_SITE_USER_EID);
		
		// Test for default handling of display ID and name outside a site context.
		checkForUnadvisedProperties(unadvisedUser);
		
		// Test that the user directory provider handles display ID and name outside
		// a site context.
		checkForAdvisedProperties(displayAdvisedUser);
		
		// This should give the same result in or out of the site context,
		// but check, just in case....
		checkForAdvisedProperties(notInSiteUser);
		
		// Test explicit calls to the test support code....
		checkExplicitContextualService(unadvisedUser);
		checkExplicitContextualService(displayAdvisedUser);
		Assert.assertEquals(null, contextualUserDisplayService.getUserDisplayId(notInSiteUser, standardSiteUid));
		Assert.assertEquals(null, contextualUserDisplayService.getUserDisplayName(notInSiteUser, standardSiteUid));
		Assert.assertEquals(null, contextualUserDisplayService.getUserDisplayId(notInSiteUser, contextualSiteUid));
		Assert.assertEquals(null, contextualUserDisplayService.getUserDisplayName(notInSiteUser, contextualSiteUid));
		
		// Set the current site context to make sure the User Directory Service
		// does the right thing.
		currentSiteUid = contextualSiteUid;
		checkForContextualProperties(unadvisedUser);
		checkForContextualProperties(displayAdvisedUser);
		checkForAdvisedProperties(notInSiteUser);
		currentSiteUid = null;
	}
	
	private void checkForAdvisedProperties(User user) {
		Assert.assertEquals(user.getEid() + DISPLAY_ADVISED_SUFFIX, user.getDisplayId());
		Assert.assertEquals(user.getFirstName() + " " + user.getLastName() + DISPLAY_ADVISED_SUFFIX, user.getDisplayName());		
	}
	
	private void checkForUnadvisedProperties(User user) {
		Assert.assertEquals(user.getEid(), user.getDisplayId());
		Assert.assertEquals(user.getFirstName() + " " + user.getLastName(), user.getDisplayName());		
	}
	
	private void checkExplicitContextualService(User user) {
		Assert.assertEquals(null, contextualUserDisplayService.getUserDisplayId(user, standardSiteUid));
		Assert.assertEquals(null, contextualUserDisplayService.getUserDisplayName(user, standardSiteUid));
		Assert.assertEquals(user.getEid() + CONTEXTUAL_SITE_NAME, contextualUserDisplayService.getUserDisplayId(user, contextualSiteUid));
		Assert.assertEquals(user.getFirstName() + " " + user.getLastName() + CONTEXTUAL_SITE_NAME, 
				contextualUserDisplayService.getUserDisplayName(user, contextualSiteUid));		
	}
	
	private void checkForContextualProperties(User user) {
		Assert.assertEquals(user.getEid() + CONTEXTUAL_SITE_NAME, user.getDisplayId());
		Assert.assertEquals(user.getFirstName() + " " + user.getLastName() + CONTEXTUAL_SITE_NAME, user.getDisplayName());		
	}

	private User addUserWithEid(String eid) throws Exception {
		User user = userDirectoryService.addUser(null, eid, FIRST_NAME, eid, eid + "@somewhere.edu", eid + "pwd", "Guest", null);
		log.debug("addUser eid=" + eid + ", id=" + user.getId());
		return user;
	}

	/**
	 * Test-supporting provider.
	 */
	public static class TestDisplayAdvisorProvider implements UserDirectoryProvider, DisplayAdvisorUDP {
		public String getDisplayId(User user) {
			if (!UNADVISED_USER_EID.equals(user.getEid())) {
				return user.getEid() + DISPLAY_ADVISED_SUFFIX;
			} else {
				return null;
			}
		}

		public String getDisplayName(User user) {
			if (!UNADVISED_USER_EID.equals(user.getEid())) {
				return user.getFirstName() + " " + user.getLastName() + DISPLAY_ADVISED_SUFFIX;
			} else {
				return null;
			}
		}

		// This test class only acts as a display name advisor, not as a user record provider. 
		public boolean authenticateUser(String eid, UserEdit user, String password) {
			return false;
		}
		public boolean authenticateWithProviderFirst(String eid) {
			return false;
		}
		public boolean findUserByEmail(UserEdit edit, String email) {
			return false;
		}
		public boolean getUser(UserEdit user) {
			return false;
		}
		@SuppressWarnings("unchecked")
		public void getUsers(Collection users) {
		}
	}
	
	public static class TestContextualUserDisplayService implements ContextualUserDisplayService {
		public String getUserDisplayId(User user, String contextReference) {
			if (contextualSiteUid.equals(contextReference)) {
				if (!NOT_IN_SITE_USER_EID.equals(user.getEid())) {
					return user.getEid() + CONTEXTUAL_SITE_NAME;
				}
			}
			return null;
		}

		public String getUserDisplayName(User user, String contextReference) {
			if (contextualSiteUid.equals(contextReference)) {
				if (!NOT_IN_SITE_USER_EID.equals(user.getEid())) {
					return user.getFirstName() + " " + user.getLastName() + CONTEXTUAL_SITE_NAME;
				}
			}
			return null;
		}

		public String getUserDisplayId(User user) {
			return getUserDisplayId(user, currentSiteUid);
		}

		public String getUserDisplayName(User user) {
			return getUserDisplayName(user, currentSiteUid);
		}
		
	}

	/**
	 * Convenience method to switch authn/authz identities.
	 * TODO Move this frequently-needed helper logic into the base class.
	 *
	 * @param userEid
	 */
	public void actAsUserEid(String userEid) {
		if (log.isDebugEnabled()) log.debug("actAsUserEid=" + userEid);
		
		SessionManager sessionManager = getService(SessionManager.class);
		
		String userId;
		try {
			userId = userDirectoryService.getUserId(userEid);
		} catch (UserNotDefinedException e) {
			log.error("Could not act as user EID=" + userEid, e);
			return;
		}
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(userEid);
		session.setUserId(userId);
		authzGroupService.refreshUser(userId);
	}
}
