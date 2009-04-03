/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mock.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.mock.domain.Placement;
import org.sakaiproject.mock.domain.Site;
import org.sakaiproject.mock.domain.Tool;
import org.sakaiproject.mock.domain.User;
import org.sakaiproject.mock.service.SiteService;
import org.sakaiproject.mock.service.ToolManager;
import org.sakaiproject.mock.service.UserDirectoryService;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ExampleSakaiCodeTest extends TestCase {
	private static final Log log = LogFactory.getLog(ExampleSakaiCodeTest.class);
	
	UserDirectoryService uds;
	SiteService siteService;
	ToolManager toolManager;
	ExampleSakaiCode exampleSakaiCode;
	
	public void setUp() {
		log.info("starting setUp()");

		uds = new UserDirectoryService();
		siteService = new SiteService();

		// Create a "current" user
		User currentUser = new User();
		currentUser.setDisplayName("My User");
		uds.setCurrentUser(currentUser);
		
		// Create a site
		String context = "foo";
		Site mySite = siteService.addSite(context, "project");
		mySite.setTitle("Foo Site Title");
		
		// Set the current placement
		Tool tool = new Tool();
		Placement placement = new Placement(tool, context);
		toolManager = new ToolManager(placement);
		
		// Build the service/tool impl that we're testing
		exampleSakaiCode = new ExampleSakaiCode();
		exampleSakaiCode.setSiteService(siteService);
		exampleSakaiCode.setToolManager(toolManager);
		exampleSakaiCode.setUds(uds);

		log.info("finished setUp()");
	}
	
	public void tearDown() {
		log.info("tearDown()");
	}
	
	public void testGetStringStartingWithContext() {
		String myString = exampleSakaiCode.getStringStartingWithContext();
		Assert.assertTrue(myString.startsWith("foo"));
	}
	
	public void testGetStringStartingWithSiteTitle() {
		String myString = exampleSakaiCode.getStringStartingWithSiteTitle();
		Assert.assertTrue(myString.startsWith("Foo Site Title"));
	}
	
	public void testGetStringStartingWithCurrentUserDisplayName() {
		String myString = exampleSakaiCode.getStringStartingWithCurrentUserDisplayName();
		Assert.assertTrue(myString.startsWith("My User"));
	}
}
