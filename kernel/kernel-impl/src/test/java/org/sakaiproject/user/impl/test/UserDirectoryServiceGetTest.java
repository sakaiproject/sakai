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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.impl.DbUserService;
import org.sakaiproject.util.BaseResourceProperties;

/**
 *
 */
@Slf4j
public class UserDirectoryServiceGetTest extends SakaiKernelTestBase {
	private static String USER_SOURCE_PROPERTY = "user.source";
	
	private static Map<String, String> eidToId = new HashMap<String, String>();
	
	private static TestProvider userDirectoryProvider;
	private UserDirectoryService userDirectoryService;
	
	/**
	 * Because framework service calls create so many irreversible side-effects,
	 * we don't want to keep a single instance of the component manager alive
	 * for all test classes. On the other hand, we'd rather not take the
	 * overhead of starting up the component manager for every method in the
	 * test class. As a compromise, we run a test suite consisting only of the
	 * class itself.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() {
		try {
			if (log.isDebugEnabled()) log.debug("starting setup");
			oneTimeSetup();
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
		
		// Sakai user services very helpfully lowercase input EIDs rather than leaving them alone.
		addUserWithEid(dbUserService, "localuser");
		addUserWithEid(dbUserService, "localfromauthn");		
		addUserWithEid(dbUserService, "localwithproviderauthn");		
		addUserWithEid(dbUserService, "localwithfailedproviderauthn");		
	}
	
	private static void loginAs(String userId) {
		SessionManager sessionManager = (SessionManager)getService(SessionManager.class.getName());
		sessionManager.getCurrentSession().setUserId(userId);
	}
	private static User addUserWithEid(UserDirectoryService userDirectoryService, String eid) throws Exception {
		BaseResourceProperties props = new BaseResourceProperties();
		props.addProperty(USER_SOURCE_PROPERTY, "local");
		User user = userDirectoryService.addUser(null, eid, "J. " + eid, "de " + eid, eid + "@somewhere.edu", eid + "pwd", "Guest", props);
		eidToId.put(eid, user.getId());
		log.debug("addUser eid=" + eid + ", id=" + user.getId());
		return user;
	}
	private static UserEdit setAsExpected(UserEdit user, boolean isLocal) {
		String eid = user.getEid();
		user.setEmail(eid + "@somewhere.edu");
		user.setFirstName("J. " + eid);
		user.setLastName("de " + eid);
		user.setPassword(eid + "pwd");
		String type = isLocal ? "Guest" : "Student";
		user.setType(type);
		String source = isLocal ? "local" : "provided";
		user.getPropertiesEdit().addProperty(USER_SOURCE_PROPERTY, source);
		return user;
	}
	private static boolean isAsExpected(User user, String eid, boolean isLocal) {
	    if (eid == null) {
	        Assert.fail("eid is null");
	    }
	    if (eidToId.get(eid) == null) {
			eidToId.put(eid, user.getId());
		}
		boolean isMatch = (eid.equals(user.getEid()) &&
			user.getId().equals(eidToId.get(eid)) &&
			user.getEmail().equals(eid + "@somewhere.edu") &&
			user.getFirstName().equals("J. " + eid) &&
			user.getLastName().equals("de " + eid) &&
			(
				(isLocal && user.getType().equals("Guest") && "local".equals(user.getProperties().get(USER_SOURCE_PROPERTY))) || 
				(!isLocal && user.getType().equals("Student") && "provided".equals(user.getProperties().get(USER_SOURCE_PROPERTY)))
			)
		);
		if (!isMatch) log.debug("For eid=" + eid + ", user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail() + ", first=" + user.getFirstName() + ", last=" + user.getLastName());
		return isMatch;
	}
	
	@Before
	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
	}
	
	@Test
	public void testLocalUser() throws Exception {
		User user = userDirectoryService.getUserByEid("localuser");
		Assert.assertTrue(isAsExpected(user, "localuser", true));
		List<User> users = userDirectoryService.getUsers();
		Assert.assertTrue(users.size() >= 4);	// Will probably also include "admin" and "postmaster"
	}
	
	@Test
	public void testLocalUserByEmail() throws Exception {
		// Get into cache.
		User user = userDirectoryService.getUserByEid("localuser");
		
		Collection<User> users = userDirectoryService.findUsersByEmail("localuser" + "@somewhere.edu");
		Assert.assertTrue(users.size() == 1);
		user = (User)users.iterator().next();
		Assert.assertTrue(isAsExpected(user, "localuser", true));
		String userId = user.getId();

		// We need to be logged in to change a user record, although not to create one.
		loginAs("admin");
		UserEdit userEdit = userDirectoryService.editUser(userId);
		userEdit.setEmail("razzle@somewhere.edu");
		userDirectoryService.commitEdit(userEdit);
		
		// Check that caches were updated.
		user = userDirectoryService.getUser(userId);
		Assert.assertTrue("razzle@somewhere.edu".equals(user.getEmail()));
		
		users = userDirectoryService.findUsersByEmail("razzle@somewhere.edu");
		Assert.assertTrue(users.size() == 1);
		user = (User)users.iterator().next();
		Assert.assertTrue("localuser".equals(user.getEid()));
		
		// Return to where we were.
		userEdit = userDirectoryService.editUser(userId);
		userEdit.setEmail("localuser@somewhere.edu");
		userDirectoryService.commitEdit(userEdit);
	}
	
	@Test
	public void testLocalUserByAuthn() throws Exception {
		User user = userDirectoryService.authenticate("localfromauthn", "WRONGpwd");
		Assert.assertTrue(user == null);
		user = userDirectoryService.authenticate("localfromauthn", "localfromauthn" + "pwd");
		Assert.assertTrue(isAsExpected(user, "localfromauthn", true));
	}
	
	@Test
	public void testLocalUserGetEid() throws Exception {
		String eid = userDirectoryService.getUserEid(eidToId.get("localuser"));
		Assert.assertTrue("localuser".equals(eid));
	}
	
	@Test
	public void testLocalUserGetId() throws Exception {
		String id = userDirectoryService.getUserId("localuser");
		Assert.assertTrue(eidToId.get("localuser").equals(id));
	}
	
	@Test
	public void testLocalUserEidEdit() throws Exception {
		String localuserId = eidToId.get("localuser");

		// Only test EID changing if EIDs are different from IDs.
		// Currently there's no way for clients to directly find out whether separate IDs and EIDs are
		// supported, and so we guess at it by looking at what ID was given to an existing record.
		boolean separateIdEid = !localuserId.equals("localuser");
		if (separateIdEid) {
			loginAs("admin");
			UserEdit userEdit = userDirectoryService.editUser(localuserId);		
			userEdit.setEid("razzle");
			userDirectoryService.commitEdit(userEdit);
			String eid = userDirectoryService.getUserEid(localuserId);
			Assert.assertTrue("razzle".equals(eid));
			String id = userDirectoryService.getUserId("razzle");
			Assert.assertTrue(localuserId.equals(id));

			// Return to where we were.
			userEdit = userDirectoryService.editUser(localuserId);
			userEdit.setEid("localuser");
			userDirectoryService.commitEdit(userEdit);
		}
	}

	@Test
	public void testLocalUserNameEdit() throws Exception {
        String localuserId = eidToId.get("localuser");
        UserEdit userEdit = null;
        User user = null;

        loginAs("admin");

        // check for basic edit
        userEdit = userDirectoryService.editUser(localuserId);
        String originalFirstName = userEdit.getFirstName();
        String originalLastName = userEdit.getLastName();
        userEdit.setFirstName("Aaron");
        userEdit.setLastName("Zeckoski");
        userDirectoryService.commitEdit(userEdit);

        user = userDirectoryService.getUser(localuserId);
        Assert.assertEquals("Aaron", user.getFirstName());
        Assert.assertEquals("Zeckoski", user.getLastName());

        // check https://jira.sakaiproject.org/browse/SAK-20226
        userEdit = userDirectoryService.editUser(localuserId);
        userEdit.setFirstName("<b>Aaron</b>");
        userEdit.setLastName("Zeckoski<script>alert(document.cookie)</script>");
        userDirectoryService.commitEdit(userEdit);

        user = userDirectoryService.getUser(localuserId);
        Assert.assertEquals("Aaron", user.getFirstName());
        Assert.assertEquals("Zeckoskialert(document.cookie)", user.getLastName());

        // Return to where we were.
        userEdit = userDirectoryService.editUser(localuserId);
        userEdit.setFirstName(originalFirstName);
        userEdit.setLastName(originalLastName);
        userDirectoryService.commitEdit(userEdit);
    }

	@Test
	public void testLocalUserRemove() throws Exception {
		// We need to be logged in to change a user record, although not to create one.
		loginAs("admin");
		UserEdit userEdit = userDirectoryService.editUser(eidToId.get("localuser"));		
		userDirectoryService.removeUser(userEdit);
		User user = null;
		try {
			user = userDirectoryService.getUserByEid("localuser");
			if (user != null) log.debug("After removeUser, user eid=" + user.getEid() + ", id=" + user.getId());
		} catch (UserNotDefinedException e) {
		}
		try {
			user = userDirectoryService.getUser(eidToId.get("localuser"));
			if (user != null) log.debug("After removeUser, user eid=" + user.getEid() + ", id=" + user.getId());
			Assert.fail();
		} catch (UserNotDefinedException e) {
		}
		addUserWithEid(userDirectoryService, "localuser");
	}
	
	@Test
	public void testProvidedUsers() throws Exception {
		User user = userDirectoryService.getUserByEid("provideduser");
		Assert.assertTrue(isAsExpected(user, "provideduser", false));
		String userId = userDirectoryService.getUserId("providedthroughid");
		user = userDirectoryService.getUser(userId);
		Assert.assertTrue(isAsExpected(user, "providedthroughid", false));
		Collection<User> users = userDirectoryService.findUsersByEmail("provideduser@somewhere.edu");
		Assert.assertTrue(users.size() == 1);
		user = (User)users.iterator().next();
		Assert.assertTrue(isAsExpected(user, "provideduser", false));
		try {
			user = userDirectoryService.getUserByEid("nosuchuser");
			Assert.fail();
		} catch (UserNotDefinedException e) {
		}
	}
	
	@Test
	public void testProvidedAuthentication() throws Exception {
		User user = userDirectoryService.authenticate("providedfromauthn", "providedfromauthn" + "Ppwd");
		Assert.assertTrue(isAsExpected(user, "providedfromauthn", false));
		user = userDirectoryService.authenticate("localwithproviderauthn", "localwithproviderauthn" + "Ppwd");
		Assert.assertTrue(isAsExpected(user, "localwithproviderauthn", true));
		user = userDirectoryService.authenticate("localwithfailedproviderauthn", "localwithfailedproviderauthn" + "pwd");
		Assert.assertTrue(isAsExpected(user, "localwithfailedproviderauthn", true));
	}
	
	@Test
	public void testGetUsersThroughIds() throws Exception {
		Collection<String> mappedIds = eidToId.values();
		log.debug("have " + mappedIds.size() + " mapped IDs");
		List<String> searchIds = new ArrayList<String>(mappedIds);
		searchIds.add("NoSuchId");
		List<User> users = userDirectoryService.getUsers(searchIds);
		log.debug("Return from getUsers=" + users);
		Assert.assertTrue(users.size() == (searchIds.size() - 1));
	}
	
	@Test
	public void testCannotChangeUserId() throws Exception {
		UserEdit newUser = ((UserFactory)userDirectoryService).newUser();
		newUser.setId("homegrownid");
		Assert.assertTrue(newUser.getId().equals("homegrownid"));
		try {
			newUser.setId("changedid");
			Assert.fail();
		} catch (UnsupportedOperationException e) {
		}
	}
	
	@Test
	public void testNoDuplicateUserEid() throws Exception {
		String eid = "localuser";
		try {
			userDirectoryService.addUser(null, eid, "J. " + eid, "de " + eid, eid + "@somewhere.edu", eid + "pwd", "Guest", null);
			Assert.fail();
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testDefaultDisplayIdAndName() throws Exception {
		String eid = "localuser";
		User user = userDirectoryService.getUserByEid(eid);
		Assert.assertEquals(user.getFirstName() + " " + user.getLastName(), user.getDisplayName());
		Assert.assertEquals(eid, user.getDisplayId());
	}
	
	/**
	 * We should get a predicatable error on trying to create a user with an Eid longer than supported
	 */
	@Test
	public void testLongEid() {
		String eid =  "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
		try {
			userDirectoryService.addUser(null, eid);
			Assert.fail();
		} catch (UserIdInvalidException e) {
			log.error(e.getMessage(), e);
		} catch (UserAlreadyDefinedException e) {
			Assert.fail();
		} catch (UserPermissionException e) {
			Assert.fail();
		}
		
	}
	
	public static class TestProvider implements UserDirectoryProvider {
		public boolean authenticateUser(String eid, UserEdit userEdit, String password) {
			if (eid.equals("providedfromauthn")) {
				setAsExpected(userEdit, false);
				return true;
			} else if (eid.equals("localwithproviderauthn")) {
				return true;
			} else {
				return false;
			}
		}

		public boolean authenticateWithProviderFirst(String eid) {
			return !eid.equals("localfromauthn");
		}

		public boolean findUserByEmail(UserEdit userEdit, String email) {
			log.debug("findUserByEmail email=" + email);
			String[] parts = email.split("@");
			String eid = parts[0];
			if (eid.startsWith("provided")) {
				userEdit.setEid(eid);
				setAsExpected(userEdit, false);
				return true;
			} else {
				return false;
			}
		}

		public boolean getUser(UserEdit userEdit) {
			String eid = userEdit.getEid();
			log.debug("getUser eid=" + eid + ", id=" + userEdit.getId() + ", email=" + userEdit.getEmail());
			
			if (eid.startsWith("provided")) {
				setAsExpected(userEdit, false);
				return true;
			} else {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
        public void getUsers(Collection users) {
			for (Iterator<User> iter = users.iterator(); iter.hasNext(); ) {
				UserEdit userEdit = (UserEdit)iter.next();
				if (!getUser(userEdit)) {
					iter.remove();
				}
			}
		}
	}
}
