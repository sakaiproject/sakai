/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.service.legacy.user.UserDirectoryService;
import org.sakaiproject.service.legacy.user.UserEdit;
import org.sakaiproject.test.SakaiTestBase;

public class SectionAwarenessImplTest extends SakaiTestBase {
	private static final Log log = LogFactory.getLog(SectionAwarenessImplTest.class);
	
	// Services
	private SectionAwareness sectionAwareness;
	private SiteService siteService;
	private UserDirectoryService userDirService;

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
		// Set username as admin
		setUser("admin");

		// Fetch the services we need to run the tests
		sectionAwareness = (SectionAwareness)getService("org.sakaiproject.api.section.SectionAwareness");
		siteService = (SiteService)getService("org.sakaiproject.service.legacy.site.SiteService");
		userDirService = (UserDirectoryService)getService("org.sakaiproject.service.legacy.user.UserDirectoryService");

		// Create some users
		userDirService.addUser("test.user.1", "Jane", "Doe", "jd@foo.com", "123", null, null);
		userDirService.addUser("test.user.2", "Joe", "Schmoe", "js@foo.com", "123", null, null);

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
		UserEdit user1 = userDirService.editUser("test.user.1");
		userDirService.removeUser(user1);
		
		UserEdit user2 = userDirService.editUser("test.user.2");
		userDirService.removeUser(user2);		
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


