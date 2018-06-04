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

import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SiteEmailNotification;


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

	private EntityManager entityManager;
	private SecurityService securityService;
	private NotificationService notificationService;
	private EventTrackingService eventTrackingService;
	private SiteService siteService;
	private TimeService timeService;
	private UserDirectoryService userDirectoryService;

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

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
		Time now = timeService.newTime();
		
		if (now.after(hdr.getDate()))
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
			buf.append(rb.getFormattedMessage("noti.header.add", new Object[]{title, url}));
		}
		else
		{
			buf.append(rb.getFormattedMessage("noti.header.update", new Object[]{title, url}));
		}
		buf.append(" " + rb.getString("at_date") + " ");
		buf.append(hdr.getDate().toStringLocalFull());
		buf.append(newline);
		buf.append(msg.getBody());
		buf.append(newline);

		// add any attachments
		List<Reference> attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append(newline + rb.getString("Attachments") + newline);
			for (Iterator<Reference> iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("<a href=\"" + attachment.getUrl() + "\">");
				buf.append(attachmentTitle);
				buf.append("</a>" + newline);
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
		rv.add(getFromAddress(event));

		// to
		rv.add(getTo(event));

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
	 * Format the announcement notification from address.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the announcement notification from address.
	 */
	protected String getFromAddress(Event event)
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
		//String no_reply = "From: \"" + userDisplay + "\" <" + userEmail + ">";
		//String no_reply_withTitle = "From: \"" + title + "\" <" + userEmail + ">";	
		String from = "From: Sakai"; // fallback value
		 if (title!=null && !title.equals("")){ 
		     from = "From: \"" + title + "\" <" + userEmail + ">"; 
		 } else {
		     String fromVal = getFrom(event); // should not return null but better safe than sorry
	         if (fromVal != null) {
	             from = fromVal;
	         }
		 }
		
		// get the message
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		String userId = msg.getAnnouncementHeader().getFrom().getId();

		//checks if "from" email id has to be included? and whether the notification is a delayed notification?. SAK-13512
		// SAK-20988 - emailFromReplyable@org.sakaiproject.event.api.NotificationService is deprecated
		boolean notificationEmailFromReplyable = ServerConfigurationService.getBoolean("notify.email.from.replyable", false);
		if (notificationEmailFromReplyable 
		        && from.contains(userEmail)
		        && userId != null) 
		{
				try
				{
					User u = userDirectoryService.getUser(userId);
					userDisplay = u.getDisplayName();
					userEmail = u.getEmail();
					if ((userEmail != null) && (userEmail.trim().length()) == 0) userEmail = null;
					
				} catch (UserNotDefinedException e) {
					log.warn("Failed to load user from announcement header: {}. Will send from no-reply@{} instead.", userId, ServerConfigurationService.getServerName());
				}
				
				// some fallback positions
				if (userEmail == null) userEmail = ServerConfigurationService.getString("setup.request","no-reply@" + ServerConfigurationService.getServerName());
				if (userDisplay == null) userDisplay = ServerConfigurationService.getString("ui.service", "Sakai");
				from="From: \"" + userDisplay + "\" <" + userEmail + ">";
		}
		
		return from;
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
		try {
			// needed to access the message
			enableSecurityAdvisorToGetAnnouncement();
			
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
			disableSecurityAdvisor();
		}
	}

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 */
	protected void enableSecurityAdvisorToGetAnnouncement() {
		// put in a security advisor so we can do our podcast work without need
		// of further permissions
		securityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				if (function.equals(AnnouncementService.SECURE_ANNC_READ) || function.equals(ContentHostingService.AUTH_RESOURCE_READ)) // SAK-23300
					return SecurityAdvice.ALLOWED;
				else
					return SecurityAdvice.PASS;
			}
		});
	}

	/**
	 * remove recent add SecurityAdvisor from stack
	 */
	protected void disableSecurityAdvisor() {
		securityService.popAdvisor();
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
			buf.append(FormattedText.convertFormattedTextToPlaintext(rb.getFormattedMessage("noti.header.add", new Object[]{title,url})));

		}
		else
		{
			buf.append(FormattedText.convertFormattedTextToPlaintext(rb.getFormattedMessage("noti.header.update", new Object[]{title,url})));
		}
		
        buf.append(" " + rb.getString("at_date") + " ");
        buf.append(hdr.getDate().toStringLocalFull());
		buf.append(newline);
		buf.append(FormattedText.convertFormattedTextToPlaintext(msg.getBody()));
		buf.append(newline);

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append(newline + rb.getString("Attachments") + newline);
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append(attachmentTitle + ": " +attachment.getUrl() + newline);
			}
		}

		return buf.toString();
	}

}
