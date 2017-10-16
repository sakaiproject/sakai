/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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
package org.sakaiproject.mailsender.logic.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

@RunWith(MockitoJUnitRunner.class)
public class ComposeLogicImplTest {
	static final String REALM_ID = "composeLogicTest";
	static final String SITE_ID = "testSiteId";

	@Mock SiteService siteService;
	@Mock Site site;
	@Mock AuthzGroupService authzGroupService;
	@Mock UserDirectoryService userDirectoryService;
	@Mock ExternalLogic externalLogic;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) ToolManager toolManager;
	@Mock AuthzGroup authzGroup;
	@Mock ServerConfigurationService serverConfigurationService;

	ComposeLogicImpl impl;

	@Before
	public void setUp() throws Exception {
		// setup roles
		HashSet<Role> roles = new HashSet<Role>();
		Role firstRole = Mockito.mock(Role.class);
		when(firstRole.getId()).thenReturn("firstRole");
		roles.add(firstRole);

		Role secondRole = Mockito.mock(Role.class);
		when(secondRole.getId()).thenReturn("Second Role");
		roles.add(secondRole);
		when(authzGroup.getRoles()).thenReturn(roles);

		// setup server configuration service
		when(serverConfigurationService.getString(isA(String.class), eq(""))).thenReturn("");

		// setup realm and authzgroup for realm
		when(externalLogic.getSiteRealmID()).thenReturn(REALM_ID);
		when(authzGroupService.getAuthzGroup(REALM_ID)).thenReturn(authzGroup);

		// setup the tool manager
		Properties props = new Properties();
		when(toolManager.getCurrentPlacement().getPlacementConfig()).thenReturn(props);

		// setup site service
		when(externalLogic.getSiteID()).thenReturn(SITE_ID);
		when(siteService.getSite(anyString())).thenReturn(site);

		// setup site
		when(site.getGroups()).thenReturn(Collections.EMPTY_SET);

		// setup compose logic
		impl = new ComposeLogicImpl();
		impl.setAuthzGroupService(authzGroupService);
		impl.setExternalLogic(externalLogic);
		impl.setSiteService(siteService);
		impl.setServerConfigurationService(serverConfigurationService);
		impl.setToolManager(toolManager);
		impl.setUserDirectoryService(userDirectoryService);
	}

	@Test
	public void ignoreOneRole() throws Exception {
		impl.setIgnoreRoles("firstRole");
		List<EmailRole> roles = impl.getEmailRoles();
		assertNotNull(roles);
		assertEquals(1, roles.size());
		assertEquals("Second Role", roles.get(0).getRoleId());

		impl.setIgnoreRoles("Second Role");
		roles = impl.getEmailRoles();
		assertNotNull(roles);
		assertEquals(1, roles.size());
		assertEquals("firstRole", roles.get(0).getRoleId());
	}

	@Test
	public void ignoreAllRoles() throws Exception {
		impl.setIgnoreRoles("firstRole, Second Role");
		List<EmailRole> roles = impl.getEmailRoles();
		assertNotNull(roles);
		assertEquals(0, roles.size());
	}

	@Test
	public void emptyEmailGroups() throws Exception {
		List<EmailRole> groups = impl.getEmailGroups();
		assertNotNull(groups);
		assertEquals(0, groups.size());
	}

	@Test
	public void emptyEmailSections() throws Exception {
		List<EmailRole> groups = impl.getEmailSections();
		assertNotNull(groups);
		assertEquals(0, groups.size());
	}
}
