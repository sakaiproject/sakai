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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import java.io.UnsupportedEncodingException;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSEStartTLSFactory;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPTLSSocketFactory;

/**
 * Currently verifies a subset of {@link PooledLDAPConnectionFactory}
 * features. Specifically, is concerned with verifying fixes to
 * object validation which were causing stale connections to remain
 * in the pool indefinitely.
 * 
 * 
 * @author Dan McCallum (dmccallum@unicon.net)
 */
public class PooledLDAPConnectionFactoryTest extends MockObjectTestCase {

    private PooledLDAPConnectionFactory factory;
    private PooledLDAPConnection conn;
    private LDAPSearchResults ldapSearchResults;
    private LDAPEntry ldapEntry;
    private LdapConnectionLivenessValidator livenessValidator;
    private LdapConnectionManager connMgr;
    private LdapConnectionManagerConfig connMgrConfig;
    private Mock mockConn;
    private Mock mockLDAPSearchResults;
    private Mock mockLDAPEntry;
    private Mock mockLivenessValidator;
    private Mock mockConnMgr;
    private Mock mockConnMgrConfig;
    
    
    protected void setUp() throws Exception {
        
        mockConn = mock(PooledLDAPConnection.class, "mockConn");
        conn = (PooledLDAPConnection)mockConn.proxy();
        
        factory = new PooledLDAPConnectionFactory() {
        	@Override
        	protected PooledLDAPConnection newConnection() {
        		return conn;
        	}
        };
        
        mockLDAPSearchResults = mock(LDAPSearchResults.class,"mockLDAPSearchResults");
        ldapSearchResults = (LDAPSearchResults)mockLDAPSearchResults.proxy();
        mockLDAPEntry = mock(LDAPEntry.class, "mockLDAPEntry");
        ldapEntry = (LDAPEntry)mockLDAPEntry.proxy();
        mockLivenessValidator = new Mock(LdapConnectionLivenessValidator.class);
        livenessValidator = (LdapConnectionLivenessValidator)mockLivenessValidator.proxy();
        factory.setConnectionLivenessValidator(livenessValidator);
        mockConnMgr = new Mock(LdapConnectionManager.class);
        connMgr = (LdapConnectionManager) mockConnMgr.proxy();
        mockConnMgrConfig = new Mock(LdapConnectionManagerConfig.class);
        connMgrConfig = (LdapConnectionManagerConfig) mockConnMgrConfig.proxy();
        // don't call setConnectionManager() b/c we don't know what expectations to set
        super.setUp();
    }

    
    
    /**
     * Verifies that {@link PooledLDAPConnectionFactory} defaults
     * its {@link LdapConnectionLivenessValidator} instance at
     * construction time,
     */
    public void testDefaultsToNativeLdapConnectionLivenessValidator() {
    	Object livenessValidator = new PooledLDAPConnectionFactory().getConnectionLivenessValidator(); 
    	assertTrue("Expected a NativeLdapConnectionLivenessValidator but was [" + livenessValidator + "]", 
    			livenessValidator instanceof NativeLdapConnectionLivenessValidator);
    }

    /**
     * Verifies that {@link PooledLDAPConnectionFactory} is never
     * without a {@link LdapConnectionLivenessValidator}.
     */
    public void testDefaultsToNativeLdapConnectionLivenessValidatorIfThatPropertySetToNull() {
    	factory.setConnectionLivenessValidator(null);
    	Object livenessValidator = factory.getConnectionLivenessValidator(); 
    	assertTrue("Expected a NativeLdapConnectionLivenessValidator but was [" + livenessValidator + "]", 
    			livenessValidator instanceof NativeLdapConnectionLivenessValidator);
    }
    
    public void testMakeObjectConnectsAndOtherwiseInitializesConnections() 
    throws LDAPException, UnsupportedEncodingException {
    	
    	setConnectionManagerConfigExpectations();
    	factory.setConnectionManager(connMgr);
    	mockConn.expects(once()).method("setConnectionManager").with(same(connMgr));
    	mockConn.expects(once()).method("setConstraints").with(NOT_NULL); // TODO make more specific
    	mockConn.expects(once()).method("connect").
    		with(eq(connMgrConfig.getLdapHost()), eq(connMgrConfig.getLdapPort())).
    		after("setConstraints");
    	mockConn.expects(once()).method("startTLS").after("connect");
    	mockConn.expects(once()).method("bind").
    		with(eq(LDAPConnection.LDAP_V3), 
    				eq(connMgrConfig.getLdapUser()), 
    				eq(connMgrConfig.getLdapPassword().getBytes("UTF-8"))).after("connect");
    	mockConn.expects(once()).method("setBindAttempted").with(eq(false)).after("bind");
    	
    	// the actual code exercise
    	assertSame(conn, factory.makeObject());
    	
    	// TODO how to test socket factory assignment (static call)
    }
    
    private void setConnectionManagerConfigExpectations() 
    {
    	
    	final String LDAP_HOST = "ldap-host";
    	final int LDAP_PORT = 389;
    	final String LDAP_USER = "ldap-user";
    	final String LDAP_PASS = "ldap-pass";
    	final LDAPTLSSocketFactory LDAP_SOCKET_FACTORY = new LDAPJSSEStartTLSFactory();
    	final int LDAP_TIMEOUT = 5000;
    	final boolean LDAP_FOLLOW_REFERRALS = true;
    	
    	mockConnMgr.expects(atLeastOnce()).method("getConfig").will(returnValue(connMgrConfig));
    	mockConnMgrConfig.expects(atLeastOnce()).method("getLdapHost").will(returnValue(LDAP_HOST));
    	mockConnMgrConfig.expects(atLeastOnce()).method("getLdapPort").will(returnValue(LDAP_PORT));
    	mockConnMgrConfig.expects(atLeastOnce()).method("isAutoBind").will(returnValue(true));
    	mockConnMgrConfig.expects(atLeastOnce()).method("getLdapUser").will(returnValue(LDAP_USER));
    	mockConnMgrConfig.expects(atLeastOnce()).method("getLdapPassword").will(returnValue(LDAP_PASS));
    	mockConnMgrConfig.expects(atLeastOnce()).method("getSecureSocketFactory").will(returnValue(LDAP_SOCKET_FACTORY));
    	mockConnMgrConfig.expects(atLeastOnce()).method("isSecureConnection").will(returnValue(true));
    	mockConnMgrConfig.expects(atLeastOnce()).method("getOperationTimeout").will(returnValue(LDAP_TIMEOUT));
    	mockConnMgrConfig.expects(atLeastOnce()).method("isFollowReferrals").will(returnValue(LDAP_FOLLOW_REFERRALS));
	}

	/**
     * Verifies that {@link PooledLDAPConnectionFactory#activateObject(Object)}
     * passes constraints and an active flag to the given {@link PooledLDAPConnection}.
     * Does not actually verify constraint values.
     *
     * @throws LDAPException test error
     */
    public void testActivateObject() throws LDAPException {
        
        // TODO validate constraint assignment
        mockConn.expects(once()).method("setConstraints").with(ANYTHING);
        mockConn.expects(once()).method("setActive").with(eq(true));
        factory.activateObject(conn);
        
    }
    
    /**
     * If the client has bound the connection but the factory is not
     * running in auto-bind mode, the connection must be invalidated.
     */
    public void testValidateObjectLowersActiveFlagIfConnectionHasBeenBoundButAutoBindIsNotSet() {
    	mockConn.expects(once()).method("isBindAttempted").will(returnValue(true));
    	// we assume autoBind defaults to false (we'd rather not go through the
    	// whole process of mocking up the connection manager again)
    	mockConn.expects(once()).method("setActive").with(eq(false));
    	assertFalse(factory.validateObject(conn));
    }
    
    /**
     * Verifies that the {@link PooledLdapConnection} to be validated is re-bound
     * as the system user prior to testing the connection for liveness. This
     * is only relevant if the client has bound the connection as another user and
     * the autoBind behavior has been enabled.
     */
    public void testValidateObjectRebindsAsAutoBindUserIfNecessaryPriorToTestingConnectionLiveness() 
    throws UnsupportedEncodingException {

    	setConnectionManagerConfigExpectations();
    	factory.setConnectionManager(connMgr);
    	    	
        mockConn.expects(once()).method("isBindAttempted").will(returnValue(true));
        mockConn.expects(once()).method("bind").
			with(eq(LDAPConnection.LDAP_V3), 
				eq(connMgrConfig.getLdapUser()), 
				eq(connMgrConfig.getLdapPassword().getBytes("UTF-8"))).after("isBindAttempted");
        mockConn.expects(once()).method("setBindAttempted").with(eq(false)).after("bind");
    	
        mockLivenessValidator.expects(once()).method("isConnectionAlive").will(returnValue(true));
        factory.setConnectionLivenessValidator(livenessValidator);
        assertTrue(factory.validateObject(conn));
        
    }
    
    /**
     * Verifies that a failed rebind during connection validation marks 
     * the connection as "inactive". This ensures that the connection is 
     * not returned to the pool by {@link PooledLDAPConnection#finalize()}. 
     * Technically, a failed rebind does not mean the connection is stale 
     * but failing to bind as the system user should indicate that 
     * something is very much wrong, so reallocating a connection is not 
     * a bad idea. Certainly better than finding oneself caught in an 
     * endless loop of validating stale connections.
     *
     */
    public void testValidateObjectLowersActiveFlagIfRebindFails() 
    throws UnsupportedEncodingException {
        
    	setConnectionManagerConfigExpectations();
    	factory.setConnectionManager(connMgr);
        LDAPException bindFailure = new LDAPException();
        
        mockConn.expects(once()).method("isBindAttempted").will(returnValue(true));
        mockConn.expects(once()).method("bind").
		with(eq(LDAPConnection.LDAP_V3), 
			eq(connMgrConfig.getLdapUser()), 
			eq(connMgrConfig.getLdapPassword().getBytes("UTF-8"))).
			after("isBindAttempted").will(throwException(bindFailure));
        mockConn.expects(once()).method("setActive").with(eq(false)).after("bind");
        
        assertFalse(factory.validateObject(conn));
        
    }
    
    
    /**
     * Verifies that {@link PooledLDAPConnectionFactory#validateObject(Object)}
     * does not adjust a {@link PooledLDAPConnection}'s active flag if the
     * connection appears to be "alive". Probably overkill, but we've had problems
     * that we think may be related to stale connections being returned to the
     * pool, so we want to be sure the validate operation is doing exactly what
     * we think it should be doing. Also verifies that a {@link PooledLDAPConnection}'s 
     * search method returns a LDAPEntry.
     * 
     * @throws LDAPException
     */
    public void testValidateObjectKeepsActiveFlagUpIfConnectionIsAlive() throws LDAPException {
        
        // will fail if the factory attempts to monkey with the active flag
    	factory.setConnectionLivenessValidator(livenessValidator);
    	mockConn.expects(once()).method("isBindAttempted").will(returnValue(false));
    	mockLivenessValidator.expects(once()).method("isConnectionAlive").will(returnValue(true));
    	
    	assertTrue(factory.validateObject(conn));
        
    }
    
    /**
     * Verifies that {@link PooledLDAPConnectionFactory#validateObject(Object)} lowers
     * {@link PooledLDAPConnection}'s active flag if the current 
     * {@link LdapConnectionLivenessValidator} reports that the connection is not live. 
     * 
     * @throws LDAPException test failure
     */
    public void testValidateObjectLowersActiveFlagIfConnectionIsNotAlive() throws LDAPException {
        
    	factory.setConnectionLivenessValidator(livenessValidator);
    	mockConn.expects(once()).method("isBindAttempted").will(returnValue(false));
    	mockLivenessValidator.expects(once()).method("isConnectionAlive").
    		will(returnValue(false));
    	mockConn.expects(once()).method("setActive").with(eq(false)).
    		after(mockLivenessValidator, "isConnectionAlive");
        assertFalse(factory.validateObject(conn));
        
    }
    
    public void testValidateObjectLowersActiveFlagIfLivenessValidationThrowsException() {
    	
    	factory.setConnectionLivenessValidator(livenessValidator);
    	mockConn.expects(once()).method("isBindAttempted").will(returnValue(false));
    	mockLivenessValidator.expects(once()).method("isConnectionAlive").will(throwException(new RuntimeException("catch me")));
    	mockConn.expects(once()).method("setActive").with(eq(false)).after(mockLivenessValidator, "isConnectionAlive");
        assertFalse(factory.validateObject(conn));
  	
    }
    
    public void testInvalidatesNullObjects() {
    	
    	assertFalse(factory.validateObject(null));
    	
    }
    
    public void testDestroyObjectInvokesDisconnect() throws Exception {
    	mockConn.expects(once()).method("setActive").with(eq(false));
    	mockConn.expects(once()).method("disconnect").after("setActive");
    	factory.destroyObject(conn);
    }
    
    public void testDestroyObjectIgnoresNullReferences() {
    	try {
    		factory.destroyObject(null);
    	} catch ( Exception e ) {
    		fail("Should have ignored null reference");
    	}
    }
    
    public void testDestroyObjectIgnoresNonPoolableLdapConnections() {
    	try {
    		factory.destroyObject(mock(LDAPConnection.class).proxy());
    	} catch ( Exception e ) {
    		fail("Should have ignored non-poolable connection");
    	}
    }
        
}
