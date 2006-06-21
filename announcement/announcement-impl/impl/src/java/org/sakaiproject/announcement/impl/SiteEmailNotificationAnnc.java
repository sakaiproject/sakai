/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
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

package org.sakaiproject.announcement.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.SiteEmailNotification;

/**
 * <p>
 * SiteEmailNotificationAnnc fills the notification message and headers with details from the announcement message that triggered the notification event.
 * </p>
 */
public class SiteEmailNotificationAnnc extends SiteEmailNotification
{
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemaanc");

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
	public NotificationAction getClone()
	{
		SiteEmailNotificationAnnc clone = new SiteEmailNotificationAnnc();
		clone.set(this);

		return clone;
	}

	/**
	 * @inheritDoc
	 */
	public void notify(Notification notification, Event event)
	{
		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// skip drafts
		if (hdr.getDraft()) return;

		super.notify(notification, event);
	}

	/**
	 * @inheritDoc
	 */
	protected String getMessage(Event event)
	{
		StringBuffer buf = new StringBuffer();
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
		buf.append("\">");
		buf.append(ServerConfigurationService.getPortalUrl());
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
	protected List getHeaders(Event e)
	{
		List rv = new ArrayList(2);

		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html");

		// set the subject
		rv.add("Subject: " + getSubject(e));

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newline, String title)
	{
		// tag the message - HTML version
		String rv = newline + rb.getString("separator") + newline + rb.getString("this") + " "
				+ ServerConfigurationService.getString("ui.service", "Sakai") + " (<a href=\""
				+ ServerConfigurationService.getPortalUrl() + "\">" + ServerConfigurationService.getPortalUrl() + "</a>) "
				+ rb.getString("forthe") + " " + title + " " + rb.getString("site") + newline + rb.getString("youcan") + newline;
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected boolean isBodyHTML(Event e)
	{
		return true;
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
}
