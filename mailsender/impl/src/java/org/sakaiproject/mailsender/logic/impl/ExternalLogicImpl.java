/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import static org.sakaiproject.mailsender.logic.impl.MailConstants.PROTOCOL_SMTP;

import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import javax.mail.MessagingException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.NoRecipientsException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveMessageEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeaderEdit;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.mailsender.AttachmentException;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;

/**
 * This is the implementation for logic which is external to our app logic
 */
@Slf4j
public class ExternalLogicImpl implements ExternalLogic
{
	// external service references
	private FunctionManager functionManager;
	private ToolManager toolManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private MailArchiveService mailArchiveService;
	private UserDirectoryService userDirectoryService;
	private TimeService timeService;
	private EmailService emailService;
	private ServerConfigurationService configService;
	private EventTrackingService eventService;
	private ContentHostingService contentHostingService;
	private EntityManager entityManager;

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
			String subject, String body, List<Attachment> attachments)
	{
		if (config == null)
		{
			config = ConfigEntry.DEFAULT_CONFIG;
		}

		if (attachments == null)
		{
			attachments = Collections.EMPTY_LIST;
		}

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
		if (useRTE())
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
			header.setDateSent(timeService.newTime());
			header.setMailHeaders(mailHeaders);
			// List of references needed.
			List<Reference> refs = new ArrayList<Reference>();
			for(Attachment attachment : attachments) {
				ContentResource resource = createAttachment(channel.getContext(), attachment.getContentTypeHeader(),
						attachment.getFilename(), attachment.getDataSource().getInputStream(), edit.getId());
				if (resource != null) {
					header.addAttachment(entityManager.newReference(resource.getReference()));
				}
			}
			channel.commitMessage(edit, NotificationService.NOTI_NONE);
		}
		catch (Exception e)
		{
			log.debug("Exception: Mailsender.appendToArchive() #2, " + e.getMessage());
			retval = false;
		}
		return retval;
	}

	protected ContentResource createAttachment(String siteId, String type, String fileName, InputStream in, String id) {
		// we just want the file name part - strip off any drive and path stuff
        // This shouldn't be necessary as it's already been processed.
		String resourceName = Validator.escapeResourceName(fileName);

		// make a set of properties to add for the new resource
		ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
		props.addProperty(ResourceProperties.PROP_DESCRIPTION, fileName);

		// make an attachment resource for this URL
		try {
			ContentResource attachment;
			if (siteId == null) {
				attachment = contentHostingService.addAttachmentResource(resourceName, type, in, props);
			} else {
				attachment = contentHostingService.addAttachmentResource(resourceName, siteId, null, type, in, props);
			}

			log.debug(id + " : attachment: " + attachment.getReference() + " size: " + attachment.getContentLength());

			return attachment;
		} catch (Exception any) {
			log.warn(id + " : exception adding attachment resource: " + fileName + " : " + any.toString());
			return null;
		}
	}

	public List<String> sendEmail(ConfigEntry config, String fromEmail, String fromName,
			Map<String, String> to, String subject, String content,
			List<Attachment> attachments) throws MailsenderException, AttachmentException
	{
        if (fromEmail == null)
        {
          MailsenderException me = new MailsenderException("'fromEmail' is required.", (Exception) null);
          me.addMessage("no.from.address");
          throw me;
        }
        if (to == null || to.isEmpty())
        {
          MailsenderException me = new MailsenderException("'to' is required.", (Exception) null);
          me.addMessage("error.no.recipients");
          throw me;
        }

        if (config == null)
        {
          config = ConfigEntry.DEFAULT_CONFIG;
        }

		ArrayList<EmailAddress> tos = new ArrayList<EmailAddress>();
        if (to != null)
        {
            for (Entry<String, String> entry : to.entrySet())
            {
                tos.add(new EmailAddress(entry.getKey(), entry.getValue()));
            }
        }

		EmailMessage msg = new EmailMessage();

		String replyToName = null;
		String replyToEmail = null;
		// set the "reply to" based on config
		if (ConfigEntry.ReplyTo.no_reply_to.name().equals(config.getReplyTo()))
		{
			replyToName = getCurrentSiteTitle();
			replyToEmail = configService.getString("setup.request","no-reply@" + configService.getServerName());
		}
		else
		{
			replyToName = fromName;
			replyToEmail = fromEmail;
		}

		msg.setFrom(new EmailAddress(replyToEmail, replyToName));

		msg.setSubject(subject);
		// set content type based on editor used
		if (useRTE())
		{
			msg.setContentType(ContentType.TEXT_HTML);
		}
		else
		{
			msg.setContentType(ContentType.TEXT_PLAIN);
		}
		msg.setBody(content);

        if (attachments != null)
        {
        	for (Attachment attachment : attachments) {
        		msg.addAttachment(attachment);
        	}
        }

		// send a copy
		if (config.isSendMeACopy())
		{
			msg.addRecipient(RecipientType.CC, fromName, fromEmail);
		}

		// add all recipients to the bcc field
		msg.addRecipients(RecipientType.BCC, tos);

		// add a special header for tracking
		msg.addHeader("X-Mailer", "sakai-mailsender");
		msg.addHeader("Content-Transfer-Encoding", "quoted-printable");

		try
		{
			List<EmailAddress> invalids = emailService.send(msg,true);
			List<String> rets = EmailAddress.toStringList(invalids);
			Event event = eventService.newEvent(ExternalLogic.EVENT_EMAIL_SEND,
					null, false);
			eventService.post(event);
			return rets;
		}
		//Catch these exceptions to give the user a better error message
		catch (AddressValidationException e)
		{
			MailsenderException me = new MailsenderException(e.getMessage(), e);
			me.addMessage("invalid.email.addresses", EmailAddress.toString(e
					.getInvalidEmailAddresses()));
			throw me;
		}
		catch (NoRecipientsException e)
		{
			MailsenderException me = new MailsenderException(e.getMessage(), e);
			me.addMessage("error.no.valid.recipients", "");
			throw me;
		} catch (MessagingException e) {
			MailsenderException me = new MailsenderException(e.getMessage(), e);
			me.addMessage("error.messaging.exception", "");
			throw me;
		}
	}

	private boolean useRTE() {
		String editor = StringUtils.trimToNull(configService.getString("wysiwyg.editor"));
		if (editor == null || "htmlarea".equals(editor)) {
			return false;
		} else {
			return true;
		}
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

	public List<String> getPermissionKeys()
	{
		String[] perms = new String[] {PERM_ADMIN, PERM_SEND};
		return Arrays.asList(perms);
	}

	public void setFunctionManager(FunctionManager functionManager)
	{
		this.functionManager = functionManager;
	}

	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
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

	public String propName(String propNameTemplate)
	{
		return propName(propNameTemplate, PROTOCOL_SMTP);
	}

	public String propName(String propNameTemplate, String protocol)
	{
		String formattedName = String.format(propNameTemplate, protocol);
		return formattedName;
	}

	public void setEmailService(EmailService emailService)
	{
		this.emailService = emailService;
	}

	public void setServerConfigurationService(ServerConfigurationService configService) {
		this.configService = configService;
	}

	public void setEventTrackingService(EventTrackingService eventService) {
		this.eventService = eventService;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
}
