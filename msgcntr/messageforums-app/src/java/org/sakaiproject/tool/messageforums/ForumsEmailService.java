/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.tool.messageforums;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.api.app.messageforums.Message;

/**
 * The ItemService calls persistent service locator to reach the manager on the
 * back end.
 */
public class ForumsEmailService {
	private static Log log = LogFactory.getLog(ForumsEmailService.class);

	private List<String> toEmailAddress;

	private Message reply;

	private String smtpServer;

	private String prefixedPath;

	private DiscussionMessageBean threadhead;

	/**
	 * Creates a new SamigoEmailService object.
	 */
	public ForumsEmailService(List<String> toEmailAddress, Message reply,
			DiscussionMessageBean currthread) {
		this.toEmailAddress = filterMailAddresses(toEmailAddress);
		this.reply = reply;
		this.smtpServer = ServerConfigurationService
				.getString("smtp@org.sakaiproject.email.api.EmailService");
		this.prefixedPath = ServerConfigurationService.getString(
				"forum.email.prefixedPath", "/tmp/");
		this.threadhead = currthread;

	}

	public void send() {
		List<Attachment> attachmentList = null;
		Attachment a = null;
		try {
			Properties props = System.getProperties();
			// TODO

			// check for testMode@org.sakaiproject.email.api.EmailService
			// Server
			if (smtpServer == null || smtpServer.equals("")) {
				log
						.info("smtp@org.sakaiproject.email.api.EmailService is not set");
				log
						.error("Please set the value of smtp@org.sakaiproject.email.api.EmailService");
				return;
			}
			props.setProperty("mail.smtp.host", smtpServer);
				
			Session session;
			session = Session.getInstance(props, null);
			session.setDebug(true);
			
			MimeMessage msg = new MimeMessage(session);
			

			String fromName = ServerConfigurationService.getString("ui.service", "LocalSakaiName");

			String fromEmailAddress = DiscussionForumTool.getResourceBundleString("email.fromAddress", 
					new Object[]{ServerConfigurationService.getString("serverName", "localhost")});
			log.info(fromEmailAddress);
			
			String subject = DiscussionForumTool
					.getResourceBundleString("email.subject");

			InternetAddress fromIA = new InternetAddress(fromEmailAddress,
					fromName);
			msg.setFrom(fromIA);
			log.debug("from: " + fromName + "<" + fromEmailAddress + ">");
			// form the list of to: addresses from the users users collection
			InternetAddress[] to = new InternetAddress[toEmailAddress.size()];

			
			int indx = 0;
			for (int i = 0; i < toEmailAddress.size(); i++) {
				String email = toEmailAddress.get(i);
				log.info("got mail <" + email + ">");

				if ((email != null) && (email.length() > 0) && isValidEmail(email)) {
					log.info("adding email <" + email + ">");
					try {
						to[indx] = new InternetAddress(email);
						indx++;
					} catch (AddressException e) {
						if (log.isDebugEnabled())
							log.debug("sendToUsers: " + e);
					}
				}
			}
			//if the to list is empty return
			if (indx == 0) {
				log.warn("no valid emails where found to send the email to");
				return;
			}
			
			msg.setSubject(subject);

			// DiscussionMessageBean
			attachmentList = reply.getAttachments();
			StringBuilder content = new StringBuilder();
			String newline = "<br/>";
			Site currentSite = null;
			String sitetitle = "";
			BaseForum baseforum = reply.getTopic().getBaseForum();
			try {
				currentSite = SiteService.getSite(ToolManager
						.getCurrentPlacement().getContext());
			} catch (IdUnusedException e) {
				log.error("ForumsEmailService.send(), Site ID not found: "
						+ e.getMessage());
			}

			if (currentSite != null) {
				sitetitle = currentSite.getTitle();
			}
			String forumtitle = baseforum.getTitle();
			Topic topic = reply.getTopic();
			String topictitle = topic.getTitle();
			String threadtitle = threadhead.getMessage().getTitle();
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.location")
					+ " "
					+ sitetitle + " > "
					+ " <a href=\""
					+ getRedirectURL(currentSite)
					+ "\" target=\"_blank\" >" 
					+ DiscussionForumTool.getResourceBundleString("cdfm_discussion_forums")
					+ "</a>"
					+ " > "
					+ forumtitle
					+ " > "
					+ topictitle
					+ " > " + threadtitle);
			content.append(newline);
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.author")
					+ " " + reply.getAuthor());
			content.append(newline);
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.msgtitle")
					+ " " + reply.getTitle());
			content.append(newline);
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.msgposted")
					+ " " + reply.getCreated().toString());
			content.append(newline);
			content.append(reply.getBody());
			content.append(newline);
			content.append(newline);
			if (log.isDebugEnabled()) {
				log.debug("Email content: " + content.toString());
			}
			ArrayList<File> fileList = new ArrayList<File>();
			ArrayList<String> fileNameList = new ArrayList<String>();
			if (attachmentList != null) {
				if (prefixedPath == null || prefixedPath.equals("")) {
					log.error("forum.email.prefixedPath is not set");
					return;
				}
				Iterator<Attachment> iter = attachmentList.iterator();
				while (iter.hasNext()) {
					a = (Attachment) iter.next();
					log.debug("send(): file");
					File attachedFile = getAttachedFile(a.getAttachmentId());
					fileList.add(attachedFile);
					fileNameList.add(a.getAttachmentName());
				}
			}

			Multipart multipart = new MimeMultipart();
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content.toString(), "text/html");
			messageBodyPart.addHeader("Content-Transfer-Encoding", "quoted-printable");
            multipart.addBodyPart(messageBodyPart);
			msg.setContent(multipart);
			
			for (int count = 0; count < fileList.size(); count++) {
				messageBodyPart = new MimeBodyPart();
				FileDataSource source = new FileDataSource((File) fileList
						.get(count));
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName((String) fileNameList.get(count));
				multipart.addBodyPart(messageBodyPart);
			}
			msg.setContent(multipart);

			String testmode = ServerConfigurationService
					.getString("testMode@org.sakaiproject.email.api.EmailService");
			if ("true".equalsIgnoreCase(testmode)) {
				log.info("Email testMode = true,  printing out text: ");
				Enumeration<String> en = msg.getAllHeaderLines();
				while (en.hasMoreElements()) {
					log.info(en.nextElement());
				}
				log.info(content.toString());
				return;
			}
			Transport.send(msg, to);
		} catch (UnsupportedEncodingException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} catch (MessagingException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} catch (ServerOverloadException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} catch (PermissionException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} catch (IdUnusedException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} catch (TypeException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} catch (IOException e) {
			log.error("Exception throws from send()" + e.getMessage());
			return;
		} finally {
			if (attachmentList != null) {
				if (prefixedPath != null && !prefixedPath.equals("")) {
					StringBuilder sbPrefixedPath;
					Iterator<Attachment> iter = attachmentList.iterator();
					while (iter.hasNext()) {
						sbPrefixedPath = new StringBuilder(prefixedPath);
						sbPrefixedPath.append("/email_tmp/");
						a = (Attachment) iter.next();
						deleteAttachedFile(sbPrefixedPath.append(
								a.getAttachmentId()).toString());
					}
				}
			}
		}
	}

	private File getAttachedFile(String resourceId) throws PermissionException,
			IdUnusedException, TypeException, ServerOverloadException,
			IOException {
		ContentResource cr = ContentHostingService.getResource(resourceId);
		byte[] data = cr.getContent();
		StringBuilder sbPrefixedPath = new StringBuilder(prefixedPath);
		sbPrefixedPath.append("/email_tmp/");
		sbPrefixedPath.append(resourceId);
		String filename = sbPrefixedPath.toString().replace(" ", "");
		String path = filename.substring(0, filename.lastIndexOf("/"));
		File dir = new File(path);
		boolean success = dir.mkdirs();
		// Shouldn't come to here because resourceId is unique
		if (!success) {
			log
					.error("getAttachedFile(): File exists already! This should not happen. Please check for resourceId.");
		}
		File file = new File(filename);
		success = file.createNewFile();
		// Shouldn't come to here because resourceId is unique
		if (!success) {
			log
					.error("getAttachedFile(): File exists already! This should not heppen. Please check for resourceId.");
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(data);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
		return file;
	}

	private void deleteAttachedFile(String filename) {
		// delete the file
		String tunedFilename = filename.replace(" ", "");
		File file = new File(tunedFilename);
		boolean success = file.delete();
		if (!success) {
			log.error("Fail to delete file: " + tunedFilename);
		}
		// delete the last directory
		String directoryName = tunedFilename.substring(0, tunedFilename
				.lastIndexOf("/"));
		File dir = new File(directoryName);
		success = dir.delete();
		if (!success) {
			log.error("Fail to delete directory: " + directoryName);
		}
	}

	public String getRedirectURL(Site currentSite) {
		// Sitepage.getUrl() takes the user back to the Forums tool of the site
		// "https://coursework-dev4.stanford.edu:8995/portal/site/W08-UROL-199-01/page/a4b4d8ef-a381-4801-0060-c701d57a527d";
		String redirecturl = "";
		String toolid = ToolManager.getCurrentPlacement().getId();
		redirecturl = currentSite.getTool(toolid).getContainingPage().getUrl();
		return redirecturl;

	}
	private boolean isValidEmail(String email) {
		
		// TODO: Use a generic Sakai utility class (when a suitable one exists)
		
		if (email == null || email.equals(""))
			return false;
		
		email = email.trim();
		//must contain @
		if (email.indexOf("@") == -1)
			return false;
		
		//an email can't contain spaces
		if (email.indexOf(" ") > 0)
			return false;
		
		
		if (email.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*$")) 
			return true;
	
		log.warn(email + " is not a valid eamil address");
		return false;
	}
	
	private List<String> filterMailAddresses(List<String> toEmailAddress) {
		
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < toEmailAddress.size(); i++) {
			String mail = (String)toEmailAddress.get(i);
			if (isValidEmail(mail))
				ret.add(mail);
		}
		
		return ret;
		
	}

}
