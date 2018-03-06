/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.test.section;

import junit.framework.Assert;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.section.sakai.SectionManagerImpl;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.SectionManager.ExternalIntegrationConfig;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

@Slf4j
public class CourseManagementIntegrationTest extends SakaiTestBase {
	// Services
	private SiteService siteService;
	private SessionManager sessionManager;
	private SectionManager sectionManager;

	// Objects we'll need to clean up on tearDown
	private String siteId;
	
	/**
	 * Setup test fixture (runs once for each test method called)
	 */
	public void setUp() throws Exception {
		// Fetch the services we need to run the tests
		siteService = (SiteService)getService(SiteService.class.getName());
		sessionManager = (SessionManager)getService(SessionManager.class.getName());
		sectionManager = (SectionManager)getService(SectionManager.class.getName());

		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");
		session.setUserEid("admin");
	}
	
	/**
	 * Remove the newly created objects, so we can run more tests with a clean slate.
	 */
	public void tearDown() throws Exception {
		if(siteId != null) {
			Site site = siteService.getSite(siteId);
			siteService.removeSite(site);
		}
	}
	
	//// Manual Mandatory ////
	
	public void testManualMandatoryConfig() throws Exception {
		// Set the sectionManager to be manual mandatory
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.MANUAL_MANDATORY.toString());
		
		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");
		String siteReference = site.getReference();
		
		// Ensure that the site is not externally managed by default
		Assert.assertFalse(sectionManager.isExternallyManaged(siteReference));

		// Ensure that the section manager won't allow the site to be set to "automatic" sections
		try {
			sectionManager.setExternallyManaged(siteReference,true);
			fail();
		} catch (Exception e) {}
	}
	
	public void testManualMandatorySections() throws Exception {
		// Set the sectionManager to be manual mandatory
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.MANUAL_MANDATORY.toString());

		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");

		// Set the provider ID to an external section EID, and save the site
		site.setProviderGroupId("bio101_f2006_lab1");
		siteService.save(site);

		// Ensure that no internal sections were created from the single attached "roster" (aka provider ID)
		Assert.assertEquals(0, sectionManager.getSections(siteId).size());

		// Now create a complex provider ID (multiple rosters)
		site.setProviderGroupId("bio101_f2006_lab1+bio101_f2006_lab2");
		siteService.save(site);

		// Ensure that no internal sections were created for multiple rosters
		Assert.assertEquals(0, sectionManager.getSections(siteId).size());
	}
	
	//// Manual Default ////

	public void testManualDefaultConfig() throws Exception {
		// Set the sectionManager to be manual default
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.MANUAL_DEFAULT.toString());
		
		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");
		String siteReference = site.getReference();
		
		// Ensure that the site is not externally managed by default
		Assert.assertFalse(sectionManager.isExternallyManaged(siteReference));

		// Ensure that the section manager will allow the site to be set to "automatic" sections
		try {
			sectionManager.setExternallyManaged(siteReference,true);
		} catch (Exception e) {
			fail("Unable to change a manual site to automatic, even though we should be able to do so: " + e.getMessage());
		}
	}
	
	public void testManualDefaultSections() throws Exception {
		// Set the sectionManager to be manual default
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.MANUAL_DEFAULT.toString());

		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");

		// Set the provider ID to an external section EID, and save the site
		site.setProviderGroupId("bio101_f2006_lab1");
		siteService.save(site);

		// Ensure that no internal sections were created from the single attached "roster" (aka provider ID)
		Assert.assertEquals(0, sectionManager.getSections(siteId).size());

		// Now create a complex provider ID (multiple rosters)
		site.setProviderGroupId("bio101_f2006_lab1+bio101_f2006_lab2");
		siteService.save(site);

		// Ensure that no internal sections were created for multiple rosters
		Assert.assertEquals(0, sectionManager.getSections(siteId).size());

		// Add a section "manually"
		CourseSection section = sectionManager.addSection(site.getReference(),
				"a manual section", "lec", Integer.valueOf(10), null, null, null, false, false,
				false, false, false, false, false);
		
		// Now change this "manual by default" site to be externally controlled (automatic)
		sectionManager.setExternallyManaged(site.getReference(), true);

		// Ensure that the manually created section was removed
		Assert.assertFalse(sectionManager.getSections(siteId).contains(section));
		
		// Get a new site object from the service, since the one we have is now out-of-date
		site = siteService.getSite(siteId);

		// Ensure that the provided rosters are reflected as sections
		Assert.assertEquals(2, sectionManager.getSections(siteId).size());
		
		// Change the provider IDs again.
		site.setProviderGroupId("bio101_f2006_lec1+bio101_f2006_lab1+bio101_f2006_lab2");
		siteService.save(site);

		// The automatic sections should have been added to the site
		Assert.assertEquals(3, sectionManager.getSections(siteId).size());
	}

	
	//// Automatic Default ////

	public void testAutomaticDefaultConfig() throws Exception {
		// Set the sectionManager to be auto default
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_DEFAULT.toString());
		
		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");
		String siteReference = site.getReference();
		
		// Ensure that the site is externally managed by default
		Assert.assertTrue(sectionManager.isExternallyManaged(siteReference));

		// Ensure that the section manager will allow the site to be set to "manual" sections
		try {
			sectionManager.setExternallyManaged(siteReference,false);
		} catch (Exception e) {
			fail("We should be able to switch to internal section management, but couldn't: " + e.getMessage());
		}
	}

	public void testAutomaticDefaultSections() throws Exception {
		// Set the sectionManager to be auto default
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_DEFAULT.toString());

		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");

		// Set the provider ID to an external section EID, and save the site
		site.setProviderGroupId("bio101_f2006_lab1");
		siteService.save(site);

		// Ensure that a single internal section was created from the attached "roster" (aka provider ID)
		Assert.assertEquals(1, sectionManager.getSections(siteId).size());

		// Now create a complex provider ID (multiple rosters)
		site.setProviderGroupId("bio101_f2006_lab1+bio101_f2006_lab2");
		siteService.save(site);

		// Ensure that both internal sections were created for the rosters
		Assert.assertEquals(2, sectionManager.getSections(siteId).size());
		
		// Ensure that we can not edit the sections
		CourseSection section = (CourseSection)sectionManager.getSections(siteId).get(0);
		try {
			sectionManager.updateSection(section.getUuid(), "a new title", null, null);
			fail("We should not be able to edit sections in an externally controlled site.");
		} catch (Exception e) {}


		// Ensure that we can not delete the sections
		try {
			sectionManager.disbandSection(section.getUuid());
			fail("We should not be able to delete sections in an externally controlled site.");
		} catch (Exception e) {}

		// Now change this "automatic by default" site to be manually controlled
		sectionManager.setExternallyManaged(site.getReference(), false);

		// Get a new site object from the service, since the one we have is now out-of-date
		site = siteService.getSite(siteId);

		// Ensure that the provided rosters are still reflected as sections
		Assert.assertEquals(2, sectionManager.getSections(siteId).size());
		
		// Change the provider IDs again.
		site.setProviderGroupId("bio101_f2006_lec1+bio101_f2006_lab1+bio101_f2006_lab2");
		siteService.save(site);

		// The sections should not have changed, since we're a manual site
		Assert.assertEquals(2, sectionManager.getSections(siteId).size());
		
		// Ensure that we can edit a section
		try {
			sectionManager.updateSection(section.getUuid(), "a new title", null, null);
		} catch (Exception e) {
			fail("We should be able to edit sections, but couldn't: " + e);
			log.error(e.getMessage(), e);
		}

		// Ensure that we can delete sections
		try {
			sectionManager.disbandSection(section.getUuid());
		} catch (Exception e) {
			fail("We should be able to delete sections in a manually controlled site: " + e.getMessage());			
		}
	}

	//// Automatic Mandatory ////
	
	public void testAutomaticMandatoryConfig() throws Exception {
		// Set the sectionManager to be automatic mandatory
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_MANDATORY.toString());
		
		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");
		String siteReference = site.getReference();
		
		// Ensure that the site is externally managed by default
		Assert.assertTrue(sectionManager.isExternallyManaged(siteReference));

		// Ensure that the section manager won't allow the site to be set to "manual" sections
		try {
			sectionManager.setExternallyManaged(siteReference,false);
			fail();
		} catch (Exception e) {}
	}

	public void testAutomaticMandatorySections() throws Exception {
		// Set the sectionManager to be automatic mandatory
		((SectionManagerImpl)sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_MANDATORY.toString());

		// Create a site
		siteId = generateSiteId();
		Site site = siteService.addSite(siteId, "course");

		// Set the provider ID to an external section EID, and save the site
		site.setProviderGroupId("bio101_f2006_lab1");
		siteService.save(site);

		// Ensure that a single internal section was created from the attached "roster" (aka provider ID)
		Assert.assertEquals(1, sectionManager.getSections(siteId).size());

		// Now create a complex provider ID (multiple rosters)
		site.setProviderGroupId("bio101_f2006_lab1+bio101_f2006_lab2");
		siteService.save(site);

		// Ensure that both internal sections were created
		Assert.assertEquals(2, sectionManager.getSections(siteId).size());
		
		// Ensure that sections in this site can not be edited
		CourseSection section = (CourseSection)sectionManager.getSections(siteId).get(0);
		try {
			sectionManager.updateSection(section.getUuid(), "a new title", null, null);
			fail();
		} catch (Exception e) {}

		// Ensure that students can not be added
		try {
			sectionManager.addSectionMembership("admin", Role.STUDENT, section.getUuid());
			fail("We should not be allowed to add students to an externally managed section");
		} catch (Exception e) {}

		// Ensure that TAs can be added
		try {
			sectionManager.addSectionMembership("admin", Role.TA, section.getUuid());
		} catch (Exception e) {
			fail("Exception adding a TA to an externally managed section: " + e.getMessage());
		}

		// Remove the provider ID (roster) from the site.
		site.setProviderGroupId(null);
		siteService.save(site);

		// Ensure that the sections were removed
		Assert.assertEquals(0, sectionManager.getSections(siteId).size());
	}
}
