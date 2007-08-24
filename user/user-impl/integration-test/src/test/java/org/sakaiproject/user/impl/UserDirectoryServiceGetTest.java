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
import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;

/**
 *
 */
public class UserDirectoryServiceGetTest extends SakaiTestBase {
	private static Log log = LogFactory.getLog(UserDirectoryServiceGetTest.class);
	private static String USER_SOURCE_PROPERTY = "user.source";
	
	private static Map<String, String> eidToId = new HashMap<String, String>();
	
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
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(UserDirectoryServiceGetTest.class)) {
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
		TestProvider userDirectoryProvider = new TestProvider();
		
		// This is a workaround until we can make it easier to load sakai.properties
		// for specific integration tests.
		DbUserService dbUserService = (DbUserService)getService(UserDirectoryService.class.getName());
		dbUserService.setProvider(userDirectoryProvider);
		
		// Sakai user services very helpfully lowercase input EIDs rather than leaving them alone.
		addUserWithEid(dbUserService, "localuser");
		addUserWithEid(dbUserService, "localfromauthn");		
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
	private static boolean isAsExpected(User user, String eid, boolean isLocal) {
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
	
	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
	}
	
	public void testLocalUser() throws Exception {
		User user = userDirectoryService.getUserByEid("localuser");
		Assert.assertTrue(isAsExpected(user, "localuser", true));
	}
	
	public void testLocalUserByEmail() throws Exception {
		Collection users = userDirectoryService.findUsersByEmail("localuser" + "@somewhere.edu");
		Assert.assertTrue(users.size() == 1);
		User user = (User)users.iterator().next();
		Assert.assertTrue(isAsExpected(user, "localuser", true));
	}
	
	public void testLocalUserByAuthn() throws Exception {
		User user = userDirectoryService.authenticate("localfromauthn", "localfromauthn" + "pwd");
		Assert.assertTrue(isAsExpected(user, "localfromauthn", true));
	}
	
	public void testLocalUserRemove() throws Exception {
		// We need to be logged in to create a user record, although not to create one.
		loginAs("admin");
		UserEdit userEdit = userDirectoryService.editUser(eidToId.get("localuser"));		
		userDirectoryService.removeUser(userEdit);
		User user = null;
		try {
			user = userDirectoryService.getUserByEid("localuser");
			if (user != null) log.debug("After removeUser, user eid=" + user.getEid() + ", id=" + user.getId());
			Assert.fail();
		} catch (UserNotDefinedException e) {
		}
		try {
			user = userDirectoryService.getUser(eidToId.get("localuser"));
			if (user != null) log.debug("After removeUser, user eid=" + user.getEid() + ", id=" + user.getId());
			Assert.fail();
		} catch (UserNotDefinedException e) {
		}
	}
	
	public static class TestProvider implements UserDirectoryProvider {

		public boolean authenticateUser(String eid, UserEdit edit, String password) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean authenticateWithProviderFirst(String eid) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean createUserRecord(String eid) {
			throw new UnsupportedOperationException("createUserRecord is legacy cruft, and should not be called at runtime");
		}

		public void destroyAuthentication() {
			throw new UnsupportedOperationException("destroyAuthentication is legacy cruft, and should not be called at runtime");
		}

		public boolean findUserByEmail(UserEdit edit, String email) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean getUser(UserEdit edit) {
			// TODO Auto-generated method stub
			return false;
		}

		public void getUsers(Collection users) {
			// TODO Auto-generated method stub
			
		}

		public boolean updateUserAfterAuthentication() {
			throw new UnsupportedOperationException("updateUserAfterAuthentication is legacy cruft, and should not be called at runtime");
		}

		public boolean userExists(String eid) {
			throw new UnsupportedOperationException("userExists is legacy cruft, and should not be called at runtime");
		}
		
	}
}
