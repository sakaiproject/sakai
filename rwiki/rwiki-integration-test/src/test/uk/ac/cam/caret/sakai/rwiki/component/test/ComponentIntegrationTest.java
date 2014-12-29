/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.test;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Stack;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.catalina.loader.WebappClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.Xml;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.cam.caret.sakai.rwiki.component.service.impl.ComponentPageLinkRenderImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.utils.SimpleCoverage;

/**
 * This is a Integragration Test case that test most of the component
 * functionality. Sakai must have been deployed for this to work correctly
 * 
 * @author ieb
 */
public class ComponentIntegrationTest extends TestCase
{
	private static Log logger = LogFactory
			.getLog(ComponentIntegrationTest.class);

	// private AnnouncementService announcementService;
	// private EntityManager entityManager;
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

	// Constants
	private static final String GROUP1_TITLE = "group1";

	private static final String GROUP2_TITLE = "group2";

	private static final String[] content = { "Some __Simple__ Content",
			"Here is a \nh1. Heading type1\n\n"

	};
// this needs looking at since I think there are problems with the XHTMLFlter
	private static final String[] rendered = {
			"Some <b class=\"bold\">Simple</b> Content",
"Here is a \n<h3 class=\"heading-h1\">\n        <a name=\"Headingtype1\"></a>Heading type1</h3>\n" };

	private static final String archiveContentResource = "/uk/ac/cam/caret/sakai/rwiki/component/test/archive.xml";

	/**
	 * Runs only once for this TestCase, so we can keep the same component
	 * manager rather than rebuilding it for each test.
	 * 
	 * @return
	 */
	public static Test suite()
	{
//		Logger.getRootLogger().setLevel(Level.DEBUG);
		TestSetup setup = new TestSetup(new TestSuite(
				ComponentIntegrationTest.class))
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
		rwikiObjectservice = (RWikiObjectService) getService(RWikiObjectService.class.getName());
		renderService = (RenderService) getService(RenderService.class.getName());

		securityService = (SecurityService) getService(SecurityService.class.getName());

		rwikiSecurityService = (RWikiSecurityService) getService(RWikiSecurityService.class.getName());

		authzGroupService = (AuthzGroupService) getService(AuthzGroupService.class.getName());

		preferenceService = (PreferenceService) getService(PreferenceService.class.getName());

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

	boolean consolidatedtest = true;

	/**
	 * Full test of all methods, using one block avoids multiple startup and
	 * shutdowns of the component manager
	 * 
	 * @throws Exception
	 */
	public void dtestAll() throws Exception
	{
		consolidatedtest = true;
		xtestBasicMethods();
		xtestRenderPage();
		xtestFindAll();
		xtestURLAccess();
		xtestEntityAccess();
		xtestArchiveAccess();
		xtestMerge();
		xtestImport();
		xtestPreference();
		xtestPermissions();

		Thread.sleep(60000);

	}

	/**
	 * test the basic methods only
	 * 
	 * @throws Exception
	 */
	public void testBasicMethods() throws Exception
	{
		consolidatedtest = false;
		xtestBasicMethods();

	}

	/**
	 * test render page only, disabled
	 * 
	 * @throws Exception
	 */
	public void testRenderPage() throws Exception
	{
		consolidatedtest = false;
		xtestRenderPage();

	}

	/**
	 * test URL access only, disabled
	 * 
	 * @throws Exception
	 */
	public void testURLAccess() throws Exception
	{
		consolidatedtest = false;
		xtestURLAccess();

	}

	/**
	 * test Entity Access only, disabled
	 * 
	 * @throws Exception
	 */
	public void testEntityAccess() throws Exception
	{
		consolidatedtest = false;
		xtestEntityAccess();

	}

	public void testArchiveAccess() throws Exception
	{
		consolidatedtest = false;
		xtestArchiveAccess();

	}

	/**
	 * test merge only, disabled
	 * 
	 * @throws Exception
	 */
	public void testMerge() throws Exception
	{
		consolidatedtest = false;
		xtestMerge();

	}

	/**
	 * test import only, disabled
	 * 
	 * @throws Exception
	 */
	public void testImport() throws Exception
	{
		consolidatedtest = false;
		xtestImport();

	}
	public void testPreference() throws Exception
	{
		consolidatedtest = false;
		xtestPreference();
	}
	public void testPermissions() throws Exception
	{
		consolidatedtest = false;
		xtestPermissions();
	}
	/**
	 * A simple set of tests of the render service
	 * 
	 * @throws Exception
	 */
	public void xtestRenderPage() throws Exception
	{
		SimpleCoverage.cover("Render Page Test");
		assertEquals("Test and results sets are not the same size ",
				content.length, rendered.length);
		Date d = new Date();
		for (int i = 0; i < content.length; i++)
		{
			SimpleCoverage.cover("Updating page ");
			rwikiObjectservice.update("HomeTestPageRENDER",
					site.getReference(), d, content[i]);
			SimpleCoverage.cover("loading page ");
			RWikiObject rwo = rwikiObjectservice.getRWikiObject(
					"HomeTestPageRENDER", site.getReference());
			d = rwo.getVersion();
			ComponentPageLinkRenderImpl cplr = new ComponentPageLinkRenderImpl(
					site.getReference(),true);
			cplr.setCachable(false);
			cplr.setUseCache(false);
			SimpleCoverage.cover("render page ");
			String renderedPage = renderService.renderPage(rwo, site
					.getReference(), cplr);
			SimpleCoverage.cover("Page Rendered as " + rwo.getContent() + "::"
					+ renderedPage + "::");
			assertEquals("Render Page results was not as expected ",
					rendered[i],renderedPage);
			
			// at the moment I cant get the render engine up and running.
		}
		SimpleCoverage.cover("Render Page Test Ok");
	}

	/**
	 * A list of paths to test
	 */
	private static final String[] accessPaths = {
			"/resources/some/resourcethat/shouldworl,123.html",
			"/resources/some/resourcethat/shouldworl.html",
			"/resources/some/resourcethat/shouldworl",
			"/resources/some/resourcethat/shouldworl,123.html",
			"/wiki/non-existant-context/ .rss",
			"/wiki/site/SITEID/hometestpageURL.html",
			"/wiki/site/SITEID/HometestpageURL.html",
			"/wiki/site/SITEID/homeTestpageURL,123123.html",
			"/wiki/site/SITEID/hometestpageURL,0.html",
			"/wiki/site/SITEID/indexURL.html",
			"/wiki/site/SITEID/indexURL.09.rss",
			"/wiki/site/SITEID/indexURL.10.rss",
			"/wiki/site/SITEID/indexURL.20.rss",
			"/wiki/site/SITEID/indexURL.atom",
			"/wiki/site/SITEID/ .09.rss",
			"/wiki/site/SITEID/ .10.rss",
			"/wiki/site/SITEID/ .20.rss",
			"/wiki/site/SITEID/ .atom",
			"/wiki/site/SITEID/changedURL.html",
			"/wiki/site/SITEID/changedURL.09.rss",
			"/wiki/site/SITEID/changedURL.10.rss",
			"/wiki/site/SITEID/changedURL.20.rss",
			"/wiki/site/SITEID/changedURL.atom",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/group/63452017-766c-43f8-00ea-c4e666e48f74/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/section/63452017-766c-43f8-00ea-c4e666e48f74/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/group/Studio1/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/section/Studio1/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/group/Studio2/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/section/Studio2/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/group/Studio3/home.html",
			"/wiki/site/8e2f826d-5a12-4b64-00f1-8cb328cd1443/section/Studio3/home.html" };

	/**
	 * some page names to populate
	 */
	private static final String[] pageNames = { "HomeTestPageURL",
			"HomeTestPage2URL", "indexURL", "changedURL" };

	/**
	 * some simple page content to use with the above pageNames
	 */
	private static final String[] pageContent = { content[0], content[1],
			"{index}", "{recent-changes}" };

	/**
	 * Load a set of pages, and process a set of URLS
	 * 
	 * @throws Exception
	 */
	public void xtestURLAccess() throws Exception
	{
		assertEquals("pageNames and pageContent must be the same length ",
				pageNames.length, pageContent.length);

		for (int i = 0; i < pageNames.length; i++)
		{
			rwikiObjectservice.update(pageNames[i], site.getReference(),
					new Date(), pageContent[i]);
		}
		Collection copy = new ArrayList();
		String siteID = site.getId();
		for (int i = 0; i < accessPaths.length; i++)
		{
			String testURL = accessPaths[i];
			int ix = testURL.indexOf("SITEID");
			if (ix != -1)
			{
				testURL = testURL.substring(0, ix) + siteID
						+ testURL.substring(ix + "SITEID".length());
			}
			logger.info("Testing " + testURL);
			Reference ref = EntityManager.newReference(testURL);
			logger.info("Got " + ref);
			EntityProducer service = ref.getEntityProducer();
			if (service != null)
			{
				MockHttpServletRequest req = new MockHttpServletRequest();
				MockHttpServletResponse res = new MockHttpServletResponse();
				HttpAccess ha = service.getHttpAccess();
				ha.handleAccess(req, res, ref, copy);
				logger.info("URL " + testURL + "Got response of "
						+ res.getContentAsString());
				Collection authZGroups = service.getEntityAuthzGroups(ref, null);
				logger.info("Reference  " + ref.getReference());
				for (Iterator ic = authZGroups.iterator(); ic.hasNext();)
				{
					String authZGroupID = (String) ic.next();
					logger.info("   AuthZGroup " + authZGroupID);
					try
					{
						AuthzGroup azg = authzGroupService
								.getAuthzGroup(authZGroupID);
						printFunction(azg, RWikiSecurityService.SECURE_ADMIN);
						printFunction(azg, RWikiSecurityService.SECURE_CREATE);
						printFunction(azg, RWikiSecurityService.SECURE_DELETE);
						printFunction(azg, RWikiSecurityService.SECURE_READ);
						printFunction(azg,
								RWikiSecurityService.SECURE_SUPER_ADMIN);
						printFunction(azg, RWikiSecurityService.SECURE_UPDATE);

					}
					catch (GroupNotDefinedException iduex)
					{
						logger.info("        Does not exist "
								+ iduex.getMessage());
					}
				}
			}
			else
			{
				logger.info("Rejected URL " + testURL + "");
			}
		}

	}

	/**
	 * Print a function in a AuthZGroup
	 * 
	 * @param azg
	 * @param function
	 */
	public void printFunction(AuthzGroup azg, String function)
	{
		logger.info("  Checking for " + function);
		Set roles = azg.getRolesIsAllowed(function);
		for (Iterator ri = roles.iterator(); ri.hasNext();)
		{
			logger.info("       " + String.valueOf(ri.next()) + " allowed to "
					+ function);
		}
	}

	/**
	 * Verift the find all operations in the object service. Some db's have
	 * problems with like statements
	 */
	public void xtestFindAll()
	{
		List l = rwikiObjectservice.findRWikiSubPages(site.getReference());
		if (l.size() == 0)
		{
			logger.info("Found " + l.size() + " pages in "
					+ site.getReference());
			logger.error(" Fialed to find any pages in " + site.getReference());
		}
		logger.info("Found " + l.size() + " pages ");
	}

	/**
	 * Test the entity access based on a URL, also fully tests the functions
	 * associated with the URL, need inspection to verify 100%
	 * 
	 * @throws Exception
	 */
	public void xtestEntityAccess() throws Exception
	{

		rwikiObjectservice.update("HomeTestPageENTITY", site.getReference(),
				new Date(), content[0]);

		RWikiObject rwo = rwikiObjectservice.getRWikiObject(
				"HomeTestPageENTITY", site.getReference());

		RWikiEntity rwe = (RWikiEntity) rwikiObjectservice.getEntity(rwo);
		logger.info("Reference is " + rwe.getReference());
		Reference r = EntityManager.newReference(rwe.getReference() + "html");

		logger.info("Reference found as " + r);
		logger.info("Reference Container " + r.getContainer());
		logger.info("Reference Contex " + r.getContext());
		logger.info("Reference Description " + r.getDescription());
		logger.info("Reference Type " + r.getType());
		Entity e = rwikiObjectservice.getEntity(r);
		assertNotNull("Entity is Null should not be", e);
		logger.info(" Got Entity from getEntity " + e);
		Collection c = rwikiObjectservice.getEntityAuthzGroups(r, null);
		assertNotNull("AuthZGroups  should not be null  ", c);
		logger.info("getAuthZGroups gave " + c);
		String description = rwikiObjectservice.getEntityDescription(r);
		assertNotNull("description  should not be null  ", description);
		logger.info("description gave " + description);

		ResourceProperties rp = rwikiObjectservice
				.getEntityResourceProperties(r);
		assertNotNull("ResourceProperties  should not be null  ", rp);
		logger.info("ResourceProperties gave " + rp);
		String url = rwikiObjectservice.getEntityUrl(r);
		assertNotNull("URL  should not be null  ", url);
		logger.info("URL gave " + url);

		// try and get the content

		Entity entity = rwikiObjectservice.getEntity(r);
		RWikiEntity rwentity = (RWikiEntity) entity;
		RWikiObject rwo2 = rwentity.getRWikiObject();

		logger.info("Got Object " + rwo2.getName());

		// try and get the access content
		HttpAccess ha = rwikiObjectservice.getHttpAccess();
		Collection copy = new ArrayList();
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();
		ha.handleAccess(req, res, r, copy);
		logger.info("Got response of " + res.getContentAsString());

	}

	/**
	 * Test the archive process, inspection of info output may be necessary
	 * 
	 * @throws Exception
	 */
	public void xtestArchiveAccess() throws Exception
	{
		rwikiObjectservice.update("HomeTestPageARCHIVE", site.getReference(),
				new Date(), content[0]);
		RWikiObject rwo = rwikiObjectservice.getRWikiObject(
				"HomeTestPageARCHIVE", site.getReference());
		rwikiObjectservice.update("HomeTestPageARCHIVE", site.getReference(),
				rwo.getVersion(), content[1]);

		rwikiObjectservice.update("HomeTestPage2ARCHIVE", site.getReference(),
				new Date(), content[0]);
		rwo = rwikiObjectservice.getRWikiObject("HomeTestPage2ARCHIVE", site
				.getReference());
		rwikiObjectservice.update("HomeTestPage2ARCHIVE", site.getReference(),
				rwo.getVersion(), content[1]);

		ArrayList attachments = new ArrayList();
		Document doc = Xml.createDocument();
		Stack stack = new Stack();
		Element root = doc.createElement("archive");
		doc.appendChild(root);
		root.setAttribute("source", site.getId());
		root.setAttribute("server", "Integration Test");
		root.setAttribute("date", (new Date()).toString());
		root.setAttribute("system", "SAKAI Integration Test");

		stack.push(root);
		File f = new File("./wikitestdir");
		f.mkdirs();
		String tmpdir = f.getAbsolutePath();
		rwikiObjectservice.archive(site.getId(), doc, stack, tmpdir,
				attachments);
		stack.pop();
		String archiveResult = Xml.writeDocumentToString(doc);
		logger.info("Got Archive \n" + archiveResult);

	}

	/**
	 * Test and import between sites
	 * 
	 * @throws Exception
	 */
	public void xtestImport() throws Exception
	{
		// create 2 pages, add their ids to the list, transfer to annother site,
		// check they were there
		List l = new ArrayList();
		rwikiObjectservice.update("HometestPageIMPORT", site.getReference(),
				new Date(), content[0]);
		RWikiObject rwo = rwikiObjectservice.getRWikiObject(
				"HometestPageIMPORT", site.getReference());
		l.add(rwo.getId());
		rwikiObjectservice.update("HometestPageIMPORT", site.getReference(),
				rwo.getVersion(), content[1]);

		rwikiObjectservice.update("HometestPage2IMPORT", site.getReference(),
				new Date(), content[0]);
		rwo = rwikiObjectservice.getRWikiObject("HometestPage2IMPORT", site
				.getReference());
		l.add(rwo.getId());
		rwikiObjectservice.update("HometestPage2IMPORT", site.getReference(),
				rwo.getVersion(), content[1]);

		rwikiObjectservice.transferCopyEntities(site.getReference(), targetSite
				.getReference(), l);
		assertEquals("HometestPage failed to import",true,rwikiObjectservice
				.exists("HometestPageIMPORT", targetSite.getReference()) );
		assertEquals("HometestPage2 failed to import",true,rwikiObjectservice
				.exists("HometestPage2IMPORT", targetSite.getReference()));
	}

	/**
	 * Test a merge operation from a standard archive file (static). If the
	 * archive format changes this may fail
	 */
	public void xtestMerge()
	{
		Document doc = Xml.readDocumentFromStream(this.getClass()
				.getResourceAsStream(archiveContentResource));
		String fromSiteId = doc.getDocumentElement().getAttribute("source");
		NodeList nl = doc
				.getElementsByTagName("uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService");
		for (int i = 0; i < nl.getLength(); i++)
		{
			Element el = (Element) nl.item(i);
			String results = rwikiObjectservice.merge(targetSite.getId(), el,
					"/tmp", fromSiteId, new HashMap(), new HashMap(),
					new HashSet());
			logger.info("Results of merge operation \n======\n" + results
					+ "\n=======");
		}
	}

	/**
	 * A quick test of the settings of the RWikiObjectServce from an
	 * EntityProducer point of view
	 */
	public void xtestBasicMethods()
	{
		assertEquals("Service was not as expected ", "wiki", rwikiObjectservice
				.getLabel());
		assertEquals("Expected to be able to archive  ", true,
				rwikiObjectservice.willArchiveMerge());
	}

	/**
	 * Test the preference service and preference resolution mecahnism
	 * 
	 * @throws Exception
	 */
	public void xtestPreference() throws Exception
	{
		rwikiObjectservice.update("HomeTestPagePreference",
				site.getReference(), new Date(), content[0]);
		RWikiObject rwo = rwikiObjectservice.getRWikiObject(
				"HomeTestPagePreference", site.getReference());
		RWikiEntity rwe = (RWikiEntity) rwikiObjectservice.getEntity(rwo);

		String ref1 = RWikiObjectService.REFERENCE_ROOT
				+ site.getReference().toLowerCase();
		String ref2 = rwe.getReference();
		String ref3 = RWikiObjectService.REFERENCE_ROOT
				+ targetSite.getReference().toLowerCase();

		logger.info("Site Reference = " + ref1);
		logger.info("Page Reference = " + ref2);
		logger.info("TargetSite Reference = " + ref3);
		logger.info("Page starts with Site " + ref2.startsWith(ref1));

		assertEquals("Site is not a subnode of page ", true, ref2
				.startsWith(ref1));

		rwikiObjectservice.update("HomeTestPagePreference",
				site.getReference(), rwo.getVersion(), content[1]);
		preferenceService.updatePreference("admin", ref1,
				PreferenceService.MAIL_NOTIFCIATION, "Yes");

		preferenceService.updatePreference("admin", ref2,
				PreferenceService.MAIL_NOTIFCIATION, "No");
		preferenceService.updatePreference("admin", ref3,
				PreferenceService.MAIL_NOTIFCIATION, "Maybe");

		String siteLevel = preferenceService.findPreferenceAt("admin", ref1,
				PreferenceService.MAIL_NOTIFCIATION);
		String pageLevel = preferenceService.findPreferenceAt("admin", ref2,
				PreferenceService.MAIL_NOTIFCIATION);

		String targetSiteLevel = preferenceService.findPreferenceAt("admin",
				ref3, PreferenceService.MAIL_NOTIFCIATION);
		assertEquals("Site Level didnt match ", "Yes", siteLevel);
		assertEquals("Subsite level didnt match ", "No", pageLevel);
		assertEquals("Target Site Preference didnt match", "Maybe",
				targetSiteLevel);
		preferenceService.deleteAllPreferences("admin", site.getReference(),
				PreferenceService.MAIL_NOTIFCIATION);
		preferenceService.deleteAllPreferences("admin", targetSite
				.getReference(), PreferenceService.MAIL_NOTIFCIATION);
		siteLevel = preferenceService.findPreferenceAt("admin", site
				.getReference(), PreferenceService.MAIL_NOTIFCIATION);
		pageLevel = preferenceService.findPreferenceAt("admin", rwe
				.getReference(), PreferenceService.MAIL_NOTIFCIATION);

		targetSiteLevel = preferenceService.findPreferenceAt("admin",
				targetSite.getReference(), PreferenceService.MAIL_NOTIFCIATION);
		// assertNull("Site Did not delete ", siteLevel);
		// assertNull("Subsite Did not delete ", pageLevel);
		// assertNull("Target Site did not delete ", targetSiteLevel);

	}

	/**
	 * Check for a lock on a site
	 * 
	 * @param user
	 *        the user
	 * @param reference
	 *        the reference
	 * @param lock
	 *        the lock to check for
	 * @param perms
	 *        a map of permissions found on the lock
	 */
	public void checkLock(String user, String reference, String lock, Map perms)
	{
		Set groups = authzGroupService
				.getAuthzGroupsIsAllowed(user, lock, null);
		perms.put(user + ":" + lock, "false");
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			String ref = i.next().toString();
			logger.debug(" Group for " + user + ":" + lock + " " + ref);
			if (ref.equals(reference))
			{
				perms.put(user + ":" + lock, "true");
			}
		}
	}

	/**
	 * Permissions we expect to se as granted
	 */
	String[] truePerms = { "test.user.1:rwiki.create",
			"test.user.1:rwiki.update", "test.user.1:rwiki.read" };

	/**
	 * Permissions we expect to see as denied
	 */
	String[] falsePerms = { "test.user.1:rwiki.admin",
			"test.user.1:rwiki.delete", "test.user.1:rwiki.superadmin",
			"test.user.2:rwiki.admin", "test.user.2:rwiki.delete",
			"test.user.2:rwiki.superadmin", "test.ta.1:rwiki.admin",
			"test.ta.1:rwiki.delete", "test.ta.1:rwiki.superadmin",
			"dummy:rwiki.admin", "dummy:rwiki.delete",
			"dummy:rwiki.superadmin", "dummy:rwiki.create",
			"dummy:rwiki.update", "dummy:rwiki.read",
			"test.user.2:rwiki.create", "test.user.2:rwiki.update",
			"test.user.2:rwiki.read", "test.ta.1:rwiki.create",
			"test.ta.1:rwiki.update", "test.ta.1:rwiki.read" };

	/**
	 * Exercises the permissions mechanisms checking first at the lowest levels
	 * and then backing out to higher levels eventually working on
	 * RWikiSecurityService. Checks for expected responses, and throws and
	 * exception if failure. Depends on a standard setup in the DB. If this
	 * changes, this test will fail
	 * 
	 * @throws Exception
	 */
	public void xtestPermissions() throws Exception
	{
		rwikiObjectservice.update("HomeTestPagePermissions", site
				.getReference(), new Date(), content[0]);
		RWikiObject rwo = rwikiObjectservice.getRWikiObject(
				"HomeTestPagePermissions", site.getReference());

		String user = "test.user.1";
		HashMap perms = new HashMap();
		String siteRef = site.getReference();

		logger.info("Current Site Ref " + siteRef);

		checkLock(user, siteRef, RWikiSecurityService.SECURE_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_CREATE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_DELETE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_READ, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_SUPER_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_UPDATE, perms);

		user = "test.user.2";
		checkLock(user, siteRef, RWikiSecurityService.SECURE_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_CREATE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_DELETE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_READ, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_SUPER_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_UPDATE, perms);

		user = "test.ta.1";
		checkLock(user, siteRef, RWikiSecurityService.SECURE_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_CREATE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_DELETE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_READ, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_SUPER_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_UPDATE, perms);

		user = "dummy";
		checkLock(user, siteRef, RWikiSecurityService.SECURE_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_CREATE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_DELETE, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_READ, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_SUPER_ADMIN, perms);
		checkLock(user, siteRef, RWikiSecurityService.SECURE_UPDATE, perms);

		for (int i = 0; i < truePerms.length; i++)
		{
			assertEquals(" Permissions Check " + truePerms[i], "true", perms
					.get(truePerms[i]));
		}
		for (int i = 0; i < falsePerms.length; i++)
		{
			assertEquals(" Permissions Check " + falsePerms[i], "false", perms
					.get(falsePerms[i]));
		}

		HashMap users = new HashMap();
		users.put("test.user.1", userDirService.getUser("test.user.1"));
		users.put("test.user.2", userDirService.getUser("test.user.2"));
		users.put("test.ta.1", userDirService.getUser("test.ta.1"));
		for (Iterator i = perms.keySet().iterator(); i.hasNext();)
		{
			String key = (String) i.next();
			String[] dec = key.split(":");
			String expected = (String) perms.get(key);
			String val = "false";
			String utest = dec[0];
			String lock = dec[1];
			User u = (User) users.get(utest);
			String userid = utest + " does not exist";

			if (u != null && securityService.unlock(u, lock, siteRef))
			{
				val = "true";
			}
			if (u != null)
			{
				userid = utest + " " + u.getId();
			}
			logger.debug("Evaluating " + userid + "--" + lock + "::" + key
					+ "==" + expected);
			assertEquals("Check lock " + userid + "--" + lock + "::" + key,
					expected, val);
		}

		logger.info(" All Sakai Security Service Locks pass ");
		RWikiEntity rwe = (RWikiEntity) rwikiObjectservice.getEntity(rwo);
		String entityRef = rwe.getReference();
		try
		{
			for (Iterator i = perms.keySet().iterator(); i.hasNext();)
			{
				String key = (String) i.next();
				String[] dec = key.split(":");
				String expected = (String) perms.get(key);
				String val = "false";
				String utest = dec[0];
				String lock = dec[1];
				boolean locktest = false;

				setUser(dec[0]);

				logger.debug("Set User to " + dec[0] + " checking " + lock
						+ " on " + siteRef);

				if (RWikiSecurityService.SECURE_ADMIN.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkAdminPermission(siteRef);
				}
				else if (RWikiSecurityService.SECURE_CREATE.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkCreatePermission(siteRef);
				}
				else if (RWikiSecurityService.SECURE_READ.equals(lock))
				{
					locktest = rwikiSecurityService.checkGetPermission(siteRef);
				}
				else if (RWikiSecurityService.SECURE_SUPER_ADMIN.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkSuperAdminPermission(siteRef);
				}
				else if (RWikiSecurityService.SECURE_UPDATE.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkUpdatePermission(siteRef);
				}

				if (locktest)
				{
					val = "true";
				}
				logger.debug("Evaluating " + utest + "--" + lock + "::" + key
						+ "==" + expected);
				assertEquals("Check RWikiSecurityService on Site  " + utest
						+ "--" + lock + "::" + key, expected, val);
			}
		}
		finally
		{
			setUser("admin");
		}
		try
		{
			for (Iterator i = perms.keySet().iterator(); i.hasNext();)
			{
				String key = (String) i.next();
				String[] dec = key.split(":");
				String expected = (String) perms.get(key);
				String val = "false";
				String utest = dec[0];
				String lock = dec[1];
				boolean locktest = false;

				setUser(dec[0]);

				logger.debug("Set User to " + dec[0] + " checking " + lock
						+ " on " + entityRef);

				if (RWikiSecurityService.SECURE_ADMIN.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkAdminPermission(entityRef);
				}
				else if (RWikiSecurityService.SECURE_CREATE.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkCreatePermission(entityRef);
				}
				else if (RWikiSecurityService.SECURE_READ.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkGetPermission(entityRef);
				}
				else if (RWikiSecurityService.SECURE_SUPER_ADMIN.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkSuperAdminPermission(entityRef);
				}
				else if (RWikiSecurityService.SECURE_UPDATE.equals(lock))
				{
					locktest = rwikiSecurityService
							.checkUpdatePermission(entityRef);
				}

				if (locktest)
				{
					val = "true";
				}
				logger.debug("Evaluating " + utest + "--" + lock + "::" + key
						+ "==" + expected);
				assertEquals("Check RWikiSecurityService on RWikiObject "
						+ utest + "--" + lock + "::" + key, expected, val);
			}
		}
		finally
		{
			setUser("admin");
		}

		setUser("admin");
		assertEquals("Check admin checkAdmin ", true, rwikiObjectservice
				.checkAdmin(rwo));
		assertEquals("Check admin checkRead ", true, rwikiObjectservice
				.checkRead(rwo));
		assertEquals("Check admin checkUpdate ", true, rwikiObjectservice
				.checkUpdate(rwo));
		try
		{
			setUser("test.user.1");
			assertEquals("Check test.user.1 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.user.1 checkRead ", true,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.user.1 checkUpdate ", true,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("test.ta.1");
			assertEquals("Check test.ta.1 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.ta.1 checkRead ", false,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.ta.1 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("test.user.2");
			assertEquals("Check test.user.2 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.user.2 checkRead ", false,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.user.2 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("null");
			assertEquals("Check null checkAdmin ", false, rwikiObjectservice
					.checkAdmin(rwo));
			assertEquals("Check null checkRead ", false, rwikiObjectservice
					.checkRead(rwo));
			assertEquals("Check null checkUpdate ", false, rwikiObjectservice
					.checkUpdate(rwo));
		}
		finally
		{

			setUser("admin");

		}

		rwo.setGroupAdmin(false);
		rwo.setGroupRead(false);
		rwo.setGroupWrite(false);
		rwo.setPublicRead(false);
		rwo.setPublicWrite(false);
		rwikiObjectservice.update(rwo.getName(), rwo.getRealm(), rwo
				.getVersion(), rwo.getPermissions());
		rwo = rwikiObjectservice.getRWikiObject(rwo);
		rwe = (RWikiEntity) rwikiObjectservice.getEntity(rwo);

		assertEquals("Check admin checkAdmin ", true, rwikiObjectservice
				.checkAdmin(rwo));
		assertEquals("Check admin checkRead ", true, rwikiObjectservice
				.checkRead(rwo));
		assertEquals("Check admin checkUpdate ", true, rwikiObjectservice
				.checkUpdate(rwo));
		try
		{
			setUser("test.user.1");
			assertEquals("Check test.user.1 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.user.1 checkRead ", false,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.user.1 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("test.ta.1");
			assertEquals("Check test.ta.1 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.ta.1 checkRead ", false,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.ta.1 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("test.user.2");
			assertEquals("Check test.user.2 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.user.2 checkRead ", false,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.user.2 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("null");
			assertEquals("Check null checkAdmin ", false, rwikiObjectservice
					.checkAdmin(rwo));
			assertEquals("Check null checkRead ", false, rwikiObjectservice
					.checkRead(rwo));
			assertEquals("Check null checkUpdate ", false, rwikiObjectservice
					.checkUpdate(rwo));
		}
		finally
		{

			setUser("admin");

		}
		rwo.setGroupAdmin(true);
		rwo.setGroupRead(true);
		rwo.setGroupWrite(true);
		rwo.setPublicRead(true);
		rwo.setPublicWrite(false);
		rwikiObjectservice.update(rwo.getName(), rwo.getRealm(), rwo
				.getVersion(), rwo.getPermissions());
		rwo = rwikiObjectservice.getRWikiObject(rwo);
		rwe = (RWikiEntity) rwikiObjectservice.getEntity(rwo);

		assertEquals("Check admin checkAdmin ", true, rwikiObjectservice
				.checkAdmin(rwo));
		assertEquals("Check admin checkRead ", true, rwikiObjectservice
				.checkRead(rwo));
		assertEquals("Check admin checkUpdate ", true, rwikiObjectservice
				.checkUpdate(rwo));
		try
		{
			setUser("test.user.1");
			assertEquals("Check test.user.1 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.user.1 checkRead ", true,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.user.1 checkUpdate ", true,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("test.ta.1");
			assertEquals("Check test.ta.1 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.ta.1 checkRead ", true, rwikiObjectservice
					.checkRead(rwo));
			assertEquals("Check test.ta.1 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("test.user.2");
			assertEquals("Check test.user.2 checkAdmin ", false,
					rwikiObjectservice.checkAdmin(rwo));
			assertEquals("Check test.user.2 checkRead ", true,
					rwikiObjectservice.checkRead(rwo));
			assertEquals("Check test.user.2 checkUpdate ", false,
					rwikiObjectservice.checkUpdate(rwo));

			setUser("null");
			assertEquals("Check null checkAdmin ", false, rwikiObjectservice
					.checkAdmin(rwo));
			assertEquals("Check null checkRead ", true, rwikiObjectservice
					.checkRead(rwo));
			assertEquals("Check null checkUpdate ", false, rwikiObjectservice
					.checkUpdate(rwo));
		}
		finally
		{

			setUser("admin");

		}

	}

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

	
	
	// Stolen shamelessly from test-harness, since I want to run these test inside exlipse
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
