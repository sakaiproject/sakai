/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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
package org.sakaiproject.site.impl.test;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

@Slf4j
public class SiteAliasCleanupNotificationActionIntegrationTest extends SakaiKernelTestBase {
	private Session session;

	@BeforeClass
	public static void beforeClass() {
		try {
			oneTimeSetup();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	@Before
	public void setUp() throws Exception {
		startSession();
	}


	@Test
	public void testSiteDeletionTriggersSiteAliasDeletion()
	throws IdInvalidException, IdUsedException, PermissionException {
		IdManager idManager = getService(IdManager.class);
		SiteService siteService = getService(SiteService.class);
		AliasService aliasService = getService(AliasService.class);
		
		// fixture setup
		Site site = siteService.addSite(idManager.createUuid(), (String)null);
		final String siteAliasId1 = "site-alias-1";
		final String siteAliasId2 = "site-alias-2";
		aliasService.setAlias(siteAliasId1, site.getReference());
		aliasService.setAlias(siteAliasId2, site.getReference());
		
		// sanity check
		List createdSiteAliases = aliasService.getAliases(site.getReference());
		Assert.assertFalse("Expected at least one alias to be created during fixture setup", 
				createdSiteAliases.isEmpty());
		
		// the "real" code exercise
		try {
			// Mark as deleted.
			siteService.removeSite(site);
			// Remove the deleted site.
			siteService.removeSite(site);
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}
		
		List remainingSiteAliases = aliasService.getAliases(site.getReference());
		Assert.assertEquals("Expected all site aliases to be deleted on site deletion", 0, 
				remainingSiteAliases.size());
	}
	
	private void startSession() {
		UsageSessionService usageSessionService = getService(UsageSessionService.class);
		usageSessionService.startSession("admin", "localhost", "integration-tests");
		SessionManager sessionManager = getService(SessionManager.class);
		session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
	}
	
	private void endSession() {
		if ( session != null ) session.invalidate();
	}
	
	@After
	public void tearDown() throws Exception {
		endSession();
	}
	
}
