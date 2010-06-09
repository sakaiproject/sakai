/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.sakaiproject.mailsender.logic.ConfigLogic.EMAIL_TEST_MODE_PROP;
import static org.sakaiproject.mailsender.logic.ConfigLogic.UPLOAD_DIRECTORY_PROP;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_ALLOW_TRANSPORT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_CONNECTION_TIMEOUT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_HOST;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_PASSWORD;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_PORT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_TIMEOUT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_USER;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_USE_SSL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.impl.ConfigLogicImpl;
import org.sakaiproject.mailsender.logic.impl.ExternalLogicBuiltinImpl;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.EditorType;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.web.multipart.MultipartFile;
import org.subethamail.wiser.Wiser;

public class TestExternalLogicBuiltinImpl extends TestCase
{
	private static final String HOST = "localhost";
	private static final int PORT = 8825;

	private ServerConfigurationService serverConfig;
	private ExternalLogicBuiltinImpl impl;

	ConfigEntry configEntry;
	ConfigLogicImpl configLogic;
	Wiser wiser;

	@Override
	protected void setUp() throws Exception
	{
		// wiser = new Wiser();
		// wiser.setPort(PORT);
		// wiser.start();

		serverConfig = createMock(ServerConfigurationService.class);
		expect(serverConfig.getString(SAKAI_SMTP_CONNECTION_TIMEOUT)).andReturn("10");
		expect(serverConfig.getString(SAKAI_SMTP_HOST)).andReturn(HOST);
		expect(serverConfig.getString(SAKAI_SMTP_HOST, HOST)).andReturn(HOST);
		expect(serverConfig.getString(SAKAI_SMTP_PASSWORD)).andReturn("somepass");
		expect(serverConfig.getInt(SAKAI_SMTP_PORT, -1)).andReturn(PORT);
		expect(serverConfig.getString(SAKAI_SMTP_TIMEOUT)).andReturn("10");
		expect(serverConfig.getString(SAKAI_SMTP_USER)).andReturn("someuser");
		expect(serverConfig.getBoolean(SAKAI_SMTP_USE_SSL, false)).andReturn(false);
		expect(serverConfig.getBoolean(SAKAI_SMTP_ALLOW_TRANSPORT, true)).andReturn(false);
		expect(serverConfig.getString(UPLOAD_DIRECTORY_PROP)).andReturn("/tmp/");
		expect(serverConfig.getBoolean(EMAIL_TEST_MODE_PROP, false)).andReturn(false);

		String subjectPrefixType = SubjectPrefixType.custom.name();
		boolean sendMeACopy = false;
		boolean addToArchive = false;
		String replyTo = ReplyTo.sender.name();
		boolean displayInvalidEmails = false;
		String editorType = EditorType.htmlarea.name();
		String subjectPrefix = "[unit test]";
		boolean displayEmptyGroups = false;
		configEntry = new ConfigEntry(subjectPrefixType, sendMeACopy, addToArchive, replyTo,
				displayInvalidEmails, editorType, subjectPrefix, displayEmptyGroups);

		Placement placement = createMock(Placement.class);
		expect(placement.getContext()).andReturn("[unit test]");

		ToolManager toolManager = createMock(ToolManager.class);
		expect(toolManager.getCurrentPlacement()).andReturn(placement);

		configLogic = new ConfigLogicImpl();
		configLogic.setServerConfigurationService(serverConfig);
		configLogic.setToolManager(toolManager);

		SessionManager sessionManager = createMock(SessionManager.class);
		expect(sessionManager.getCurrentSessionUserId()).andReturn("someuser");

		impl = new ExternalLogicBuiltinImpl();
		impl.setConfigLogic(configLogic);
		impl.setServerConfigurationService(serverConfig);
		impl.setSessionManager(sessionManager);
		impl.setToolManager(toolManager);

		configLogic.setExternalLogic(impl);

		replay(serverConfig, placement, toolManager, sessionManager);
	}

	@Override
	public void tearDown()
	{
		// wiser.stop();
	}

	public void testSend() throws Exception
	{
		String fromEmail = "admin@localhost.localdomain";
		String fromName = "admin";
		Map<String, String> to = new HashMap<String, String>();
		to.put("test@example.com", "test");
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = null;

		impl.sendEmail(configEntry, fromEmail, fromName, to, subject, content, attachments);
	}

	public void testEmptyFrom()
	{
		String fromEmail = "";
		String fromName = "";
		Map<String, String> to = new HashMap<String, String>();
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = null;

		try
		{
			impl.sendEmail(configEntry, fromEmail, fromName, to, subject, content, attachments);
			fail("Shouldn't be able to send with a bad from address [\"" + fromName + "\" <"
					+ fromEmail + ">");
		}
		catch (MailsenderException e)
		{
			List<Map<String, Object[]>> messages = e.getMessages();
			assertNotNull(messages);
			assertTrue(messages.size() > 0);

			verifyException("invalid.from_replyto.address", e, messages);
		}
	}

	public void testInvalidFrom()
	{
		String fromEmail = "wrong";
		String fromName = "user";
		Map<String, String> to = new HashMap<String, String>();
		to.put("test@example.com", "test");
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = null;

		try
		{
			impl.sendEmail(configEntry, fromEmail, fromName, to, subject, content, attachments);
			fail("Shouldn't be able to send with a bad from address [" + fromName + " <"
					+ fromEmail + ">");
		}
		catch (MailsenderException e)
		{
			List<Map<String, Object[]>> messages = e.getMessages();
			assertNotNull(messages);
			assertTrue(messages.size() > 0);

			verifyException("invalid.from_replyto.address", e, messages);
		}
	}

	public void testEmptyTo() throws Exception
	{
		String fromEmail = "admin@localhost.localdomain";
		String fromName = "admin";
		Map<String, String> to = null;
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = null;

		try
		{
			impl.sendEmail(configEntry, fromEmail, fromName, to, subject, content, attachments);
			fail("Shouldn't be able to send without a 'TO'");
		}
		catch (MailsenderException e)
		{
			List<Map<String, Object[]>> messages = e.getMessages();
			assertNotNull(messages);
			assertTrue(messages.size() > 0);

			verifyException("error.no.recipients", e, messages);
		}
	}

	public void testAllInvalidTos()
	{
		String fromEmail = "admin@localhost.localdomain";
		String fromName = "admin";
		Map<String, String> to = new HashMap<String, String>();
		to.put("test", "test");
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();

		try
		{
			impl.sendEmail(configEntry, fromEmail, fromName, to, subject, content, attachments);
			fail("Shouldn't be able to send without a valid 'TO'");
		}
		catch (MailsenderException e)
		{
			List<Map<String, Object[]>> messages = e.getMessages();
			assertNotNull(messages);
			assertTrue(messages.size() > 0);

			verifyException("error.no.valid.recipients", e, messages);
		}
	}

	private void verifyException(String key, MailsenderException e,
			List<Map<String, Object[]>> messages)
	{
		boolean hasKey = false;
		for (Map<String, Object[]> message : messages)
		{
			if (message.containsKey(key))
			{
				hasKey = true;
				break;
			}
		}
		if (!hasKey)
		{
			fail("Expected exception containing '" + key + "' but didn't find it.");
		}
	}
}
