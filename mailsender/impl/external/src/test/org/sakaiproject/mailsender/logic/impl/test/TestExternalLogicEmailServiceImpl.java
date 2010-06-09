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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.impl.ExternalLogicEmailServiceImpl;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.EditorType;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;
import org.springframework.web.multipart.MultipartFile;

public class TestExternalLogicEmailServiceImpl extends TestCase
{
	private EmailService emailService;
	private ServerConfigurationService serverConfig;
	private ExternalLogicEmailServiceImpl impl;

	ConfigEntry config;

	@Override
	protected void setUp() throws Exception
	{
		serverConfig = createMock(ServerConfigurationService.class);
		expect(serverConfig.getServerName()).andReturn("localhost").anyTimes();

		emailService = createMock(EmailService.class);

		String subjectPrefixType = SubjectPrefixType.custom.name();
		boolean sendMeACopy = false;
		boolean addToArchive = false;
		String replyTo = ReplyTo.sender.name();
		boolean displayInvalidEmails = false;
		String editorType = EditorType.htmlarea.name();
		String subjectPrefix = "[unit test]";
		boolean displayEmptyGroups = false;
		config = new ConfigEntry(subjectPrefixType, sendMeACopy, addToArchive, replyTo,
				displayInvalidEmails, editorType, subjectPrefix, displayEmptyGroups);

		impl = new ExternalLogicEmailServiceImpl();
		impl.setEmailService(emailService);
	}

	public void testSend() throws Exception
	{
		expect(emailService.send((EmailMessage) EasyMock.anyObject())).andReturn(null);
		replay(serverConfig, emailService);

		String fromEmail = "admin@localhost.localdomain";
		String fromName = "admin";
		Map<String, String> to = new HashMap<String, String>();
		to.put("test@example.com", "test");
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();

		impl.sendEmail(config, fromEmail, fromName, to, subject, content, attachments);
	}

	public void testBadFrom() throws Exception
	{
		expect(emailService.send((EmailMessage) EasyMock.anyObject())).andThrow(
				new AddressValidationException("'FROM' is wonky.", new EmailAddress("", "")));
		replay(serverConfig, emailService);

		String fromEmail = "";
		String fromName = "";
		Map<String, String> to = new HashMap<String, String>();
		String subject = "test message";
		String content = "A test message for unit testing.";
		Map<String, MultipartFile> attachments = new HashMap<String, MultipartFile>();

		try
		{
			impl.sendEmail(config, fromEmail, fromName, to, subject, content, attachments);
			fail("Shouldn't be able to send with a bad from address [" + fromName + " <"
					+ fromEmail + ">");
		}
		catch (MailsenderException e)
		{
			if (!e.getMessage().contains("'FROM'"))
			{
				fail("Expected exception about invalid 'FROM': " + e.getMessage());
			}
		}
	}
}
