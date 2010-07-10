package org.sakaiproject.site.impl.test;

import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

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
		SessionManager sessionManager = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");


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


	public void testNonExistentSiteId() {
		/*
		 * See KNL-512 sending a realm ID that doesn't exit to 
		 * .BaseSiteService.setUserSecurity causes a db error
		 * 
		 */

		SiteService siteService = org.sakaiproject.site.cover.SiteService.getInstance();
		SessionManager sessionManager = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");

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
}
