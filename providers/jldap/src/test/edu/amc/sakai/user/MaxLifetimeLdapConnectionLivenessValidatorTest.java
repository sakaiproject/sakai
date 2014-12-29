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

import java.util.Calendar;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import com.novell.ldap.LDAPConnection;

public class MaxLifetimeLdapConnectionLivenessValidatorTest extends MockObjectTestCase {

	private MaxLifetimeLdapConnectionLivenessValidator validator;
	private Mock mockConn;
	private PooledLDAPConnection conn;
	
	protected void setUp() {
		validator = new MaxLifetimeLdapConnectionLivenessValidator();
		mockConn = mock(PooledLDAPConnection.class);
		conn = (PooledLDAPConnection) mockConn.proxy();
	}
	
	public void testInvalidatesLongLivedConnection() {
		final long ONE_HOUR = 1000 * 60 * 60; 
		validator.setMaxTtl(ONE_HOUR);
		Calendar connBirthdateCalendar = Calendar.getInstance();
		connBirthdateCalendar.add(Calendar.HOUR_OF_DAY, -2);
		final long CONN_BIRTHDATE = connBirthdateCalendar.getTimeInMillis();
		mockConn.expects(once()).method("getBirthdate").will(returnValue(CONN_BIRTHDATE));
		assertFalse(validator.isConnectionAlive(conn));
	}
	
	public void testTreatsZeroTtlAsInfinite() {
		Calendar epoch = Calendar.getInstance();
		epoch.setTimeInMillis(0L); // highly unlikely this will fall within the TTL
		validator.setMaxTtl(0);
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testTreatsNegativeTtlAsInfinite() {
		Calendar epoch = Calendar.getInstance();
		epoch.setTimeInMillis(0L); // highly unlikely this will fall within the TTL
		validator.setMaxTtl(-1);
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testDefaultsToInifiniteTtl() {
		Calendar epoch = Calendar.getInstance();
		epoch.setTimeInMillis(0L); // highly unlikely this will fall within the TTL
		validator.setMaxTtl(-1);
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testAlwaysValidatesUnpooledLDAPConnections() {
		LDAPConnection conn = new LDAPConnection();
		assertTrue(validator.isConnectionAlive(conn));
	}
	
}
