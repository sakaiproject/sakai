/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.test.section;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

public class SectionAwarenessImplTest extends SakaiTestBase {
	private static final Log log = LogFactory.getLog(SectionAwarenessImplTest.class);
	
	// Services
	private SectionAwareness sectionAwareness;
	private SiteService siteService;
	private UserDirectoryService userDirService;
	private SessionManager sessionManager;

	// Shared fields needed in each test (and initialized in setUp())
	private Site site;
	private Group group1;
	
	// Constants
	private static final String GROUP1_TITLE = "group1";
	
	/**
	 * Runs only once for this TestCase, so we can keep the same component manager
	 * rather than rebuilding it for each test.
	 * 
	 * @return
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(SectionAwarenessImplTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	/**
	 * Setup test fixture (runs once for each test method called)
	 */
	public void setUp() throws Exception {
		// Fetch the services we need to run the tests
		sectionAwareness = (SectionAwareness)getService(SectionAwareness.class.getName());
		siteService = (SiteService)getService(SiteService.class.getName());
		userDirService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		sessionManager = (SessionManager)getService(SessionManager.class.getName());

		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");
		session.setUserEid("admin");

		// Create some users
		userDirService.addUser("test.user.a", "test.user.a", "Jane", "Doe", "jd@foo.com", "123", null, null);
		userDirService.addUser("test.user.b", "test.user.b", "Joe", "Schmoe", "js@foo.com", "123", null, null);

		// Create a site
		site = siteService.addSite(generateSiteId(), "course");
		
		// Create a group for SectionAwareness to, er, become aware of
		group1 = site.addGroup();
		group1.setTitle(GROUP1_TITLE);
				
		// Save the group
		siteService.save(site);
		
		site.addMember("test.user.1", "Student", true, false);

		// Save the site and its new member
		siteService.save(site);

		// Add a user to a group
		group1.addMember("test.user.1", "Student", true, false);
		
		// Save the group with its new member
		siteService.saveGroupMembership(site);
	}
	
	/**
	 * Remove the newly created objects, so we can run more tests with a clean slate.
	 */
	public void tearDown() throws Exception {
		// Remove the site (along with its groups)
		siteService.removeSite(site);
		
		// Remove the users
		UserEdit user1 = userDirService.editUser("test.user.a");
		userDirService.removeUser(user1);
		
		UserEdit user2 = userDirService.editUser("test.user.b");
		userDirService.removeUser(user2);
		
		sessionManager.getCurrentSession().invalidate();
	}
	
	public void testGetSectionList() throws Exception {
		// Ensure that section awareness sees the group
		List sections = sectionAwareness.getSections(site.getId());
		Assert.assertEquals(sections.size(),1);
		Assert.assertTrue(((CourseSection)sections.get(0)).getTitle().equals(GROUP1_TITLE));
	}
	
	public void testGetSectionMembership() throws Exception {
		String groupRef = group1.getReference();
		
		List members = sectionAwareness.getSectionMembers(groupRef);
		
		Assert.assertTrue(members.size() == 1);
		ParticipationRecord record = (ParticipationRecord)members.get(0);
		Assert.assertTrue(record.getUser().getUserUid().equals("test.user.1"));
	}
}


