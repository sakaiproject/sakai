/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.user.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.impl.BaseUserDirectoryService.BaseUserEdit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Matthew Buckett
 */
public class BaseUserEqualsTest {

	private BaseUserDirectoryService service;

	@Before
	public void setUp() {
		final TimeService timeService = mock(TimeService.class);
		when(timeService.newTime()).thenReturn(Mockito.<Time>mock(Time.class));
		final SessionManager sessionManager = mock(SessionManager.class);
		when(sessionManager.getCurrentSessionUserId()).thenReturn("userId");
		service = new ConcreteUserDirectoryService(){
			protected TimeService timeService() {
				return timeService;
			}
			protected SessionManager sessionManager() {
				return sessionManager;
			}
		};
	}

	protected void assertSymetric(Object o1, Object o2, boolean same) {
		assertEquals("Checking :"+ o1.toString()+ " equals "+ o2.toString(), same, o1.equals(o2));
		assertEquals("Checking :"+ o2.toString()+ " equals "+ o1.toString(), same, o2.equals(o1));
	}

	private BaseUserEdit newUser(String id, String eid) {
		return service.new BaseUserEdit(id, eid);
	}

	@Test
	public void testSameObject() {
		BaseUserEdit user = newUser("id", "eid");
		assertSymetric(user, user, true);
		user = newUser(null, null);
		assertSymetric(user, user, true);
	}

	@Test
	public void testEqualId() {
		BaseUserEdit user1 = newUser("id", "eid");
		BaseUserEdit user2 = newUser("id", "eid");
		assertSymetric(user1, user2, true);
	}

	@Test
	public void testDiffId() {
		// Slightly contrived, but if ID doesn't match it shouldn't matter about eid.
		BaseUserEdit user1 = newUser("id1", "eid");
		BaseUserEdit user2 = newUser("id2", "eid");
		assertSymetric(user1, user2, false);
	}

	@Test
	public void testEqualIdNoEid() {
		BaseUserEdit user1 = newUser("id", null);
		BaseUserEdit user2 = newUser("id", null);
		assertSymetric(user1, user2, true);
	}

	@Test
	public void testEqualsNoId() {
		BaseUserEdit user1 = newUser("id", "eid");
		BaseUserEdit user2 = newUser(null, "eid");
		assertSymetric(user1, user2, false);
	}

	@Test
	public void testEqualEid() {
		BaseUserEdit user1 = newUser(null, "eid");
		BaseUserEdit user2 = newUser(null, "eid");
		assertSymetric(user1, user2, true);
	}

	@Test
	public void testDiffEid() {
		BaseUserEdit user1 = newUser(null, "eid1");
		BaseUserEdit user2 = newUser(null, "eid2");
		assertSymetric(user1, user2, false);
	}

	@Test
	public void testNullIds() {
		BaseUserEdit user1 = newUser(null, null);
		BaseUserEdit user2 = newUser(null, null);
		assertSymetric(user1, user2, true);
	}

}
