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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleProvider;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

@Slf4j
public class RoleProviderTest extends SakaiKernelTestBase {
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
	public void testCheck() throws Exception {
		
		// Get the Services
		SiteService siteService = getService(SiteService.class);
		
		UserDirectoryService userService = getService(UserDirectoryService.class);
		
		SessionManager sessionManager = getService(SessionManager.class);
		
		BaseAuthzGroupService authzGroupService = (BaseAuthzGroupService) getService(AuthzGroupService.class);

		RoleProvider roleProvider = new RoleProvider() {
			
			public String getDisplayName(String role) {
				return null;
			}
			
			public Collection<String> getAllAdditionalRoles() {
				return Collections.emptyList();
			}
			
			public Set<String> getAdditionalRoles(String userId) {
                /*
				if ("user3".equals(userId)) {
					return Collections.singleton(".other");
				}
                */
				return Collections.emptySet();
			}
		};
		
		authzGroupService.setRoleProvider(roleProvider);
		
		UserEdit user1 = userService.addUser("user1", "user1");
		userService.commitEdit(user1);
		UserEdit user2 = userService.addUser("user2", "user2");
		userService.commitEdit(user2);
		UserEdit user3 = userService.addUser("user3", "user3");
		userService.commitEdit(user3);
		
		
		// Login as admin (to set stuff up).
		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");
		
		Site site1 = siteService.addSite("site1", "project");
		site1.addMember("user1", "maintain", true, false);
		// Check the anon role
		Role anonRole = site1.addRole(".anon");
		anonRole.allowFunction(SiteService.SITE_VISIT);
        /*
		Role otherRole = site1.addRole(".other");
		otherRole.allowFunction(SiteService.SECURE_UPDATE_SITE);
        */
		
		siteService.save(site1);
		
		// Site.isAllowed checks by loading the role and look at the in memory structure.
		// AuthzGroupService.isAllowed checks through the DB.
		
		
		Assert.assertTrue(site1.isAllowed("user1", SiteService.SECURE_UPDATE_SITE));
		Assert.assertTrue(authzGroupService.isAllowed("user1", SiteService.SECURE_UPDATE_SITE, Collections.singletonList(site1.getReference())));
		Assert.assertTrue(authzGroupService.isAllowed("user1", SiteService.SECURE_UPDATE_SITE, site1.getReference()));
		Assert.assertFalse(site1.isAllowed("user2", SiteService.SECURE_UPDATE_SITE));
		Assert.assertFalse(authzGroupService.isAllowed("user2", SiteService.SECURE_UPDATE_SITE, Collections.singletonList(site1.getReference())));
		Assert.assertFalse(authzGroupService.isAllowed("user2", SiteService.SECURE_UPDATE_SITE, site1.getReference()));
		
		// Check .anon works.
		Assert.assertTrue(site1.isAllowed("user2", SiteService.SITE_VISIT));
		Assert.assertTrue(authzGroupService.isAllowed("user2", SiteService.SITE_VISIT, Collections.singletonList(site1.getReference())));
		Assert.assertTrue(authzGroupService.isAllowed("user2", SiteService.SITE_VISIT, site1.getReference()));
		
        /*
		// Check roleprovider works
		assertTrue(site1.isAllowed("user3", SiteService.SECURE_UPDATE_SITE));
		assertTrue(authzGroupService.isAllowed("user3", SiteService.SECURE_UPDATE_SITE, Collections.singletonList(site1.getReference())));
		assertTrue(authzGroupService.isAllowed("user3", SiteService.SECURE_UPDATE_SITE, site1.getReference()));
        */
		
		// Make people in site2 with the right permission be able to modify site one.
		session.setUserId("user1");
		Assert.assertTrue(siteService.allowUpdateSite(site1.getId()));
		session.setUserId("user2");
		Assert.assertFalse(siteService.allowUpdateSite(site1.getId()));
        /*
		session.setUserId("user3");
		assertTrue(siteService.allowUpdateSite(site1.getId()));
        */
	}
}
