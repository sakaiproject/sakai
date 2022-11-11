/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * SiteEmailNotification is an EmailNotification that selects the site's participants (based on site access) as the recipients of the notification.
 * </p>
 * <p>
 * getRecipients() is satified here (although it can be customized by the extensions to this class).
 * </p>
 * <p>
 * The following should be specified to extend the class:
 * <ul>
 * <li>getResourceAbility() - to require an additional permission to qualify as a recipient (other than site membership)</li>
 * <li>addSpecialRecipients() - to add other recipients</li>
 * </ul>
 * </p>
 */
public class SiteEmailNotification extends EmailNotification
{
	private AliasService aliasService;

	/**
	 * Construct.
	 */
	public SiteEmailNotification()
	{
		this(ComponentManager.get(AliasService.class));
	}

	public SiteEmailNotification(AliasService aliasService)
	{
		this.aliasService = aliasService;
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
		EmailNotification clone = makeEmailNotification();
		clone.set(this);

		return clone;
	}

	/**
	 * @inheritDoc
	 */
	protected List<User> getRecipients(Event event)
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

			// get the list of users who can do the right kind of visit
			List<User> users = SecurityService.unlockUsers(ability, ref.getReference());

			// get the list of users who have the appropriate access to the resource
			if (getResourceAbility() != null)
			{
				List<User> users2 = SecurityService.unlockUsers(getResourceAbility(), ref.getReference());

				// find intersection of users and user2
				users.retainAll(users2);
			}

			// add any other users
			addSpecialRecipients(users, ref);

			//only use direct site members for the base list of users
			refineToSiteMembers(users, site);

			return users;
		}
		catch (Exception any)
		{
			return new Vector<User>();
		}
	}

	/**
	 * Add to the user list any other users who should be notified about this ref's change.
	 * 
	 * @param users
	 *        The user list, already populated based on site visit and resource ability.
	 * @param ref
	 *        The entity reference.
	 */
	protected void addSpecialRecipients(List<User> users, Reference ref)
	{
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

	/**
	 * Format a to address, sensitive to the notification service's replyable configuration.
	 * 
	 * @param event
	 * @return
	 */
	protected String getTo(Event event)
	{
		if (NotificationService.isNotificationToReplyable())
		{
			// to site title <email>
			return "To: " + getToSite(event);
		}
		else
		{
			// to the site, but with no reply
			return "To: " + getToSiteNoReply(event);
		}
	}

	/**
	 * Format a to address, to the related site, but with no reply.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return a to address, to the related site, but with no reply.
	 */
	protected String getToSiteNoReply(Event event)
	{
		Reference ref = EntityManager.newReference(event.getResource());

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

		return "\"" + title + "\" <"+ ServerConfigurationService.getString("setup.request","no-reply@" + ServerConfigurationService.getServerName()) + ">";
	}

	/**
	 * Format the to site email address.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the email address attribution for the site.
	 */
	protected String getToSite(Event event)
	{
		Reference ref = EntityManager.newReference(event.getResource());

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		String email = null;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();

			// check that the channel exists
			String channel = "/mailarchive/channel/" + siteId + "/main";
			EntityManager.newReference(channel);
	
			// find the alias for this site's mail channel
			List<Alias> all = aliasService.getAliases(channel);
			if (!all.isEmpty()) email = ((Alias) all.get(0)).getId();
		}
		catch (Exception ignore)
		{
		}

		// if for any reason we did not find an email, setup for the no-reply for email
		if (email == null) email = "no-reply";

		String rv = "\"" + title + "\" <" + email + "@" + ServerConfigurationService.getServerName() + ">";

		return rv;
	}

	/**
	 * Refine the recipients list to only users that are actually members
	 * of the given site.
	 *
	 * @param users
	 * 		The list of users to refine
	 * @param site
	 * 		The site whose membership the users will be refined to
	 */
	protected void refineToSiteMembers(List<User> users, Site site) {
		Set<Member> members = site.getMembers();
		Set<String> memberUserIds = getUserIds(members);

		for (Iterator<User> i = users.listIterator(); i.hasNext();) {
			User user = i.next();

			if (!memberUserIds.contains(user.getId())) {
				i.remove();
			}
		}
	}

	/**
	 * Extract a 'Set' of user ids from the given set of members.
	 *
	 * @param members	The Set of members from which to extract the userIds
	 * @return
	 * 		The set of user ids that belong to the users in the member set.
	 */
	private Set<String> getUserIds(Collection<Member> members) {
		Set<String> userIds = new HashSet<String>();

		for (Member member : members)
			userIds.add(member.getUserId());

		return userIds;
	}
}
