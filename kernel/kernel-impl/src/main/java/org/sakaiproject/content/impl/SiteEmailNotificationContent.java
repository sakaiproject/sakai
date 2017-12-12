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

package org.sakaiproject.content.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * SiteEmailNotificationContent fills the notification message and headers with details from the content change that triggered the notification event.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
@Slf4j
public class SiteEmailNotificationContent extends SiteEmailNotification
{
	
	/* property bundles */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.SiteemaconProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.siteemacon.siteemacon";
	private static final String RESOURCECLASS = "resource.class.siteemacon";
	private static final String RESOURCEBUNDLE = "resource.bundle.siteemacon";
	private String resourceClass;
	private String resourceBundle;
	private ResourceLoader rb;
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private ContentHostingService contentHostingService;
	private EntityManager entityManager;
	private SiteService siteService;
	protected String plainTextContent(Event event) {
		return generateContentForType(false, event);
	}
	
	protected String htmlContent(Event event) {
		return generateContentForType(true, event);
	}

	private String generateContentForType(boolean shouldProduceHtml, Event event) {
		// get the content & properties
		Reference ref = entityManager.newReference(event.getResource());
		// TODO:  ResourceProperties props = ref.getProperties();

		// get the function
		String function = event.getEvent();
		String subject = getSubject(event);

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = siteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}
		
		StringBuilder buf = new StringBuilder();
		addMessageText(buf, ref, subject, title, function, shouldProduceHtml);
		return buf.toString();
	}

	public SiteEmailNotificationContent(SecurityService securityService, ServerConfigurationService serverConfigurationService,
			ContentHostingService contentHostingService, EntityManager entityManager, SiteService siteService) {
		this.securityService = securityService;
		this.serverConfigurationService = serverConfigurationService;
		this.contentHostingService = contentHostingService;
		this.entityManager = entityManager;
		this.siteService = siteService;
		loadResources(serverConfigurationService);
	}

	private void loadResources(ServerConfigurationService serverConfigurationService) {
		resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
		rb = new Resource().getLoader(resourceClass, resourceBundle);
	}

	/**
	 * The preferred form for construction is to supply the needed items rather than having to do a lookup. This constructor was
	 * left in place for compatibility with any custom tool that might currently be using it, but should be considered deprecated.
	 * 
	 * @deprecated
	 */
	public SiteEmailNotificationContent(String siteId)
	{
		super(siteId);
		this.securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		this.contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		this.entityManager = (EntityManager) ComponentManager.get("org.sakaiproject.entity.api.EntityManager");
		this.siteService = (SiteService) ComponentManager.get("org.sakaiproject.site.api.SiteService");
		this.serverConfigurationService = (ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");
		loadResources(serverConfigurationService);
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility()
	{
		return contentHostingService.EVENT_RESOURCE_READ;
	}
	
	protected EmailNotification makeEmailNotification() {
		return new SiteEmailNotificationContent(securityService, serverConfigurationService, contentHostingService, entityManager,
				siteService);
	}

	/**
	 * @inheritDoc
	 */
	private void addMessageText(StringBuilder buf, Reference ref, String subject, String title, String function, boolean doHtml)
	{
		ResourceProperties props = ref.getProperties();
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		String description = props.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION);
		String url = ref.getUrl();
		String blankLine = "\n\n";
		String newLine = "\n";

		if ( doHtml ) 
		{
			title = FormattedText.escapeHtmlFormattedTextarea(title);
			//subject = FormattedText.escapeHtmlFormattedTextarea(subject);
			resourceName = FormattedText.escapeHtmlFormattedTextarea(resourceName);
			description = FormattedText.escapeHtmlFormattedTextarea(description);
			blankLine = "\n</p><p>\n";
			newLine = "<br/>\n";
		}

		// get the resource copyright alert property
		boolean copyrightAlert = props.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT) != null ? true : false;

		// Now build up the message text.
		if (doHtml) {
			buf.append("<p>");
		}
		if (contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function))
		{
			buf.append(rb.getString("anewres"));
		}
		else
		{
			buf.append(rb.getString("anewres2"));
		}
		buf.append(" ");
		buf.append(rb.getString("the"));
		buf.append(" \"");
		buf.append(title);
		buf.append("\" ");
		buf.append(rb.getString("sitat"));
		buf.append(" ");
		if ( doHtml )
		{
			buf.append("<a href=\"");
			buf.append(serverConfigurationService.getPortalUrl());
			buf.append("\">");
			buf.append(serverConfigurationService.getString("ui.service", "Sakai"));
			buf.append("</a>");
		}
		else
		{
			buf.append(serverConfigurationService.getString("ui.service", "Sakai"));
			buf.append(" (");
			buf.append(serverConfigurationService.getPortalUrl());
			buf.append(")");
		}
		buf.append(blankLine);

		// add location
		String path = constructPath(ref.getReference());
		buf.append(rb.getString("locsit") + " \"" + title + "\" > " + rb.getString("reso") + " " + path + " > ");
		if ( doHtml ) 
		{
			buf.append("<a href=\"");
			buf.append(url);
			buf.append("\">");
			buf.append(resourceName);
			buf.append("</a>");
		}
		else
		{
			buf.append(resourceName);
		}

		if (copyrightAlert)
		{
			buf.append(" (c)");
		}
		buf.append(blankLine);

		// resource description
		if ((description != null) && (description.length() > 0))
		{
			buf.append(rb.getString("descrip") + " " + description);
			buf.append(blankLine);
		}

		// add a reference to the resource for non-HTML
		if ( ! doHtml )
		{
			buf.append("\n" + rb.getString("resour") + " " + resourceName);
			if (copyrightAlert)
			{
				buf.append(" (c)");
			}
			buf.append(" " + url);
			buf.append("\n\n");  // End on a blank line
		}

		// Add the tag
		if (doHtml) {
			buf.append("<hr/>" + newLine + rb.getString("this") + " "
	                                + serverConfigurationService.getString("ui.service", "Sakai") + " (<a href=\"" + serverConfigurationService.getPortalUrl()
	                                + "\" >" + serverConfigurationService.getPortalUrl() + "</a>) " + rb.getString("forthe") + " " + title + " "
	                                + rb.getString("site") + newLine + rb.getString("youcan"));
		} else {
			buf.append(rb.getString("separator") + newLine + rb.getString("this") + " "
                    + serverConfigurationService.getString("ui.service", "Sakai") + " (" + serverConfigurationService.getPortalUrl()
                    + ") " + rb.getString("forthe") + " " + title + " " + rb.getString("site") + newLine + rb.getString("youcan"));
		}
		
		if (doHtml) {
			buf.append("</p>");
		}
		
	}

	/**
	 * @inheritDoc
	 */
	protected List<String> getHeaders(Event event)
	{
		List<String> rv = super.getHeaders(event);

		// the Subject
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
		// tbd: move here from generateContentForType 
		return "";
	}

	/**
	 * Form a "Bread Crumb" style path showing the folders in which this referenced resource lives.
	 * 
	 * @param ref
	 *        The reference string to the resource.
	 * @return The path string for this resource.
	 */
	protected String constructPath(String ref)
	{
		StringBuilder buf = new StringBuilder();

		// expect the ref to be /content/group/site/folder/folder2/folderEtc/file.ext
		String[] parts = StringUtil.split(ref, Entity.SEPARATOR);

		// 0 is null, 1 is "content", 2 is "group" or whatever, 3 is the site, the last is the file name
		if (parts.length > 4)
		{
			// grow this collection id as we descend into the collections
			StringBuilder root = new StringBuilder(Entity.SEPARATOR + parts[2] + Entity.SEPARATOR + parts[3] + Entity.SEPARATOR);

			// take all the collection parts
			for (int i = 4; i < parts.length - 1; i++)
			{
				buf.append(" > ");
				String collectionId = parts[i];
				root.append(collectionId + Entity.SEPARATOR);
				try
				{
					// get the display name
					ContentCollection collection = contentHostingService.getCollection(root.toString());
					buf.append(collection.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
				}
				catch (Exception any)
				{
					// use the id if there's a problem
					buf.append(collectionId);
				}
			}
		}

		return buf.toString();
	}

	/**
	 * Get the subject for the email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the subject for the email.
	 */
	protected String getSubject(Event event)
	{
		Reference ref = entityManager.newReference(event.getResource());
		ResourceProperties props = ref.getProperties();

		// get the function
		String function = event.getEvent();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = siteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		return "[ " + title + " - "
				+ (contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function) ? rb.getString("new") : rb.getString("chan")) + " "
				+ rb.getString("reso2") + " ] " + resourceName;
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
		// include any users who have AnnouncementService.SECURE_ALL_GROUPS and getResourceAbility() in the context
		String contextRef = siteService.siteReference(ref.getContext());

		// get the list of users who have SECURE_ALL_GROUPS
		List<User> allGroupUsers = securityService.unlockUsers(contentHostingService.AUTH_RESOURCE_ALL_GROUPS, contextRef);

		// filter down by the permission
		if (getResourceAbility() != null)
		{
			boolean hidden = false;
			if (!contentHostingService.isCollection(ref.getId())) {
				try {
					ContentResource resource = contentHostingService.getResource(ref.getId());
					hidden = resource.isHidden();
					
				    //we need to check the containing folder too
					ContentCollection folder = resource.getContainingCollection();
					if (folder.isHidden()) {
						hidden = folder.isHidden();
						
					}
					
				} catch (PermissionException e) {
					log.error(e.getMessage(), e);
				} catch (IdUnusedException e) {
					log.error(e.getMessage(), e);
				} catch (TypeException e) {
					log.error(e.getMessage(), e);
				}
			}
			
			List<User> allGroupUsers2 = null;
			if (!hidden) {
				//resource is visible get all users
				allGroupUsers2 = securityService.unlockUsers(getResourceAbility(), contextRef);
				
			} else {
				allGroupUsers2 = securityService.unlockUsers(contentHostingService.AUTH_RESOURCE_HIDDEN, contextRef);
				//we need to remove all users from the list as that is too open in this case
				users.clear();
				
			}
			
			allGroupUsers.retainAll(allGroupUsers2);
		}

		// remove any in the list already
		allGroupUsers.removeAll(users);

		// combine
		users.addAll(allGroupUsers);
	}
}
