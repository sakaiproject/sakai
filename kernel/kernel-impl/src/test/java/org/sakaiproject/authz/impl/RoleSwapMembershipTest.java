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
package org.sakaiproject.authz.impl;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

@Slf4j
public class RoleSwapMembershipTest extends SakaiKernelTestBase {
	@BeforeClass
	public static void beforeClass() {
		try {
			log.debug("starting oneTimeSetup");
			oneTimeSetup();
			log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Test
	public void testRoleSwap() throws Exception {
		
		// Get the Services
		SiteService siteService = getService(SiteService.class);
		
		BaseAuthzGroupService authzGroupService = (BaseAuthzGroupService) getService(AuthzGroupService.class);

		SecurityService securityService = (SecurityService) getService(SecurityService.class);

		SessionManager sessionManager = getService(SessionManager.class);
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		
		// Create another user.
		UserDirectoryService userService = (UserDirectoryService) getService(UserDirectoryService.class);
		UserEdit accessUser = userService.addUser("access", "access");
		userService.commitEdit(accessUser);
		UserEdit maintainUser = userService.addUser("maintain", "maintain");
		userService.commitEdit(maintainUser);
		
		// Create a site
		Site  site = siteService.addSite("roleSwapSiteVisit", "test");
		site.addMember("access", "access", true, false);
		site.addMember("maintain", "maintain", true, false);
		//site.setPublished(false); // This is the default, but we this is what we are wanting to test.
		Role maintain = site.getRole("maintain");
		maintain.allowFunction(SiteService.SITE_ROLE_SWAP);
		maintain.allowFunction(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP);
		siteService.save(site);
		
		//this should work
		Group group1 = site.addGroup();
		group1.setTitle("group1");
		group1.addMember("maintain", "maintain", true, false);
		Group group2 = site.addGroup();
		group2.setTitle("group2");
		group2.addMember("access", "access", true, false);
		siteService.save(site);

		// Log as maintain
		session.setUserEid("maintain");
		session.setUserId("maintain");
		
		// Maintain user has maintain role
		Assert.assertTrue(authzGroupService.isAllowed("maintain", SiteService.SITE_ROLE_SWAP, site.getReference()));
		
		// Maintain user can View the Site, and the group1 but not the group2
		Assert.assertTrue(authzGroupService.isAllowed("maintain", SiteService.SITE_VISIT, site.getReference()));
		Assert.assertTrue(authzGroupService.isAllowed("maintain", SiteService.SITE_VISIT, group1.getReference()));
		Assert.assertFalse(authzGroupService.isAllowed("maintain", SiteService.SITE_VISIT, group2.getReference()));
		
		// Now go to student view
		siteService.activateRoleViewOnSite(site.getReference(), "access");
		String mockupUserId = site.getId().toLowerCase() + "+" + "access";
		
		// Update site information and get current user role on site
		site = siteService.getSite(site.getId());
		Role role = site.getUserRole(session.getUserId());
				
		// Mockup user can't swap roles
		Assert.assertFalse(authzGroupService.isAllowed(mockupUserId, SiteService.SITE_ROLE_SWAP, site.getReference()));

		// Check current user id and role 
		Assert.assertTrue(mockupUserId.equals(session.getUserEid()));
		Assert.assertTrue("access".equals(role.getId()));
		
	}

	
	@Test
	public void testDelegatedAccess() throws Exception {
		
		// Get the Services
		SiteService siteService = getService(SiteService.class);
		
		BaseAuthzGroupService authzGroupService = (BaseAuthzGroupService) getService(AuthzGroupService.class);

		SecurityService securityService = (SecurityService) getService(SecurityService.class);

		SessionManager sessionManager = getService(SessionManager.class);
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		
		// Create another user.
		UserDirectoryService userService = (UserDirectoryService) getService(UserDirectoryService.class);
		UserEdit daUser = userService.addUser("da", "da");
		userService.commitEdit(daUser);
		UserEdit otherUser = userService.addUser("other", "other");
		userService.commitEdit(otherUser);
		
		// Create a site
		Site  site = siteService.addSite("delegatedAccessSiteVisit", "testda");
		site.addMember("access", "access", true, false);
		site.addMember("maintain", "maintain", true, false);
		//site.setPublished(false); // This is the default, but we this is what we are wanting to test.
		Role maintain = site.getRole("maintain");
		maintain.allowFunction(SiteService.SITE_ROLE_SWAP);
		siteService.save(site);
		
		// Log as da
		session.setUserEid("da");
		session.setUserId("da");
		
		// Add delegated access for user da as maintain in this site
		HashMap daMap = new HashMap<String,String[]>();
		daMap.put("/site/delegatedAccessSiteVisit",new String[]{"/site/delegatedAccessSiteVisit","maintain"});
		
		session.setAttribute("delegatedaccess.accessmapflag",true);
		session.setAttribute("delegatedaccess.accessmap",daMap);
		
		// DA user has maintain role
		Assert.assertTrue(authzGroupService.isAllowed("da", SiteService.SITE_ROLE_SWAP, site.getReference()));
		
		// DA user can View the Site but Other can't
		Assert.assertTrue(authzGroupService.isAllowed("da", SiteService.SITE_VISIT, site.getReference()));
		Assert.assertFalse(authzGroupService.isAllowed("other", SiteService.SITE_VISIT, site.getReference()));
		
	}
	
}
