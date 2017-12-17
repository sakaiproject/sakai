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
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.NoRecipientsException;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.impl.BasicEmailService;

@Slf4j
public class EmailServiceTest
{
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

	@BeforeClass
	public static void setUpEmailService() throws Exception {
		log.info("Setting up test case...");
		final ServerConfigurationService config = context.mock(ServerConfigurationService.class);

		emailService = new BasicEmailService();
		emailService.setServerConfigurationService(config);

		emailService.setSmtp(HOST);
		emailService.setSmtpPort(Integer.toString(PORT));
		emailService.setMaxRecipients("100");
		emailService.setOneMessagePerConnection(false);
		emailService.setAllowTransport(ALLOW_TRANSPORT);

		context.checking(new Expectations() {
			{
				allowing(config).getServerName();
				will(returnValue("localhost"));

				String connTimeoutKey = emailService.propName(BasicEmailService.MAIL_CONNECTIONTIMEOUT_T);
				allowing(config).getString(connTimeoutKey, null);
				will(returnValue(null));

				String timeoutKey = emailService.propName(BasicEmailService.MAIL_TIMEOUT_T);
				allowing(config).getString(timeoutKey, null);
				will(returnValue(null));

				allowing(config).getString(BasicEmailService.MAIL_SENDFROMSAKAI, "true");
				will(returnValue("true"));

				allowing(config).getString(BasicEmailService.MAIL_SENDFROMSAKAI_EXCEPTIONS, null);
				will(returnValue(null));

				allowing(config).getString(BasicEmailService.MAIL_SENDFROMSAKAI_FROMTEXT, "{}");
				will(returnValue("{}"));

				allowing(config).getInt(BasicEmailService.MAIL_SENDFROMSAKAI_MAXSIZE, 25000000);
				will(returnValue(25000000));

			}
		});

		log.debug("Initing EmailService...");
		emailService.init();
		log.debug("EmailService inited.");

		if (ALLOW_TRANSPORT) {
			log.debug("Starting internal mail server...");
			wiser = new Wiser();
			wiser.setPort(PORT);
			wiser.start();
			log.debug("Internal mail server started.");
		}
	}

	@AfterClass
	public static void tearDownEmailService() throws Exception {
		emailService.destroy();

		if (wiser != null && wiser.getServer().isRunning()) {
			if (LOG_SENT_EMAIL) {
				for (WiserMessage msg : wiser.getMessages()) {
					log.info(msg.toString());
				}
			}
			wiser.stop();
		}
	}

	@Before
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
	
	@Test
	public void testSend() throws Exception
	{
		emailService.send(from.getAddress(), to[0].getAddress() + ", test2@example.com", subject,
				content, headerTo[0].getAddress(), replyTo[0].getAddress(), additionalHeaders);
	}

	@Test
	public void testNoRecipients() throws Exception
	{
		EmailMessage msg = new EmailMessage(from.getAddress(), subject, content);
		try
		{
			emailService.send(msg);
			Assert.fail("Should not be able to send successfully with no recipients.");
		}
		catch (NoRecipientsException e)
		{
			// expected
		}
	}

	@Test
	public void testInvalidFrom() throws Exception
	{
		EmailMessage msg = new EmailMessage("test", subject, content);
		try
		{
			emailService.send(msg);
			Assert.fail("Should not be able to send successfully with invalid 'from'.");
		}
		catch (AddressValidationException e)
		{
			// expected
		}
	}

	@Test
	public void testInvalidReplyTo() throws Exception
	{
		EmailMessage msg = new EmailMessage("test", subject, content);
		msg.addReplyTo(new EmailAddress("test", "test"));
		try
		{
			emailService.send(msg);
			Assert.fail("Should not be able to send successfully with invalid 'reply to'.");
		}
		catch (AddressValidationException e)
		{
			// expected
		}
	}

	@Test
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

	@Test
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

	@Test
	public void testSendMailBasic() throws Exception
	{
		emailService.sendMail(from, to, subject, content, null, null, null, null);
	}

	@Test
	public void testSendMailAllButAttachments() throws Exception
	{
		emailService.sendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders);
	}

	@Test
	public void testSendMailAll() throws Exception
	{
		emailService.sendMail(from, to, subject, content, headerToMap, replyTo, additionalHeaders,
				attachments);
	}
}
