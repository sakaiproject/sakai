/*********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-comp-shared/src/java/org/sakaiproject/component/app/syllabus/SiteEmailNotification.java,v 1.4 2005/07/01 09:11:28 cwen.iupui.edu Exp $
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

import java.util.List;
import java.util.Vector;

import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.legacy.alias.Alias;
import org.sakaiproject.service.legacy.alias.cover.AliasService;
import org.sakaiproject.service.legacy.email.cover.MailArchiveService;
import org.sakaiproject.service.legacy.event.Event;
import org.sakaiproject.service.legacy.notification.NotificationAction;
import org.sakaiproject.service.legacy.resource.Reference;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;

public class SiteEmailNotification
	extends EmailNotification
{
  private org.sakaiproject.api.kernel.component.ComponentManager cm;
  private static String SYLLABUS_MANAGER = "org.sakaiproject.api.app.syllabus.SyllabusManager";
  private SyllabusManager syllabusManager; 
	
	public SiteEmailNotification() 
	{
    cm = ComponentManager.getInstance();
    syllabusManager = (org.sakaiproject.api.app.syllabus.SyllabusManager) cm.get(SYLLABUS_MANAGER);
	}

	public SiteEmailNotification(String siteId)
	{
		super(siteId);

	}

	protected String getResourceAbility()
	{
		return null;
	}

	public NotificationAction getClone()
	{
		SiteEmailNotification clone = new SiteEmailNotification();
		clone.set(this);

		return clone;

	}

	protected List getRecipients(Event event)
	{
		Reference ref = new Reference(event.getResource());

		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		if(siteId == null)
		{
		  int lastIndex = ref.getReference().lastIndexOf("/");
		  String dataId = ref.getReference().substring(lastIndex+1);
		  SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		  SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
		  siteId = syllabusItem.getContextId();
		}

		try
		{
			Site site = SiteService.getSite(siteId);
			String ability = SiteService.SITE_VISIT;
			if (!site.isPublished())
			{
				ability = SiteService.SITE_VISIT_UNPUBLISHED;
			}

			List users = SecurityService.unlockUsers(ability, SiteService.siteReference(siteId));

			if (getResourceAbility() != null)
			{
				List users2 = SecurityService.unlockUsers(getResourceAbility(), ref.getReference());

				users.retainAll(users2);
			}

			return users;
		}
		catch (Exception any)
		{
			return new Vector();
		}

	}

	protected String getTo(Event event)
	{
		Reference ref = new Reference(event.getResource());

		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		
		if(siteId == null)
		{
		  int lastIndex = ref.getReference().lastIndexOf("/");
		  String dataId = ref.getReference().substring(lastIndex+1);
		  SyllabusData syllabusData = syllabusManager.getSyllabusData(dataId);
		  SyllabusItem syllabusItem = syllabusData.getSyllabusItem();
		  siteId = syllabusItem.getContextId();
		}

		String siteMailId = siteId;

		String channelRef = MailArchiveService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		List aliases = AliasService.getAliases(channelRef, 1, 1);
		if (aliases.isEmpty())
		{
			String siteRef = SiteService.siteReference(siteId);
			aliases = AliasService.getAliases(siteRef, 1, 1);
		}
		
		// if there was an alias
		if (!aliases.isEmpty())
		{
			siteMailId = ((Alias) aliases.get(0)).getId();
		}

		return siteMailId + " <" + siteMailId + "@" + ServerConfigurationService.getServerName() + ">";

	}

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-comp-shared/src/java/org/sakaiproject/component/app/syllabus/SiteEmailNotification.java,v 1.4 2005/07/01 09:11:28 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/

