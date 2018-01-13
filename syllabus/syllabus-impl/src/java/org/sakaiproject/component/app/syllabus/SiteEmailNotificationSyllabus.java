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
package org.sakaiproject.component.app.syllabus;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import org.sakaiproject.util.ResourceLoader;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.SiteEmailNotification;

@Slf4j
public class SiteEmailNotificationSyllabus extends SiteEmailNotification
{
	// //private static ResourceBundle rb = ResourceBundle.getBundle("siteemaanc");
	private static ResourceLoader rb = new ResourceLoader("siteemacon");

	private org.sakaiproject.component.api.ComponentManager cm;

	private static String SYLLABUS_MANAGER = "org.sakaiproject.api.app.syllabus.SyllabusManager";

	private SyllabusManager syllabusManager;

	public SiteEmailNotificationSyllabus()
	{
		cm = ComponentManager.getInstance();
		syllabusManager = (org.sakaiproject.api.app.syllabus.SyllabusManager) cm.get(SYLLABUS_MANAGER);
	}

	public SiteEmailNotificationSyllabus(String siteId)
	{
		super(siteId);
	}

	public NotificationAction getClone()
	{
		SiteEmailNotificationSyllabus clone = new SiteEmailNotificationSyllabus();
		clone.set(this);

		return clone;
	}

	protected String getSubject(Event event)
	{
		Reference ref = EntityManager.newReference(event.getResource());
		String function = event.getEvent();
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		if (siteId == null)
		{
			int lastIndex = ref.getReference().lastIndexOf("/");
			String dataId = ref.getReference().substring(lastIndex + 1);
			SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
			SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
			siteId = syllabusItem.getContextId();
		}

		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// String syllabusName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		int lastIndex = ref.getReference().lastIndexOf("/");
		String dataId = ref.getReference().substring(lastIndex + 1);
		SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		String syllabusName = syllabusData.getTitle();
		String returnedString = "";
		if (SyllabusService.EVENT_SYLLABUS_POST_NEW.equals(function))
		{
			returnedString =rb.getFormattedMessage("event.syllabus.post.new", new Object[]{title, syllabusName});
		}
		else if (SyllabusService.EVENT_SYLLABUS_POST_CHANGE.equals(function))
		{
			returnedString =rb.getFormattedMessage("event.syllabus.post.changed", new Object[]{title, syllabusName});
		}
		else if (SyllabusService.EVENT_SYLLABUS_DELETE_POST.equals(function))
		{
			returnedString =rb.getFormattedMessage("event.syllabus.post.delete", new Object[]{title, syllabusName});
		}

		return returnedString;
	}
	
	protected String plainTextContent(Event event) {
		String content = htmlContent(event);
		content = FormattedText.convertFormattedTextToPlaintext(content);
		return content;
	}
	
	protected String htmlContent(Event event) {
		StringBuilder buf = new StringBuilder();
		
		String newline = "<br />\n";
		
		Reference ref = EntityManager.newReference(event.getResource());
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		int lastIndex = ref.getReference().lastIndexOf("/");
		String dataId = ref.getReference().substring(lastIndex + 1);
		SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
		if (siteId == null)
		{
			siteId = syllabusItem.getContextId();
		}
		
		if (SyllabusService.EVENT_SYLLABUS_POST_NEW.equals(event.getEvent())
				|| SyllabusService.EVENT_SYLLABUS_POST_CHANGE.equals(event.getEvent()))
		{
			String content = syllabusData.getAsset();
			
			//Set of type SyllabusAttachment
			Set attachments = syllabusManager.getSyllabusAttachmentsForSyllabusData(syllabusData); 
			
			//don't let the word 'null' get through to the email
			if (content != null) {
				buf.append(content + newline + newline);
			}
			
			if (attachments != null && attachments.size() > 0) {
				buf.append(rb.getString("syllabus.attachments.list") + newline);
				buf.append("<ul>");
				// get the server url and prepend it to the relative url stored in the database
				String surl = ServerConfigurationService.getServerUrl();
				
				for (Iterator i = attachments.iterator(); i.hasNext();) {
					SyllabusAttachment attachment = (SyllabusAttachment) i.next();
					
					// take into account if the data begins with the server url and adjust the output accordingly
					String tempUrl = attachment.getUrl();
					String url = "";
					if (tempUrl.startsWith(surl))
					{
					    url = tempUrl;
					}
					else
					{
					    url = surl + attachment.getUrl();
					}
					
					buf.append("<li>\t");
					buf.append("<a href=\"" + url + "\">");
					buf.append(url);
					buf.append("</a></li>" + newline);
				}
				
				buf.append("</ul>" + newline);
			}
		}
		else if (SyllabusService.EVENT_SYLLABUS_DELETE_POST.equals(event.getEvent()))
		{
			String s =rb.getFormattedMessage("event.syllabus.delete", new Object[]{syllabusData.getTitle(),siteId});
			buf.append(s);
			buf.append(newline);
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

	protected void addSpecialRecipients(List users, Reference ref)
	{
	  //for SiteEmailNotification.getRecipients method doesn't get syllabus' recipients. 
	  //List users = SecurityService.unlockUsers(ability, ref.getReference()); doesn't get users for the site because of permission messing
	  //need add syllabus permission later
		try
		{
			String siteId = (getSite() != null) ? getSite() : ref.getContext();	
			Site site = SiteService.getSite(siteId);
			Set activeUsersIdSet = site.getUsers();
			List activeUsersIdList = new ArrayList(activeUsersIdSet.size());
//			List activeUserList = new Vector();
			Iterator iter = activeUsersIdSet.iterator();
			while(iter.hasNext())
			{
				activeUsersIdList.add((String)iter.next());
			}

			List activeUserList = UserDirectoryService.getUsers(activeUsersIdList);
			activeUserList.removeAll(users);
			for(int i=0; i<activeUserList.size(); i++)
			{
				users.add(activeUserList.get(i));
			}
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}

}
