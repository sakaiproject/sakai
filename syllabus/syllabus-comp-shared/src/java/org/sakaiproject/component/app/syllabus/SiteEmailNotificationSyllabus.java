/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.event.Event;
import org.sakaiproject.service.legacy.notification.NotificationAction;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;

public class SiteEmailNotificationSyllabus extends SiteEmailNotification
{
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemaanc");

	private org.sakaiproject.api.kernel.component.ComponentManager cm;

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
			returnedString = "[ " + title + " - " + "Existed Syllabus Item Changed" + " ] " + syllabusName;
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
}
