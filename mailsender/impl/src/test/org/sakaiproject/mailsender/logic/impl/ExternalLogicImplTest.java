/**********************************************************************************
 * Copyright 2010 Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ECL-2.0
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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveMessageEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeaderEdit;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import javax.activation.DataSource;

/**
 * @author chall
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ExternalLogicImplTest {
	ExternalLogicImpl impl;

	@Mock
	FunctionManager functionManager;
	@Mock
	TimeService timeService;
	@Mock
	MailArchiveService mailArchiveService;
	@Mock
	SecurityService securityService;
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
    @Mock
    EmailService emailService;
    @Mock
    ServerConfigurationService configService;
    @Mock
    EventTrackingService eventService;
    @Mock
    EntityManager entityManager;
    @Mock
    ContentHostingService contentHostingService;

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
        impl.setEmailService(emailService);
		impl.setTimeService(timeService);
		impl.setFunctionManager(functionManager);
		impl.setMailArchiveService(mailArchiveService);
		impl.setSecurityService(securityService);
		impl.setSessionManager(sessionManager);
		impl.setSiteService(siteService);
		impl.setToolManager(toolManager);
		impl.setUserDirectoryService(userDirectoryService);
		impl.setServerConfigurationService(configService);
		impl.setEventTrackingService(eventService);
		impl.setEntityManager(entityManager);
		impl.setContentHostingService(contentHostingService);

		impl.init();
	}

	@Test
	public void init() throws Exception {
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
		when(securityService.isSuperUser(isA(String.class))).thenReturn(true)
				.thenReturn(false);
		assertTrue(impl.isUserAdmin(USER_ID));
		assertFalse(impl.isUserAdmin(USER_ID));
	}

	@Test
	public void getPermissionKeys() {
		assertNotNull(impl.getPermissionKeys());
	}

	@Test(expected = MailsenderException.class)
	public void sendMailNullFrom() throws Exception {
		impl.sendEmail(null, null, null, null, null, null, null);
		fail("Must define 'from'");
	}

	@Test(expected = MailsenderException.class)
	public void sendMailEmptyFrom() throws Exception {
		impl.sendEmail(null, "", null, null, null, null, null);
		fail("Must define 'from'");
	}

	@Test(expected = MailsenderException.class)
	public void sendMailNullTo() throws Exception {
		impl.sendEmail(null, "from@example.com", null, null, null, null,
				null);
		fail("Must define 'to'");
	}

	@Test(expected = MailsenderException.class)
	public void sendMailEmptyTo() throws Exception {
		impl.sendEmail(null, "from@example.com", null,
				new HashMap<String, String>(), null, null, null);
		fail("Must define 'to'");
	}

	@Test
	public void sendMailRequiredArgs() throws Exception {
		HashMap<String, String> to = new HashMap<String, String>();
		to.put("test", "test");
		impl.sendEmail(null, "from@example.com", null, to, null, null, null);
	}

	@Test
	public void emailArchiveIsNotAddedToSite() throws Exception {
		MailArchiveChannel channel = mock(MailArchiveChannel.class);
		MailArchiveMessageEdit msg = mock(MailArchiveMessageEdit.class);
		MailArchiveMessageHeaderEdit header = mock(MailArchiveMessageHeaderEdit.class);

		when(mailArchiveService.getMailArchiveChannel("channel"))
				.thenThrow(new PermissionException(null, null, null)) // #1
				.thenReturn(null) // #2
				.thenReturn(channel); // #3
		when(channel.addMessage())
				.thenThrow(new PermissionException(null, null, null)) // #3
				.thenReturn(msg); // #4
		when(msg.getMailArchiveHeaderEdit()).thenReturn(header);

		// #1
		assertFalse("Permission exception from getMailArchiveChannel() should return false",
				impl.addToArchive(null, "channel", null, null, null, null));
		// #2
		assertFalse("Need a non-null channel",
				impl.addToArchive(null, "channel", null, null, null, null));
		// #3
		assertFalse("Permission exception from addMessage() should return false",
				impl.addToArchive(null, "channel", null, null, null, null));
		// #4
		assertTrue(impl.addToArchive(null, "channel", null, null, null, null));

		verify(channel).commitMessage(eq(msg), eq(NotificationService.NOTI_NONE));
	}

	@Test
	public void sendEmailToArchiveWithAttachments() throws Exception {
		// Simple test of adding email to archive.
		MailArchiveChannel channel = mock(MailArchiveChannel.class);
		MailArchiveMessageEdit message = mock(MailArchiveMessageEdit.class);
		MailArchiveMessageHeaderEdit headers = mock(MailArchiveMessageHeaderEdit.class);

		when(channel.addMessage()).thenReturn(message);
		when(message.getMailArchiveHeaderEdit()).thenReturn(headers);
		when(message.getId()).thenReturn("messageId");

		when(channel.getContext()).thenReturn("siteId");

		ContentResource resource = mock(ContentResource.class);
		ResourcePropertiesEdit attachmentProperties = mock(ResourcePropertiesEdit.class);

		when(contentHostingService.newResourceProperties()).thenReturn(attachmentProperties);
		when(contentHostingService.addAttachmentResource(
				anyString(), anyString(), eq(null), anyString(), any(InputStream.class), any(ResourceProperties.class)
		)).thenReturn(resource);
		when(resource.getReference()).thenReturn("attachmentReference");

		Reference ref = mock(Reference.class);
		when(entityManager.newReference("attachmentReference")).thenReturn(ref);

		when(mailArchiveService.getMailArchiveChannel("channel")).thenReturn(channel);

		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getInputStream()).thenReturn(new ByteArrayInputStream("Hello World".getBytes("UTF-8")));
		Attachment attachment = mock(Attachment.class);
		when(attachment.getContentTypeHeader()).thenReturn("text/plain");
		when(attachment.getFilename()).thenReturn("test.txt");
		when(attachment.getDataSource()).thenReturn(dataSource);

		impl.addToArchive(null, "channel", "sender@example.com", "Subject", "Body Message",
				Collections.singletonList(attachment));

		verify(channel).commitMessage(message, NotificationService.NOTI_NONE);
		verify(headers).addAttachment(ref);
	}
}
