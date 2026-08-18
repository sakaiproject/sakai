/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Integration-style tests for {@link LTIService} tool API function persistence.
 */
@ContextConfiguration(classes = { LtiTestConfiguration.class })
public class LTIServiceToolPermissionTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String ADMIN_SITE = LTIService.ADMIN_SITE;
	private static final String REGULAR_SITE = "site.example.com";

	@Autowired
	private FunctionManager functionManager;

	@Autowired
	private LTIService ltiService;

	@Autowired
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	private SiteService siteService;

	@Before
	public void setUp() {

		reset(siteService);
		reset(functionManager);
		reset(serverConfigurationService);
	}

	@Test
	public void setAndGetToolPermissions_roundTrip() {
		Long toolId = insertTestTool();
		Set<String> requested = Set.of("content.read", "gradebook.write");

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
		when(functionManager.getRegisteredFunctions()).thenReturn(List.of("content.read", "gradebook.write"));

		try {
			ltiService.setToolPermissions(toolId, requested, ADMIN_SITE);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED, LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
		  .thenReturn(true);

		Set<String> stored = ltiService.getToolPermissions(toolId, ADMIN_SITE);
		assertEquals(2, stored.size());
		assertTrue(stored.contains("content.read"));
		assertTrue(stored.contains("gradebook.write"));
	}

	@Test
	public void setToolPermissions_replacesExisting() {
		Long toolId = insertTestTool();

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
		when(functionManager.getRegisteredFunctions()).thenReturn(List.of("content.read", "gradebook.read"));

		try {
			ltiService.setToolPermissions(toolId, new HashSet<>(Arrays.asList("content.read")), ADMIN_SITE);
			ltiService.setToolPermissions(toolId, new HashSet<>(Arrays.asList("gradebook.read")), ADMIN_SITE);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED, LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
		  .thenReturn(true);

		Set<String> stored = ltiService.getToolPermissions(toolId, ADMIN_SITE);
		assertEquals(1, stored.size());
		assertEquals("gradebook.read", stored.iterator().next());
	}

	@Test
	public void setToolPermissions_ignoresUnregisteredFunctions() {
		Long toolId = insertTestTool();

		when(functionManager.getRegisteredFunctions()).thenReturn(List.of("content.read", "gradebook.read"));

		Set<String> requested = Set.of("content.read", "not.a.real.function");

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
		when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED, LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
		  .thenReturn(true);

		try {
			ltiService.setToolPermissions(toolId, requested, ADMIN_SITE);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		Set<String> stored = ltiService.getToolPermissions(toolId, ADMIN_SITE);
		assertEquals(1, stored.size());
		assertEquals("content.read", stored.iterator().next());
	}

	@Test
	public void setToolPermissions_emptySetClearsAll() {
		Long toolId = insertTestTool();

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);

		try {
			ltiService.setToolPermissions(toolId, new HashSet<>(Arrays.asList("content.read", "content.write")), ADMIN_SITE);
			ltiService.setToolPermissions(toolId, new HashSet<>(), ADMIN_SITE);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(ltiService.getToolPermissions(toolId, ADMIN_SITE).isEmpty());
	}

	@Test
	public void getToolPermissions_nonAdminSiteReturnsEmpty() {
		Long toolId = insertTestTool();

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);

		try {
			ltiService.setToolPermissions(toolId, new HashSet<>(Arrays.asList("content.read")), ADMIN_SITE);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(ltiService.getToolPermissions(toolId, REGULAR_SITE).isEmpty());
	}

	@Test
	public void setToolPermissions_nonAdminSiteNotAuthorized() {
		Long toolId = insertTestTool();

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);

		Exception error = assertThrows(Exception.class, () -> ltiService.setToolPermissions(toolId, Set.of("content.read"), REGULAR_SITE));

		assertEquals("Not authorized", error.getMessage());
		assertTrue(ltiService.getToolPermissions(toolId, ADMIN_SITE).isEmpty());
	}

	@Test
	public void setToolPermissions_unknownToolThrowsException() {
		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
		Exception error = assertThrows(Exception.class, () -> ltiService.setToolPermissions(99999L, Set.of("content.read"), ADMIN_SITE));
		assertEquals("Tool not found", error.getMessage());
	}

	@Test
	public void deleteToolPermissionsForToolId_removesAllRows() {
		Long toolId = insertTestTool();

		when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
		when(functionManager.getRegisteredFunctions()).thenReturn(List.of("content.read", "gradebook.read"));

		try {
			ltiService.setToolPermissions(toolId, Set.of("content.read", "gradebook.read"), ADMIN_SITE);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		ltiService.deleteToolPermissions(toolId);

		assertTrue(ltiService.getToolPermissions(toolId, ADMIN_SITE).isEmpty());
	}

	private Long insertTestTool() {
		Properties toolProps = new Properties();
		toolProps.setProperty(LTIService.LTI_TITLE, "API Permission Test Tool");
		toolProps.setProperty(LTIService.LTI_LAUNCH, "https://example.com/lti/launch");
		Long toolId = (Long) ltiService.insertTool(toolProps, ADMIN_SITE);
		assertNotNull(toolId);
		assertTrue(toolId instanceof Long);
		return toolId;
	}
}
