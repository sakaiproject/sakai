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

package org.sakaiproject.mailarchive.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.mailarchive.api.MailArchiveMessage;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeader;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.Web;

/**
 * <p>
 * SiteEmailNotificationMail fills the notification message and headers with details from the email message that triggered the notification event.
 * </p>
 */
public class SiteEmailNotificationMail extends SiteEmailNotification
{
	// borrow from announcement's notification class
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemaanc");

	/**
	 * Construct.
	 */
	public SiteEmailNotificationMail()
	{
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationMail(String siteId)
	{
		super(siteId);
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility()
	{
		return MailArchiveService.SECURE_MAIL_READ;
	}

	/**
	 * @inheritDoc
	 */
	protected List getHeaders(Event event)
	{
		// send most of the headers from the original message, removing some
		Reference ref = EntityManager.newReference(event.getResource());
		MailArchiveMessage msg = (MailArchiveMessage) ref.getEntity();
		MailArchiveMessageHeader hdr = (MailArchiveMessageHeader) msg.getMailArchiveHeader();
		List headers = hdr.getMailHeaders();

		List filteredHeaders = super.getHeaders(event);
		String innerContentType = null;
		String outerContentType = null;
		String contentType = null;

		for (int i = 0; i < headers.size(); i++)
		{
			String headerStr = (String) headers.get(i);

			if (headerStr.startsWith("Return-Path") || headerStr.startsWith("Content-Transfer-Encoding")) continue;

			if (headerStr.startsWith(MailArchiveService.HEADER_INNER_CONTENT_TYPE + ": ")) innerContentType = headerStr;
			if (headerStr.startsWith(MailArchiveService.HEADER_OUTER_CONTENT_TYPE + ": ")) outerContentType = headerStr;

			if (!headerStr.startsWith("Content-Type: "))
			{
				filteredHeaders.add(headerStr);
			}
			else
			{
				contentType = headerStr;
			}
		}

		if (innerContentType != null)
		{
			// use the content type of the inner email message body
			//filteredHeaders.add(innerContentType.replaceAll(MailArchiveService.HEADER_INNER_CONTENT_TYPE, "Content-Type"));
		}
		else if (outerContentType != null)
		{
			// use the content type from the outer message (content type as set in the email originally)
			//filteredHeaders.add(outerContentType.replaceAll(MailArchiveService.HEADER_OUTER_CONTENT_TYPE, "Content-Type"));
		}
		else if (contentType != null)
		{
			// Oh well, use the plain old Content-Type header
			//filteredHeaders.add(contentType);
		}

		return filteredHeaders;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newline, String title, boolean shouldUseHtml)
	{
		{
			if (shouldUseHtml) {
				return ("<hr/>" + newline + rb.getString("this") + " "
						+ ServerConfigurationService.getString("ui.service", "Sakai") + " (<a href=\""
						+ ServerConfigurationService.getPortalUrl() + "\">" + ServerConfigurationService.getPortalUrl() + "</a>) "
						+ rb.getString("forthe") + " " + title + " " + rb.getString("site") + newline + rb.getString("youcan") + newline);
			} else {
				return (rb.getString("separator") + newline + rb.getString("separator") + newline + rb.getString("this") + " "
						+ ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl()
						+ ") " + rb.getString("forthe") + " " + title + " " + rb.getString("site") + newline + rb.getString("youcan")
						+ newline);
			}
		}
	}

	@Override
	protected String htmlContent() {
		StringBuilder buf = new StringBuilder();

		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		MailArchiveMessage msg = (MailArchiveMessage) ref.getEntity();
		MailArchiveMessageHeader hdr = (MailArchiveMessageHeader) msg.getMailArchiveHeader();

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

		// use the message's body
		buf.append(Web.encodeUrlsAsHtml(msg.getBody()));

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append("<br/>\n" + "Attachments:<br/>\n");
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("<br/>\n<a href=\"" + attachment.getUrl() + "\" >" + attachmentTitle + "</a><br/>\n");
			}
		}

		return buf.toString();
	}

	@Override
	protected EmailNotification makeEmailNotification() {
		return new SiteEmailNotificationMail();
	}

	@Override
	protected String plainTextContent() {
		StringBuilder buf = new StringBuilder();

		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		MailArchiveMessage msg = (MailArchiveMessage) ref.getEntity();
		MailArchiveMessageHeader hdr = (MailArchiveMessageHeader) msg.getMailArchiveHeader();

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

		// use the message's body
		// %%% JANDERSE convert to plaintext - email is currently sent plaintext only,
		// so text formatting that may be present in the message should be removed.
		buf.append(FormattedText.convertFormattedTextToPlaintext(msg.getBody()));

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append("\n" + "Attachments:\n");
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("\n" + attachmentTitle);
				buf.append("\n" + attachment.getUrl() + "\n");
			}
		}
		
		// tag the message
		buf.append("\n" + rb.getString("separator") + "\n" + rb.getString("this") + " "
				+ ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl()
				+ ") " + rb.getString("forthe") + " " + title + " " + rb.getString("site") + "\n" + rb.getString("youcan")
				+ "\n");

		return buf.toString();
	}
}
