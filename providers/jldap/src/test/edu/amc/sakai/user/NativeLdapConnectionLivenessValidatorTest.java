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

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;


public class NativeLdapConnectionLivenessValidatorTest extends MockObjectTestCase {

	private NativeLdapConnectionLivenessValidator validator;
	private Mock mockConn;
	private LDAPConnection conn;
	
	protected void setUp() {
		validator = new NativeLdapConnectionLivenessValidator();
		mockConn = mock(PooledLDAPConnection.class, "mockConn");
        conn = (PooledLDAPConnection)mockConn.proxy();    
	}
	
	public void testDelegatesLivenessTestToConnectionsOwnLivenessTest() {
		mockConn.expects(once()).method("isConnectionAlive").will(returnValue(true));
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testDelegatesLivenessTestToConnectionsOwnLivenessTest_Negative() {
		mockConn.expects(once()).method("isConnectionAlive").will(returnValue(false));
		assertFalse(validator.isConnectionAlive(conn));
	}
	
	
}
