/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package edu.amc.sakai.user;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.user.api.UserEdit;
import org.springframework.util.StringUtils;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

/**
 * Has more sys.outs than normal in a unit test... mainly for the
 * benefit of UArts.
 * 
 * @author Dan McCallum, Unicon Inc
 * @author John Lewis, Unicon Inc
 *
 */
public class JLDAPDirectoryProviderIntegrationTest extends SakaiTestBase {

	public static final String AUTHENTICATABLE_USER_BEAN_NAME_CONFIG_KEY = 
		"authenticatable-user-bean-name";
	
	public static final String NON_AUTHENTICATABLE_USER_BEAN_NAME_CONFIG_KEY =
		"non-authenticatable-user-bean-name";
	
	public static final String GETTABLE_BY_EID_USER_BEAN_NAMES_CONFIG_KEY = 
		"gettable-by-eid-user-bean-names";
	
	public static final String GETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY = 
		"gettable-by-eid-user-bean-name";
	
	public static final String UNGETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY = 
		"ungettable-by-eid-user-bean-name";
	
	public static final String GETTABLE_BY_EMAIL_USER_BEAN_NAME_CONFIG_KEY = 
		"gettable-by-email-user-bean-name";
	
	public static final String UNGETTABLE_BY_EMAIL_USER_BEAN_NAME_CONFIG_KEY = 
		"ungettable-by-email-user-bean-name";

	public static final String BLACKLISTED_EID = "blacklisted-eid";

	public static final String BLACKLISTED_EID_PASSWORD = "blacklisted-password";
	
	/** Encapsulates test configuration and convenience utilities */
	private JLDAPDirectoryProviderIntegrationTestSupport support;

	protected void setUp() throws Exception {
		super.setUp();
		support = new JLDAPDirectoryProviderIntegrationTestSupport();
		support.setUp();
	}
	
	protected void tearDown() throws Exception {
		support.tearDown();
		super.tearDown();
	}

	public void testAuthenticatesValidUserCredentials() {
		
		String userBeanName = 
			support.getConfiguredValue(AUTHENTICATABLE_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub authenticatableUser = 
			support.getConfiguredUserEditStub(userBeanName);
		
		String login = authenticatableUser.getLogin();
		String pass = authenticatableUser.getPassword();
		assertTrue("Expected successful authentication", 
				support.udp.authenticateUser(login, null, pass));
	}
	
	public void testFailsToAuthenticateInvalidUserCredentials() {
		
		String userBeanName = 
			support.getConfiguredValue(NON_AUTHENTICATABLE_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub nonAuthenticatableUser = 
			support.getConfiguredUserEditStub(userBeanName);
		
		String login = nonAuthenticatableUser.getLogin();
		String pass = nonAuthenticatableUser.getPassword();
		assertFalse("Expected unsuccessful authentication", 
				support.udp.authenticateUser(login, null, pass));
		
	}
	
	public void testFailsToAuthenticateValidLoginPairedWithInvalidPassword() {
		
		String authenticatableUserBeanName = 
			support.getConfiguredValue(AUTHENTICATABLE_USER_BEAN_NAME_CONFIG_KEY);
		
		String nonAuthenticatableUserBeanName = 
			support.getConfiguredValue(NON_AUTHENTICATABLE_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub authenticatableUser = 
			support.getConfiguredUserEditStub(authenticatableUserBeanName);
		
		UserEditStub nonAuthenticatableUser = 
			support.getConfiguredUserEditStub(nonAuthenticatableUserBeanName);
		
		String login = authenticatableUser.getLogin();
		String pass = nonAuthenticatableUser.getPassword();
		assertFalse("Expected unsuccessful authentication", 
				support.udp.authenticateUser(login, null, pass));
		
	}
	
	/**
	 * Iterates over a set of configured {@link UserEditStub} objects,
	 * looks each up by eid, and compares each to the resulting
	 * {@link UserEdit} object. This is intended to give
	 * local implementations a chance to verify unique mapping
	 * strategies, e.g. {@link UserTypeMapper}.
	 * 
	 * <p>Configure {@link GETTABLE_BY_EID_USER_BEAN_NAMES_CONFIG_KEY}
	 * with a comma-delimited list of {@link UserEditStub} bean names
	 * which can resolve successfully to LDAP user entries.</p>
	 */
	public void testMapsLdapAttributesOntoSakaiUserEditInstanceWhenSearchingByEid() {
		
		String userBeanNames = 
			support.getConfiguredValue(GETTABLE_BY_EID_USER_BEAN_NAMES_CONFIG_KEY);
		
		String[] splitUserBeanNames = userBeanNames.split(",");
		for ( String userBeanName : splitUserBeanNames ) {
			UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
			String eid = expectedUser.getEid();
			UserEditStub actualUser = support.newUserEditStub(eid);
			boolean foundUser = support.udp.getUser(actualUser);
			assertTrue("Expected to find user entry by eid [eid = " + eid + 
					"][configured user bean name = " + userBeanName +"]", foundUser);
			assertEquals("UserEdit was not correctly updated [eid = " + eid + 
					"][configured user bean name =" + userBeanName +"])", expectedUser, actualUser);
		}
		
	}
	
	/**
	 * Identical to {@link #testMapsLdapAttributesOntoSakaiUserEditInstanceWhenSearchingByEid()}
	 * but exercises {@link JLDAPDirectoryProvider#getUsers(java.util.Collection)}
	 * rather than {@link JLDAPDirectoryProvider#getUser(UserEdit)}.
	 */
	public void testMapsLdapAttributesOntoCandidateSakaiUserEditInstancesPassedInBulk() {
		
		String userBeanNames = 
			support.getConfiguredValue(GETTABLE_BY_EID_USER_BEAN_NAMES_CONFIG_KEY);
		
		List expectedUsers = new ArrayList();
		List actualUsers = new ArrayList();
		String[] splitUserBeanNames = userBeanNames.split(",");
		for ( String userBeanName : splitUserBeanNames ) {
			UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
			String eid = expectedUser.getEid();
			UserEditStub actualUser = support.newUserEditStub(eid);
			expectedUsers.add(expectedUser);
			actualUsers.add(actualUser);
		}
		
		support.udp.getUsers(actualUsers);
		assertEquals(expectedUsers, actualUsers);
		
	}
	
	/**
	 * Very similar to {@link #testMapsLdapAttributesOntoCandidateSakaiUserEditInstancesPassedInBulk()}
	 * but expects to be configured with at least one user who will not resolve
	 * to an LDAP entry. Reuses {@link #GETTABLE_BY_EID_USER_BEAN_NAMES_CONFIG_KEY} and 
	 * {@link #UNGETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY} to build the list
	 * of {@link UserEdits} to pass into 
	 * {@link JLDAPDirectoryProvider#getUsers(java.util.Collection)}.
	 */
	public void testMapsLdapAttributesOntoSakaiUserEditInstancesAndRemovesReferencesToUnknownUserEditsWhenCandidateUserEditsPassedInBulk() {
		
		String gettableUserBeanNames = 
			support.getConfiguredValue(GETTABLE_BY_EID_USER_BEAN_NAMES_CONFIG_KEY);
			
		String ungettableUserBeanName =
			support.getConfiguredValue(UNGETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY);
		
		List expectedUsers = new ArrayList();
		List actualUsers = new ArrayList();
		String[] splitUserBeanNames = gettableUserBeanNames.split(",");
		for ( String userBeanName : splitUserBeanNames ) {
			UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
			String eid = expectedUser.getEid();
			UserEditStub actualUser = support.newUserEditStub(eid);
			expectedUsers.add(expectedUser);
			actualUsers.add(actualUser);
		}
		
		UserEditStub ungettableUser = support.getConfiguredUserEditStub(ungettableUserBeanName);
		actualUsers.add(ungettableUser);
		
		support.udp.getUsers(actualUsers);
		assertEquals(expectedUsers, actualUsers);
		
	}
	
	public void testLeavesUserEditUntouchedIfEidDoesNotResolveToLdapEntry() {
		
		String userBeanName = 
			support.getConfiguredValue(UNGETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub expectedUser = 
			support.getConfiguredUserEditStub(userBeanName);
		UserEditStub actualUser = 
			support.getConfiguredUserEditStub(userBeanName);
		assertNotSame("UserEditStub beans must be prototypes", expectedUser, actualUser);
		
		// we need an "empty" user with only this eid so we can verify that
		// the user is not updated
		
		boolean foundUser = support.udp.getUser(actualUser);
		assertFalse("Did not expect to find user record",foundUser);
		assertEquals("UserEdit should not have been modified", expectedUser, actualUser);
		
	}
	
	public void testConfirmsUserExistenceIfPassedValidEid() {
		
		String userBeanName = 
			support.getConfiguredValue(GETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
		String eid = expectedUser.getEid();
		assertTrue("Should have confirmed existence of user [eid = " + eid + "]",
				support.udp.userExists(eid));
		
	}
	
	public void testDeniesUserExistenceIfPassedUnrecognizedEid() {
		
		String userBeanName = 
			support.getConfiguredValue(UNGETTABLE_BY_EID_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
		String eid = expectedUser.getEid();
		assertFalse("Should have denied existence of user [eid = " + eid + "]",
				support.udp.userExists(eid));
		
	}

	public void testMapsLdapAttributesOntoSakaiUserEditInstanceWhenSearchingByEmail() {
		
		String userBeanName = 
			support.getConfiguredValue(GETTABLE_BY_EMAIL_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
		String email = expectedUser.getEmail();
		UserEditStub actualUser = support.newUserEditStub(null);
		boolean foundUser = support.udp.findUserByEmail(actualUser, email);
		assertTrue("Should have found user by email [" + email + "]", foundUser);
		assertEquals("Failed to map user attributes correctly when finding a user by email [" + email + "]",
				expectedUser, actualUser);
		
	}
	
	public void testLeavesUserEditUntouchedIfEmailDoesNotResolveToLdapEntry() {
		String userBeanName = 
			support.getConfiguredValue(UNGETTABLE_BY_EMAIL_USER_BEAN_NAME_CONFIG_KEY);
		
		UserEditStub expectedUser = support.getConfiguredUserEditStub(userBeanName);
		String email = expectedUser.getEmail();
		UserEditStub actualUser = support.newUserEditStub(null);
		expectedUser = support.newUserEditStub(null); // TODO better variable names
		boolean foundUser = support.udp.findUserByEmail(actualUser, email);
		assertFalse("Should have failed to find user by email [" + email + "]", foundUser);
		assertEquals("Should have left user attributes untouched if search by email [" + email + "] failed",
				expectedUser, actualUser);
	}
    
    /**
     * Verifies that disconnecting an LDAP connection before
     * returning it to the pool does not corrupt the pool.
     * 
     * <p>
     * TODO more valuable if we could toggle between pooled and unpooled configurations
     * </p>
     *
     */
    public void testPoolSurvivesDisconnectedConnection() throws LDAPException {
        
        if ( !(support.udp.isPooling()) ) {
            // Pooled not enabled. No point in executing this test
            return;
        }
        
        LdapConnectionManager connMgr = support.udp.getLdapConnectionManager();
        LDAPConnection conn = connMgr.getConnection();
        
        // We don't actually know that disconnect() hasn't been overridden
        // to return the conn to the pool, a la JDBC. So we cover all bases
        // by attempting both a direct disconnect() and a finalize() which
        // we fully expect to disconnect a deactivated, i.e. in-pool connection.
        conn.disconnect();
        connMgr.returnConnection(conn);
        ((PooledLDAPConnection)conn).finalize(); 
        
        // should be able to allocate an LDAP connection and pass the following simple test
        testConfirmsUserExistenceIfPassedValidEid();
        
    }
    
    public void testBlacklistedEidCannotAuthenticate() {
    	String blacklistedEid = support.getConfiguredValue(BLACKLISTED_EID);
    	String blacklistedEidPassword = support.getConfiguredValue(BLACKLISTED_EID_PASSWORD);
    	if ( !(StringUtils.hasText(blacklistedEid)) ) {
    		// no blacklisted EID configured, not able to perform test
    		return;
    	}
    	if ( support.udp.isSearchableEid(blacklistedEid) ) {
    		fail("Test configuration is not valid. JLDAPDirectoryProvider.isSearchableEid() " +
    				"reports that the configured blacklisted EID [" + blacklistedEid + 
    				"] _is_ searchable. Please set [" + BLACKLISTED_EID + "] to a blacklisted EID.");
    	}
    	if ( !(StringUtils.hasText(blacklistedEidPassword)) ) {
    		fail("Test configuration is not valid. A blacklisted EID has been configured [" + 
    				blacklistedEid + "], but a corresponding password has not. Please set [" + 
    				BLACKLISTED_EID_PASSWORD + 
    				"] to a valid password for EID [" + blacklistedEid + "]");
    	}
    	// We have no good way to guarantee that the corresponding directory entry
    	// doesn't exist, so some of this test is really taking a portion of the
    	// implementation on faith.
    	assertFalse("Should have refused to authenticate a blacklisted EID [" + blacklistedEid + "]",
    			support.udp.authenticateUser(blacklistedEid, null, blacklistedEidPassword));
    }
    
    public void testBlacklistedEidCannotRetrieveAttributes() {
    	// most of this is boilerplate copied from testBlacklistedEidCannotAuthenticate()
    	String blacklistedEid = support.getConfiguredValue(BLACKLISTED_EID);
    	String blacklistedEidPassword = support.getConfiguredValue(BLACKLISTED_EID_PASSWORD);
    	if ( !(StringUtils.hasText(blacklistedEid)) ) {
    		// no blacklisted EID configured, not able to perform test
    		return;
    	}
    	if ( support.udp.isSearchableEid(blacklistedEid) ) {
    		fail("Test configuration is not valid. JLDAPDirectoryProvider.isSearchableEid() " +
    				"reports that the configured blacklisted EID [" + blacklistedEid + 
    				"] _is_ searchable. Please set [" + BLACKLISTED_EID + "] to a blacklisted EID.");
    	}
    	if ( !(StringUtils.hasText(blacklistedEidPassword)) ) {
    		fail("Test configuration is not valid. A blacklisted EID has been configured [" + 
    				blacklistedEid + "], but a corresponding password has not. Please set [" + 
    				BLACKLISTED_EID_PASSWORD + 
    				"] to a valid password for EID [" + blacklistedEid + "]");
    	}
    	// we expect the stub to remain effectively "empty" except for the EID field,
    	// hence the two instances here for asserting unchanged state
    	UserEditStub expectedEdit = support.newUserEditStub(blacklistedEid);
    	UserEditStub actualEdit = support.newUserEditStub(blacklistedEid);
    	assertFalse(support.udp.getUser(actualEdit));
    	assertEquals("Expected the search to short-circuit and for the passed UserEdit to consequently remain unchanged", 
    				expectedEdit, actualEdit);
    }
	

}
