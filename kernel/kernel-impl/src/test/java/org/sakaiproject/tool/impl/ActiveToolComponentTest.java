/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.impl.BasicConfigurationService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.thread_local.impl.ThreadLocalComponent;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Tool.AccessSecurity;

// All these tests don't have any i18n on the tool details as we don't call setResourceLoader
@RunWith(JMock.class)
public class ActiveToolComponentTest {

	private Mockery context = new JUnit4Mockery();
	private ThreadLocalComponent threadLocalComponent;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private FunctionManager functionManager;
	private BasicConfigurationService serverConfigurationService;
	private ActiveToolManager activeToolManager;

	@Before
	public void setUp() {
		// Simple so don't need to mock.
		threadLocalComponent = new ThreadLocalComponent();
		// Mock the others.
		securityService = context.mock(SecurityService.class);
		sessionManager = context.mock(SessionManager.class);
		functionManager = context.mock(FunctionManager.class);
		serverConfigurationService = new BasicConfigurationService();
		serverConfigurationService.setThreadLocalManager(threadLocalComponent);
		serverConfigurationService.setSessionManager(sessionManager);
		ActiveToolComponent activeToolComponent = new ActiveToolComponent() {
			
			@Override
			protected ThreadLocalManager threadLocalManager() {
				return threadLocalComponent;
			}
			
			@Override
			protected SecurityService securityService() {
				return securityService;
			}
			
			@Override
			protected SessionManager sessionManager() {
				return sessionManager;
			}
			
			@Override
			protected FunctionManager functionManager() {
				return functionManager;
			}
			
			@Override
			protected ServerConfigurationService serverConfigurationService() {
				return serverConfigurationService;
			}
		};
		activeToolComponent.init();
		this.activeToolManager = activeToolComponent;
	}

	@Test
	public void testSimpleToolRegistration() {
		activeToolManager .register(getClass().getResourceAsStream("site-info-original.xml"));
		Tool tool = activeToolManager.getTool("sakai.siteinfo");
		assertNotNull(tool);
		
		// The basics are ok.
		assertEquals("sakai.siteinfo", tool.getId());
		assertEquals("Site Info", tool.getTitle());
		assertEquals("For showing worksite information and site participants.", tool.getDescription());
		assertEquals(null, tool.getHome());
		
		// Got the configuration
		Properties registeredConfig = tool.getRegisteredConfig();
		assertEquals("course,project,portfolio", registeredConfig.getProperty("siteTypes"));
		assertEquals("project", registeredConfig.getProperty("defaultSiteType"));
		assertEquals("project", registeredConfig.getProperty("publicChangeableSiteTypes"));
		assertEquals("course", registeredConfig.getProperty("publicSiteTypes"));
		assertEquals("", registeredConfig.getProperty("privateSiteTypes"));
		assertEquals("siteinfo", registeredConfig.getProperty("site_mode"));
		assertEquals("site.visit", registeredConfig.getProperty("functions.require"));
		assertNull(registeredConfig.getProperty("title"));
		
		// Parsed the categories
		Set<String> categories = tool.getCategories();
		assertTrue(categories.contains("course"));
		assertTrue(categories.contains("project"));
		assertTrue(categories.contains("portfolio"));
		assertFalse(categories.contains("other"));
		
		// Check the default.
		assertEquals(AccessSecurity.PORTAL, tool.getAccessSecurity());
		
		// Check the final config
		Properties finalConfig = tool.getFinalConfig();
		assertEquals("siteinfo", finalConfig.getProperty("site_mode"));
		assertNull(finalConfig.getProperty("siteTypes"));
		
		// Check the mutable config
		Properties mutableConfig = tool.getMutableConfig();
		assertEquals("project", mutableConfig.getProperty("defaultSiteType"));
		assertNull(mutableConfig.getProperty("site_mode"));
		
		Set<String> keywords = tool.getKeywords();
		assertTrue(keywords.contains("site"));
		assertTrue(keywords.contains("setup"));
		assertFalse(keywords.contains("notfound"));
		
		// Test it can be found ok.
		assertEquals(1, activeToolManager.findTools(null, null).size());
		assertEquals(1, activeToolManager.findTools(Collections.<String>emptySet(), null).size());
		assertEquals(1, activeToolManager.findTools(Collections.singleton("project"), null).size());
		assertEquals(1, activeToolManager.findTools(null, Collections.singleton("site")).size());
		assertTrue(activeToolManager.findTools(Collections.singleton("other"), null).isEmpty());
		assertTrue(activeToolManager.findTools(null, Collections.singleton("notfound")).isEmpty());
	}
	

	@Test
	public void testReRegistration() {
		// Tests that re-registering the same tool ID works.
		assertNull(activeToolManager.getTool("simple"));
		activeToolManager .register(getClass().getResourceAsStream("simple.xml"));
		Tool tool = activeToolManager.getTool("simple");
		assertNotNull(tool);
		assertEquals("Simple", tool.getTitle());
		activeToolManager .register(getClass().getResourceAsStream("simple-alt.xml"));
		tool = activeToolManager.getTool("simple");
		assertNotNull(tool);
		assertEquals("Simple Alternative", tool.getTitle());
	}
	
	@Test
	public void testMultipleRegistrations() {
		// Check that multiple registrations in one file work.
		assertTrue(activeToolManager.findTools(null, null).isEmpty());
		activeToolManager.register(getClass().getResourceAsStream("simple-multiple.xml"));
		Set<Tool> findTools = activeToolManager.findTools(null, null);
		assertEquals(3, findTools.size());
		Tool tool1 = activeToolManager.getTool("simple1");
		assertNotNull(tool1);
		assertEquals("Simple1", tool1.getTitle());
		
		Tool tool2 = activeToolManager.getTool("simple2");
		assertNotNull(tool2);
		assertEquals("Simple2", tool2.getTitle());
		
		Tool tool3 = activeToolManager.getTool("simple3");
		assertNotNull(tool3);
		assertEquals("Simple3", tool3.getTitle());
	}
	
	@Test
	public void testExtraXml() {
		// Check that we don't fall over when extra XML is included in the file.
		activeToolManager.register(getClass().getResourceAsStream("simple-extra.xml"));
		Tool tool = activeToolManager.getTool("simple");
		assertNotNull(tool);
		assertEquals("Simple", tool.getTitle());
	}
	
	@Test
	public void testEmpty() {
		activeToolManager.register(getClass().getResourceAsStream("empty.xml"));
		assertTrue(activeToolManager.findTools(null, null).isEmpty());
	}
	
	@Test
	public void testPerformance() {
		for (int i = 0; i< 100; i++) {
			activeToolManager.register(getClass().getResourceAsStream("site-info-original.xml"));
		}
	}

	@Test
	public void testFlatmapOrdering() {
		Properties props = new Properties();
		props.put(ToolManager.TOOLCONFIG_REQUIRED_PERMISSIONS,
				"dropbox.own | dropbox.maintain | dropbox.maintain.own.groups | dropbox.delete.any | dropbox.delete.own | dropbox.write.any | dropbox.write.own");

		ToolConfiguration config = mock(ToolConfiguration.class);
		when(config.getConfig()).thenReturn(props);

		List<String> permissions = activeToolManager.getRequiredPermissions(config)
				.stream().flatMap(Collection::stream).collect(Collectors.toList());

		assertTrue("Order is maintained for item one", permissions.get(0).equals("dropbox.own"));
		assertTrue("Order is maintained for item two", permissions.get(1).equals("dropbox.maintain"));
	}

	@Test
	public void testFunctionsRequire() {

		Properties props = new Properties();
		props.put(ToolManager.TOOLCONFIG_REQUIRED_PERMISSIONS
			, "asn.new,asn.delete|asn.read,asn.submit|asn.grade");

		ToolConfiguration config = mock(ToolConfiguration.class);
		when(config.getConfig()).thenReturn(props);
		List<Set<String>> permissionSets = activeToolManager.getRequiredPermissions(config);
		permissionSets.forEach(set -> {
			set.forEach(perm -> assertFalse("Required functions should *not* contains whitespace", perm.matches("\\S*\\s\\S*")));
		});
		assertTrue("There should be 3 sets of permissions", permissionSets.size() == 3);
		assertTrue("The first set should have 2 permissions", permissionSets.get(0).size() == 2);
		assertTrue("The second set should have 2 permissions", permissionSets.get(1).size() == 2);
		assertTrue("The third set should have 1 permission", permissionSets.get(2).size() == 1);
		assertTrue("The first set should contain asn.new", permissionSets.get(0).contains("asn.new"));
		assertTrue("The first set should contain asn.delete", permissionSets.get(0).contains("asn.delete"));
		assertTrue("The second set should contain asn.read", permissionSets.get(1).contains("asn.read"));
		assertTrue("The second set should contain asn.submit", permissionSets.get(1).contains("asn.submit"));
		assertEquals("The third set should contain asn.grade", permissionSets.get(2).iterator().next(), "asn.grade");

		props.put(ToolManager.TOOLCONFIG_REQUIRED_PERMISSIONS, "asn.new,asn.delete,asn.read");
		permissionSets.forEach(set -> {
			set.forEach(perm -> assertFalse("Required functions should *not* contains whitespace", perm.matches("\\S*\\s\\S*")));
		});
		permissionSets = activeToolManager.getRequiredPermissions(config);
		assertTrue("There should be 1 set of permissions", permissionSets.size() == 1);
		assertTrue("The set should have 3 permissions", permissionSets.get(0).size() == 3);

		props.put(ToolManager.TOOLCONFIG_REQUIRED_PERMISSIONS, "asn.new | asn.delete | asn.read");
		permissionSets = activeToolManager.getRequiredPermissions(config);
		permissionSets.forEach(set -> {
			set.forEach(perm -> assertFalse("Required functions should *not* contains whitespace", perm.matches("\\S*\\s\\S*")));
		});
		assertTrue("There should be 3 sets of permissions", permissionSets.size() == 3);
		assertTrue("The first set should have 1 permission", permissionSets.get(0).size() == 1);
		assertEquals("The first set should contain asn.new", permissionSets.get(0).iterator().next(), "asn.new");
		assertEquals("The second set should contain asn.delete", permissionSets.get(1).iterator().next(), "asn.delete");
		assertEquals("The third set should contain asn.read", permissionSets.get(2).iterator().next(), "asn.read");
	}
}
