/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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
package org.sakaiproject.search.tool;


//import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

//import junit.framework.TestCase;

public class SearchBeanImplTest extends MockObjectTestCase {

	SearchBeanImpl sbi = null; 
	Mock mockSearchService = null;
	Mock mockToolManager = null;
	Mock mockPlacement = null;
	Mock mockProperties = null;
	Mock mockSecurityService = null;
	Mock mockServerConfiguration = null;
	String defaultSiteId = "defaultSiteId";
	
	protected void setUp() throws Exception {
		super.setUp();
//		sbi = new SearchBeanImpl("defaultSiteId");
		mockSearchService = mock(SearchService.class,"searchService");
		mockToolManager = mock(ToolManager.class,"toolManager");
		mockPlacement = mock(Placement.class,"placement");
		mockProperties = mock(Properties.class,"properties");
		mockSecurityService = mock(SecurityService.class,"securityService");
		mockServerConfiguration = mock(ServerConfigurationService.class, "serverconfigurationService");

		sbi = new SearchBeanImpl(defaultSiteId,(SearchService) mockSearchService.proxy(),
				"defaultSearch", (ToolManager) mockToolManager.proxy(), (SecurityService)mockSecurityService.proxy(), (ServerConfigurationService)mockServerConfiguration.proxy());
	}


	// extractPropertiesFromTool
	
	public void testExtractPropertiesFromToolPlacement() {
		mockToolManager.expects(once()).method("getCurrentPlacement").will(returnValue(mockPlacement.proxy()));
		mockPlacement.expects(once()).method("getPlacementConfig").will(returnValue(mockProperties.proxy()));
		mockProperties.expects(once()).method("isEmpty").will(returnValue(true));
		mockPlacement.expects(once()).method("getConfig").will(returnValue(mockProperties.proxy()));				
		assertNotNull(sbi.extractPropertiesFromTool());
	}
	
	public void testExtractPropertiesFromToolCurrentPlacement() {
		mockToolManager.expects(once()).method("getCurrentPlacement").will(returnValue(mockPlacement.proxy()));
		mockPlacement.expects(once()).method("getPlacementConfig").will(returnValue(mockProperties.proxy()));
		mockProperties.expects(once()).method("isEmpty").will(returnValue(false));
						
		assertNotNull(sbi.extractPropertiesFromTool());
	}
	
	// extractSiteIdsFromToolProperty
	
	public void testExtractSiteIdsFromPropertiesNoProperties() {
		mockProperties.expects(once()).method("getProperty").with(eq("search_site_ids")).will(returnValue(null));
		String [] s = sbi.extractSiteIdsFromProperties((Properties) mockProperties.proxy()); 
		assertTrue("array of one empty siteIds",s.length==1);
		assertTrue("siteId is empty","".equals(s[0]));
	}
	
	public void testExtractSiteIdsFromPropertiesEmptyProperties() {
		mockProperties.expects(once()).method("getProperty").with(eq("search_site_ids")).will(returnValue(""));
		String [] s = sbi.extractSiteIdsFromProperties((Properties) mockProperties.proxy()); 
		assertTrue("array of one empty siteIds",s.length==1);
		assertTrue("siteId is empty","".equals(s[0]));
	}
	
	public void testExtractSiteIdsFromProperties1SiteId() {
		mockProperties.expects(once()).method("getProperty").with(eq("search_site_ids")).will(returnValue("SiteIdOne"));
		String [] s = sbi.extractSiteIdsFromProperties((Properties) mockProperties.proxy()); 
		assertTrue("array of one siteid",s.length==1);
		assertTrue("siteId is default","SiteIdOne".equals(s[0]));
	}
	
	public void testExtractSiteIdsFromProperties2SiteId() {
		mockProperties.expects(once()).method("getProperty").with(eq("search_site_ids")).will(returnValue("SiteIdOne,SiteIdTwo"));
		String [] s = sbi.extractSiteIdsFromProperties((Properties) mockProperties.proxy()); 
		assertTrue("array of two siteIds",s.length==2);
		assertTrue("siteId has two entries","SiteIdOne".equals(s[0]));
		assertTrue("siteId has two entries","SiteIdTwo".equals(s[1]));
	}
	
	public void testExtractSiteIdsFromProperties2SiteIdBlanks() {
		mockProperties.expects(once()).method("getProperty").with(eq("search_site_ids")).will(returnValue(" SiteIdOne , SiteIdTwo "));
		String [] s = sbi.extractSiteIdsFromProperties((Properties) mockProperties.proxy()); 
		assertTrue("array of siteIds with blanks",s.length==2);
		assertTrue("siteId has two entries","SiteIdOne".equals(s[0]));
		assertTrue("siteId has two entries","SiteIdTwo".equals(s[1]));
	}

	public void testExtractSiteIdsFromPropertiesJustBlank() {
		mockProperties.expects(once()).method("getProperty").with(eq("search_site_ids")).will(returnValue(" "));
		String [] s = sbi.extractSiteIdsFromProperties((Properties) mockProperties.proxy()); 
		assertTrue("single blank siteid",s.length==1);
		assertTrue("siteId is empty","".equals(s[0]));
	}
	// Not tested explicitly as very simple and just calls other tested methods.
//	public void GetToolPropertySiteIds() {
	// fail("not yet tested");
//	}
	
	// If no ids from tool properties, will get only current site id
	public void testGetSearchSitesNullSiteIdsArray() {
		List l = sbi.getSearchSites(null);
		assertTrue("no property site ids",l.size()==1);
		assertEquals("default site id",defaultSiteId,l.get(0));
	}

	public void testGetSearchSitesEmptySiteIdsArray() {
		List l = sbi.getSearchSites(new String[] {});
		assertTrue("no property site ids",l.size()==1);
		assertEquals("default site id",defaultSiteId,l.get(0));
	}
	
	public void testGetSearchSitesEmptyValueSiteIdsArray() {
		List l = sbi.getSearchSites(new String[] {""});
		assertTrue("no property site ids",l.size()==1);
		assertEquals("default site id",defaultSiteId,l.get(0));
	}
	
	public void testGetSearchSitesOneValueSiteIdsArray() {
		List l = sbi.getSearchSites(new String[] {"toolProp1"});
		assertTrue("no property site ids",l.size()==2);
		assertEquals("default site id",defaultSiteId,l.get(0));
		assertEquals("toolProp1","toolProp1",l.get(1));
	}
	
	public void testGetSearchSitesTwoValueSiteIdsArray() {
		List l = sbi.getSearchSites(new String[] {"toolSiteId1","toolSiteId2"});
		assertTrue("no property site ids",l.size()==3);
		assertEquals("default site id",defaultSiteId,l.get(0));
		assertEquals("toolSiteId1","toolSiteId1",l.get(1));
		assertEquals("toolSiteId2","toolSiteId2",l.get(2));
	}
	
}

//Mock mockSubscriber = mock(Subscriber.class);
//Publisher publisher = new Publisher();
//publisher.add( (Subscriber)mockSubscriber.proxy() );
