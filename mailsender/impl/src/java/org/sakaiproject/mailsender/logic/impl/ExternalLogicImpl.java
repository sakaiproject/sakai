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
package org.sakaiproject.mailsender.logic.impl;

import net.htmlparser.jericho.Source;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.MAIL_SMTP_CONNECTIONTIMEOUT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.MAIL_SMTP_SENDPARTIAL;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.MAIL_SMTP_TIMEOUT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_ALLOW_TRANSPORT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_CONNECTION_TIMEOUT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_HOST;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_PASSWORD;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_PORT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_TIMEOUT;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_USER;
import static org.sakaiproject.mailsender.logic.impl.MailConstants.SAKAI_SMTP_USE_SSL;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveMessageEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeaderEdit;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is the implementation for logic which is external to our app logic
 */
public class ExternalLogicImpl implements ExternalLogic
{
	private static Log log = LogFactory.getLog(ExternalLogicImpl.class);

    private static final String UTF_8 = "UTF-8";

	/** Default value of smtp host */
	private static final String DEFAULT_SMTP_HOST = "localhost";

	/** Default value of smtp port */
	private static final int DEFAULT_SMTP_PORT = -1;

	/** Defaut value for use of ssl */
	boolean DEFAULT_USE_SSL = false;

	private FunctionManager functionManager;
	private ToolManager toolManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private MailArchiveService mailArchiveService;
	private UserDirectoryService userDirectoryService;
    private ConfigLogic configLogic;
    private ServerConfigurationService configService;

	/**
	 * Place any code that should run when this class is initialized by spring here
	 */
	public void init()
	{
		log.debug("init");
		// register Sakai permissions for this tool
		functionManager.registerFunction(PERM_ADMIN);
		functionManager.registerFunction(PERM_SEND);
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getCurrentSiteTitle()
	 */
	public String getCurrentSiteTitle()
	{
		Site site = getCurrentSite();
		String title = "----------";
		if (site != null)
		{
			title = site.getTitle();
		}
		return title;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getCurrentSite()
	 */
	public Site getCurrentSite()
	{
		String locationId = toolManager.getCurrentPlacement().getContext();
		Site site = null;
		try
		{
			site = siteService.getSite(locationId);
		}
		catch (IdUnusedException e)
		{
			log.error("Cannot get the info about locationId: " + locationId);
		}
		return site;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getCurrentUserId()
	 */
	public String getCurrentUserId()
	{
		return sessionManager.getCurrentSessionUserId();
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getSiteID()
	 */
	public String getSiteID()
	{
		return toolManager.getCurrentPlacement().getContext();
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getSiteRealmID()
	 */
	public String getSiteRealmID()
	{
		return ("/site/" + getSiteID());
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getSiteType()
	 */
	public String getSiteType()
	{
		String type = null;
		try
		{
			type = siteService.getSite(getSiteID()).getType();
		}
		catch (IdUnusedException e)
		{
			log.debug(e.getMessage(), e);
		}
		return type;
	}

	/**
	 * Get the details for the current user
	 *
	 * @return
	 */
	public User getCurrentUser()
	{
		User user = userDirectoryService.getCurrentUser();
		return user;
	}

	/**
	 * Get the details for a user
	 *
	 * @param userId
	 * @return
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getUser(java.lang.String)
	 */
	public User getUser(String userId)
	{
		User user = null;
		try
		{
			user = userDirectoryService.getUser(userId);
		}
		catch (UserNotDefinedException e)
		{
			log.warn("Cannot get user for id: " + userId);
		}
		return user;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#getUserDisplayName(java.lang.String)
	 */
	public String getUserDisplayName(String userId)
	{
		String name = "--------";
		User user = getUser(userId);
		if (user != null)
		{
			name = user.getDisplayName();
		}
		return name;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#isUserAdmin(java.lang.String)
	 */
	public boolean isUserAdmin(String userId)
	{
		return securityService.isSuperUser(userId);
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ExternalLogic#isUserAllowedInLocation(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean isUserAllowedInLocation(String userId, String permission, String locationId)
	{
		boolean allowed = false;
		if (securityService.unlock(userId, permission, locationId))
		{
			allowed = true;
		}
		return allowed;
	}

	/**
	 * Check that the email archive has been added to the current site
	 */
	public boolean isEmailArchiveAddedToSite()
	{
		boolean hasEmailArchive = false;
		String toolid = "sakai.mailbox";
		try
		{
			String siteId = toolManager.getCurrentPlacement().getContext();
			Site site = siteService.getSite(siteId);

			Collection<?> toolsInSite = site.getTools(toolid);
			if (!toolsInSite.isEmpty())
			{
				hasEmailArchive = true;
			}
		}
		catch (Exception e)
		{
			log.debug("Exception: OptionsBean.isEmailArchiveAddedToSite(), " + e.getMessage());
		}
		return hasEmailArchive;
	}

	public boolean addToArchive(ConfigEntry config, String channelRef, String sender,
			String subject, String body)
	{
		boolean retval = true;
		MailArchiveChannel channel = null;
		try
		{
			channel = mailArchiveService.getMailArchiveChannel(channelRef);
		}
		catch (Exception e)
		{
			log.debug("Exception: Mailsender.appendToArchive() #1, " + e.getMessage());
			return false;
		}
		if (channel == null)
		{
			log.debug("Mailsender: The channel: " + channelRef + " is null.");

			return false;
		}
		List<String> mailHeaders = new ArrayList<String>();
		if (config.useRichTextEditor())
		{
			mailHeaders.add(MailArchiveService.HEADER_OUTER_CONTENT_TYPE
					+ ": text/html; charset=ISO-8859-1");
			mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE
					+ ": text/html; charset=ISO-8859-1");
		}
		else
		{
			mailHeaders.add(MailArchiveService.HEADER_OUTER_CONTENT_TYPE
					+ ": text/plain; charset=ISO-8859-1");
			mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE
					+ ": text/plain; charset=ISO-8859-1");
		}
		mailHeaders.add("Mime-Version: 1.0");
		mailHeaders.add("From: " + sender);
		mailHeaders.add("Reply-To: " + sender);
		try
		{
			// This way actually sends the email too
			// channel.addMailArchiveMessage(subject, sender,
			// TimeService.newTime(), mailHeaders, null, body);
			MailArchiveMessageEdit edit = (MailArchiveMessageEdit) channel.addMessage();
			MailArchiveMessageHeaderEdit header = edit.getMailArchiveHeaderEdit();
			edit.setBody(body);
			header.replaceAttachments(null);
			header.setSubject(subject);
			header.setFromAddress(sender);
			header.setDateSent(TimeService.newTime());
			header.setMailHeaders(mailHeaders);
			channel.commitMessage(edit, NotificationService.NOTI_NONE);
		}
		catch (Exception e)
		{
			log.debug("Exception: Mailsender.appendToArchive() #2, " + e.getMessage());
			retval = false;
		}
		return retval;
	}

    public List<String> sendEmail(ConfigEntry config, String fromEmail, String fromName,
			Map<String, String> to, String subject, String content,
			Map<String, MultipartFile> attachments) throws MailsenderException
	{
		// validate inputs
		if (fromEmail == null || fromEmail.trim().length() == 0)
		{
			throw new MailsenderException("invalid.from_replyto.address", fromEmail);
		}
		else if (to == null || to.isEmpty())
		{
			throw new MailsenderException().addMessage("error.no.recipients");
		}

		ArrayList<String> invalids = new ArrayList<String>();

		// host
		String smtpHost = configService.getString(SAKAI_SMTP_HOST, DEFAULT_SMTP_HOST);

		// port
		int smtpPort = configService.getInt(SAKAI_SMTP_PORT, DEFAULT_SMTP_PORT);

		// authentication
		String smtpUser = configService.getString(SAKAI_SMTP_USER);
		String smtpPassword = configService.getString(SAKAI_SMTP_PASSWORD);

		// allow sending
		boolean allowTransport = configService.getBoolean(SAKAI_SMTP_ALLOW_TRANSPORT, true);

		try
		{
			// gather attachments first to help determine the type of message to create
			String attachmentDirectory = configLogic.getUploadDirectory();
			LinkedList<EmailAttachment> emailAttachments = null;
			if (attachments != null && !attachments.isEmpty())
			{
				emailAttachments = new LinkedList<EmailAttachment>();
				for (MultipartFile file : attachments.values())
				{
					// store the file for permanence
					File f = new File(attachmentDirectory + file.getOriginalFilename());
					file.transferTo(f);

					EmailAttachment attachment = new EmailAttachment();
					attachment.setName(f.getName());
					attachment.setDisposition(EmailAttachment.ATTACHMENT);
					attachment.setPath(f.getPath());

					emailAttachments.add(attachment);
				}
			}

			Email emailMsg = null;

			// add text part first if HTML
			if (config.useRichTextEditor())
			{
				emailMsg = buildHtmlMessage(content, emailAttachments);
			}
			else
			{
				emailMsg = buildPlainMessage(content, emailAttachments);
			}

			// set the simple stuff
			emailMsg.setCharset(UTF_8);
			emailMsg.setHostName(smtpHost);
			emailMsg.setSmtpPort(smtpPort);
			emailMsg.setSubject(subject);

			// gather the sender
			setFrom(config, fromEmail, fromName, emailMsg);

			// add recipients
			setRecipients(to, invalids, emailMsg);

			if (invalids.size() == to.size())
			{
				throw new MailsenderException().addMessage("error.no.valid.recipients");
			}

			// add the sender, if requested. no need to validate the fromEmail as that happened when
			// setting the sender of the message
			if (config.isSendMeACopy())
			{
				emailMsg.addCc(fromEmail);
			}

			// add an identifier to the message for debugging later.
			// this helps differentiate messages sent from mail sender.
			emailMsg.addHeader("X-Mailer", "sakai-mailsender");

			// log message for debugging purposes
			if (log.isDebugEnabled())
			{
				// String addresses = InternetAddress.toString(internetAddresses);
				// TODO Add addresses
				String addresses = "TODO Add addresses";
				String logMsg = "EmailBean.sendEmail(): [SITE: " + getSiteID() + "], [From: "
						+ getCurrentUserId() + "-" + fromEmail + "], [Bcc: " + addresses
						+ "], [Subject: " + subject + "]";
				log.debug(logMsg);
			}

			// send if not in test mode
			if (!configLogic.isEmailTestMode())
			{
				if (smtpUser != null && smtpPassword != null)
				{
					emailMsg.setAuthentication(smtpUser, smtpPassword);
				}

				boolean useSsl = configService.getBoolean(SAKAI_SMTP_USE_SSL, DEFAULT_USE_SSL);
				emailMsg.setSSL(useSsl);

				// set some properties used during sending (partial send, connection timeout,
				// timeout)
				setSendProperites();

				// send if transport is allowed
				if (allowTransport)
				{
					emailMsg.send();
				}
			}
			// log if in test mode
			else
			{
				logMessage(emailMsg);
			}

			return invalids;
		}
		catch (EmailException ee)
		{
			String msg = ee.getMessage();
			log.error(ee.getMessage(), ee);
			throw new MailsenderException("exception.generic", msg);
		}
		catch (MessagingException msge)
		{
			String msg = msge.getMessage();
			log.error(msge.getMessage(), msge);
			throw new MailsenderException("exception.generic", msg);
		}
		catch (IOException ie)
		{
			String msg = ie.getMessage();
			log.error(ie.getMessage(), ie);
			throw new MailsenderException("exception.generic", msg);
		}
	}

	private void setSendProperites()
	{
		String connectionTimeout = configService.getString(SAKAI_SMTP_CONNECTION_TIMEOUT);
		if (connectionTimeout != null)
		{
			System.setProperty(MAIL_SMTP_CONNECTIONTIMEOUT, connectionTimeout);
		}

		String timeout = configService.getString(SAKAI_SMTP_TIMEOUT);
		if (timeout != null)
		{
			System.setProperty(MAIL_SMTP_TIMEOUT, timeout);
		}

		System.setProperty(MAIL_SMTP_SENDPARTIAL, Boolean.TRUE.toString());
	}

	private void setRecipients(Map<String, String> to, ArrayList<String> invalids, Email emailMsg)
	{
		for (Entry<String, String> entry : to.entrySet())
		{
			String email = entry.getKey();
			String name = entry.getValue();

			try
			{
				if (entry.getKey() != null)
				{
					emailMsg.addBcc(email, name);
				}
				else
				{
					emailMsg.addBcc(email);
				}
			}
			catch (EmailException e)
			{
				collectInvalids(invalids, email, name);
			}
		}
	}

	private void setFrom(ConfigEntry config, String fromEmail, String fromName, Email emailMsg)
			throws MailsenderException
	{
		String replyToName = null;
		String replyToEmail = null;

		if (ConfigEntry.ReplyTo.no_reply_to.name().equals(config.getReplyTo()))
		{
			replyToName = getCurrentSiteTitle();
			replyToEmail = "";
		}
		else
		{
			replyToName = fromName;
			replyToEmail = fromEmail;
		}

		// set the sender on the message
		try
		{
			emailMsg.setFrom(replyToEmail, replyToName);
		}
		catch (EmailException ae)
		{
			String msg = ae.getMessage() + ": " + replyToName + " " + replyToEmail;
			throw new MailsenderException("invalid.from_replyto.address", msg);
		}
	}

	private void logMessage(Email emailMessage) throws EmailException, MessagingException,
			IOException
	{
		emailMessage.buildMimeMessage();
		MimeMessage mimeMessage = emailMessage.getMimeMessage();
		StringBuilder msg = new StringBuilder("sendMail: from: "
				+ Arrays.toString(mimeMessage.getFrom()) + " to: "
				+ Arrays.toString(mimeMessage.getAllRecipients()) + " subject: "
				+ mimeMessage.getSubject() + " replyTo: "
				+ Arrays.toString(mimeMessage.getReplyTo()));

		msg.append("\n----[ HEADERS ]--------------------------------------\n"
				+ headersToStr(mimeMessage.getAllHeaders())
				+ "\n----------------------------------------------------\n");

		msg.append("----[ CONTENT ]--------------------------------------\n");

		Object messageContent = mimeMessage.getContent();

		if (messageContent instanceof MimeMultipart)
		{
			MimeMultipart mp = (MimeMultipart) messageContent;

			msg.append(multipartToStr(mp));
		}
		else
		{
			msg.append(messageContent);
		}

		msg.append("\n----------------------------------------------------\n");

		log.info(msg.toString());
	}

	/**
	 * Build a message part with plain text content. Add the message part to a multipart message.
	 *
	 * @param content
	 * @param multipart
	 * @param messageBodyPart
	 * @throws MessagingException
	 */
	private Email buildPlainMessage(String content, List<EmailAttachment> attachments)
			throws EmailException
	{
		Email retval = null;
		if (attachments == null || attachments.isEmpty())
		{
			SimpleEmail email = new SimpleEmail();
			email.addHeader("Content-Transfer-Encoding", "quoted-printable");
			email.setMsg(content);
			retval = email;
		}
		else
		{
			MultiPartEmail email = new MultiPartEmail();
			email.addHeader("Content-Transfer-Encoding", "quoted-printable");
			email.setMsg(content);

			for (EmailAttachment attachment : attachments)
			{
				email.attach(attachment);
			}
			retval = email;
		}

		return retval;
	}

	/**
	 * Build a message part with html content. Add the message part to a multipart message with an
	 * alternative plain text part.
	 *
	 * @param content
	 * @param multipart
	 * @param altpart
	 * @param messageBodyPart
	 * @throws MessagingException
	 */
	private Email buildHtmlMessage(String content, List<EmailAttachment> attachments)
			throws EmailException
	{
		HtmlEmail email = new HtmlEmail();
		email.addHeader("Content-Transfer-Encoding", "quoted-printable");

		Source source = new Source(content);
		String text = source.getRenderer().toString();

		// set the plain text part
		email.setTextMsg(text);

		// set the html part
		email.setHtmlMsg(content);

		// add the attachments
		if (attachments != null && !attachments.isEmpty())
		{
			for (EmailAttachment attachment : attachments)
			{
				email.attach(attachment);
			}
		}

		return email;
	}

	private void collectInvalids(ArrayList<String> invalids, String email, String name)
	{
		invalids.add("\"" + name + "\" <" + email + ">");
	}

	private String multipartToStr(MimeMultipart msg)
	{
		StringBuilder rv = new StringBuilder();

		if (msg != null)
		{
			try
			{
				int parts = msg.getCount();

				for (int i = 0; i < parts; i++)
				{
					BodyPart part = msg.getBodyPart(i);

					if (part != null)
					{
						rv.append(part.getContent());
					}
				}
			}
			catch (MessagingException e)
			{
				log.warn("Exception aggregating email content", e);
			}
			catch (IOException e)
			{
				log.warn("Exception aggregating email content", e);
			}
		}

		return rv.toString();
	}

	private String headersToStr(Enumeration<Header> headers)
	{
		StringBuilder rv = new StringBuilder("[\n");

		if (headers != null)
		{
			while (headers.hasMoreElements())
			{
				Header h = headers.nextElement();

				if (h != null)
				{
					rv.append("[ " + h.getName() + ": " + h.getValue() + " ]\n");
				}
			}
		}

		rv.append("]");

		return rv.toString();
	}

	public String getCurrentLocationId()
	{
		return getCurrentSite().getReference();
	}

	public boolean isUserSiteAdmin(String userId, String locationId)
	{
		return securityService.unlock(userId,
				org.sakaiproject.site.api.SiteService.SECURE_UPDATE_SITE, locationId);
	}

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	public void setFunctionManager(FunctionManager functionManager)
	{
		this.functionManager = functionManager;
	}

	public void setMailArchiveService(MailArchiveService mailArchiveService)
	{
		this.mailArchiveService = mailArchiveService;
	}

	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.configService = serverConfigurationService;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}
}
