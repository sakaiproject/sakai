/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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

package org.sakaiproject.tool.assessment.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;


/**
 * The ItemService calls persistent service locator to reach the
 * manager on the back end.
 */
public class SamigoEmailService {
	private static Log log = LogFactory.getLog(SamigoEmailService.class);

	private String fromName;
	private String fromEmailAddress;
	private String toName;
	private String toEmailAddress;
	private String ccMe;
	private String subject;
	private String message;
	private String smtpServer;
	private String smtpPort;
	private String username;
	private String password;
	
	/**
	 * Creates a new SamigoEmailService object.
	 */
	public SamigoEmailService(String fromName, String fromEmailAddress, 
			String toName, String toEmailAddress, String ccMe, String subject, String message) {
		this.fromName = fromName;
		this.fromEmailAddress = fromEmailAddress;
		this.toName = toName;
		this.toEmailAddress = toEmailAddress;
		this.ccMe = ccMe;
		this.subject = subject;
		this.message = message;
		this.smtpServer = ServerConfigurationService.getString("samigo.smtp.server");
		this.smtpPort = ServerConfigurationService.getString("samigo.smtp.port");
		this.username = ServerConfigurationService.getString("samigo.email.username");
		this.password = ServerConfigurationService.getString("samigo.email.password");
	}
	
	public String send() throws MessagingException, ServerOverloadException,
			PermissionException, IdUnusedException, TypeException, IOException {
		if (smtpServer == null || smtpServer.equals("")) {
			return "error";
		}

		if (smtpPort == null || smtpPort.equals("")) {
			return "error";
		}
		
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", smtpServer);
		props.setProperty("mail.smtp.port", smtpPort);
		Session session;
		if ((username == null || username.equals("")) || (password == null || password.equals(""))) {
			session = Session.getInstance(props, null);
		}
		else {
			SamigoEmailAuthenticator samigoEmailAuthenticator = new SamigoEmailAuthenticator();
			session = Session.getInstance(props, samigoEmailAuthenticator);
		}
		
		session.setDebug(true);
		MimeMessage msg = new MimeMessage(session);

		InternetAddress fromIA = new InternetAddress(fromEmailAddress, fromName);
		msg.setFrom(fromIA);
		InternetAddress[] toIA = { new InternetAddress(toEmailAddress, toName) };
		msg.setRecipients(Message.RecipientType.TO, toIA);

		if (ccMe.equals("yes")) {
			InternetAddress[] ccIA = { new InternetAddress(fromEmailAddress,
					fromName) };
			msg.setRecipients(Message.RecipientType.CC, ccIA);
		}
		msg.setSubject(subject);

		 Multipart mp = new MimeMultipart(); 
		 // create and fill the first message part 
		 MimeBodyPart mbp1 = new MimeBodyPart();
		 mbp1.setText(message); 
		 mp.addBodyPart(mbp1);
		 /*
		 // create the second message part 
		 MimeBodyPart mbp2 = new MimeBodyPart();
		 // attach the file to the message 
		 FileDataSource fds = new FileDataSource("C:/Karen.txt"); 
		 mbp2.setDataHandler(new DataHandler(fds));
		 mbp2.setFileName(fds.getName()); 
		 mp.addBodyPart(mbp2);
		 */
		 // add the Multipart to the message 
		 msg.setContent(mp);
		 
		/*
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		//messageBodyPart.setText(message);
		messageBodyPart.setContent(message, "text/html");
		// msg.addHeaderLine("Content-Type: text/html; charset=\"iso-8859-1\"");
		// msg.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		AttachmentData a = null;
		EmailBean emailBean = (EmailBean) ContextUtil.lookupBean("email");
		List attachmentList = emailBean.getAttachmentList();
		if (attachmentList != null) {
			Iterator iter = attachmentList.iterator();
			while (iter.hasNext()) {
				a = (AttachmentData) iter.next();
				File attachedFile = getAttachedFile(a.getResourceId());
				messageBodyPart = new MimeBodyPart();
				// DataSource source = new FileDataSource(a.getLocation());
				DataSource source = new FileDataSource(attachedFile);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(a.getFilename());
				multipart.addBodyPart(messageBodyPart);
			}
		}
		msg.setContent(multipart);
		*/
		   
		// msg.setText(message);

		Transport.send(msg);
		
		return "send";
		
	}

	private File getAttachedFile(String resourceId) throws ServerOverloadException,
			PermissionException, IdUnusedException, TypeException, IOException {
		//int BUFFER_SIZE = 2048;
		// byte data[] = new byte[ BUFFER_SIZE ];
		byte data[];
		ContentResource cr = ContentHostingService.getResource(resourceId);
		data = cr.getContent();
		//File file = new File(resourceId);
		File file = new File("KarenTestFile");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(data);
		return file;
	}
}
