/**
 * Copyright (c) 2006-2014 The Apereo Foundation
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
package org.sakaiproject.coursemanagement.impl.provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

// This is a test of getUserRolesForGroup

@RunWith(MockitoJUnitRunner.class)
public class CourseManagementGroupProviderTest {

	private CourseManagementGroupProvider courseManagementGroupProvider;

	@Mock
	private RoleResolver roleResolver;

	@Mock
	private CourseManagementService cm;

	@Mock
	private Section section1;
	private Map<String, String> section1UserRoles = new HashMap<String, String>();
	{
		section1UserRoles.put("user1", "maintain");
		section1UserRoles.put("user2", "access");
	}

	@Mock
	private Section section2;
	private Map<String, String> section2UserRoles = new HashMap<String, String>();
	{
		section2UserRoles.put("user1", "access");
		section2UserRoles.put("user2", "maintain");
		section2UserRoles.put("user3", "access");
		section2UserRoles.put("user4", null);
	}

	private final static String PACKED_ID = "section1"+ CourseManagementGroupProvider.EID_SEPARATOR+ "section2";

	@Before
	public void setUp() {
		courseManagementGroupProvider = new CourseManagementGroupProvider();
		courseManagementGroupProvider.setCmService(cm);
		courseManagementGroupProvider.setRoleResolvers(Collections.singletonList(roleResolver));
		// Set the roles we prefer, earlier roles are better.
		courseManagementGroupProvider.setRolePreferences(Arrays.asList(new String[]{"maintain", "access"}));

		when(roleResolver.getUserRoles(cm, section1)).thenReturn(section1UserRoles);
		when(roleResolver.getUserRoles(cm, section2)).thenReturn(section2UserRoles);

		when(cm.getSection("section1")).thenReturn(section1);
		when(cm.getSection("section2")).thenReturn(section2);

	}

	@Test
	public void testgetUserRolesForGroup() {
		Map<String, String> userRolesForGroup = courseManagementGroupProvider.getUserRolesForGroup(PACKED_ID);
		assertNotNull(userRolesForGroup);
		assertEquals(3, userRolesForGroup.size());
		// user1 is in both sections, but it a lower role in the second section. Check that the preferred role
		// wins out.
		assertEquals("maintain", userRolesForGroup.get("user1"));
		assertEquals("maintain", userRolesForGroup.get("user2"));
		assertEquals("access", userRolesForGroup.get("user3"));
		// user4 has a null role, so check we didn't get this one.
		assertNull(userRolesForGroup.get("user4"));
	}
}
