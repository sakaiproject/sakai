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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.sakaiproject.api.app.messageforums.AnonymousManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.NoRecipientsException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * This sends out emails to a list of users.
 * Really this should be part of the API, rather than the tool.
 */
@Slf4j
public class ForumsEmailService {

	private List<String> toEmailAddress;

	private Message reply;

	private String prefixedPath;

	private DiscussionMessageBean threadhead;

	/**
	 * Creates a new ForumsEmailService object.
	 */
	public ForumsEmailService(List<String> toEmailAddress, Message reply,
			DiscussionMessageBean currthread) {
		this.toEmailAddress = filterMailAddresses(toEmailAddress);
		this.reply = reply;
		this.prefixedPath = ServerConfigurationService.getString(
				"forum.email.prefixedPath", System.getProperty("java.io.tmpdir"));
		this.threadhead = currthread;

	}

	private ToolManager getToolManager()
	{
		return ComponentManager.get(ToolManager.class);
	}

	public void send() {
		List<Attachment> attachmentList = null;
		Attachment a = null;
		try {
			EmailMessage msg = new EmailMessage();
			

			String fromName = ServerConfigurationService.getString("ui.service", "LocalSakaiName");
			String fromEmailAddress = ServerConfigurationService.getSmtpFrom();
			log.debug("Sending the forums messages from {}", fromEmailAddress);

			String anonAwareAuthor = getAnonAwareAuthor(reply);
			String subject = DiscussionForumTool.getResourceBundleString("email.subject", 
					new Object[]{fromName, anonAwareAuthor});

			EmailAddress fromIA = new EmailAddress(fromEmailAddress,
					fromName);
			msg.setFrom(fromIA);
			log.debug("from: " + fromName + "<" + fromEmailAddress + ">");

			
			int indx = 0;
			for (int i = 0; i < toEmailAddress.size(); i++) {
				String email = toEmailAddress.get(i);
				log.debug("got mail <" + email + ">");

				if ((email != null) && (email.length() > 0) && isValidEmail(email)) {
					log.debug("adding email <" + email + ">");
					msg.addRecipient(RecipientType.BCC, email);
					indx++;
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
			FormattedText formattedText = ComponentManager.get(FormattedText.class);
			String greaterThanHtml = formattedText.escapeHtml(">", true);
			Site currentSite = null;
			String sitetitle = "";
			BaseForum baseforum = reply.getTopic().getBaseForum();
			try {
				SiteService siteService = ComponentManager.get(SiteService.class);
				currentSite = siteService.getSite(getToolManager()
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
			SimpleDateFormat formatter = new SimpleDateFormat(DiscussionForumTool.getResourceBundleString("date_format"), new ResourceLoader().getLocale());
			UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
			formatter.setTimeZone(userTimeService.getLocalTimeZone());
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.location")
					+ " "
					+ sitetitle + " " + greaterThanHtml + " "
					+ " <a href=\""
					+ getRedirectURL(currentSite)
					+ "\" target=\"_blank\" >" 
					+ DiscussionForumTool.getResourceBundleString("cdfm_discussions")
					+ "</a>"
					+ " " + greaterThanHtml + " "
					+ forumtitle
					+ " " + greaterThanHtml + " "
					+ topictitle);
			if (topic.getIncludeContentsInEmails()) {
				content.append(" ").append(greaterThanHtml).append(" ").append(threadtitle);
			}
			content.append(newline);
			content.append(newline);
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.author")
					+ " " + anonAwareAuthor);
			if (topic.getIncludeContentsInEmails()) {
				content.append(newline);
				content.append(DiscussionForumTool.getResourceBundleString("email.body.msgtitle"))
						.append(" ").append(reply.getTitle());
			}
			content.append(newline);
			content.append(DiscussionForumTool
					.getResourceBundleString("email.body.msgposted")
					+ " " + formatter.format(reply.getCreated()));
			content.append(newline);
			content.append(newline);
			if (topic.getIncludeContentsInEmails()) {
				content.append(reply.getBody());
			} else {
				content.append(DiscussionForumTool.getResourceBundleString("email.body.noContents", new Object[] { fromName }));
			}
			content.append(newline);
			content.append(newline);
			if (log.isDebugEnabled()) {
				log.debug("Email content: " + content.toString());
			}

			msg.setBody(formattedText.escapeHtmlFormattedText(content.toString()));
			msg.setContentType("text/html");
			msg.setCharacterSet("utf-8");
			msg.addHeader("Content-Transfer-Encoding", "quoted-printable");
			if (topic.getIncludeContentsInEmails()) {
				// Create temporary files to send as attachments
				ArrayList<File> fileList = new ArrayList<>();
				ArrayList<String> fileNameList = new ArrayList<>();
				if (attachmentList != null) {
					if (prefixedPath == null || "".equals(prefixedPath)) {
						log.error("forum.email.prefixedPath is not set");
						return;
					}
					Iterator<Attachment> iter = attachmentList.iterator();
					while (iter.hasNext()) {
						try {
						a = (Attachment) iter.next();
						log.debug("send(): file");
						File attachedFile = getAttachedFile(a.getAttachmentId());
						fileList.add(attachedFile);
						fileNameList.add(a.getAttachmentName());
						} catch (Exception e) {
							log.warn("Failed to load attachment: {}", a.getAttachmentId(), e);
						}
					}
				}

				// Attach temporary files to the emails
				org.sakaiproject.email.api.Attachment attachment;
				for (int count = 0; count < fileList.size(); count++) {
					attachment = new org.sakaiproject.email.api.Attachment(fileList
							.get(count), fileNameList.get(count));
					msg.addAttachment(attachment);
				}
			}
			
			EmailService instance = ComponentManager.get(EmailService.class);
			instance.send(msg);
		} catch (AddressValidationException e) {
			log.error("Failed to send all emails: "+ e.getMessage());
		} catch (NoRecipientsException e) {
			log.warn("No valid recipients found: "+ toEmailAddress.toString());
		} finally {
			if (attachmentList != null && reply.getTopic().getIncludeContentsInEmails()) {
				// Schedule cleanup of temporary files with 10 second delay
				if (prefixedPath != null && !"".equals(prefixedPath)) {
					final List<Attachment> finalAttachmentList = attachmentList;
					final String finalPrefixedPath = prefixedPath;
					Timer timer = new Timer("EmailAttachment-Cleanup", true);
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							Iterator<Attachment> iter = finalAttachmentList.iterator();
							while (iter.hasNext()) {
								Attachment attachment = iter.next();
								StringBuilder filePath = new StringBuilder(finalPrefixedPath);
								filePath.append("/email_tmp/");
								filePath.append(attachment.getAttachmentId().replace('\\', '/'));
								deleteAttachedFile(filePath.toString());
							}
						}
					}, 10000);
				}
			}
		}
	}

	private File getAttachedFile(String resourceId) throws PermissionException,
			IdUnusedException, TypeException, ServerOverloadException,
			IOException {
		ContentHostingService contentHostingService = ComponentManager.get(ContentHostingService.class);;
		ContentResource cr = contentHostingService.getResource(resourceId);
		byte[] data = cr.getContent();
		StringBuilder sbPrefixedPath = new StringBuilder(prefixedPath);
		sbPrefixedPath.append("/email_tmp/");
		sbPrefixedPath.append(resourceId.replace('\\', '/'));
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
			log.warn("Failed to delete file immediately, marking for deletion on exit: {}", tunedFilename);
			if (file.exists()) {
				file.deleteOnExit();
			}
		} else {
			log.debug("Successfully deleted file: " + tunedFilename);
		}
		// delete the last directory
		String directoryName = tunedFilename.substring(0, tunedFilename
				.lastIndexOf("/"));
		File dir = new File(directoryName);
		success = dir.delete();
		if (!success) {
			log.debug("Failed to delete directory (may not be empty): {}", directoryName);
			if (dir.exists()) {
				dir.deleteOnExit();
			}
		} else {
			log.debug("Successfully deleted directory: " + directoryName);
		}
	}

	public String getRedirectURL(Site currentSite) {
		// Sitepage.getUrl() takes the user back to the Forums tool of the site
		// "https://coursework-dev4.stanford.edu:8995/portal/site/W08-UROL-199-01/page/a4b4d8ef-a381-4801-0060-c701d57a527d";
		String redirecturl = "";
		String toolid = getToolManager().getCurrentPlacement().getId();
		redirecturl = currentSite.getTool(toolid).getContainingPage().getUrl();
		return redirecturl;

	}
	private boolean isValidEmail(String email) {
		
		// TODO: Use a generic Sakai utility class (when a suitable one exists)
		
		if (email == null || "".equals(email))
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

	private String getAnonAwareAuthor(Message message)
	{
		if (message == null)
		{
			throw new IllegalArgumentException("getAnonAwareAuthor invoked with null message");
		}

		if (isUseAnonymousId(message.getTopic()))
		{
			String siteId = getToolManager().getCurrentPlacement().getContext();
			String userId = message.getAuthorId();
			return getAnonymousManager().getOrCreateAnonId(siteId, userId);
		}
		return message.getAuthor();
	}

	private AnonymousManager getAnonymousManager()
	{
		return ComponentManager.get(AnonymousManager.class);
	}

	private boolean isUseAnonymousId(Topic topic)
	{
		if (topic == null)
		{
			throw new IllegalArgumentException("isUseAnonymousId invoked with null topic");
		}

		/* 
		 * TODO: we ignore isRevealIDsToRoles / isIdentifyAnonAuthors here because everybody's getting the same email.
		 * This is okay, but if isRevealIDsToRoles is enabled, it would be better to send two separate emails (one revealed, one anonymous) 
		 * so that the instructor can't match the message up with the UI to discover the anonymous mapping for the author
		 */
		return getAnonymousManager().isAnonymousEnabled() && topic.getPostAnonymous();
	}
}
