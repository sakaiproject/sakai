/**********************************************************************************
 * Copyright 2010 Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * @author chall
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ExternalLogicImplTest {
	ExternalLogicImpl impl;

	@Mock
	ConfigLogic configLogic;
	@Mock
	FunctionManager functionManager;
	@Mock
	MailArchiveService mailArchiveService;
	@Mock
	SecurityService securityService;
	@Mock
	ServerConfigurationService serverConfigurationService;
	@Mock
	SessionManager sessionManager;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	SiteService siteService;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	ToolManager toolManager;
	@Mock
	UserDirectoryService userDirectoryService;
	@Mock
	Site site;
	@Mock
	User user;

	static final String LOCATION_ID = "locationId";
	static final String LOCATION_TITLE = "Location Title";
	static final String USER_ID = "userId";
	static final String USER_DISPLAY_NAME = "User Displayname";
	static final String SITE_TYPE = "project";

	@Before
	public void setUp() throws Exception {

		when(toolManager.getCurrentPlacement().getContext()).thenReturn(
				LOCATION_ID);
		when(site.getTitle()).thenReturn(LOCATION_TITLE);
		when(site.getReference()).thenReturn(LOCATION_ID);
		when(siteService.getSite(LOCATION_ID)).thenReturn(site);
		when(siteService.getSite(LOCATION_ID).getType()).thenReturn(SITE_TYPE);
		when(userDirectoryService.getCurrentUser()).thenReturn(user);
		when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
		when(userDirectoryService.getUser(USER_ID)).thenReturn(user);
		when(user.getDisplayName()).thenReturn(USER_DISPLAY_NAME);

		impl = new ExternalLogicImpl();
		impl.setConfigLogic(configLogic);
		impl.setFunctionManager(functionManager);
		impl.setMailArchiveService(mailArchiveService);
		impl.setSecurityService(securityService);
		impl.setServerConfigurationService(serverConfigurationService);
		impl.setSessionManager(sessionManager);
		impl.setSiteService(siteService);
		impl.setToolManager(toolManager);
		impl.setUserDirectoryService(userDirectoryService);
	}

	@Test
	public void init() throws Exception {
		impl.init();
		verify(functionManager).registerFunction(ExternalLogic.PERM_ADMIN);
		verify(functionManager).registerFunction(ExternalLogic.PERM_SEND);
	}

	@Test
	public void getCurrentLocationId() throws Exception {
		assertEquals(LOCATION_ID, impl.getCurrentLocationId());
	}

	@Test
	public void getCurrentSite() throws Exception {
		Site s = impl.getCurrentSite();
		assertNotNull(impl.getCurrentSite());
		assertEquals(site, s);
	}

	@Test
	public void cantGetCurrentSite() throws Exception {
		reset(siteService);

		when(siteService.getSite(LOCATION_ID)).thenThrow(
				new IdUnusedException(LOCATION_ID));

		Site s = impl.getCurrentSite();
		assertNull(s);
	}

	@Test
	public void getCurrentSiteTitle() throws Exception {
		assertEquals(LOCATION_TITLE, impl.getCurrentSiteTitle());
	}

	@Test
	public void getCurrentUser() throws Exception {
		assertEquals(user, impl.getCurrentUser());
	}

	@Test
	public void getCurrentUserId() throws Exception {
		assertEquals(USER_ID, impl.getCurrentUserId());
	}

	@Test
	public void getSiteId() throws Exception {
		assertEquals(LOCATION_ID, impl.getSiteID());
	}

	@Test
	public void getSiteRealmId() throws Exception {
		assertEquals("/site/" + LOCATION_ID, impl.getSiteRealmID());
	}

	@Test
	public void getSiteType() throws Exception {
		assertEquals(SITE_TYPE, impl.getSiteType());
	}

	@Test
	public void cantGetSiteType() throws Exception {
		reset(siteService);
		when(siteService.getSite(LOCATION_ID)).thenThrow(
				new IdUnusedException(LOCATION_ID));

		String type = impl.getSiteType();

		assertNull(type);
	}

	@Test
	public void getUser() throws Exception {
		User u = impl.getUser(USER_ID);
		assertNotNull(u);
		assertEquals(user, u);
	}

	@Test
	public void cantGetUser() throws Exception {
		reset(userDirectoryService);
		when(userDirectoryService.getUser(USER_ID)).thenThrow(
				new UserNotDefinedException(USER_ID));
		User u = impl.getUser(USER_ID);
		assertNull(u);
	}

	@Test
	public void getUserDisplayName() throws Exception {
		assertEquals(USER_DISPLAY_NAME, impl.getUserDisplayName(USER_ID));
	}

	@Test
	public void isUserSiteAdmin() {
		when(
				securityService
						.unlock(USER_ID,
								org.sakaiproject.site.api.SiteService.SECURE_UPDATE_SITE,
								LOCATION_ID)).thenReturn(true)
				.thenReturn(false);
		assertTrue(impl.isUserSiteAdmin(USER_ID, LOCATION_ID));
		assertFalse(impl.isUserSiteAdmin(USER_ID, LOCATION_ID));
	}

	@Test
	public void isUserAllowedInLocation() {
		when(
				securityService.unlock(isA(String.class), isA(String.class),
						isA(String.class))).thenReturn(true).thenReturn(false);
		assertTrue(impl.isUserAllowedInLocation(USER_ID, "perm1", LOCATION_ID));
		assertFalse(impl.isUserAllowedInLocation(USER_ID, "perm2", LOCATION_ID));
	}

	@Test
	public void isUserAdmin() {
		when(securityService.isSuperUser(isA(String.class))).thenReturn(true).thenReturn(false);
		assertTrue(impl.isUserAdmin(USER_ID));
		assertFalse(impl.isUserAdmin(USER_ID));
	}
}
