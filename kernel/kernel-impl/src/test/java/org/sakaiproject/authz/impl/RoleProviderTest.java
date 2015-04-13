package org.sakaiproject.authz.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

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

public class RoleProviderTest extends SakaiKernelTestBase {
	
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(RoleProviderTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}
	
	public void testCheck() throws Exception {
		
		// Get the Services
		SiteService siteService = (SiteService)getService(SiteService.class.getName());
		
		UserDirectoryService userService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		
		SessionManager sessionManager = (SessionManager)getService(SessionManager.class.getName());
		
		BaseAuthzGroupService authzGroupService = (BaseAuthzGroupService)getService(AuthzGroupService.class.getName());

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
		
		
		assertTrue(site1.isAllowed("user1", SiteService.SECURE_UPDATE_SITE));
		assertTrue(authzGroupService.isAllowed("user1", SiteService.SECURE_UPDATE_SITE, Collections.singletonList(site1.getReference())));
		assertTrue(authzGroupService.isAllowed("user1", SiteService.SECURE_UPDATE_SITE, site1.getReference()));
		assertFalse(site1.isAllowed("user2", SiteService.SECURE_UPDATE_SITE));
		assertFalse(authzGroupService.isAllowed("user2", SiteService.SECURE_UPDATE_SITE, Collections.singletonList(site1.getReference())));
		assertFalse(authzGroupService.isAllowed("user2", SiteService.SECURE_UPDATE_SITE, site1.getReference()));
		
		// Check .anon works.
		assertTrue(site1.isAllowed("user2", SiteService.SITE_VISIT));
		assertTrue(authzGroupService.isAllowed("user2", SiteService.SITE_VISIT, Collections.singletonList(site1.getReference())));
		assertTrue(authzGroupService.isAllowed("user2", SiteService.SITE_VISIT, site1.getReference()));
		
        /*
		// Check roleprovider works
		assertTrue(site1.isAllowed("user3", SiteService.SECURE_UPDATE_SITE));
		assertTrue(authzGroupService.isAllowed("user3", SiteService.SECURE_UPDATE_SITE, Collections.singletonList(site1.getReference())));
		assertTrue(authzGroupService.isAllowed("user3", SiteService.SECURE_UPDATE_SITE, site1.getReference()));
        */
		
		// Make people in site2 with the right permission be able to modify site one.
		session.setUserId("user1");
		assertTrue(siteService.allowUpdateSite(site1.getId()));
		session.setUserId("user2");
		assertFalse(siteService.allowUpdateSite(site1.getId()));
        /*
		session.setUserId("user3");
		assertTrue(siteService.allowUpdateSite(site1.getId()));
        */
	}
}
