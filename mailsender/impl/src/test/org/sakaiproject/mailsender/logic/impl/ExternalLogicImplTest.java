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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_ALLOW_TRANSPORT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_PORT;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.EditorType;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.web.multipart.MultipartFile;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * @author chall
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ExternalLogicImplTest {
	private static final String CONTENT_DISPOSITION_ATTACHMENT = "Content-Disposition: attachment;";

	ExternalLogicImpl impl;

	ConfigLogicImpl configLogic;
	@Mock
	FunctionManager functionManager;
	@Mock
	TimeService timeService;
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

	Wiser mailServer;

	static final String LOCATION_ID = "locationId";
	static final String LOCATION_TITLE = "Location Title";
	static final String USER_ID = "userId";
	static final String USER_DISPLAY_NAME = "User Displayname";
	static final String SITE_TYPE = "project";

	private static final boolean DEBUG = false;
	private static final String QUOTED_PRINTABLE = "quoted-printable";

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

		configLogic = new ConfigLogicImpl();
		configLogic.setExternalLogic(impl);
		configLogic.setServerConfigurationService(serverConfigurationService);
		configLogic.setToolManager(toolManager);

		impl = new ExternalLogicImpl();
		impl.setConfigLogic(configLogic);
		impl.setTimeService(timeService);
		impl.setFunctionManager(functionManager);
		impl.setMailArchiveService(mailArchiveService);
		impl.setSecurityService(securityService);
		impl.setServerConfigurationService(serverConfigurationService);
		impl.setSessionManager(sessionManager);
		impl.setSiteService(siteService);
		impl.setToolManager(toolManager);
		impl.setUserDirectoryService(userDirectoryService);

		when(serverConfigurationService.getString(MailConstants.SAKAI_HOST,
						ExternalLogicImpl.DEFAULT_SMTP_HOST)).thenReturn(
				ExternalLogicImpl.DEFAULT_SMTP_HOST);
		when(serverConfigurationService.getInt(MailConstants.SAKAI_PORT,
						ExternalLogicImpl.DEFAULT_SMTP_PORT)).thenReturn(
				ExternalLogicImpl.DEFAULT_SMTP_PORT);

		impl.init();
	}

	@After
	public void tearDown() throws Exception {
		if (mailServer != null) {
			if (DEBUG) {
				List<WiserMessage> msgs = mailServer.getMessages();
				System.out.println(msgs.size() + " messages");
				for (WiserMessage msg : msgs) {
					MimeMessage mimeMsg = msg.getMimeMessage();
					String output = getContent(mimeMsg);
					System.out.println("=========================");
					System.out.println("from: " + msg.getEnvelopeSender()
							+ ", to: " + msg.getEnvelopeReceiver()
							+ ", content-type: " + mimeMsg.getContentType()
							+ ", content:\n" + output);

					InputStream rawIs = mimeMsg.getRawInputStream();
					StringBuilder sb = new StringBuilder();
					String line;
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(rawIs));
						while ((line = reader.readLine()) != null) {
							sb.append(line).append("\n");
						}
					} finally {
						rawIs.close();
					}
					System.out.println("\n::: raw output :::\n" + sb.toString());
				}
			}
			mailServer.stop();
		}
	}

	private String getContent(MimeMessage mimeMsg) throws IOException,
			MessagingException {
		Object msgContent = mimeMsg.getContent();

		if (msgContent instanceof String) {
			return (String) msgContent;
		} else if (msgContent instanceof MimeMultipart) {
			MimeMultipart content = (MimeMultipart) msgContent;

			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			content.writeTo(byteOut);
			return byteOut.toString();
		} else {
			return null;
		}
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
	public void sendMailMissingFrom() throws Exception {
		try {
			impl.sendEmail(null, null, null, null, null, null, null);
			fail("Must define 'from'");
		} catch (MailsenderException e) {
			// expected
		}

		try {
			impl.sendEmail(null, "", null, null, null, null, null);
			fail("Must define 'from'");
		} catch (MailsenderException e) {
			// expected
		}
	}

	@Test
	public void sendMailMissingTo() throws Exception {
		try {
			impl.sendEmail(null, "from@example.com", null, null, null, null,
					null);
			fail("Must define 'to'");
		} catch (MailsenderException e) {
			// expected
		}

		try {
			impl.sendEmail(null, "from@example.com", null,
					new HashMap<String, String>(), null, null, null);
			fail("Must define 'from'");
		} catch (MailsenderException e) {
			// expected
		}
	}

	@Test
	public void sendPlainMailNoAttachments() throws Exception {
		ConfigEntry config = configLogic.getConfig();
		config.setEditorType(EditorType.htmlarea.toString());

		String fromEmail = "from@example.com";
		String fromName = "Potamus, Peter";
		HashMap<String, String> to = new HashMap<String, String>();
		to.put("to@example.com", "Birdman, Harvey");
		String subject = "That thing I sent you";
		String body = "You get that thing I sent you?";
		HashMap<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();

		int port = startServer();

		Mockito.reset(serverConfigurationService);
		when(serverConfigurationService.getString(eq(MailConstants.SAKAI_HOST),
				isA(String.class))).thenReturn(
						ExternalLogicImpl.DEFAULT_SMTP_HOST);
		when(serverConfigurationService.getInt(eq(SAKAI_PORT), isA(Integer.class)))
				.thenReturn(port);
		when(serverConfigurationService.getBoolean(eq(SAKAI_ALLOW_TRANSPORT),
				isA(Boolean.class))).thenReturn(true);

		impl.init();
		impl.sendEmail(config, fromEmail, fromName, to, subject, body,
				attachments);

		List<WiserMessage> msgs = mailServer.getMessages();
		assertEquals(1, msgs.size());
		WiserMessage msg = msgs.get(0);
		assertEquals(fromEmail, msg.getEnvelopeSender());
		assertEquals("to@example.com", msg.getEnvelopeReceiver());
		MimeMessage mimeMsg = msg.getMimeMessage();
		assertEquals(subject, mimeMsg.getSubject());
		String content = (String) mimeMsg.getContent();
		assertEquals(body, content);
		assertFalse(content.contains(CONTENT_DISPOSITION_ATTACHMENT));
	}

	@Test
	public void sendMailNoAttachments() throws Exception {
		ConfigEntry config = configLogic.getConfig();
		String fromEmail = "from@example.com";
		String fromName = "Potamus, Peter";
		HashMap<String, String> to = new HashMap<String, String>();
		to.put("to@example.com", "Birdman, Harvey");
		String subject = "That thing I sent you";
		String body = "You get <em>that thing</em> I sent you?\n3x6=18\nåæÆÐ";
		HashMap<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();

		int port = startServer();

		Mockito.reset(serverConfigurationService);
		when(serverConfigurationService.getString(eq(MailConstants.SAKAI_HOST),
				isA(String.class))).thenReturn(
						ExternalLogicImpl.DEFAULT_SMTP_HOST);
		when(serverConfigurationService.getInt(eq(SAKAI_PORT), isA(Integer.class)))
				.thenReturn(port);
		when(serverConfigurationService.getBoolean(eq(SAKAI_ALLOW_TRANSPORT),
				isA(Boolean.class))).thenReturn(true);

		impl.init();
		impl.sendEmail(config, fromEmail, fromName, to, subject, body,
				attachments);

		List<WiserMessage> msgs = mailServer.getMessages();
		assertEquals(1, msgs.size());
		WiserMessage msg = msgs.get(0);
		assertEquals(fromEmail, msg.getEnvelopeSender());
		assertEquals("to@example.com", msg.getEnvelopeReceiver());
		MimeMessage mimeMsg = msg.getMimeMessage();
		assertEquals(subject, mimeMsg.getSubject());
		String content = getContent(mimeMsg);
		assertTrue(content.contains(QUOTED_PRINTABLE));
		assertFalse(content.contains("åæÆÐ"));
		assertFalse(content.contains(CONTENT_DISPOSITION_ATTACHMENT));
	}

	@Test
	public void sendPlainMailWithAttachments() throws Exception {
		ConfigEntry config = configLogic.getConfig();
		config.setEditorType(EditorType.htmlarea.toString());

		String fromEmail = "from@example.com";
		String fromName = "Potamus, Peter";
		HashMap<String, String> to = new HashMap<String, String>();
		to.put("to@example.com", "Birdman, Harvey");
		String subject = "That thing I sent you";
		String body = "You get that thing I sent you?\n3x6=18\nåæÆÐ";
		HashMap<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();
		MultipartFile attachment = createAttachment("greatfile.txt");
		attachments.put("greatfile.txt", attachment);
		int port = startServer();

		Mockito.reset(serverConfigurationService);
		when(serverConfigurationService.getString(eq(MailConstants.SAKAI_HOST),
				isA(String.class))).thenReturn(
						ExternalLogicImpl.DEFAULT_SMTP_HOST);
		when(serverConfigurationService.getInt(eq(SAKAI_PORT), isA(Integer.class)))
				.thenReturn(port);
		when(serverConfigurationService.getBoolean(eq(SAKAI_ALLOW_TRANSPORT),
				isA(Boolean.class))).thenReturn(true);
		
		impl.init();
		impl.sendEmail(config, fromEmail, fromName, to, subject, body,
				attachments);

		List<WiserMessage> msgs = mailServer.getMessages();
		assertEquals(1, msgs.size());
		WiserMessage msg = msgs.get(0);
		assertEquals(fromEmail, msg.getEnvelopeSender());
		assertEquals("to@example.com", msg.getEnvelopeReceiver());
		MimeMessage mimeMsg = msg.getMimeMessage();
		assertEquals(subject, mimeMsg.getSubject());
		String content = getContent(mimeMsg);
		assertFalse(content.contains("åæÆÐ"));
		assertTrue(content.contains(CONTENT_DISPOSITION_ATTACHMENT));
		assertTrue(content.contains("filename=" + attachment.getName()));
	}

	@Test
	public void sendMailWithAttachments() throws Exception {
		ConfigEntry config = configLogic.getConfig();
		String fromEmail = "from@example.com";
		String fromName = "Potamus, Peter";
		HashMap<String, String> to = new HashMap<String, String>();
		to.put("to@example.com", "Birdman, Harvey");
		String subject = "That thing I sent you";
		String body = "You get <em>that thing</em> I sent you?\n3x6=18\nåæÆÐ";
		HashMap<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();
		MultipartFile attachment = createAttachment("greatfile.txt");
		attachments.put("greatfile.txt", attachment);
		int port = startServer();

		Mockito.reset(serverConfigurationService);
		when(serverConfigurationService.getString(eq(MailConstants.SAKAI_HOST),
				isA(String.class))).thenReturn(
						ExternalLogicImpl.DEFAULT_SMTP_HOST);
		when(serverConfigurationService.getInt(eq(SAKAI_PORT), isA(Integer.class)))
				.thenReturn(port);
		when(serverConfigurationService.getBoolean(eq(SAKAI_ALLOW_TRANSPORT),
				isA(Boolean.class))).thenReturn(true);
		
		impl.init();
		impl.sendEmail(config, fromEmail, fromName, to, subject, body,
				attachments);

		List<WiserMessage> msgs = mailServer.getMessages();
		assertEquals(1, msgs.size());
		WiserMessage msg = msgs.get(0);
		assertEquals(fromEmail, msg.getEnvelopeSender());
		assertEquals("to@example.com", msg.getEnvelopeReceiver());
		MimeMessage mimeMsg = msg.getMimeMessage();
		assertEquals(subject, mimeMsg.getSubject());
		String content = getContent(mimeMsg);
		assertTrue(content.contains(QUOTED_PRINTABLE));
		assertFalse(content.contains("åæÆÐ"));
		assertTrue(content.contains(CONTENT_DISPOSITION_ATTACHMENT));
		assertTrue(content.contains("filename=" + attachment.getName()));
	}

	@Test(expected = MailsenderException.class)
	public void sendMailNoValidRcpts() throws Exception {
		ConfigEntry config = configLogic.getConfig();
		String fromEmail = "from@example.com";
		String fromName = "Potamus, Peter";
		HashMap<String, String> to = new HashMap<String, String>();
		to.put("nope", "wrong");
		to.put("", "");
		String subject = "That thing I sent you";
		String body = "You get <em>that thing</em> I sent you?\n3x6=18\nåæÆÐ";
		HashMap<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();
		int port = startServer();

		Mockito.reset(serverConfigurationService);
		when(serverConfigurationService.getString(eq(MailConstants.SAKAI_HOST),
				isA(String.class))).thenReturn(
						ExternalLogicImpl.DEFAULT_SMTP_HOST);
		when(serverConfigurationService.getInt(eq(SAKAI_PORT), isA(Integer.class)))
				.thenReturn(port);
		when(serverConfigurationService.getBoolean(eq(SAKAI_ALLOW_TRANSPORT),
				isA(Boolean.class))).thenReturn(true);
		
		impl.init();
		impl.sendEmail(config, fromEmail, fromName, to, subject, body,
				attachments);
		
		List<WiserMessage> msgs = mailServer.getMessages();
		assertEquals(1, msgs.size());
		WiserMessage msg = msgs.get(0);
		assertEquals(fromEmail, msg.getEnvelopeSender());
		assertEquals("to@example.com", msg.getEnvelopeReceiver());
		MimeMessage mimeMsg = msg.getMimeMessage();
		assertEquals(subject, mimeMsg.getSubject());
		String content = getContent(mimeMsg);
		assertTrue(content.contains(QUOTED_PRINTABLE));
		assertFalse(content.contains("åæÆÐ"));
	}

	private int startServer() throws IOException {
		// have the system discover a free port
		ServerSocket server = new ServerSocket();
		server.bind(new InetSocketAddress("localhost", 0));
		int port = server.getLocalPort();
		server.close();

		// start a test mail server
		mailServer = new Wiser();
		mailServer.setHostname("localhost");
		mailServer.setPort(port);
		mailServer.start();

		return port;
	}

	private MultipartFile createAttachment(String name) throws IOException {
		final File file = File.createTempFile("/" + name, null);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(name.getBytes());
		fos.flush();
		fos.close();

		MultipartFile mpfile = new MultipartFile() {
			public void transferTo(File dest) throws IOException, IllegalStateException {
				FileInputStream in = new FileInputStream(file);
				FileOutputStream out = new FileOutputStream(dest);
				// Create the byte array to hold the data
		        byte[] buf = new byte[1024];

		        int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }
		        in.close();
		        out.close();
			}

			public boolean isEmpty() {
				return file.length() == 0;
			}

			public long getSize() {
				return file.length();
			}

			public String getOriginalFilename() {
				return file.getName();
			}

			public String getName() {
				return file.getName();
			}

			public InputStream getInputStream() throws IOException {
				FileInputStream is = new FileInputStream(file);
				return is;
			}

			public String getContentType() {
				return "text/plain";
			}

			public byte[] getBytes() throws IOException {
				FileInputStream is = new FileInputStream(file);

		        // Create the byte array to hold the data
		        byte[] bytes = new byte[(int) file.length()];

		        // Read in the bytes
		        int offset = 0;
		        int numRead = 0;
		        while (offset < bytes.length
		               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		            offset += numRead;
		        }

		        // Ensure all the bytes have been read in
		        if (offset < bytes.length) {
		            throw new IOException("Could not completely read file "+file.getName());
		        }

		        // Close the input stream and return bytes
		        is.close();
		        return bytes;
			}
		};

		return mpfile;
	}
}
