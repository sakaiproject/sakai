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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(JMock.class)
// A set of tests that check the returned title of the page.
// Must be in the same package as the class it's testing.
public class BaseSitePageTitleTest {

	private Mockery context = new JUnit4Mockery() {{
		setImposteriser(ClassImposteriser.INSTANCE); // Needed to mock an abstract class.
	}};
	private BaseSiteService service;
	private Storage storage;
	private IdManager idManager = new UuidV4IdComponent();
	private ServerConfigurationService serverConfigurationService;
	private ActiveToolManager activeToolManager;
	private BaseSitePage page;

	@Before
	public void setUp() {
		serverConfigurationService = context.mock(ServerConfigurationService.class);
		activeToolManager = context.mock(ActiveToolManager.class);
		service = context.mock(BaseSiteService.class);
		storage = context.mock(Storage.class);
		context.checking(new Expectations() {
			{
				ignoring(service).storage();
				will(returnValue(storage));
				
				ignoring(service).idManager();
				will(returnValue(idManager));
				
				ignoring(service).serverConfigurationService();
				will(returnValue(serverConfigurationService));
				
				ignoring(service).activeToolManager();
				will(returnValue(activeToolManager));
				
				ignoring(storage).readPageProperties(with(any(SitePage.class)), with(any(ResourcePropertiesEdit.class)));
				ignoring(storage).readPageTools(with(any(SitePage.class)), with(any(ResourceVector.class)));

				oneOf(serverConfigurationService).getStrings("site.tool.custom.titles");
				will(returnValue(null));
			}
		});
		page = new BaseSitePage(service, "pageId", "Page Title", "0,0", false, "siteId", "skin");
	}
	
	@Test
	public void testNoTool() {
		context.checking(new Expectations() {
			{
				oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
				will(returnValue(true));
			}
		});
		// Without any tools on the page.
		assertEquals("Page Title", page.getTitle());
	}
	
	@Test
	public void testMissingTool() {
		
		// With a non-existent tool on the page.
		context.checking(new Expectations() {
			{
				oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
				will(returnValue(true));
				one(activeToolManager).getTool("toolId");
				will(returnValue(null));
			}
		});
		page.addTool("toolId");
		assertEquals("Page Title", page.getTitle());
	}
	
	@Test
	public void testToolTitle() {
		// With a known tool on the page.
		context.checking(new Expectations() {{
			oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
			will(returnValue(true));
			
			one(activeToolManager).getTool("toolId");
			ToolImpl result = new ToolImpl(activeToolManager);
			result.setId("toolId");
			result.setTitle("Tool Title");
			will(returnValue(result));
			
			one(activeToolManager).getLocalizedToolProperty("toolId", "title");
			will(returnValue(null));
		}});
		page.addTool("toolId");
		assertEquals("Tool Title", page.getTitle());
	}
	
	@Test
	public void testLocalizedTitle() {
		// With a tool localized tool title.
		context.checking(new Expectations() {{
			oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
			will(returnValue(true));
			
			one(activeToolManager).getTool("toolId");
			ToolImpl result = new ToolImpl(activeToolManager);
			result.setId("toolId");
			result.setTitle("Tool Title");
			will(returnValue(result));
			
			one(activeToolManager).getLocalizedToolProperty("toolId", "title");
			will(returnValue("Localized Title"));
		}});
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
		context.checking(new Expectations() {{
			oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
			will(returnValue(true));
			one(activeToolManager).getLocalizedToolProperty("sakai.home", "title");
			will(returnValue(null));
		}});
		page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, "true");
		page.addTool("toolId");
		assertEquals("Page Title", page.getTitle());
	}

	@Test
	public void testHomePage() {
		// The page being marked as a home page, but no localized tool property
		context.checking(new Expectations() {{
			oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
			will(returnValue(true));
			one(activeToolManager).getLocalizedToolProperty("sakai.home", "title");
			will(returnValue("Localized Home Title"));
		}});
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
		context.checking(new Expectations() {{
			oneOf(serverConfigurationService).getBoolean("legacyPageTitleCustom", true);
			will(returnValue(true));
			one(activeToolManager).getLocalizedToolProperty("sakai.home", "title");
			will(returnValue("Localized Home Title"));
		}});
		page.getPropertiesEdit().addProperty(SitePage.IS_HOME_PAGE, "true");
		page.addTool("sakai.iframe");
		assertEquals("Localized Home Title", page.getTitle());
	}
}
