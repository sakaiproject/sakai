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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.impl.DbUserService;

/**
 * This is a white-box-ish test which uses inner knowledge of the current
 * UserDirectoryService implementation.
 */
@Slf4j
public class GetUsersByEidTest extends SakaiKernelTestBase {
	// Oracle will throw a SQLException if we put more than this into a
	// "WHERE tbl.col IN (:paramList)" query, and so we need to test for
	// that condition.
	private static int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;
	
	private static String USER_EMAIL_PREFIX = "_user@some.edu";
	private static String USER_FIRST_NAME_PREFIX = "F. ";
	private static String USER_LAST_NAME_PREFIX = "de ";
	
	private static String LOCAL_USER_EID = "local_maint";
	private static String LOCAL_USER_WITH_NO_METADATA_EID = "very_private";
	private static String NO_SUCH_EID = "no_way_no_how";
	private static String SURPRISE_FOR_EID_TEST_EID = "provided_for_eid_test";
	
	private static List<String> mappedUserIds = new ArrayList<String>();
	
	// This is static because we can't completely undo UserDirectoryService actions
	// in a normal tearDown, and so we need to load data for all tests in a
	// one-time setup.
	// This is the implementation class because there's no way to inject the
	// test provider or to clear the user cache through the official API.
	private static DbUserService dbUserService;
	private static Cache<String, User> callCache;
	private static AuthzGroupService authzGroupService;
	private static ThreadLocalManager threadLocalManager;
	private static SessionManager sessionManager;

	@BeforeClass
	public static void beforeClass() {
		try {
			oneTimeSetup("disable_user_cache");
			oneTimeSetupAfter();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public static void oneTimeSetupAfter() throws Exception {
		TestProvider userDirectoryProvider = new TestProvider();
		
		// This is a workaround until we can make it easier to load sakai.properties
		// for specific integration tests.
		dbUserService = (DbUserService)getService("org.sakaiproject.user.api.UserDirectoryService");
		dbUserService.setProvider(userDirectoryProvider);
		
		callCache = ((MemoryService) getService("org.sakaiproject.memory.api.MemoryService")).getCache(
				"org.sakaiproject.user.api.UserDirectoryService.callCache");

		authzGroupService = getService(AuthzGroupService.class);

		threadLocalManager = getService(ThreadLocalManager.class);

		sessionManager = getService(SessionManager.class);
		
		// Sakai provides no way to undo a EID-to-ID mapping, and so we can't use
		// a normal setUp and tearDown approach to loading test data.
		
		// Act as admin since we're checking functionality rather than authz.
		actAsAdmin();
		
		// Add two local users for honesty's sake.
		User user;
		user = dbUserService.addUser(null, LOCAL_USER_EID, "Joe", "Guest", "joe@somewhere.edu", "pw", "Student", null);
		mappedUserIds.add(user.getId());	// Store for later use
		clearUserFromServiceCaches(user.getId());
		
		//for the search later we want a similar user
		user = dbUserService.addUser(null, "thisIs a search user", "Joe", "Guest", "joe@somewhere.edu", "pw", "Student", null);
		
		// The User Directory Service implementation currently includes no metadata that
		// distinguishes a metadata-free Sakai-managed user from a provided user, and so
		// no field of a full join can be safely checked to decide whether the provider
		// needs to be called. To make sure no erroneous assumptions get made, create a
		// mostly-null user record.
		user = dbUserService.addUser(null, LOCAL_USER_WITH_NO_METADATA_EID);
		mappedUserIds.add(user.getId());	// Store for later use
		clearUserFromServiceCaches(user.getId());
		
		// Our interest is in testing retrieval rather than creation. Pre-load all the
		// EID-to-ID mappings before doing the searches.
		for (int providedCounter = 0; providedCounter < MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST; providedCounter++) {
			user = dbUserService.getUserByEid(String.valueOf(providedCounter));
			mappedUserIds.add(user.getId());	// Store for later use
			clearUserFromServiceCaches(user.getId());
		}
	}
	
	private static void actAsAdmin() {
		sessionManager.getCurrentSession().setUserId("admin");
		authzGroupService.refreshUser("admin");
	}
	
	private static void clearUserFromServiceCaches(String userId) {
		dbUserService.getIdEidCache().clear();
		String ref = "/user/" + userId;
		threadLocalManager.set(ref, null);
		if (callCache != null) { callCache.remove(ref); }
	}
	
	@Test
	public void testGetUsersByEid() throws Exception {
		// Our big search list should contain:
		//   - All the legitimate provided user EIDs.
		//   - The two Sakai-managed users, including one with null metadata.
		//   - Continued case-insensitive EIDs by default
		//   - A bogus EID which won't match anyone.
		//   - An EID for a newly provided user who hasn't been preloaded.
		List<String> searchEids = new ArrayList<String>();
		searchEids.add(LOCAL_USER_EID.toUpperCase());
		searchEids.add(LOCAL_USER_WITH_NO_METADATA_EID);
		searchEids.add(NO_SUCH_EID);
		for (int providedCounter = 0; providedCounter < MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST; providedCounter++) {
			searchEids.add(String.valueOf(providedCounter));
		}
		searchEids.add(SURPRISE_FOR_EID_TEST_EID);	// Previously unseen

		// What we're really interested in is the number of DB queries, but
		// we don't yet have an easy way to monitor that. Instead, we make
		// sure that the provider methods are being called in the most efficient
		// way possible.

		TestProvider.GET_USER_CALLS_COUNTER = 0;
		TestProvider.GET_USERS_CALLS_COUNTER = 0;
		List<User> users = dbUserService.getUsersByEids(searchEids);
		Assert.assertEquals(MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST + 3, users.size());	// Everyone but the NO_SUCH_EID
		searchEids.remove(NO_SUCH_EID);
		Assert.assertEquals(0, TestProvider.GET_USER_CALLS_COUNTER);
		Assert.assertEquals(1, TestProvider.GET_USERS_CALLS_COUNTER);

		// Make sure caching wasn't broken. Again we need to use our inside
		// knowledge that even when all other caching is turned off, the
		// UDS ThreadLocal cache should keep the provider from needing to
		// be called.

		TestProvider.GET_USER_CALLS_COUNTER = 0;
		for (String eid : searchEids) {
			User user = dbUserService.getUserByEid(eid);
			clearUserFromServiceCaches(user.getId());
		}
		Assert.assertEquals(0, TestProvider.GET_USER_CALLS_COUNTER);
	}

	@Test
	public void testGetUsersById() throws Exception {
		// Our big search list should contain:
		//   - All the existing IDs for legitimate provided users.
		//   - The two Sakai-managed users, including one with null metadata.
		//   - A bogus ID which won't match anyone.
		List<String> searchIds = new ArrayList<String>(mappedUserIds);
		searchIds.add(NO_SUCH_EID);

		// What we're really interested in is the number of DB queries, but
		// we don't yet have an easy way to monitor that. Instead, we make
		// sure that the provider methods are being called in the most efficient
		// way possible.

		TestProvider.GET_USER_CALLS_COUNTER = 0;
		TestProvider.GET_USERS_CALLS_COUNTER = 0;
		@SuppressWarnings("unchecked")
		List<User> users = dbUserService.getUsers(searchIds);
		Assert.assertEquals(mappedUserIds.size(), users.size());	// Everyone but the NO_SUCH_EID
		Assert.assertEquals(0, TestProvider.GET_USER_CALLS_COUNTER);
		Assert.assertEquals(1, TestProvider.GET_USERS_CALLS_COUNTER);

		// Make sure caching wasn't broken. Again we need to use our inside
		// knowledge that even when all other caching is turned off, the
		// UDS ThreadLocal cache should keep the provider from needing to
		// be called.

		TestProvider.GET_USER_CALLS_COUNTER = 0;
		for (String id : mappedUserIds) {
			User user = dbUserService.getUser(id);
			clearUserFromServiceCaches(user.getId());
		}
		Assert.assertEquals(0, TestProvider.GET_USER_CALLS_COUNTER);
	}
	
	@Test
	public void testSearchUsers() {
		List<User> users = dbUserService.searchUsers("Joe", 1, 1);
		if (users == null) {
			log.error("empty list from search");
			Assert.fail();
		} else if (users.size() == 0 || users.size() > 1) {
			log.error("list of size 1 expected list contained: " + users.size());
			Assert.fail();
		}
 	}

	public static class TestProvider implements UserDirectoryProvider {
		public static int GET_USER_CALLS_COUNTER = 0;
		public static int GET_USERS_CALLS_COUNTER = 0;
		
		public boolean authenticateUser(String eid, UserEdit userEdit, String password) {
			return false;
		}

		public boolean authenticateWithProviderFirst(String eid) {
			return false;
		}

		public boolean findUserByEmail(UserEdit userEdit, String email) {
			return false;
		}
		
		private boolean fillUserRecord(UserEdit userEdit) {
			String userEid = userEdit.getEid();
			
			// If the EID isn't numeric, we have a ringer.
			if (!userEid.matches("^\\d+$") && !SURPRISE_FOR_EID_TEST_EID.equals(userEid)) {
				return false;
			}
			
			userEdit.setEmail(userEid + USER_EMAIL_PREFIX);
			userEdit.setFirstName(USER_FIRST_NAME_PREFIX + userEid);
			userEdit.setLastName(USER_LAST_NAME_PREFIX + userEid);
			return true;
		}

		public boolean getUser(UserEdit userEdit) {
			GET_USER_CALLS_COUNTER++;
			return fillUserRecord(userEdit);
		}

		public void getUsers(Collection<UserEdit> users) {
			GET_USERS_CALLS_COUNTER++;
			
			// This is where an efficient single DB query might
			// be made if we used a DB....
			
			// We're forced to use an iterator here. We can't explicitly
			// remove an unmatched user record from the input collection
			// because BaseUserEdit.equals() is based purely on getID(),
			// which will be null for all unmapped users.
			for (Iterator<UserEdit> userIter = users.iterator(); userIter.hasNext(); )
			{
				UserEdit userEdit = userIter.next();
				if (!fillUserRecord(userEdit)) {
					userIter.remove();
				}
			}
		}
	}

}
