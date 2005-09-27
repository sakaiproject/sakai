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

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.email.cover.EmailService;
import org.sakaiproject.service.legacy.event.Event;
import org.sakaiproject.service.legacy.notification.Notification;
import org.sakaiproject.service.legacy.notification.NotificationAction;
import org.sakaiproject.service.legacy.notification.cover.NotificationService;
import org.sakaiproject.service.legacy.preference.Preferences;
import org.sakaiproject.service.legacy.preference.PreferencesEdit;
import org.sakaiproject.service.legacy.preference.cover.PreferencesService;
import org.sakaiproject.service.legacy.resource.Reference;
import org.sakaiproject.service.legacy.resource.ResourceProperties;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.util.java.StringUtil;
import org.w3c.dom.Element;

public class EmailNotification
	implements NotificationAction
{
	private ResourceBundle rb = ResourceBundle.getBundle("emailnotification");
	
	protected String m_siteId = null;

  private org.sakaiproject.api.kernel.component.ComponentManager cm;
  private static String SYLLABUS_MANAGER = "org.sakaiproject.api.app.syllabus.SyllabusManager";
  private SyllabusManager syllabusManager; 
	
	public EmailNotification()
	{
    cm = ComponentManager.getInstance();
    syllabusManager = (org.sakaiproject.api.app.syllabus.SyllabusManager) cm.get(SYLLABUS_MANAGER);
	}

	public EmailNotification(String siteId)
	{
		m_siteId = siteId;

	}

	public void set(Element el)
	{
		m_siteId = StringUtil.trimToNull(el.getAttribute("site"));

	}

	public void set(NotificationAction other)
	{
		EmailNotification eOther = (EmailNotification) other;
		m_siteId = eOther.m_siteId;

	}

	public NotificationAction getClone()
	{
		EmailNotification clone = new EmailNotification();
		clone.set(this);

		return clone;

	}

	public void toXml(Element el)
	{
		if (m_siteId != null) el.setAttribute("site", m_siteId);

	}

	public void notify(Notification notification, Event event)
	{
		if 	(event.getPriority() == NotificationService.NOTI_NONE) return;

		List recipients = getRecipients(event);

		List immediate = immediateRecipients(recipients, notification, event);

		if (immediate.size() == 0) return;

		String message = getMessage(event);
		String from = getFrom(event);
		String headerTo = getTo(event);
		String replyTo = getReplyTo(event);
		String subject = getSubject(event);
		List additionalHeaders = getAdditionalHeaders(event);
		boolean isBodyHTML = isBodyHTML(event);
		String newline = (isBodyHTML) ? "<br />\n" : "\n";

		if (immediate.size() > 0)
		{
			Reference ref = EntityManager.newReference(event.getResource());
//			Resource r = ref.getResource();
			String title = (getSite() != null) ? getSite() : ref.getContext();
			if(title == null)
			{
			  int lastIndex = ref.getReference().lastIndexOf("/");
			  String dataId = ref.getReference().substring(lastIndex+1);
			  SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
			  SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
			  title = syllabusItem.getContextId();
			}
			
			org.sakaiproject.service.legacy.site.SiteService siteService = org.sakaiproject.service.legacy.site.cover.SiteService.getInstance();
			try
			{
				org.sakaiproject.service.legacy.site.Site site = siteService.getSite(title);
				title = site.getTitle();
			}
			catch (Exception ignore) {}

			String messageForImmediates = message
						+ newline + rb.getString("separator") + newline 
						+ rb.getString("this")
						+ " "
						+ ServerConfigurationService.getString("ui.service","Sakai")
						+ " "
						+ rb.getString("par1")
						+ ServerConfigurationService.getServerUrl()
						+ rb.getString("par2") 
						+ " "
						+ rb.getString("forthe") + " "
						+ title						+ " " + rb.getString("site") + newline
						+ rb.getString("youcan") + newline;

			if (from != null && from.length() > 0)
			{
				for (Iterator ii = immediate.iterator(); ii.hasNext();)
				{
					User user = (User) ii.next();
					String email = user.getEmail();
					if ((email != null) && (email.length() > 0))
					{
						// email = user.getDisplayName() + "<" + user.getEmail() + ">";
					    EmailService.send(from, email, subject, messageForImmediates, headerTo, replyTo, additionalHeaders);
					}
				}
			}
		}
	}

	protected String getFrom(Event event)
	{
		return null;

	}

	public String getSubject(Event event)
	{
		return "";

	}

	protected String getMessage(Event event)
	{
		return "";

	}
	
	protected List getAdditionalHeaders(Event event)
	{
		return null;
	}
	
	protected boolean isBodyHTML(Event event)
	{
	    return false;
	}

	protected String getTo(Event event)
	{
		return null;

	}

	protected String getReplyTo(Event event)
	{
		return null;

	}

	protected List getRecipients(Event event)
	{
		return new Vector();

	}

	protected String getSite()
	{
		return m_siteId;

	}

	protected List immediateRecipients(List recipients, Notification notification, Event event)
	{
		int priority = event.getPriority();

		if (priority == NotificationService.NOTI_REQUIRED)
		{
			return recipients;
		}

		List rv = new Vector();
		for (Iterator iUsers = recipients.iterator(); iUsers.hasNext();)
		{
			User user = (User) iUsers.next();

			int option = getOption(user, notification, event);

			if (isImmediateDeliveryOption(option, notification))
			{
				rv.add(user);
			}
			else if (option == NotificationService.PREF_NONE)
			{
			  Preferences prefs = (PreferencesEdit) PreferencesService.getPreferences(user.getId());
			  
			  ResourceProperties props = prefs.getProperties(NotificationService.PREFS_TYPE + "org.sakaiproject.api.app.syllabus.SyllabusService");
			  
			  String value = props.getProperty(new Integer(NotificationService.NOTI_OPTIONAL).toString());
			  if (value != null)
			  {
			    if(value.equals("2"))
			    {
			      rv.add(user);
			    }
			  }
			  else 
			  {
			    //default
			    rv.add(user);
			  }
			}
		}

		return rv;

	}

	protected boolean isImmediateDeliveryOption(int option, Notification notification)
	{
		if (option == NotificationService.PREF_IMMEDIATE)
		{
			return true;
		}
		
		return false;
	}

	protected int getOption(User user, Notification notification, Event event)
	{
	  if(event.getPriority() == NotificationService.NOTI_REQUIRED)
	  {
	    return NotificationService.PREF_IMMEDIATE;
	  }
	  else if(event.getPriority() == NotificationService.NOTI_OPTIONAL)
	  {
	    return NotificationService.PREF_NONE;
	  }
	  else if(event.getPriority() == NotificationService.NOTI_NONE)
	  {
	    return NotificationService.PREF_IGNORE;
	  }
	  else
	  {
	    return NotificationService.PREF_IGNORE;
	  }
	}
}


