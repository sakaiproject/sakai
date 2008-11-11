/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.announcement.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * SiteEmailNotificationAnnc fills the notification message and headers with details from the announcement message that triggered the notification event.
 * </p>
 */
public class SiteEmailNotificationAnnc extends SiteEmailNotification 
				implements ScheduledInvocationCommand
{
	private ResourceLoader rb = new ResourceLoader("siteemaanc");

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SiteEmailNotificationAnnc.class);

	private ScheduledInvocationManager scheduledInvocationManager;
	
	private ComponentManager componentManager;
	
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
	 * Inject ScheudledInvocationManager
	 */
	public void setScheduledInvocationManager(
			ScheduledInvocationManager service) 
	{
		scheduledInvocationManager = service;
	}

	/**
	 * Inject ComponentManager
	 */
	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
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
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessageEdit msg = (AnnouncementMessageEdit) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// do not do notification for hidden (draft) messages
		if (hdr.getDraft()) return;

		// Put here since if release date after now, do not notify
		// since scheduled notification has been set.
		Time now = TimeService.newTime();
		
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
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// Now build up the message text.
		buf.append(rb.getString("An_announcement_has_been"));
		if (AnnouncementService.SECURE_ANNC_ADD.equals(event.getEvent()))
		{
			buf.append(" " + rb.getString("added"));
		}
		else
		{
			buf.append(" " + rb.getString("updated"));
		}
		buf.append(" " + rb.getString("in_the") + " \"");
		buf.append(title);
		buf.append("\" " + rb.getString("site_at"));
		buf.append(" " + ServerConfigurationService.getString("ui.service", "Sakai"));
		buf.append(" (<a href=\"");
		buf.append(ServerConfigurationService.getPortalUrl());
		buf.append("/site/");
		buf.append(siteId);
		buf.append("\">");
		buf.append(ServerConfigurationService.getPortalUrl());
		buf.append("/site/");
		buf.append(siteId);
		buf.append("</a>)");
		buf.append(newline);
		buf.append(newline);
		buf.append(newline);
		buf.append(rb.getString("Subject") + ": ");
		buf.append(hdr.getSubject());
		buf.append(newline);
		buf.append(newline);
		buf.append(rb.getString("From") + ": ");
		buf.append(hdr.getFrom().getDisplayName());
		buf.append(newline);
		buf.append(newline);
		buf.append(rb.getString("Date") + ": ");
		buf.append(hdr.getDate().toStringLocalFull());
		buf.append(newline);
		buf.append(newline);
		buf.append(rb.getString("Message") + ": ");
		buf.append(newline);
		buf.append(newline);
		buf.append(msg.getBody());
		buf.append(newline);
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
				buf.append("<a href=\"" + attachment.getUrl() + "\">" + attachmentTitle + "</a>" + newline);
			}
		}

		return buf.toString();
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
		rv.add(getFrom(event));

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
			return ("<hr/><br/>" + rb.getString("this") + " "
					+ ServerConfigurationService.getString("ui.service", "Sakai") + " (<a href=\""
					+ ServerConfigurationService.getPortalUrl() + "\">" + ServerConfigurationService.getPortalUrl() + "</a>) "
					+ rb.getString("forthe") + " " + title + " " + rb.getString("site") + "<br/>" + rb.getString("youcan") + "<br/>");
		} else {
			return (rb.getString("separator") + "\n" + rb.getString("this") + " "
					+ ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl()
					+ ") " + rb.getString("forthe") + " " + title + " " + rb.getString("site") + "\n" + rb.getString("youcan")
					+ "\n");
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
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		return "[ " + title + " - " + rb.getString("Announcement") + " ]   " + hdr.getSubject();
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
		// include any users who have AnnouncementService.SECURE_ALL_GROUPS and getResourceAbility() in the context
		String contextRef = SiteService.siteReference(ref.getContext());

		// get the list of users who have SECURE_ALL_GROUPS
		List allGroupUsers = SecurityService.unlockUsers(AnnouncementService.SECURE_ANNC_ALL_GROUPS, contextRef);

		// filter down by the permission
		if (getResourceAbility() != null)
		{
			List allGroupUsers2 = SecurityService.unlockUsers(getResourceAbility(), contextRef);
			allGroupUsers.retainAll(allGroupUsers2);
		}

		// remove any in the list already
		allGroupUsers.removeAll(users);

		// combine
		users.addAll(allGroupUsers);
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
		final Reference ref = EntityManager.newReference(opaqueContext);
		
		// needed to access the message
		enableSecurityAdvisor();
		
		final AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
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
			
		final Event delayedNotificationEvent = EventTrackingService.newEvent("annc.schInv.notify", msg.getReference(), true, noti);
//		EventTrackingService.post(event);

		final NotificationService notificationService = (NotificationService) ComponentManager.get(org.sakaiproject.event.api.NotificationService.class);
		NotificationEdit notify = notificationService.addTransientNotification();
		
		super.notify(notify, delayedNotificationEvent);

		// since we build the notification by accessing the
		// message within the super class, can't remove the
		// SecurityAdvisor until this point
		// done with access, need to remove from stack
		SecurityService.clearAdvisors();
	}

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 */
	protected void enableSecurityAdvisor() {
		// put in a security advisor so we can do our podcast work without need
		// of further permissions
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}


	@Override
	protected EmailNotification makeEmailNotification() {
		return new SiteEmailNotificationAnnc();
	}

	@Override
	protected String plainTextContent(Event event) {
		String content = htmlContent(event);
		content = FormattedText.convertFormattedTextToPlaintext(content);
		return content;
	}

}
