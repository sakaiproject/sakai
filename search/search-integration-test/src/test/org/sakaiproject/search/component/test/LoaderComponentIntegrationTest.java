/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search.component.test;

import java.io.File;
import java.io.FileReader;
import java.util.Date;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.search.SearchIndexBuilder;
import org.sakaiproject.search.SearchService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;

/**
 * @author ieb
 */
public class LoaderComponentIntegrationTest extends SakaiTestBase
{
	private static Log logger = LogFactory
			.getLog(LoaderComponentIntegrationTest.class);

	private SiteService siteService;

	private Site site;

	private Group group1;

	private Site targetSite;

	private Group group2;

	private UserDirectoryService userDirService;

	private RWikiObjectService rwikiObjectservice = null;

	private SecurityService securityService = null;

	private AuthzGroupService authzGroupService = null;

	private RWikiSecurityService rwikiSecurityService = null;

	private RenderService renderService = null;

	private PreferenceService preferenceService = null;

	private SearchService searchService = null;

	private SearchIndexBuilder searchIndexBuilder = null;

	// Constants
	private static final String GROUP1_TITLE = "group1";

	private static final String GROUP2_TITLE = "group2";

	private static final String loaderDirectory = "/Users/ieb/Caret/testdataset";

	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(
				LoaderComponentIntegrationTest.class))
		{
			protected void setUp() throws Exception
			{
				oneTimeSetup();
			}
		};
		return setup;
	}

	/**
	 * Setup test fixture (runs once for each test method called)
	 */
	public void setUp() throws Exception
	{

		// Get the services we need for the tests
		siteService = (SiteService) getService("org.sakaiproject.service.legacy.site.SiteService");
		userDirService = (UserDirectoryService) getService("org.sakaiproject.service.legacy.user.UserDirectoryService");
		rwikiObjectservice = (RWikiObjectService) getService("uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService");
		renderService = (RenderService) getService("uk.ac.cam.caret.sakai.rwiki.service.api.RenderService");

		securityService = (SecurityService) getService("org.sakaiproject.service.legacy.security.SecurityService");

		rwikiSecurityService = (RWikiSecurityService) getService("securityService");

		authzGroupService = (AuthzGroupService) getService("org.sakaiproject.service.legacy.authzGroup.AuthzGroupService");

		preferenceService = (PreferenceService) getService("uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService");

		searchService = (SearchService) getService("org.sakaiproject.search.SearchService");
		searchIndexBuilder = (SearchIndexBuilder) getService("org.sakaiproject.search.SearchIndexBuilder");

		assertNotNull(
				"Cant find site service as org.sakaiproject.service.legacy.authzGroup.AuthzGroupService ",
				authzGroupService);

		assertNotNull(
				"Cant find site service as org.sakaiproject.service.legacy.security.SecurityService ",
				securityService);
		assertNotNull("Cant find site service as securityService ",
				rwikiSecurityService);

		assertNotNull(
				"Cant find site service as org.sakaiproject.service.legacy.site.SiteService ",
				siteService);
		assertNotNull(
				"Cant find User Directory service as org.sakaiproject.service.legacy.user.UserDirectoryService ",
				userDirService);
		assertNotNull(
				"Cant find User Preference Service as uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService ",
				preferenceService);
		assertNotNull("Cant find RWiki Object service as "
				+ RWikiObjectService.class.getName(), rwikiObjectservice);
		assertNotNull("Cant find Render Service service as "
				+ RenderService.class.getName(), renderService);
		assertNotNull(
				"Cant find Search Service as org.sakaiproject.search.SearchService ",
				searchService);
		assertNotNull(
				"Cant find Search Index Builder as org.sakaiproject.search.SearchIndexBuilder ",
				searchIndexBuilder);
		// Set username as admin
		setUser("admin");

		tearDown();

		userDirService.addUser("test.user.1", "Jane", "Doe", "jd1@foo.com",
				"123", null, null);
		userDirService.addUser("test.user.2", "Joe", "Schmoe", "js2@foo.com",
				"123", null, null);
		userDirService.addUser("test.ta.1", "TA", "Doe", "tajd1@foo.com",
				"123", null, null);

		// Create a site
		site = siteService.addSite(generateSiteId(), "course");
		targetSite = siteService.addSite(generateSiteId(), "course");
		// Create a group for SectionAwareness to, er, become aware of
		group1 = site.addGroup();
		group1.setTitle(GROUP1_TITLE);

		// Save the group
		siteService.save(site);

		site.addMember("test.user.1", "Student", true, false);
		// Save the site and its new member
		siteService.save(site);

		site.addMember("test.ta.1", "TA", true, false);
		siteService.save(site);

		// Add a user to a group
		group1.addMember("test.user.1", "Student", true, false);
		group1.addMember("test.ta.1", "TA", true, false);

		group2 = targetSite.addGroup();
		group2.setTitle(GROUP2_TITLE);

		// Save the group
		siteService.save(targetSite);

		targetSite.addMember("test.user.1", "Student", true, false);

		// Save the site and its new member
		siteService.save(targetSite);

		// Add a user to a group
		group2.addMember("test.user.1", "Student", true, false);

		logger.info("Site Ref  " + site.getReference());
		logger.info("Target Site ref " + targetSite.getReference());
		logger.info("Group 1 ref " + group1.getReference());
		logger.info("Group 2 ref " + group2.getReference());

	}

	/**
	 * Remove the newly created objects, so we can run more tests with a clean
	 * slate.
	 */
	public void tearDown() throws Exception
	{
		setUser("admin");
		try
		{
			// Remove the site (along with its groups)
			siteService.removeSite(site);

		}
		catch (Throwable t)
		{
		}
		try
		{
			// Remove the site (along with its groups)
			siteService.removeSite(targetSite);

		}
		catch (Throwable t)
		{
		}
		try
		{
			// Remove the users
			UserEdit user1 = userDirService.editUser("test.user.1");
			userDirService.removeUser(user1);
		}
		catch (Throwable t)
		{
			// logger.info("Failed to remove user ",t);
			logger.info("Failed to remove user " + t.getMessage());

		}

		try
		{
			UserEdit user2 = userDirService.editUser("test.user.2");
			userDirService.removeUser(user2);
		}
		catch (Throwable t)
		{
			// logger.info("Failed to remove user ",t);
			logger.info("Failed to remove user " + t.getMessage());
		}
		try
		{
			// Remove the users
			UserEdit user1 = userDirService.editUser("test.ta.1");
			userDirService.removeUser(user1);
		}
		catch (Throwable t)
		{
			// logger.info("Failed to remove user ",t);
			logger.info("Failed to remove user " + t.getMessage());
		}
	}

	public void testLoadFromDisk() throws Exception
	{
		File d = new File(loaderDirectory);
		if (!d.exists())
		{
			d.mkdirs();
			logger
					.error("Directory for loader information does not exist so it has been created at "
							+ d.getAbsolutePath());
		}
		File[] files = d.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			String pageName = f.getName();
			int li = pageName.lastIndexOf(".");
			if (li > -1)
			{
				pageName = pageName.substring(0, li);
			}
			FileReader fr = new FileReader(f);
			char[] c = new char[4096];
			StringBuffer contents = new StringBuffer();
			int ci = -1;
			while ((ci = fr.read(c)) != -1)
			{
				contents.append(c, 0, ci);
			}
			try
			{
				logger.info("Adding " + pageName);
				rwikiObjectservice.update(pageName, site.getReference(),
						new Date(), contents.toString());
				logger.info("Added " + pageName);
			}
			catch (Exception ex)
			{
				logger.error("Problem ", ex);
			}
		}
		while (!searchIndexBuilder.isBuildQueueEmpty())
		{
			Thread.sleep(5000);
		}
		searchIndexBuilder.destroy();
		Thread.sleep(15000);
	}

	public void testRefreshIndex() throws Exception
	{
		searchIndexBuilder.refreshIndex();
		while (!searchIndexBuilder.isBuildQueueEmpty())
		{
			Thread.sleep(5000);
		}
		searchIndexBuilder.destroy();
		Thread.sleep(15000);
	}

	public void testRebuildIndex() throws Exception
	{
		searchIndexBuilder.rebuildIndex();
		while (!searchIndexBuilder.isBuildQueueEmpty())
		{
			Thread.sleep(5000);
		}
		searchIndexBuilder.destroy();
		Thread.sleep(15000);
	}

	public void testRebuildIndexAndWait() throws Exception
	{
		searchIndexBuilder.refreshIndex();
		searchIndexBuilder.rebuildIndex();
		searchIndexBuilder.refreshIndex();
		Thread.sleep(15000);
		while (!searchIndexBuilder.isBuildQueueEmpty())
		{
			Thread.sleep(5000);
		}
		searchIndexBuilder.destroy();
		Thread.sleep(15000);
	}

}
