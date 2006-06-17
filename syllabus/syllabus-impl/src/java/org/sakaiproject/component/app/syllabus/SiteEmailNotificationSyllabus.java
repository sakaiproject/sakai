/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/syllabus/trunk/syllabus-impl/src/java/org/sakaiproject/component/app/syllabus/SiteEmailNotificationSyllabus.java $
 * $Id: SiteEmailNotificationSyllabus.java 8122 2006-05-01 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.syllabus;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.SiteEmailNotification;

public class SiteEmailNotificationSyllabus extends SiteEmailNotification
{
	////private static ResourceBundle rb = ResourceBundle.getBundle("siteemaanc");
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemacon");

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
			returnedString = "[ " + title + " - " + "New Posted Syllabus Item" + " ] " + syllabusName;
			;
		}
		else if (SyllabusService.EVENT_SYLLABUS_POST_CHANGE.equals(function))
		{
			returnedString = "[ " + title + " - " + "Existing Syllabus Item Changed" + " ] " + syllabusName;
			;
		}
		else if (SyllabusService.EVENT_SYLLABUS_DELETE_POST.equals(function))
		{
			returnedString = "[ " + title + " - " + "Posted Syllabus Item Has Been Deleted" + " ] " + syllabusName;
			;
		}

		return returnedString;
	}

	/**
	 * @inheritDoc
	 */
	protected String getMessage(Event event)
	{
		StringBuffer buf = new StringBuffer();

		if (SyllabusService.EVENT_SYLLABUS_POST_NEW.equals(event.getEvent())
				|| SyllabusService.EVENT_SYLLABUS_POST_CHANGE.equals(event.getEvent()))
		{
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

			buf.append(syllabusData.getAsset() + newline);
		}
		else if (SyllabusService.EVENT_SYLLABUS_DELETE_POST.equals(event.getEvent()))
		{
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

			buf.append(" Syllabus Item - ");
			buf.append(syllabusData.getTitle());
			buf.append(" for Site - ");
			buf.append(siteId);
			buf.append(" has been deleted.");
			buf.append(newline);
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
	protected boolean isBodyHTML(Event e)
	{
		return true;
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
}
