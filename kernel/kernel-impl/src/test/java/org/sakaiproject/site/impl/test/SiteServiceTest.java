package org.sakaiproject.site.impl.test;

import java.util.Set;
import java.util.TreeSet;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserPermissionException;

public class SiteServiceTest extends SakaiKernelTestBase {
	private static final Log log = LogFactory.getLog(SiteServiceTest.class);
	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(SiteServiceTest.class))
		{
			protected void setUp() throws Exception 
			{
				log.debug("starting oneTimeSetup");
				oneTimeSetup(null);
				log.debug("finished oneTimeSetup");
			}
			protected void tearDown() throws Exception 
			{
				log.debug("starting tearDown");
				oneTimeTearDown();
				log.debug("finished tearDown");
			}
		};
		return setup;
	}

	public void testNullSiteId() {
		SiteService siteService = org.sakaiproject.site.cover.SiteService.getInstance();
		workAsAdmin();


		try {
			Site site = siteService.addSite("", "other");
			fail();
		} catch (IdInvalidException e) {
			log.info("when passed a null id the test correctly responded with an IdInvalidException");
		} catch (IdUsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


	}

	private void workAsAdmin() {
		workAsUser("admin", "admin");
	}
	
	private void workAsUser(String eid, String id) {
		SessionManager sessionManager = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(eid);
		session.setUserId(id);
	}


	public void testNonExistentSiteId() {
		/*
		 * See KNL-512 sending a realm ID that doesn't exit to 
		 * .BaseSiteService.setUserSecurity causes a db error
		 * 
		 */

		SiteService siteService = org.sakaiproject.site.cover.SiteService.getInstance();
		workAsAdmin();

		Set<String> siteSet =  new TreeSet<String>();
		siteSet.add("nosuchSite");
		try { 
			siteService.setUserSecurity("admin", siteSet, siteSet, siteSet);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Check that when a site is unpublished a user roleswapped user can still access the site when switching to a role which can't access the unpublished site.
	 */
	public void testRoleSwapSiteVisit() throws IdUnusedException, PermissionException, IdInvalidException, IdUsedException, UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException {
		
		SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
		
		SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);
		workAsAdmin();
		
		// Create another user.
		UserDirectoryService userService = (UserDirectoryService)ComponentManager.get(UserDirectoryService.class);
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
		assertTrue(siteService.allowAccessSite("roleSwapSiteVisit"));
		try {
			siteService.getSiteVisit("roleSwapSiteVisit");
		} catch (PermissionException pe) {
			fail("Should have been able to get the site fine.");
		}
		
		assertTrue(securityService.setUserEffectiveRole(site.getReference(), "access"));
		// Check accessor as well
		assertTrue(siteService.allowAccessSite("roleSwapSiteVisit"));
		try {
			siteService.getSiteVisit("roleSwapSiteVisit");
		} catch (PermissionException pe) {
			fail("Should have been able to get the site fine.");
		}
		
	}
	
	public void testGroupSave() throws IdInvalidException, IdUsedException, PermissionException, IdUnusedException {
		
		SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
		
		SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);
		workAsAdmin();
		
		Site site = siteService.addSite("groupTestSite", "test");
		siteService.save(site);
		
		//this should work
		Group group1 = site.addGroup();
		group1.setTitle("group1");
		siteService.save(site);
		
		//This should get an IllegalArgumentException
		try {
			Group group = site.addGroup();
			siteService.save(site);
			fail("Should not be able to save a group without a title");
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
}
