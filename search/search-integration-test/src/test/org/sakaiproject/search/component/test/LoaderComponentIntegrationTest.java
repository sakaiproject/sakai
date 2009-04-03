/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.test;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.PropertyResourceBundle;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.catalina.loader.WebappClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;


/**
 * @author ieb
 */
public class LoaderComponentIntegrationTest extends TestCase
{
	private static Log logger = LogFactory
			.getLog(LoaderComponentIntegrationTest.class);

	private SiteService siteService;

	private Site site;

	private Group group1;

	private Site targetSite;

	private Group group2;

	private UserDirectoryService userDirService;


	private SecurityService securityService = null;

	private AuthzGroupService authzGroupService = null;

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
		siteService = (SiteService) getService(SiteService.class.getName());
		userDirService = (UserDirectoryService) getService(UserDirectoryService.class.getName());

		securityService = (SecurityService) getService(SecurityService.class.getName());


		authzGroupService = (AuthzGroupService) getService(AuthzGroupService.class.getName());


		searchService = (SearchService) getService(SearchService.class.getName());
		searchIndexBuilder = (SearchIndexBuilder) getService(SearchIndexBuilder.class.getName());

		assertNotNull(
				"Cant find site service as org.sakaiproject.service.legacy.authzGroup.AuthzGroupService ",
				authzGroupService);

		assertNotNull(
				"Cant find site service as org.sakaiproject.service.legacy.security.SecurityService ",
				securityService);

		assertNotNull(
				"Cant find site service as org.sakaiproject.service.legacy.site.SiteService ",
				siteService);
		assertNotNull(
				"Cant find User Directory service as org.sakaiproject.service.legacy.user.UserDirectoryService ",
				userDirService);
		assertNotNull(
				"Cant find Search Service as org.sakaiproject.search.api.SearchService ",
				searchService);
		assertNotNull(
				"Cant find Search Index Builder as org.sakaiproject.search.api.SearchIndexBuilder ",
				searchIndexBuilder);
		// Set username as admin
		setUser("admin");

		tearDown();

		userDirService.addUser("test.user.1", "test.user.1", "Jane", "Doe", "jd1@foo.com",
				"123", null, null);
		userDirService.addUser("test.user.2", "test.user.2", "Joe", "Schmoe", "js2@foo.com",
				"123", null, null);
		userDirService.addUser("test.ta.1", "test.ta.1", "TA", "Doe", "tajd1@foo.com",
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
			int x = 1; //ignore
		}
		try
		{
			// Remove the site (along with its groups)
			siteService.removeSite(targetSite);

		}
		catch (Throwable t)
		{
			int x = 1; //ignore
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
	// Stolen shamelessly from test-harness, since I want to run these test inside exlipse

	protected static ComponentManager compMgr;
	
	/**
	 * Initialize the component manager once for all tests, and log in as admin.
	 */
	protected static void oneTimeSetup() throws Exception {
		if(compMgr == null) {
			// Find the sakai home dir
			String tomcatHome = getTomcatHome();
			String sakaiHome = tomcatHome + File.separatorChar + "sakai" + File.separatorChar;
			String componentsDir = tomcatHome + "components/";
			
			// Set the system properties needed by the sakai component manager
			System.setProperty("sakai.home", sakaiHome);
			System.setProperty(ComponentManager.SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsDir);

			// Get a tomcat classloader
			logger.debug("Creating a tomcat classloader for component loading");
			WebappClassLoader wcloader = new WebappClassLoader(Thread.currentThread().getContextClassLoader());
			wcloader.start();

			// Initialize spring component manager
			logger.debug("Loading component manager via tomcat's classloader");
			Class clazz = wcloader.loadClass(SpringCompMgr.class.getName());
			Constructor constructor = clazz.getConstructor(new Class[] {ComponentManager.class});
			compMgr = (ComponentManager)constructor.newInstance(new Object[] {null});
			Method initMethod = clazz.getMethod("init", new Class[0]);
			initMethod.invoke(compMgr, new Object[0]);
		}
		 
		// Sign in as admin
		if(SessionManager.getCurrentSession() == null) {
			SessionManager.startSession();
			Session session = SessionManager.getCurrentSession();
			session.setUserId("admin");
		}
	}

	
	/**
	 * Close the component manager when the tests finish.
	 */
	public static void oneTimeTearDown() {
		if(compMgr != null) {
			compMgr.close();
		}
	}

	/**
	 * Fetches the "maven.tomcat.home" property from the maven build.properties
	 * file located in the user's $HOME directory.
	 * 
	 * @return
	 * @throws Exception
	 */
	private static String getTomcatHome() throws Exception {
		String testTomcatHome = System.getProperty("test.tomcat.home");
		if ( testTomcatHome != null && testTomcatHome.length() > 0 ) {
			return testTomcatHome;
		} else {
			String homeDir = System.getProperty("user.home");
			File file = new File(homeDir + File.separatorChar + "build.properties");
			FileInputStream fis = new FileInputStream(file);
			PropertyResourceBundle rb = new PropertyResourceBundle(fis);
			return rb.getString("maven.tomcat.home");
		}
	}
	
	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	public static final Object getService(String beanId) {
		return org.sakaiproject.component.cover.ComponentManager.get(beanId);
	}

	/**
	 * Convenience method to set the current user in sakai.  By default, the user
	 * is admin.
	 * 
	 * @param userUid The user to become
	 */
	public static final void setUser(String userUid) {
		Session session = SessionManager.getCurrentSession();
		session.setUserId(userUid);
	}
	
	/**
	 * Convenience method to create a somewhat unique site id for testing.  Useful
	 * in tests that need to create a site to run tests upon.
	 * 
	 * @return A string suitable for using as a site id.
	 */
	protected String generateSiteId() {
		return "site-" + getClass().getName() + "-" + Math.floor(Math.random()*100000);
	}
	
	/**
	 * Returns a dynamic proxy for a service interface.  Useful for testing with
	 * customized service implementations without needing to write custom stubs.
	 * 
	 * @param clazz The service interface class
	 * @param handler The invocation handler that defines how the dynamic proxy should behave
	 * 
	 * @return The dynamic proxy to use as a collaborator
	 */
	public static final Object getServiceProxy(Class clazz, InvocationHandler handler) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {clazz}, handler);
	}

}
