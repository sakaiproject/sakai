/*********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-comp-shared/src/java/org/sakaiproject/component/app/syllabus/SiteEmailNotificationSyllabus.java,v 1.4 2005/07/01 09:11:28 cwen.iupui.edu Exp $
*
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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.service.legacy.event.Event;
import org.sakaiproject.service.legacy.notification.Notification;
import org.sakaiproject.service.legacy.notification.NotificationAction;
import org.sakaiproject.service.legacy.resource.Reference;
import org.sakaiproject.service.legacy.resource.ResourceProperties;
import org.sakaiproject.service.legacy.resource.ReferenceVector;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;

import org.sakaiproject.service.legacy.resource.Resource;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.session.cover.UsageSessionService;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.api.app.syllabus.*;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.service.legacy.user.User;

public class SiteEmailNotificationSyllabus
	extends SiteEmailNotification
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

	public void notify(Notification notification, Event event)
	{
		Reference ref = new Reference(event.getResource());

		super.notify(notification, event);

	}

	protected String getFrom(Event event)
	{
/*		Reference ref = new Reference(event.getResource());
		Resource r = ref.getResource();
		ResourceProperties props = ref.getProperties();
*/
		try
		{
//			return props.getUserProperty(ResourceProperties.PROP_MODIFIED_BY).getEmail();
//		  return UsageSessionService.getSessionUserId();
		  User user = UserDirectoryService.getUser(UsageSessionService.getSessionUserId());
		  return user.getEmail();
		}
		catch (Throwable e)
		{
			return "postmaster@" + ServerConfigurationService.getServerName();
		}
	}

	public String getSubject(Event event)
	{
		Reference ref = new Reference(event.getResource());
/*		Resource r = ref.getResource();
		ResourceProperties props = ref.getProperties();*/

		String function = event.getEvent();

		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		
		if(siteId == null)
		{
		  int lastIndex = ref.getReference().lastIndexOf("/");
		  String dataId = ref.getReference().substring(lastIndex+1);
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

		//String syllabusName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
	  int lastIndex = ref.getReference().lastIndexOf("/");
	  String dataId = ref.getReference().substring(lastIndex+1);
	  SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		String syllabusName = syllabusData.getTitle();
		String returnedString = "";
		if(SyllabusService.EVENT_SYLLABUS_POST_NEW.equals(function))
		{
		  returnedString = "[ "	+ title	+ " - "
				+ "New Posted Syllabus Item" +  " ] " + syllabusName;;
		}
		else if(SyllabusService.EVENT_SYLLABUS_POST_CHANGE.equals(function))
		{
		  returnedString = "[ "	+ title	+ " - "
				+ "Existed Syllabus Item Changed" +  " ] " + syllabusName;;
		}
		else if(SyllabusService.EVENT_SYLLABUS_DELETE_POST.equals(function))
		{
		  returnedString = "[ "	+ title	+ " - "
				+ "Posted Syllabus Item Has Been Deleted" +  " ] " + syllabusName;;
		}
		
		return returnedString;
	}

	protected String getMessage(Event event)
	{
		StringBuffer buf = new StringBuffer();

		if(SyllabusService.EVENT_SYLLABUS_POST_NEW.equals(event.getEvent()) || 
		    SyllabusService.EVENT_SYLLABUS_POST_CHANGE.equals(event.getEvent()))
		{
		  String newline = "<br />\n";
		  
		  Reference ref = new Reference(event.getResource());
		  String siteId = (getSite() != null) ? getSite() : ref.getContext();
		  
		  int lastIndex = ref.getReference().lastIndexOf("/");
		  String dataId = ref.getReference().substring(lastIndex+1);
		  SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		  SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
		  if(siteId == null)
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
		  catch (Exception ignore) {}
		  
		  buf.append(syllabusData.getAsset() + newline);
		  
		  /* when attachment is done for syllabus - can get attachments from syllabusData.		
		   ReferenceVector attachments = hdr.getAttachments();
		   if (attachments.size() > 0)
		   {
		   buf.append(newline + rb.getString("att") + newline);
		   for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext(); )
		   {
		   Reference attachment = (Reference) iAttachments.next();
		   String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		   buf.append("<a href=\"" + attachment.getUrl() + "\">" + attachmentTitle + "</a>" + newline);
		   }
		   }*/
		  
		}
		else if(SyllabusService.EVENT_SYLLABUS_DELETE_POST.equals(event.getEvent()))
		{
		  String newline = "<br />\n";
		  
		  Reference ref = new Reference(event.getResource());
		  String siteId = (getSite() != null) ? getSite() : ref.getContext();
		  
		  int lastIndex = ref.getReference().lastIndexOf("/");
		  String dataId = ref.getReference().substring(lastIndex+1);
		  SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		  SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
		  if(siteId == null)
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

	public List getAdditionalHeaders(Event e)
	{
	  List ret = new ArrayList(1);
	  ret.add("Content-Type: text/html");
	  return ret;
	}
	
	public boolean isBodyHTML(Event e)
	{
	  return true;
	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-comp-shared/src/java/org/sakaiproject/component/app/syllabus/SiteEmailNotificationSyllabus.java,v 1.4 2005/07/01 09:11:28 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
