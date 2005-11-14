/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/legacy/util/src/java/org/sakaiproject/util/notification/SiteEmailNotification.java $
 * $Id: SiteEmailNotification.java 3819 2005-11-14 00:24:35Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.legacy.alias.Alias;
import org.sakaiproject.service.legacy.alias.cover.AliasService;
import org.sakaiproject.service.legacy.email.cover.MailArchiveService;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.event.Event;
import org.sakaiproject.service.legacy.notification.NotificationAction;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;

/**
 * <p>Note: copied from legacy util SiteEmailNotification.java 3819</p>
 * <p>
 * SiteEmailNotification is an EmailNotification that selects the site's participants (based on site access) as the recipients of the notification.
 * </p>
 * <p>
 * getRecipients() is satisified here, but you can refine it by implementing getResourceAbility()
 * </p>
 * Although these are not abstract, the following still need be specified to extend the class:
 * <ul>
 * <li>getMessage()</li>
 * <li>getHeaders()</li>
 * <li>getTag()</li>
 * <li>isBodyHTML()</li>
 * <li>headerToRecipient</li>
 * </ul>
 * </p>
 * <p>
 * getClone() should also be extended to clone the proper type of object.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public class SiteEmailNotification extends EmailNotification
{
	/**
	 * Construct.
	 */
	public SiteEmailNotification()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param siteId
	 *        The id of the site whose users will get a mailing.
	 */
	public SiteEmailNotification(String siteId)
	{
		super(siteId);
	}

	/**
	 * @inheritDoc
	 */
	public NotificationAction getClone()
	{
		SiteEmailNotification clone = new SiteEmailNotification();
		clone.set(this);

		return clone;
	}

	/**
	 * @inheritDoc
	 */
	protected List getRecipients(Event event)
	{
		// get the resource reference
		Reference ref = EntityManager.newReference(event.getResource());

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// if the site is published, use the list of users who can SITE_VISIT the site,
		// else use the list of users who can SITE_VISIT_UNP the site.
		try
		{
			Site site = SiteService.getSite(siteId);
			String ability = SiteService.SITE_VISIT;
			if (!site.isPublished())
			{
				ability = SiteService.SITE_VISIT_UNPUBLISHED;
			}

			// get the list of users
			List users = SecurityService.unlockUsers(ability, SiteService.siteReference(siteId));

			// get the list of users who have the appropriate access to the resource
			if (getResourceAbility() != null)
			{
				List users2 = SecurityService.unlockUsers(getResourceAbility(), ref.getReference());

				// find intersection of users and user2
				users.retainAll(users2);
			}

			return users;
		}
		catch (Exception any)
		{
			return new Vector();
		}
	}

	/**
	 * Format a To: header value for the site.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the to: header for the email.
	 */
	protected String getSiteTo(Event event)
	{
		// get the resource reference
		Reference ref = EntityManager.newReference(event.getResource());

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// make the to: field look like the email address of the site

		// select the site's main mail channel alias, or the site's alias, or the site id
		String siteMailId = siteId;

		// first check aliases for the site's main email channel
		String channelRef = MailArchiveService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		List aliases = AliasService.getAliases(channelRef, 1, 1);
		if (aliases.isEmpty())
		{
			// next try aliases for the site
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

	/**
	 * Get the additional security function string needed for the resource that is the target of the notification <br />
	 * users who get notified need to have this ability with this resource, too.
	 * 
	 * @return The additional ability string needed for a user to receive notification.
	 */
	protected String getResourceAbility()
	{
		return null;
	}
}
