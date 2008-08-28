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

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetAddress;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.RecipientType;
import org.sakaiproject.email.impl.BaseAttachment;
import org.sakaiproject.email.impl.BaseEmailAddress;
import org.sakaiproject.email.impl.BaseEmailMessage;
import org.sakaiproject.email.impl.BasicEmailService;

public class EmailServiceTest extends TestCase
{
	static Log log = LogFactory.getLog(EmailServiceTest.class);

	static final boolean USE_INT_MAIL_SERVER = false;
	static final boolean LOG_SENT_EMAIL = false;

	static SimpleSmtpServer server;
	static BasicEmailService emailService;

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
			protected void setUp() throws Exception
			{
				try
				{
					emailService = new BasicEmailService();

					if (USE_INT_MAIL_SERVER)
					{
						emailService.setSmtp("localhost");
						emailService.setSmtpPort("8025");
						emailService.init();
						server = SimpleSmtpServer.start(8025);
					}
					else
					{
						emailService.setTestMode(true);
					}
				}
				catch (Exception e)
				{
					log.warn(e);
				}
			}

			protected void tearDown() throws Exception
			{
				emailService.destroy();
				if (server != null && !server.isStopped())
				{
					if (LOG_SENT_EMAIL)
					{
						for (Iterator<SmtpMessage> emails = server.getReceivedEmail(); emails.hasNext(); )
						{
							SmtpMessage email = emails.next();
							log.info(email);
						}
					}
					server.stop();
				}
			}
		};
		return setup;
	}

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
		attachments.add(new BaseAttachment(f1));
		attachments.add(new BaseAttachment(f2));
	}

	public void tearDown() throws Exception
	{
	}

	public void testSend() throws Exception
	{
		emailService.send(from.getAddress(), to[0].getAddress() + ", test2@example.com", subject,
				content, headerTo[0].getAddress(), replyTo[0].getAddress(), additionalHeaders);
	}

	public void testSendMessageWithoutAttachments() throws Exception
	{
		// create message with from, subject, content
		EmailMessage msg = new BaseEmailMessage(from.getAddress(), subject, content);
		// add message recipients that appear in the header
		HashMap<RecipientType, List<EmailAddress>> tos = new HashMap<RecipientType, List<EmailAddress>>();
		for (RecipientType type : headerToMap.keySet())
		{
			ArrayList<EmailAddress> addrs = new ArrayList<EmailAddress>();
			for (InternetAddress iaddr : headerToMap.get(type))
			{
				addrs.add(new BaseEmailAddress(iaddr.getAddress(), iaddr.getPersonal()));
			}
			tos.put(type, addrs);
		}
		// add the actual recipients
		tos.put(RecipientType.ACTUAL, BaseEmailAddress.toEmailAddress(to));
		msg.setRecipients(tos);
		// add additional headers
		msg.setHeaders(additionalHeaders);
		// send message
		emailService.send(msg);
	}

	public void testSendEmailMessage() throws Exception
	{
		// create message with from, subject, content
		EmailMessage msg = new BaseEmailMessage(from.getAddress(), subject + " with attachments",
				content);
		// add message recipients that appear in the header
		HashMap<RecipientType, List<EmailAddress>> tos = new HashMap<RecipientType, List<EmailAddress>>();
		for (RecipientType type : headerToMap.keySet())
		{
			ArrayList<EmailAddress> addrs = new ArrayList<EmailAddress>();
			for (InternetAddress iaddr : headerToMap.get(type))
			{
				addrs.add(new BaseEmailAddress(iaddr.getAddress(), iaddr.getPersonal()));
			}
			tos.put(type, addrs);
		}
		// add the actual recipients
		tos.put(RecipientType.ACTUAL, BaseEmailAddress.toEmailAddress(to));
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