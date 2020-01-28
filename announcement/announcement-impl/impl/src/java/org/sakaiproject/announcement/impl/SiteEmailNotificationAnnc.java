/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.announcement.impl;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.api.FormattedText;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * SiteEmailNotificationAnnc fills the notification message and headers with details from the announcement message that triggered the notification event.
 * </p>
 */
@Slf4j
public class SiteEmailNotificationAnnc extends SiteEmailNotification  
				implements ScheduledInvocationCommand
{
	private static ResourceLoader rb = new ResourceLoader("siteemaanc");
	private static final String PORTLET_CONFIG_PARM_MERGED_CHANNELS = "mergedAnnouncementChannels";

	private static final String SAK_PROP_EMAIL_TO_MATCHES_FROM = "announcement.notification.email.to.matches.from";
	private static final boolean SAK_PROP_EMAIL_TO_MATCHES_FROM_DEFAULT = false;

	@Setter private EntityManager entityManager;
	@Setter private SecurityService securityService;
	@Setter private NotificationService notificationService;
	@Setter private EventTrackingService eventTrackingService;
	@Setter private SiteService siteService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private UserTimeService userTimeService;
	@Setter private FormattedText formattedText;

	/**
	 * Construct.
	 */
	public SiteEmailNotificationAnnc()
	{
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationAnnc(String siteId)
	{
		super(siteId);
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility()
	{
		return AnnouncementService.SECURE_ANNC_READ;
	}

	/**
	 * @inheritDoc
	 */
	public void notify(Notification notification, Event event)
	{
		// get the message
		Reference ref = entityManager.newReference(event.getResource());
		AnnouncementMessageEdit msg = (AnnouncementMessageEdit) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// do not do notification for hidden (draft) messages
		if (hdr.getDraft()) return;

		// Put here since if release date after now, do not notify
		// since scheduled notification has been set.
		Instant now = Instant.now();
		
		if (now.isAfter(hdr.getInstant()))
		{
			super.notify(notification, event);
		}
	}

	/**
	 * @inheritDoc
	 */
	protected String htmlContent(Event event)
	{
		StringBuilder buf = new StringBuilder();
		String newline = "<br />\n";

		// get the message
		Reference ref = entityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		String url = ServerConfigurationService.getPortalUrl()+ "/site/"+ siteId;
		try
		{
			Site site = siteService.getSite(siteId);
			title = site.getTitle();
			url = site.getUrl(); // Might have a better URL.
		}
		catch (Exception ignore)
		{
			log.warn("Failed to load site: {} for: {}", siteId, event.getResource());
		}

		// Now build up the message text.
		if (AnnouncementService.SECURE_ANNC_ADD.equals(event.getEvent()))
		{
			if(!serverConfigurationService.getBoolean("notify.email.from.replyable", false)) {
				buf.append(rb.getFormattedMessage("noti.header.sender.info.add", title, url, hdr.getFrom().getDisplayName()));
			}
			else {
				buf.append(rb.getFormattedMessage("noti.header.add", title, url));
			}
		}
		else
		{
			if(!serverConfigurationService.getBoolean("notify.email.from.replyable", false)) {
				buf.append(rb.getFormattedMessage("noti.header.sender.info.update", title, url, hdr.getFrom().getDisplayName()));
			}
			else {
				buf.append(rb.getFormattedMessage("noti.header.update", title, url));
			}
		}
		buf.append(" ").append(rb.getString("at_date")).append(" ");
		buf.append(userTimeService.shortLocalizedTimestamp(hdr.getInstant(), rb.getLocale()));
		buf.append(newline);
		buf.append(msg.getBody());
		buf.append(newline);

		// add any attachments
		List<Reference> attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append(newline).append(rb.getString("Attachments")).append(newline);
			for (Reference attachment : attachments)
			{
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("<a href=\"").append(attachment.getUrl()).append("\">");
				buf.append(attachmentTitle);
				buf.append("</a>").append(newline);
			}
		}

		return buf.toString();
	}

	/**
	 * get announcement group information
	 */
	private String getAnnouncementGroup(AnnouncementMessage a)
	{
		if (a.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) != null
				&& a.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW).equals(Boolean.TRUE.toString()))
		{
			return rb.getString("Public");
		}
		else if (a.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL))
		{
			return rb.getString("Allgroups");
		}
		else
		{
			int count = 0;
			String allGroupString = "";
			try
			{
				Site site = siteService.getSite(entityManager.newReference(a.getReference()).getContext());
				for (Iterator i = a.getAnnouncementHeader().getGroups().iterator(); i.hasNext();)
				{
					Group aGroup = site.getGroup((String) i.next());
					if (aGroup != null)
					{
						count++;
						if (count > 1)
						{
							allGroupString = allGroupString.concat(", ").concat(aGroup.getTitle());
						}
						else
						{
							allGroupString = aGroup.getTitle();
						}
					}
				}
			}
			catch (IdUnusedException e)
			{
				// No site available.
			}
			return allGroupString;
		}
	}	
	
	/**
	 * @inheritDoc
	 */
	protected List getHeaders(Event event)
	{
		List rv = super.getHeaders(event);

		// Set the content type of the message body to HTML
		// rv.add("Content-Type: text/html");

		// set the subject
		rv.add("Subject: " + getSubject(event));
		
		// from
		rv.add(getAddress(event, AddressField.FROM));

		// to
		if (ServerConfigurationService.getBoolean(SAK_PROP_EMAIL_TO_MATCHES_FROM, SAK_PROP_EMAIL_TO_MATCHES_FROM_DEFAULT))
		{
			rv.add(getAddress(event, AddressField.TO));
		}
		else
		{
			rv.add(getTo(event));
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String title, boolean shouldUseHtml)
	{
		if (shouldUseHtml) {
			return rb.getFormattedMessage("noti.tag.html", new Object[]{ServerConfigurationService.getString("ui.service", "Sakai"), ServerConfigurationService.getPortalUrl(), title});
		} else {
			return rb.getFormattedMessage("noti.tag", new Object[]{ServerConfigurationService.getString("ui.service", "Sakai"), ServerConfigurationService.getPortalUrl(), title});
		}
	}

	/**
	 * Format the announcement notification subject line.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the announcement notification subject line.
	 */
	protected String getSubject(Event event)
	{
		// get the message
		Reference ref = entityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			final Site site = siteService.getSite(siteId);
			boolean shortDescription = ServerConfigurationService.getBoolean("announcement.email.use.short.description", false);

			title = site.getTitle();
			if(shortDescription && StringUtils.isNotEmpty(site.getShortDescription())) {
				title = site.getShortDescription();
			}
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		return rb.getFormattedMessage("noti.subj", new Object[]{title, hdr.getSubject()});
	}
	
	/**
	 * Defines the possible parameters for getAddress() to determine if we're getting the address for the 'From' or the 'To' field
	 */
	private enum AddressField
	{
		FROM,
		TO;
	}

	/**
	 * Gets the address for either the "From:" or "To:" field in an announcement notification email.
	 * They are formatted as follows:
	 * From/To: "display address" <email address>
	 * @param event the announcement event backing the notification
	 * @param field specifies if we are getting the 'From' or the 'To' address
	 */
	private String getAddress(Event event, AddressField field)
	{
		Reference ref = entityManager.newReference(event.getResource());
		
		//SAK-14831, yorkadam, make site title reflected in 'From:' name instead of default ServerConfigurationService.getString("ui.service", "Sakai");
		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		String title = "";
		try
		{
			Site site = siteService.getSite(siteId);
			title = site.getTitle();
		}
		catch(Exception ignore)
		{}
		
		String userEmail = ServerConfigurationService.getString("setup.request","no-reply@" + ServerConfigurationService.getServerName());
		String userDisplay = ServerConfigurationService.getString("ui.service", "Sakai");
		String address = field == AddressField.FROM ? "From: " : "To: ";
		if (title!=null && !title.equals("")){
			address = address + "\"" + title + "\" <" + userEmail + ">";
		} else {
			String val = field == AddressField.FROM ? getFrom(event) : getTo(event);
			if (val != null)
			{
				address = val;
			}
		}
		
		// get the message
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		String userId = msg.getAnnouncementHeader().getFrom().getId();

		//checks if "from" email id has to be included? and whether the notification is a delayed notification?. SAK-13512
		// SAK-20988 - emailFromReplyable@org.sakaiproject.event.api.NotificationService is deprecated
		boolean notificationEmailFromReplyable = ServerConfigurationService.getBoolean("notify.email.from.replyable", false);
		if (notificationEmailFromReplyable 
		        && address.contains(userEmail)
		        && userId != null) 
		{
				try
				{
					User u = userDirectoryService.getUser(userId);
					userDisplay = u.getDisplayName();
					userEmail = u.getEmail();
					if ((userEmail != null) && (userEmail.trim().length()) == 0) userEmail = null;
					
				} catch (UserNotDefinedException e) {
					log.warn("Failed to load user from announcement header: {}. Will send with no-reply@{} instead.", userId, ServerConfigurationService.getServerName());
				}
				
				// some fallback positions
				if (userEmail == null) userEmail = ServerConfigurationService.getString("setup.request","no-reply@" + ServerConfigurationService.getServerName());
				if (userDisplay == null) userDisplay = ServerConfigurationService.getString("ui.service", "Sakai");

				address = field == AddressField.FROM ? "From: \"" : "To: \"";
				// 'From' should display the user; 'To' should display the site title; if the title is unavailable, fallback to the user
				String display = (field == AddressField.FROM || StringUtils.isBlank(title)) ? userDisplay : title;
				address = address + display + "\" <" + userEmail + ">";
		}
		
		return address;
	}

	/**
	 * Add to the user list any other users who should be notified about this ref's change.
	 * 
	 * @param users
	 *        The user list, already populated based on site visit and resource ability.
	 * @param ref
	 *        The entity reference.
	 */
	protected void addSpecialRecipients(List users, Reference ref)
	{
		//Reverting the faulty logic of SAK-21798 and SAK-18433. 
	}

	/**
	 * Implementation of command pattern. Will be called by ScheduledInvocationManager 
	 * for delayed announcement notifications
	 * 
	 * @param opaqueContext
	 * 			reference (context) for message
	 */
	public void execute(String opaqueContext) 
	{
		// get the message
		final Reference ref = entityManager.newReference(opaqueContext);

		// needed to access the message
		SecurityAdvisor sa = enableSecurityAdvisorToGetAnnouncement();

		try {
			final AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
			if (msg!=null) {
				final AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();
		
				// read the notification options
				final String notification = msg.getProperties().getProperty("notificationLevel");
		
				int noti = NotificationService.NOTI_OPTIONAL;
				if ("r".equals(notification))
				{
					noti = NotificationService.NOTI_REQUIRED;
				}
				else if ("n".equals(notification))
				{
					noti = NotificationService.NOTI_NONE;
				}
					
				final Event delayedNotificationEvent = eventTrackingService.newEvent("annc.schInv.notify", msg.getReference(), true, noti);
				//eventTrackingService.post(event);
		
				NotificationEdit notify = notificationService.addTransientNotification();
				
				super.notify(notify, delayedNotificationEvent);
			}
			
		} finally {
			// since we build the notification by accessing the
			// message within the super class, can't remove the
			// SecurityAdvisor until this point
			// done with access, need to remove from stack
			disableSecurityAdvisor(sa);
		}
	}

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 */
	protected SecurityAdvisor enableSecurityAdvisorToGetAnnouncement() {
		// put in a security advisor so we can do our podcast work without need
		// of further permissions
		SecurityAdvisor sa = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				if (function.equals(AnnouncementService.SECURE_ANNC_READ) || function.equals(ContentHostingService.AUTH_RESOURCE_READ)) // SAK-23300
					return SecurityAdvice.ALLOWED;
				else
					return SecurityAdvice.PASS;
			}
		};
		securityService.pushAdvisor(sa);
		return sa;
	}

	/**
	 * remove recent add SecurityAdvisor from stack
	 */
	protected void disableSecurityAdvisor(SecurityAdvisor sa) {
		securityService.popAdvisor(sa);
	}


	@Override
	protected EmailNotification makeEmailNotification() {
		return new SiteEmailNotificationAnnc();
	}

	@Override
	protected String plainTextContent(Event event) {
		StringBuilder buf = new StringBuilder();
		String newline = "\n\r";

		// get the message
		Reference ref = entityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();
				
		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		
		String url = ServerConfigurationService.getPortalUrl()+ "/site/"+ siteId;

		// get a site title
		String title = siteId;
		try
		{
			Site site = siteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
			
		}

		// Now build up the message text.
		if (AnnouncementService.SECURE_ANNC_ADD.equals(event.getEvent()))
		{
			if(!serverConfigurationService.getBoolean("notify.email.from.replyable", false)) {
				buf.append(formattedText.convertFormattedTextToPlaintext(rb.getFormattedMessage("noti.header.sender.info.add", title, url, hdr.getFrom().getDisplayName())));
			}
			else {
				buf.append(formattedText.convertFormattedTextToPlaintext(rb.getFormattedMessage("noti.header.add", title, url)));
			}

		}
		else
		{
			if(!serverConfigurationService.getBoolean("notify.email.from.replyable", false)) {
				buf.append(formattedText.convertFormattedTextToPlaintext(rb.getFormattedMessage("noti.header.sender.info.update", title, url, hdr.getFrom().getDisplayName())));
			}
			else {
				buf.append(formattedText.convertFormattedTextToPlaintext(rb.getFormattedMessage("noti.header.update", title, url)));
			}
		}
		
		buf.append(" ").append(rb.getString("at_date")).append(" ");
        buf.append(userTimeService.shortLocalizedTimestamp(hdr.getInstant(), rb.getLocale()));
		buf.append(newline);
		buf.append(formattedText.convertFormattedTextToPlaintext(msg.getBody()));
		buf.append(newline);

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append(newline).append(rb.getString("Attachments")).append(newline);
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append(attachmentTitle).append(": ").append(attachment.getUrl()).append(newline);
			}
		}

		return buf.toString();
	}

}
