/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.mailarchive.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.mailarchive.api.MailArchiveMessage;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeader;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.SiteEmailNotification;

/**
 * <p>
 * SiteEmailNotificationMail fills the notification message and headers with details from the email message that triggered the notification event.
 * </p>
 */
public class SiteEmailNotificationMail extends SiteEmailNotification
{
	// ResourceBundle _not_ ResourceLoader -- we want the site's default locale
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

		for (int i = 0; i < headers.size(); i++)
		{
			String headerStr = (String) headers.get(i);

			if (headerStr.regionMatches(true, 0, MailArchiveService.HEADER_RETURN_PATH, 0, MailArchiveService.HEADER_RETURN_PATH.length())) 
				continue;
			if (headerStr.regionMatches(true, 0, MailArchiveService.HEADER_CONTENT_TRANSFER_ENCODING, 0, MailArchiveService.HEADER_CONTENT_TRANSFER_ENCODING.length())) 
				continue;
			if (headerStr.regionMatches(true, 0, MailArchiveService.HEADER_CONTENT_TYPE, 0, MailArchiveService.HEADER_CONTENT_TYPE.length())) 
				continue;
			
			filteredHeaders.add(headerStr);
		}

		return filteredHeaders;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String title, boolean shouldUseHtml)
	{
		StringBuilder buf = new StringBuilder();
			
		if (shouldUseHtml) {
			buf.append("<br/><hr/><br/>");
			String portalUrl = "<a href=\"" + ServerConfigurationService.getPortalUrl() + "\" >" + ServerConfigurationService.getPortalUrl() + "<a/>"; 
			buf.append( MessageFormat.format( rb.getString("automsg1"),  
														 new Object[]{ServerConfigurationService.getString("ui.service", "Sakai"), 
																		  portalUrl, title} ));
			buf.append( "<br/>" + rb.getString("automsg2")+"<br/>" );
		} 
		else {
			buf.append("\n----------------------\n" );
			buf.append( MessageFormat.format( rb.getString("automsg1"),
														 new Object[]{ServerConfigurationService.getString("ui.service", "Sakai"), 
																		  ServerConfigurationService.getPortalUrl(), title} ));
			buf.append( "\n" + rb.getString("automsg2") + "\n" );
		}
		
		return buf.toString();
	}

	
	@Override
	protected String htmlContent(Event event) {
		StringBuilder buf = new StringBuilder();

		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		MailArchiveMessage msg = (MailArchiveMessage) ref.getEntity();
		MailArchiveMessageHeader hdr = (MailArchiveMessageHeader) msg.getMailArchiveHeader();

		// if html isn't available, convert plain-text into html
		buf.append( msg.getFormattedBody() );

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append("<br/>" + "Attachments:<br/>");
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("<br/><a href=\"" + attachment.getUrl() + "\" >" + attachmentTitle + "</a><br/>");
			}
		}
		
		return buf.toString();
	}

	@Override
	protected EmailNotification makeEmailNotification() {
		return new SiteEmailNotificationMail();
	}

	@Override
	protected String plainTextContent(Event event) {
		StringBuilder buf = new StringBuilder();

		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		MailArchiveMessage msg = (MailArchiveMessage) ref.getEntity();
		MailArchiveMessageHeader hdr = (MailArchiveMessageHeader) msg.getMailArchiveHeader();

		// if plain-text isn't available, convert html into plain text
		if ( msg.getBody() != null && msg.getBody().length() > 0 )
			buf.append( msg.getBody() );
		else
			buf.append(FormattedText.convertFormattedTextToPlaintext(msg.getHtmlBody()));

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append("\n\n" + "Attachments:\n");
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("\n" + attachmentTitle);
				buf.append("\n" + attachment.getUrl() + "\n");
			}
		}

		return buf.toString();
	}
}
