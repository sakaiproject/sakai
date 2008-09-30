/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.tool.assessment.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.util.EmailBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


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
	private String prefixedPath;
	
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
		this.prefixedPath = ServerConfigurationService.getString("samigo.email.prefixedPath");
	}
	
	public String send() {
		List attachmentList = null;
		AttachmentData a = null;
		try {
			Properties props = new Properties();
			
			// Server
			if (smtpServer == null || smtpServer.equals("")) {
				log.info("samigo.email.smtpServer is not set");
				smtpServer = ServerConfigurationService.getString("smtp@org.sakaiproject.email.api.EmailService");
				if (smtpServer == null || smtpServer.equals("")) {
					log.info("smtp@org.sakaiproject.email.api.EmailService is not set");
					log.error("Please set the value of samigo.email.smtpServer or smtp@org.sakaiproject.email.api.EmailService");
					return "error";
				}
			}
			props.setProperty("mail.smtp.host", smtpServer);

			// Port
			if (smtpPort == null || smtpPort.equals("")) {
				log.warn("samigo.email.smtpPort is not set. The default port 25 will be used.");
			} else {
				props.setProperty("mail.smtp.port", smtpPort);
			}

			Session session;
			session = Session.getInstance(props);
			session.setDebug(true);
			MimeMessage msg = new MimeMessage(session);
			
			InternetAddress fromIA = new InternetAddress(fromEmailAddress, fromName);
			msg.setFrom(fromIA);
			InternetAddress[] toIA = { new InternetAddress(toEmailAddress, toName) };
			msg.setRecipients(Message.RecipientType.TO, toIA);

			if (ccMe.equals("yes")) {
				InternetAddress[] ccIA = { new InternetAddress(fromEmailAddress, fromName) };
				msg.setRecipients(Message.RecipientType.CC, ccIA);
			}
			msg.setSubject(subject);

			EmailBean emailBean = (EmailBean) ContextUtil.lookupBean("email");
			attachmentList = emailBean.getAttachmentList();
			StringBuilder content = new StringBuilder(message);
			ArrayList fileList = new ArrayList();
			ArrayList fileNameList = new ArrayList();
			if (attachmentList != null) {
				if (prefixedPath == null || prefixedPath.equals("")) {
					log.error("samigo.email.prefixedPath is not set");
					return "error";
				}
				Iterator iter = attachmentList.iterator();
				while (iter.hasNext()) {
					a = (AttachmentData) iter.next();
					if (a.getIsLink().booleanValue()) {
						log.debug("send(): url");
						content.append("<br/>\n\r");
						content.append("<br/>"); // give a new line
						content.append(a.getFilename());
					}
					else {
						log.debug("send(): file");
						File attachedFile = getAttachedFile(a.getResourceId());
						fileList.add(attachedFile);
						fileNameList.add(a.getFilename());
					}
				}
			}
			
			Multipart multipart = new MimeMultipart(); 
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content.toString(), "text/html");
			multipart.addBodyPart(messageBodyPart);
			msg.setContent(multipart);
			
			for (int i = 0; i < fileList.size(); i++) {
				messageBodyPart = new MimeBodyPart();
				FileDataSource source = new FileDataSource((File)fileList.get(i));
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName((String)fileNameList.get(i));
				multipart.addBodyPart(messageBodyPart);
			}
			msg.setContent(multipart);
			
			Transport.send(msg);
		} catch (UnsupportedEncodingException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} catch (MessagingException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} catch (ServerOverloadException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} catch (PermissionException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} catch (IdUnusedException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} catch (TypeException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} catch (IOException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return "error";
		} finally {
			if (attachmentList != null) {
				if (prefixedPath != null && !prefixedPath.equals("")) {
					StringBuilder sbPrefixedPath;
					Iterator iter = attachmentList.iterator();
					while (iter.hasNext()) {
						sbPrefixedPath = new StringBuilder(prefixedPath);
						sbPrefixedPath.append("/email_tmp/");
						a = (AttachmentData) iter.next();
						if (!a.getIsLink().booleanValue()) {
							deleteAttachedFile(sbPrefixedPath.append(a.getResourceId()).toString());
						}
					}
				}
			}
		}
		return "send";
	}

	private File getAttachedFile(String resourceId) throws PermissionException, IdUnusedException, TypeException, ServerOverloadException, IOException {
		ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
		log.debug("getAttachedFile(): resourceId = " + resourceId);
		byte[] data = cr.getContent();
		StringBuilder sbPrefixedPath = new StringBuilder(prefixedPath);
		sbPrefixedPath.append("/email_tmp/");
		sbPrefixedPath.append(resourceId);
		String filename = sbPrefixedPath.toString().replace(" ", "");
		log.debug("getAttachedFile(): filename = " + filename);
		String path = filename.substring(0, filename.lastIndexOf("/"));
		log.debug("getAttachedFile(): path = " + path);
		File dir = new File(path);
		boolean success = dir.mkdirs();
		// Shouldn't come to here because resourceId is unique
		if (!success) {
			log.error("getAttachedFile(): File exists already! This should not heppen. Please check for resourceId.");
		}
		File file = new File(filename);
		success = file.createNewFile();
		// Shouldn't come to here because resourceId is unique
		if (!success) {
			log.error("getAttachedFile(): File exists already! This should not heppen. Please check for resourceId.");
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(data);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
		return file;
	}
	
	private void deleteAttachedFile(String filename) {
		log.debug("deleteAttachedFile(): filename = " + filename);
		// delete the file
		String tunedFilename = filename.replace(" ", "");
		log.debug("deleteAttachedFile(): tunedFilename = " + tunedFilename);
		File file = new File(tunedFilename);
		boolean success = file.delete();
		if (!success) {
			log.error("Fail to delete file: " + tunedFilename);
		}
		// delete the last directory
		String directoryName = tunedFilename.substring(0, tunedFilename.lastIndexOf("/"));
		log.debug("deleteAttachedFile(): directoryName = " + directoryName);
		File dir = new File(directoryName);
		success = dir.delete();
		if (!success) {
			log.error("Fail to delete directory: " + directoryName);
		}
	}
}
