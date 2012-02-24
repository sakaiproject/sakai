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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ConsensusLdapConnectionLivenessValidatorTest extends MockObjectTestCase {

	private ConsensusLdapConnectionLivenessValidator validator;
	private Mock mockDelegateValidator1;
	private Mock mockDelegateValidator2;
	private Mock mockDelegateValidator3;
	private LdapConnectionLivenessValidator delegateValidator1; 
	private LdapConnectionLivenessValidator delegateValidator2;
	private LdapConnectionLivenessValidator delegateValidator3;
	
	protected void setUp() {
		validator = new ConsensusLdapConnectionLivenessValidator();
		mockDelegateValidator1 = new Mock(LdapConnectionLivenessValidator.class, "mockDelegateValidator1");
		mockDelegateValidator2 = new Mock(LdapConnectionLivenessValidator.class, "mockDelegateValidator2");
		mockDelegateValidator3 = new Mock(LdapConnectionLivenessValidator.class, "mockDelegateValidator3");
		delegateValidator1 = (LdapConnectionLivenessValidator) mockDelegateValidator1.proxy();
		delegateValidator2 = (LdapConnectionLivenessValidator) mockDelegateValidator2.proxy();
		delegateValidator3 = (LdapConnectionLivenessValidator) mockDelegateValidator3.proxy();
	}
	
	public void testValidatesConnectionIfNoDelegatesRegistered() {
		PooledLDAPConnection conn = new PooledLDAPConnection();
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testValidatesConnectionIfAllDelegatesValidateConnection() {
		PooledLDAPConnection conn = new PooledLDAPConnection();
		mockDelegateValidator1.expects(once()).method("isConnectionAlive").
		    with(same(conn)).will(returnValue(true));
		mockDelegateValidator2.expects(once()).method("isConnectionAlive").
		    with(same(conn)).after(mockDelegateValidator1, "isConnectionAlive").
		    will(returnValue(true));
		mockDelegateValidator3.expects(once()).method("isConnectionAlive").
		    with(same(conn)).after(mockDelegateValidator2, "isConnectionAlive").
	        will(returnValue(true));
		List<LdapConnectionLivenessValidator> delegates = 
			new ArrayList<LdapConnectionLivenessValidator>(3);
		delegates.add(delegateValidator1);
		delegates.add(delegateValidator2);
		delegates.add(delegateValidator3);
		validator.setDelegates(delegates);
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testInvalidatesConnectionIfOneDelegateInvalidatesConnection() {
		PooledLDAPConnection conn = new PooledLDAPConnection();
		mockDelegateValidator1.expects(once()).method("isConnectionAlive").
		    with(same(conn)).will(returnValue(true));
		mockDelegateValidator2.expects(once()).method("isConnectionAlive").
		    with(same(conn)).after(mockDelegateValidator1, "isConnectionAlive").
		    will(returnValue(false));
		List<LdapConnectionLivenessValidator> delegates = 
			new ArrayList<LdapConnectionLivenessValidator>(3);
		delegates.add(delegateValidator1);
		delegates.add(delegateValidator2);
		delegates.add(delegateValidator3); // expects iteration to short-circuit
		validator.setDelegates(delegates);
		assertFalse(validator.isConnectionAlive(conn));
	}
	
	public void testTreatsNullDelegateListInjectionAsEmptyList() {
		validator.setDelegates(null);
		testValidatesConnectionIfNoDelegatesRegistered();
	}
	
}
