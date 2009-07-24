/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

package org.sakaiproject.email.impl.test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.NoRecipientsException;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.impl.BasicEmailService;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class EmailServiceTest extends TestCase
{
	private static Log log = LogFactory.getLog(EmailServiceTest.class);

	private static final boolean ALLOW_TRANSPORT = false;
	private static final boolean LOG_SENT_EMAIL = false;
	private static final String HOST = "localhost";
	private static final int PORT = 8025;

	static Wiser wiser;
	static BasicEmailService emailService;

	static Mockery context = new Mockery();

	InternetAddress from;
	InternetAddress[] to;
	String subject;
	String content;
	HashMap<RecipientType, InternetAddress[]> headerToMap;
	InternetAddress[] headerTo;
	InternetAddress[] replyTo;
	ArrayList<String> additionalHeaders;
	ArrayList<Attachment> attachments;

	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(EmailServiceTest.class))
		{
			@Override
			protected void setUp() throws Exception
			{
				log.info("Setting up test case...");
				final ServerConfigurationService config = context
						.mock(ServerConfigurationService.class);
				context.checking(new Expectations() {{
					allowing(config).getServerName();
					will(returnValue("localhost"));

					allowing(config).getString(BasicEmailService.SMTP_CONNECTIONTIMEOUT, null);
					will(returnValue(null));

					allowing(config).getString(BasicEmailService.SMTP_TIMEOUT, null);
					will(returnValue(null));
				}});

				emailService = new BasicEmailService();
				emailService.setServerConfigurationService(config);

				emailService.setSmtp(HOST);
				emailService.setSmtpPort(Integer.toString(PORT));
				emailService.setMaxRecipients("100");
				emailService.setOneMessagePerConnection(false);
				emailService.setAllowTransport(ALLOW_TRANSPORT);
				System.err.println("Initing EmailService...");
				emailService.init();
				System.err.println("EmailService inited.");

				if (ALLOW_TRANSPORT)
				{
					System.err.println("Starting internal mail server...");
					wiser = new Wiser();
					wiser.setPort(PORT);
					wiser.start();
					System.err.println("Internal mail server started.");
				}
			}

			@Override
			protected void tearDown() throws Exception
			{
				emailService.destroy();

				if (wiser != null && wiser.getServer().isRunning())
				{
					if (LOG_SENT_EMAIL)
					{
						for (WiserMessage msg : wiser.getMessages())
						{
							log.info(msg);
						}
					}
					wiser.stop();
				}
			}
		};
		return setup;
	}

	@Override
	public void setUp() throws Exception
	{
		from = new InternetAddress("from@example.com");

		to = new InternetAddress[2];
		to[0] = new InternetAddress("to@example.com");
		to[1] = new InternetAddress("too@example.com");

		subject = "Super cool test subject";

		content = "Super cool test content";

		headerToMap = new HashMap<RecipientType, InternetAddress[]>();
		headerTo = new InternetAddress[1];
		// create the TO
		headerTo[0] = new InternetAddress("randomDude@example.com", "Random Dude");
		headerToMap.put(RecipientType.TO, headerTo);
		// create the CC
		headerTo[0] = new InternetAddress("otherPerson@example.com");
		headerToMap.put(RecipientType.CC, headerTo);

		replyTo = new InternetAddress[1];
		replyTo[0] = new InternetAddress("replyTo@example.com");

		additionalHeaders = new ArrayList<String>();
		additionalHeaders.add("x-testmessage-rocks: super-awesome");

		attachments = new ArrayList<Attachment>();
		File f1 = File.createTempFile("testFile1", ".txt");
		f1.deleteOnExit();
		FileWriter fw1 = new FileWriter(f1);
		fw1.write("This is some really killer test text for the first attachment.");
		fw1.flush();
		fw1.close();
		File f2 = File.createTempFile("testFile2", ".csv");
		f2.deleteOnExit();
		FileWriter fw2 = new FileWriter(f2);
		fw2.write("this,is,some,comma,delimited\ntext,in,a,message,body");
		fw2.flush();
		fw2.close();
		attachments.add(new Attachment(f1, f1.getPath()));
		attachments.add(new Attachment(f2, f2.getPath()));
	}

	public void testSend() throws Exception
	{
		emailService.send(from.getAddress(), to[0].getAddress() + ", test2@example.com", subject,
				content, headerTo[0].getAddress(), replyTo[0].getAddress(), additionalHeaders);
	}

	public void testNoRecipients() throws Exception
	{
		EmailMessage msg = new EmailMessage(from.getAddress(), subject, content);
		try
		{
			emailService.send(msg);
			fail("Should not be able to send successfully with no recipients.");
		}
		catch (NoRecipientsException e)
		{
			// expected
		}
	}

	public void testInvalidFrom() throws Exception
	{
		EmailMessage msg = new EmailMessage("test", subject, content);
		try
		{
			emailService.send(msg);
			fail("Should not be able to send successfully with invalid 'from'.");
		}
		catch (AddressValidationException e)
		{
			// expected
		}
	}

	public void testInvalidReplyTo() throws Exception
	{
		EmailMessage msg = new EmailMessage("test", subject, content);
		msg.addReplyTo(new EmailAddress("test", "test"));
		try
		{
			emailService.send(msg);
			fail("Should not be able to send successfully with invalid 'reply to'.");
		}
		catch (AddressValidationException e)
		{
			// expected
		}
	}

	public void testSendMessageWithoutAttachments() throws Exception
	{
		// create message with from, subject, content
		EmailMessage msg = new EmailMessage(from.getAddress(), subject, content);
		// add message recipients that appear in the header
		HashMap<RecipientType, List<EmailAddress>> tos = new HashMap<RecipientType, List<EmailAddress>>();
		for (RecipientType type : headerToMap.keySet())
		{
			ArrayList<EmailAddress> addrs = new ArrayList<EmailAddress>();
			for (InternetAddress iaddr : headerToMap.get(type))
			{
				addrs.add(new EmailAddress(iaddr.getAddress(), iaddr.getPersonal()));
			}
			tos.put(type, addrs);
		}
		// add the actual recipients
		LinkedList<EmailAddress> addys = new LinkedList<EmailAddress>();
		for (InternetAddress t : to) {
			addys.add(new EmailAddress(t.getAddress(), t.getPersonal()));
		}
		tos.put(RecipientType.ACTUAL, addys);
		msg.setRecipients(tos);
		// add additional headers
		msg.setHeaders(additionalHeaders);
		// send message
		emailService.send(msg);
	}

	public void testSendEmailMessage() throws Exception
	{
		// create message with from, subject, content
		EmailMessage msg = new EmailMessage(from.getAddress(), subject + " with attachments",
				content);
		// add message recipients that appear in the header
		HashMap<RecipientType, List<EmailAddress>> tos = new HashMap<RecipientType, List<EmailAddress>>();
		for (RecipientType type : headerToMap.keySet())
		{
			ArrayList<EmailAddress> addrs = new ArrayList<EmailAddress>();
			for (InternetAddress iaddr : headerToMap.get(type))
			{
				addrs.add(new EmailAddress(iaddr.getAddress(), iaddr.getPersonal()));
			}
			tos.put(type, addrs);
		}
		// add the actual recipients
		LinkedList<EmailAddress> addys = new LinkedList<EmailAddress>();
		for (InternetAddress t : to) {
			addys.add(new EmailAddress(t.getAddress(), t.getPersonal()));
		}
		tos.put(RecipientType.ACTUAL, addys);
		msg.setRecipients(tos);
		// add additional headers
		msg.setHeaders(additionalHeaders);
		// add attachments
		msg.setAttachments(attachments);
		// send message
		emailService.send(msg);
	}

	public void testSendMailBasic() throws Exception
	{
		emailService.sendMail(from, to, subject, content, null, null, null, null);
	}

	public void testSendMailAllButAttachments() throws Exception
	{
		emailService.sendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders);
	}

	public void testSendMailAll() throws Exception
	{
		emailService.sendMail(from, to, subject, content, headerToMap, replyTo, additionalHeaders,
				attachments);
	}
}