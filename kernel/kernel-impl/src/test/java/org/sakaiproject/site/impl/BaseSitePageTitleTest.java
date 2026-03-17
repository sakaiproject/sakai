/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.site.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.id.impl.UuidV4IdComponent;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.impl.BaseSitePage;
import org.sakaiproject.site.impl.BaseSiteService;
import org.sakaiproject.site.impl.BaseSiteService.Storage;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.impl.ToolImpl;

@RunWith(MockitoJUnitRunner.class)
// A set of tests that check the returned title of the page.
// Must be in the same package as the class it's testing.
public class BaseSitePageTitleTest {

	@Mock private BaseSiteService service;
	@Mock private Storage storage;
	@Mock private ServerConfigurationService serverConfigurationService;
	@Mock private ActiveToolManager activeToolManager;

	private IdManager idManager = new UuidV4IdComponent();
	private BaseSitePage page;

	@Before
	public void setUp() {
		service.serverConfigurationService = serverConfigurationService;
		service.activeToolManager = activeToolManager;
		service.idManager = idManager;

		when(service.storage()).thenReturn(storage);
		when(serverConfigurationService.getStrings("site.tool.custom.titles")).thenReturn(null);

		page = new BaseSitePage(service, "pageId", "Page Title", "0,0", false, "siteId", "skin");
	}
	
	@Test
	public void testNoTool() {
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);

		// Without any tools on the page.
		assertEquals("Page Title", page.getTitle());
	}
	
	@Test
	public void testMissingTool() {
		// With a non-existent tool on the page.
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);
		when(activeToolManager.getTool("toolId")).thenReturn(null);

		page.addTool("toolId");
		assertEquals("Page Title", page.getTitle());
	}
	
	@Test
	public void testToolTitle() {
		// With a known tool on the page.
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);

		ToolImpl result = new ToolImpl(activeToolManager);
		result.setId("toolId");
		result.setTitle("Tool Title");
		when(activeToolManager.getTool("toolId")).thenReturn(result);

		when(activeToolManager.getLocalizedToolProperty("toolId", "title")).thenReturn(null);

		page.addTool("toolId");
		assertEquals("Tool Title", page.getTitle());
	}
	
	@Test
	public void testLocalizedTitle() {
		// With a tool localized tool title.
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);

		ToolImpl result = new ToolImpl(activeToolManager);
		result.setId("toolId");
		result.setTitle("Tool Title");
		when(activeToolManager.getTool("toolId")).thenReturn(result);

		when(activeToolManager.getLocalizedToolProperty("toolId", "title")).thenReturn("Localized Title");

		page.addTool("toolId");
		assertEquals("Localized Title", page.getTitle());
	}
	
	@Test
	public void testCustomTitle() {
		// With a custom title property set.
		page.setTitleCustom(true);
		page.addTool("toolId");
		assertEquals("Page Title", page.getTitle());
	}
	
	@Test
	public void testHomePageMissingProperty() {
		// The page being marked as a home page, but no localized tool property
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);
		when(activeToolManager.getLocalizedToolProperty("sakai.home", "title")).thenReturn(null);

		page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, "true");
		page.addTool("toolId");
		assertEquals("Page Title", page.getTitle());
	}

	@Test
	public void testHomePage() {
		// The page being marked as a home page, but no localized tool property
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);
		when(activeToolManager.getLocalizedToolProperty("sakai.home", "title")).thenReturn("Localized Home Title");

		page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, "true");
		page.addTool("toolId");
		assertEquals("Localized Home Title", page.getTitle());
	}
	
	@Test
	public void testHomePageCustom() {
		// The page being marked as a home page, but also has a custom title.
		page.setTitleCustom(true);
		page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, "true");
		page.addTool("toolId");
		assertEquals("Page Title", page.getTitle());
	}

	@Test
	public void testHomePageWithIFrameTool() {
		// The page being marked as a home page, but has it's first tool as an iframe
		// still gets the Home Tool title.
		when(serverConfigurationService.getBoolean("legacyPageTitleCustom", true)).thenReturn(true);
		when(activeToolManager.getLocalizedToolProperty("sakai.home", "title")).thenReturn("Localized Home Title");

		page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, "true");
		page.addTool("sakai.iframe");
		assertEquals("Localized Home Title", page.getTitle());
	}
}
