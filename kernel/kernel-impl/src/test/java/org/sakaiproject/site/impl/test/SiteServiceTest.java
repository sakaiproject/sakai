/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.site.impl.test;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.impl.BaseSitePage;
import org.sakaiproject.site.impl.BaseSiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserPermissionException;

@Slf4j
public class SiteServiceTest extends SakaiKernelTestBase {
	@BeforeClass
	public static void beforeClass() {
		try {
			log.debug("starting oneTimeSetup");
			oneTimeSetup(null);
			log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	@Test
	public void testNullSiteId() {
		SiteService siteService = getService(SiteService.class);
		workAsAdmin();

		try {
			siteService.addSite("", "other");
			Assert.fail();
		} catch (IdInvalidException e) {
			log.info("when passed a null id the test correctly responded with an IdInvalidException");
		} catch (IdUsedException e) {
			log.error(e.getMessage(), e);
		} catch (PermissionException e) {
			log.error(e.getMessage(), e);
		} 


	}

	private void workAsAdmin() {
		workAsUser("admin", "admin");
	}
	
	private void workAsUser(String eid, String id) {
		SessionManager sessionManager = getService(SessionManager.class);
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(eid);
		session.setUserId(id);
	}

	@Test
	public void testNonExistentSiteId() {
		/*
		 * See KNL-512 sending a realm ID that doesn't exit to 
		 * .BaseSiteService.setUserSecurity causes a db error
		 * 
		 */

		SiteService siteService = getService(SiteService.class);
		workAsAdmin();

		Set<String> siteSet =  new TreeSet<String>();
		siteSet.add("nosuchSite");
		try { 
			siteService.setUserSecurity("admin", siteSet, siteSet, siteSet);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail();
		}
	}

	/**
	 * Check that when a site is unpublished a user roleswapped user can still access the site when switching to a role which can't access the unpublished site.
	 */
	@Test
	public void testRoleSwapSiteVisit() throws IdUnusedException, PermissionException, IdInvalidException, IdUsedException, UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException {
		
		SiteService siteService = (SiteService) getService(SiteService.class);
		
		SecurityService securityService = (SecurityService) getService(SecurityService.class);
		workAsAdmin();
		
		// Create another user.
		UserDirectoryService userService = (UserDirectoryService) getService(UserDirectoryService.class);
		UserEdit accessUser = userService.addUser("access", "access");
		userService.commitEdit(accessUser);
		UserEdit maintainUser = userService.addUser("maintain", "maintain");
		userService.commitEdit(maintainUser);
		
		Site  site = siteService.addSite("roleSwapSiteVisit", "test");
		site.addMember("access", "access", true, false);
		site.addMember("maintain", "maintain", true, false);
		site.setPublished(false); // This is the default, but we this is what we are wanting to test.
		Role maintain = site.getRole("maintain");
		maintain.allowFunction(SiteService.SITE_ROLE_SWAP);
		siteService.save(site);
		
		workAsUser("maintain", "maintain");
		// Check maintainer has same access through allowAccess as getSiteVisit.
		Assert.assertTrue(siteService.allowAccessSite("roleSwapSiteVisit"));
		try {
			siteService.getSiteVisit("roleSwapSiteVisit");
		} catch (PermissionException pe) {
			Assert.fail("Should have been able to get the site fine.");
		}
		
		Assert.assertTrue(securityService.setUserEffectiveRole(site.getReference(), "access"));
		// Check accessor as well
		Assert.assertTrue(siteService.allowAccessSite("roleSwapSiteVisit"));
		try {
			siteService.getSiteVisit("roleSwapSiteVisit");
		} catch (PermissionException pe) {
			Assert.fail("Should have been able to get the site fine.");
		}
		
	}
	
	@Test
	public void testGroupSave() throws IdInvalidException, IdUsedException, PermissionException, IdUnusedException {
		SiteService siteService = (SiteService) getService(SiteService.class);
		workAsAdmin();
		
		Site site = siteService.addSite("groupTestSite", "test");
		siteService.save(site);
		
		//this should work
		Group group1 = site.addGroup();
		group1.setTitle("group1");
		siteService.save(site);
		
		//This should get an IllegalArgumentException
		try {
			site.addGroup();
			siteService.save(site);
			Assert.fail("Should not be able to save a group without a title");
		}
		catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			Assert.fail();
		}
	}
	
	@Test
	public void testPagePosition() throws Exception{
		BaseSiteService siteService = (BaseSiteService) getService(SiteService.class);
		workAsUser("admin", "admin");

		Site site = siteService.addSite("test", "test");
		//Add 5 pages
		SitePage page1 = site.addPage();
		SitePage page2 = site.addPage();
		SitePage page3 = site.addPage();

		// Just set some positions that should work fine title property set.
		page1.setPosition(0);
		page2.setPosition(1);
		page3.setPosition(10);
		//Assert it goes at the end and other pages are still in position
		assertEquals(0, page1.getPosition());
		assertEquals(1, page2.getPosition());
		assertEquals(2, page3.getPosition());
		//Now put page 1 at the end
		page1.setPosition(10);

		//Assert page one is at the end now
		assertEquals(2, page1.getPosition());
		assertEquals(0, page2.getPosition());
		assertEquals(1, page3.getPosition());

	}
}
