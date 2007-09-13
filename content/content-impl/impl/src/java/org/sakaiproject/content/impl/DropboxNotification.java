/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

package org.sakaiproject.content.impl;

import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.EmailNotification;

/**
 * <p>
 * DropboxNotification is the notification action that handles the act of message (email) based notify related to changes in an individual dropbox.
 * </p>
 * <p>
 * The following should be specified to extend the class:
 * <ul>
 * <li>getRecipients() - get a collection of Users to send the notification to</li>
 * <li>getHeaders() - form the complete message headers (like from: to: reply-to: date: subject: etc). from: and to: are for display only</li>
 * <li>getMessage() - form the complete message body (minus headers)</li>
 * <li>getTag() - the part of the body at the end that identifies the list</li>
 * <li>isBodyHTML() - say if your body is html or not (not would be plain text)</li>
 * </ul>
 * </p>
 * <p>
 * getClone() should also be extended to clone the proper type of object.
 * </p>
 */
public class DropboxNotification extends EmailNotification 
{
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemacon");
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getClone()
	 */
	@Override
	public NotificationAction getClone() 
	{
		// TODO Auto-generated method stub
		return super.getClone();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getHeaders(org.sakaiproject.event.api.Event)
	 */
	@Override
	protected List<String> getHeaders(Event event) 
	{
		// TODO Auto-generated method stub
		return super.getHeaders(event);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getMessage(org.sakaiproject.event.api.Event)
	 */
	@Override
	protected String getMessage(Event event) 
	{
		// TODO Auto-generated method stub
		return super.getMessage(event);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getRecipients(org.sakaiproject.event.api.Event)
	 */
	@Override
	protected List getRecipients(Event event) 
	{
		// TODO Auto-generated method stub
		return super.getRecipients(event);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getTag(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getTag(String newline, String title) 
	{
		// TODO Auto-generated method stub
		return super.getTag(newline, title);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#isBodyHTML(org.sakaiproject.event.api.Event)
	 */
	@Override
	protected boolean isBodyHTML(Event event) 
	{
		// TODO Auto-generated method stub
		return super.isBodyHTML(event);
	}

	protected String plainTextContent() 
	{
		return generateContentForType(false);
	}
	
	protected String htmlContent() 
	{
		return generateContentForType(true);
	}

	private String generateContentForType(boolean shouldProduceHtml) 
	{
		// get the content & properties
		Reference ref = EntityManager.newReference(this.event.getResource());
		// TODO:  ResourceProperties props = ref.getProperties();

		// get the function
		String function = this.event.getEvent();
		//String subject = getSubject(this.event);

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
		
		StringBuilder buf = new StringBuilder();
		//addMessageText(buf, ref, subject, title, function, shouldProduceHtml);
		return buf.toString();
	}

}
