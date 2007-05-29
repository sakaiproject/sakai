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
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;

/**
 *
 */
public class UserDirectoryServiceIntegrationTest extends SakaiTestBase {
	private static Log log = LogFactory.getLog(UserDirectoryServiceIntegrationTest.class);
	private static String[] USER_DATA_IN_LOCAL_STORAGE = {"LocalOnlyUser", null, "First", "Last", "local@edu", "localpassword"};
	private static String[] USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT = {"LocalOnlyButHopefulUser", null, "First", "Last", "localhopeful@edu", "localpassword"};
	private static String[] USER_DATA_IN_PROVIDER = {"ProviderOnlyUser", null, "First", "Last", "provider@edu", "providerpassword"};
	private static String[] USER_DATA_FROM_PROVIDER_AUTHN = {"ProviderAuthnUser", null, "First", "Last", "providerauthn@edu", "providerauthnpassword"};
	private static String[] USER_DATA_UPDATED_BY_PROVIDER = {"LocalFirstUser", null, "First", "Last", "localfirst@edu", "localfirstpassword"};
	private static String AUTHN_ID_NOT_PROVIDER_EID = "ProviderAuthnUserIsNotEid";
	private static String AUTHN_ID_NOT_LOCAL_EID = "LocalAuthnUserIsNotEid";
	private static String EMAIL_FOR_UPDATED_USER_EQUAL_IDS = "localsecond@edu"; 
	private static String EMAIL_FOR_UPDATED_USER_UNEQUAL_IDS = "localthird@edu"; 
	
	private UserDirectoryService userDirectoryService;
	
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(UserDirectoryServiceIntegrationTest.class)) {
			protected void setUp() throws Exception {
				log.debug("starting setup");
				oneTimeSetup();
				log.debug("finished setup");
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void setUp() throws Exception {
		log.debug("Setting up UserDirectoryServiceIntegrationTest");
		
		userDirectoryService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		log.debug("userDirectoryService=" + userDirectoryService + " for name=" + UserDirectoryService.class.getName());
		
		TestProvider userDirectoryProvider = new TestProvider();
		
		// Now for the tricky part.... This is just a workaround until we can make it
		// easier to load sakai.properties for specific integration tests.
		DbUserService dbUserService = (DbUserService)userDirectoryService;
		dbUserService.setProvider(userDirectoryProvider);
		dbUserService.setCacheMinutes("15");
		dbUserService.setCacheCleanerMinutes("15");
		userDirectoryProvider.setUserFactory(dbUserService);
		
		User localUser = userDirectoryService.addUser(USER_DATA_IN_LOCAL_STORAGE[1], USER_DATA_IN_LOCAL_STORAGE[0], 
				USER_DATA_IN_LOCAL_STORAGE[2], USER_DATA_IN_LOCAL_STORAGE[3], USER_DATA_IN_LOCAL_STORAGE[4], USER_DATA_IN_LOCAL_STORAGE[5], null, null);
		log.debug("Created local user eid=" + localUser.getEid() + ", id=" + localUser.getId());
		USER_DATA_IN_LOCAL_STORAGE[1] = localUser.getId();
		localUser = userDirectoryService.addUser(USER_DATA_UPDATED_BY_PROVIDER[1], USER_DATA_UPDATED_BY_PROVIDER[0], 
				USER_DATA_UPDATED_BY_PROVIDER[2], USER_DATA_UPDATED_BY_PROVIDER[3], USER_DATA_UPDATED_BY_PROVIDER[4], USER_DATA_UPDATED_BY_PROVIDER[5], null, null);
		log.debug("Created local user eid=" + localUser.getEid() + ", id=" + localUser.getId());
		USER_DATA_UPDATED_BY_PROVIDER[1] = localUser.getId();
		localUser = userDirectoryService.addUser(USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[1], USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[0], 
				USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[2], USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[3], USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[4], USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[5], null, null);
		log.debug("Created local user eid=" + localUser.getEid() + ", id=" + localUser.getId());
		USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[1] = localUser.getId();
	}
	
	/**
	 * Because a lot of what we have to test in the legacy user provider service involves
	 * irreversible side-effects (such as use of in-memory cache), we can't put much
	 * trust in the "tearDown" approach. Instead, we rely on the "one long
	 * complex test method" approach.
	 */
	public void tearDown() throws Exception {
	}
	
	public void testGetAndAuthUser() throws Exception {
		User user = userDirectoryService.getUserByEid(USER_DATA_IN_LOCAL_STORAGE[0]);
		if (log.isDebugEnabled()) log.debug("Got local user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_IN_LOCAL_STORAGE[4]));
		
		user = userDirectoryService.getUserByEid(USER_DATA_IN_PROVIDER[0]);
		if (log.isDebugEnabled()) log.debug("Got provided user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_IN_PROVIDER[4]));
		
		// Test legacy getUser(id) path;
		user = userDirectoryService.getUser(USER_DATA_UPDATED_BY_PROVIDER[1]);
		if (log.isDebugEnabled()) log.debug("Got provided user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_UPDATED_BY_PROVIDER[4]));

		// Default Base authentication of a locally stored user.
		user = userDirectoryService.authenticate(USER_DATA_IN_LOCAL_STORAGE[0], USER_DATA_IN_LOCAL_STORAGE[5]);
		if (log.isDebugEnabled()) log.debug("Locally authenticated user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_IN_LOCAL_STORAGE[4]));

		// Try (but fall through) provided authentication of a locally stored user.
		user = userDirectoryService.authenticate(USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[0], USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[5]);
		if (log.isDebugEnabled()) log.debug("Locally authenticated user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[4]));
		
		// Authn-provided update of already loaded locally stored user, with
		// Authentication ID == EID. (In other words, test the current code
		// path for authenticate-first update-after.)
		user = userDirectoryService.authenticate(USER_DATA_UPDATED_BY_PROVIDER[0], USER_DATA_UPDATED_BY_PROVIDER[5]);
		if (log.isDebugEnabled()) log.debug("Provider authenticated local user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(EMAIL_FOR_UPDATED_USER_EQUAL_IDS));		
		user = userDirectoryService.getUserByEid(USER_DATA_UPDATED_BY_PROVIDER[0]);
		if (log.isDebugEnabled()) log.debug("On re-get local user email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(EMAIL_FOR_UPDATED_USER_EQUAL_IDS));		
				
		// Authn-provided user where Authentication ID != EID.
		user = userDirectoryService.authenticate(AUTHN_ID_NOT_PROVIDER_EID, USER_DATA_FROM_PROVIDER_AUTHN[5]);
		if (log.isDebugEnabled()) log.debug("Provider authenticated user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_FROM_PROVIDER_AUTHN[4]));
		
		// Remember the assigned ID for the next go round below.
		String authnProvidedId = StringUtil.trimToNull(user.getId());
		Assert.assertTrue(authnProvidedId != null);
		
		// Authn-provided update of already loaded locally stored user, with
		// Authentication ID != EID.
		user = userDirectoryService.authenticate(AUTHN_ID_NOT_LOCAL_EID, USER_DATA_UPDATED_BY_PROVIDER[5]);
		if (log.isDebugEnabled()) log.debug("Provider non-EID authenticated local user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(EMAIL_FOR_UPDATED_USER_UNEQUAL_IDS));		
		user = userDirectoryService.getUserByEid(USER_DATA_UPDATED_BY_PROVIDER[0]);
		if (log.isDebugEnabled()) log.debug("On re-get local user email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(EMAIL_FOR_UPDATED_USER_UNEQUAL_IDS));		
		
		// Authn-provided user where Authentication ID != EID.
		user = userDirectoryService.authenticate(AUTHN_ID_NOT_PROVIDER_EID, USER_DATA_FROM_PROVIDER_AUTHN[5]);
		if (log.isDebugEnabled()) log.debug("Provider authenticated user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(user.getEmail().equals(USER_DATA_FROM_PROVIDER_AUTHN[4]));

		// Second go-round for authn-provided user where Authentication ID != EID,
		// to make sure the ID isn't lost.
		user = userDirectoryService.authenticate(AUTHN_ID_NOT_PROVIDER_EID, USER_DATA_FROM_PROVIDER_AUTHN[5]);
		if (log.isDebugEnabled()) log.debug("Provider authenticated user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail()); 
		Assert.assertTrue(authnProvidedId.equals(user.getId()));
		
		// Make sure nothing horrible happens when there's no such user anywhere.
		user = userDirectoryService.authenticate("NoSuchUser", "NoSuchPassword");
		Assert.assertTrue(user == null);
	}
	
	private static boolean loadProvidedUserData(String eid, UserEdit toUser) {
		String[] fromData = {};
		if (eid.equalsIgnoreCase(USER_DATA_IN_PROVIDER[0])) {
			fromData = USER_DATA_IN_PROVIDER;
		} else if (eid.equalsIgnoreCase(USER_DATA_FROM_PROVIDER_AUTHN[0])) {
			fromData = USER_DATA_FROM_PROVIDER_AUTHN;
		} else {
			return false;
		}
		
		toUser.setEid(fromData[0]);
		toUser.setFirstName(fromData[2]);
		toUser.setLastName(fromData[3]);
		toUser.setEmail(fromData[4]);
		toUser.setPassword(fromData[5]);
		return true;
	}
	
	public class TestProvider implements UserDirectoryProvider, AuthenticatedUserProvider {
		private UserFactory userFactory;

		public boolean authenticateUser(String authenticationId, UserEdit user, String password) {
			if (log.isDebugEnabled()) {
				log.debug("provider authenticateUser authenticationId=" + authenticationId + ", user=" + user);
				if (user != null) log.debug("provider authenticateUser authenticationId=" + authenticationId + ", user eid=" + user.getEid() + ", id=" + user.getId() + ", email=" + user.getEmail());
			}
			
			// This should never be called since we implement the new interface.
			throw new RuntimeException("authenticateUser unexpectedly called");
		}

		public boolean authenticateWithProviderFirst(String eid) {
			return (!eid.equalsIgnoreCase(USER_DATA_IN_LOCAL_STORAGE[0]));
		}

		/*
		 * AFAIK this is never used by anything and should be removed from the interface....
		 */
//		public boolean createUserRecord(String eid) {
//			if (log.isWarnEnabled()) log.warn("provider createUserRecord called unexpectedly!");
//			return false;
//		}

		/**
		 * AFAIK this is never used by anything and should be removed from the interface....
		 */
		public void destroyAuthentication() {			
		}

		public boolean findUserByEmail(UserEdit edit, String email) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean getUser(UserEdit user) {
			if (log.isDebugEnabled()) log.debug("provider getUser eid=" + user.getEid());
			return loadProvidedUserData(user.getEid(), user);
		}

		public void getUsers(Collection users) {
			// TODO Auto-generated method stub
			
		}

		public boolean updateUserAfterAuthentication() {
			return true;
		}

		/**
		 * AFAIK this is never used by anything and should be removed from the interface....
		 */
		public boolean userExists(String eid) {
			// TODO Auto-generated method stub
			return false;
		}

		public UserEdit getAuthenticatedUser(String authenticationId, String password) {
			String eid;
			String revisedEmail = null;
			
			if (log.isDebugEnabled()) log.debug("provider getAuthenticatedUser authenticationId=" + authenticationId);

			// Make sure the service obeyed our authenticateWithProviderFirst directive.
			Assert.assertFalse(authenticationId.equalsIgnoreCase(USER_DATA_IN_LOCAL_STORAGE[0]));

			// The "local user" case in which authentication should really be handled by the
			// base service rather than the provider, but the provider wasn't able to predict that.
			if (authenticationId.equalsIgnoreCase(USER_DATA_IN_LOCAL_STORAGE_WITH_AUTHN_ATTEMPT[0])) {
				return null;
			}
			
			// For testing purposes, we have a case where the authentication
			// ID equals the EID.
			if (authenticationId.equalsIgnoreCase(USER_DATA_UPDATED_BY_PROVIDER[0])) {
				eid = authenticationId;
				revisedEmail = EMAIL_FOR_UPDATED_USER_EQUAL_IDS;
			} else if (authenticationId.equalsIgnoreCase(AUTHN_ID_NOT_LOCAL_EID)) {
				eid = USER_DATA_UPDATED_BY_PROVIDER[0];
				revisedEmail = EMAIL_FOR_UPDATED_USER_UNEQUAL_IDS;
			} else if (authenticationId.equalsIgnoreCase(AUTHN_ID_NOT_PROVIDER_EID)) {
				eid = USER_DATA_FROM_PROVIDER_AUTHN[0];
			} else {
				return null;
			}
			
			UserEdit user = null;
			if (eid.equalsIgnoreCase(USER_DATA_UPDATED_BY_PROVIDER[0])) {
				try {
					user = (UserEdit)userDirectoryService.getUserByEid(eid);
				} catch (UserNotDefinedException e) {
					if (log.isDebugEnabled()) log.debug("provider did not find user with eid=" + eid);
					return null;
				}
			} else if (eid.equalsIgnoreCase(USER_DATA_FROM_PROVIDER_AUTHN[0])) {
				// Mimic the case where we have user data at hand via something
				// like LDAP.
				user = userFactory.newUser();
				loadProvidedUserData(eid, user);
			}
			
			if (user.getEid().equalsIgnoreCase(USER_DATA_UPDATED_BY_PROVIDER[0])) {
				user.setEmail(revisedEmail);
			}
			
			return user;
		}

		public void setUserFactory(UserFactory userFactory) {
			this.userFactory = userFactory;
		}
	}

}
