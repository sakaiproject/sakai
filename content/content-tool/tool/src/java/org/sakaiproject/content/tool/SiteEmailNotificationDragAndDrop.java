/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.tool;

import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;

@Slf4j
public class SiteEmailNotificationDragAndDrop extends SiteEmailNotification
{
	/* property bundles */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.SiteemaconProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.siteemacon.siteemacon";
	private static final String RESOURCECLASS = "resource.class.siteemacon";
	private static final String RESOURCEBUNDLE = "resource.bundle.siteemacon";
	private String resourceClass;
	private String resourceBundle;
	private ResourceLoader rb;

	private ContentHostingService contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private EntityManager entityManager;
	private UserDirectoryService userDirectoryService;

	private static FormattedText formattedText;
	private static Object LOCK = new Object();

	private ArrayList<String> fileList;
	private boolean dropboxFolder=false;

	/**
	 * Construct.
	 */
	public SiteEmailNotificationDragAndDrop()
	{
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationDragAndDrop(String siteId)
	{
		super(siteId);
		this.securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		this.contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		this.siteService = (SiteService) ComponentManager.get("org.sakaiproject.site.api.SiteService");
		this.serverConfigurationService = (ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");
		this.entityManager = (EntityManager) ComponentManager.get("org.sakaiproject.entity.api.EntityManager");
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		loadResources(serverConfigurationService);
	}

	private void loadResources(ServerConfigurationService serverConfigurationService) {
		resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
		rb = new Resource().getLoader(resourceClass, resourceBundle);
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility()
	{
		return ContentHostingService.EVENT_RESOURCE_READ;
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
		String folderName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);

		String[] args = {title, folderName};
		if (this.isDropboxFolder())
		{
			return rb.getFormattedMessage((contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function) ? "db.subj.new.dnd" : "db.subj.upd-dnd"), args);
		}
		else return "[ " + title + " - "
		+ ((contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function))||(contentHostingService.EVENT_RESOURCE_ADD.equals(function)) ? rb.getString("newDnD") : rb.getString("chan")) + " "
		+ rb.getString("reso") + " ] " + rb.getString("folder") + " " + folderName;
	}

	protected String getFromAddress(Event event) {
		String userEmail = serverConfigurationService.getString("setup.request","no-reply@" + serverConfigurationService.getServerName());
		String userDisplay = serverConfigurationService.getString("ui.service", "Sakai");
		String no_reply = "From: \"" + userDisplay + "\" <" + userEmail + ">";
		String from = getFrom(event);

		return from;
	}

	protected List getHeaders(Event event) {
		List rv = super.getHeaders(event);
		rv.add("Subject: " + getSubject(event));
		rv.add(getFromAddress(event));
		rv.add(getTo(event));
		return rv;
	}

	public void setFileList(ArrayList<String> fileList)
	{
		this.fileList=fileList;
	}

	public ArrayList<String> getFileList()
	{
		return this.fileList;
	}

	public void setDropboxFolder(boolean dropboxFolder)
	{
		this.dropboxFolder=dropboxFolder;
	}

	public boolean isDropboxFolder()
	{
		return this.dropboxFolder;
	}

	/**
	 * Get the list of User objects who are eligible to receive the notification email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the list of User objects who are eligible to receive the notification email.
	 */
	protected List getRecipients(Event event) 
	{
		//For resources tool, superclass method is fine.
		if (this.isDropboxFolder())
		{
			List recipients = new ArrayList();

			String resourceRef = event.getResource();
			Reference ref = entityManager.newReference(resourceRef);
			String siteId = (getSite() != null) ? getSite() : ref.getContext();

			Site site;
			// get a site 
			try {
				site = siteService.getSite(siteId);
			}
			catch (IdUnusedException e) {
				log.warn("Could not getSite for {} not returning any recipients.", siteId);
				return recipients;
			}

			ResourceProperties props = ref.getProperties();

			//I get the current user to compare with dropboxOwnerId, because I can't get the last user who modified the uploaded files.
			String modifiedBy=userDirectoryService.getCurrentUser().getId();

			String parts[] = resourceRef.split("/");
			if(parts.length >= 4)
			{
				String dropboxOwnerId = parts[4];
				if(modifiedBy != null && modifiedBy.equals(dropboxOwnerId))
				{
					// notify instructor(s)
					StringBuilder buf = new StringBuilder();
					buf.append("/content/group-user/"); 
					buf.append(parts[3]); 
					buf.append("/"); 
					String siteDropbox = buf.toString();

					recipients.addAll(securityService.unlockUsers(contentHostingService.AUTH_DROPBOX_MAINTAIN, siteDropbox));
					
					//SAK-11647 - Adding to notifications all users with AUTH_DROPBOX_GROUPS who belong to current user's groups.
					List<User> dropboxGroupsRecipients = new ArrayList<User>();
					dropboxGroupsRecipients.addAll(securityService.unlockUsers(contentHostingService.AUTH_DROPBOX_GROUPS, siteDropbox));
					recipients.addAll(filterUsersInGroups(dropboxGroupsRecipients, modifiedBy, site));
					
					refineToSiteMembers(recipients, site);
				}
				else
				{
					// notify student
					try
					{
						User user = userDirectoryService.getUser(dropboxOwnerId);
						recipients.add(user);
					}
					catch(UserNotDefinedException e0)
					{
						try
						{
							User user = userDirectoryService.getUserByEid(dropboxOwnerId);
							recipients.add(user);
						}
						catch(UserNotDefinedException e1)
						{
							log.warn("UserNotDefinedException trying to get user: {}", dropboxOwnerId);
						}
					}

				}
			}

			return recipients;
		}
		else
		{
			return super.getRecipients(event);
		}
	}
	
	private List<User> filterUsersInGroups(List<User> usersToFilter, String currentUser, Site site)
	{
		List<User> usersInCurrentUserGroups = new ArrayList<User>();
		List<Group> site_groups = new ArrayList<Group>();
		List<String> allGroupsUsers = new ArrayList<String>();
		
		site_groups.addAll(site.getGroupsWithMember(currentUser));
		if (site_groups.size()>0)
		{
			for (Group g : site_groups)
			{
				allGroupsUsers.addAll(g.getUsers());
			}
		}
		for (User user : usersToFilter)
		{
			if (allGroupsUsers.contains(user.getId())) usersInCurrentUserGroups.add(user);
		}
		return usersInCurrentUserGroups;
	}

	/**
	 * Only include actual site members in the notification.
	 * 
	 * @param users
	 *        List of users that emails would be sent to.
	 * @param site
	 *        Site that the emails would be sent to
	 * @return 
	 *        Refined list of users who are members of this site.
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
	 * @param members   The Set of members from which to extract the userIds
	 * @return
	 *      The set of user ids that belong to the users in the member set.
	 */
	private Set<String> getUserIds(Collection<Member> members) {
		Set<String> userIds = new HashSet<String>();

		for (Member member : members)
			userIds.add(member.getUserId());

		return userIds;
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
		if (!this.isDropboxFolder())
		{
			// include any users who have AnnouncementService.SECURE_ALL_GROUPS and getResourceAbility() in the context
			String contextRef = siteService.siteReference(ref.getContext());

			// get the list of users who have SECURE_ALL_GROUPS
			List<User> allGroupUsers = securityService.unlockUsers(contentHostingService.AUTH_RESOURCE_ALL_GROUPS, contextRef);

			// filter down by the permission
			if (getResourceAbility() != null)
			{
				boolean hidden = false;
				//It is always a collection with Drag & Drop.
				//if (!contentHostingService.isCollection(ref.getId())) {
				if (contentHostingService.isCollection(ref.getId())) {
					try {
						ContentCollection folder = contentHostingService.getCollection(ref.getId());
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


	protected static FormattedText getFormattedText() {
		if (formattedText == null) {
			synchronized (LOCK) {
				FormattedText component = (FormattedText) ComponentManager.get(FormattedText.class);
				if (component == null) {
					throw new IllegalStateException("Unable to find the FormattedText using the ComponentManager");
				} else {
					formattedText = component;
				}
			}
		}
		return formattedText;
	}

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
		if (this.isDropboxFolder()) addMessageTextDropbox(buf, title, ref, function, shouldProduceHtml);
		else addMessageTextResources(buf, title, ref, function, shouldProduceHtml);
		return buf.toString();
	}

	/**
	 * @inheritDoc
	 */
	private void addMessageTextResources(StringBuilder buf, String title, Reference ref, String function, boolean doHtml)
	{
		ResourceProperties props;
		String url="";
		String resourceName="";
		String blankLine = "\n\n";
		String newLine = "\n";
		Reference ref2;

		if ( doHtml ) 
		{
			title = getFormattedText().escapeHtmlFormattedTextarea(title);
			blankLine = "\n</p><p>\n";
			newLine = "<br/>\n";
		}

		// Now build up the message text.
		if (doHtml) {
			buf.append("<p>");
		}

		if ((contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function))||(contentHostingService.EVENT_RESOURCE_ADD.equals(function)))
		{
			buf.append(rb.getString("anewresDnD"));
		}
		else
		{
			buf.append(rb.getString("anewres2DnD"));
		}
		buf.append(" ");
		buf.append(rb.getString("tothe"));
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

		for (int i=0;i<this.fileList.size();i++)
		{
			ref2 = entityManager.newReference((String)this.getFileList().get(i));
			url = ref2.getUrl();
			props = ref2.getProperties();
			resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);

			// add location
			String path = constructPath(ref2.getReference());
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

			buf.append(blankLine);

			// add a reference to the resource for non-HTML
			if ( ! doHtml )
			{
				buf.append("\n" + rb.getString("resour") + " " + resourceName);
				buf.append(" " + url);
				buf.append("\n\n");  // End on a blank line
			}
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

	private void addMessageTextDropbox(StringBuilder buf, String siteTitle, Reference ref, String function, boolean doHtml)
	{
		Reference ref2;
		ResourceProperties props = ref.getProperties();
		String resourceName=""; 
		String url="";
		String blankLine = "\n\n";
		String newLine = "\n";

		String dropboxId = contentHostingService.getIndividualDropboxId(ref.getId());
		String dropboxTitle = null;
		try 
		{
			ResourceProperties dbProps = contentHostingService.getProperties(dropboxId);
			dropboxTitle = dbProps.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		} 
		catch (PermissionException e) 
		{
			log.warn("PermissionException trying to get title for individual dropbox: {}", dropboxId);
		} 
		catch (IdUnusedException e) 
		{
			log.warn("IdUnusedException trying to get title for individual dropbox: {}", dropboxId);
		}

		if ( doHtml ) 
		{
			siteTitle = getFormattedText().escapeHtmlFormattedTextarea(siteTitle);
			resourceName = getFormattedText().escapeHtmlFormattedTextarea(resourceName);
			dropboxTitle = getFormattedText().escapeHtmlFormattedTextarea(dropboxTitle);
			blankLine = "\n</p><p>\n";
			newLine = "<br/>\n";
		}

		// Now build up the message text.
		if (doHtml) 
		{
			buf.append("<p>");
		}
		String portalName = serverConfigurationService.getString("ui.service", "Sakai");
		String portalUrl = serverConfigurationService.getPortalUrl();
		if(doHtml)
		{
			portalUrl = "<a href=\"" + portalUrl + "\">" + portalName + "</a>";
		}
		if (contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function))
		{
			buf.append(rb.getFormattedMessage("db.text.new.dnd", new String[]{dropboxTitle, siteTitle, portalName, portalUrl}));
		}
		else
		{
			buf.append(rb.getFormattedMessage("db.text.upd.dnd", new String[]{dropboxTitle, siteTitle, portalName, portalUrl}));
		}
		buf.append(blankLine);

		//FOR
		for (int i=0;i<this.fileList.size();i++)
		{
			ref2 = entityManager.newReference((String)this.getFileList().get(i));
			url = ref2.getUrl();
			props = ref2.getProperties();
			resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);

			// add location
			String path = constructPath(ref2.getReference());
			String item = resourceName;

			if(doHtml)
			{
				item = "<a href=\"" + url + "\">" + item + "</a>";
			}
			buf.append(rb.getFormattedMessage("db.text.location", new String[]{siteTitle, path, item}));

			buf.append(blankLine);

			// add a reference to the resource for non-HTML
			if ( ! doHtml )
			{
				buf.append("\n" + rb.getString("resour") + " " + resourceName);
				buf.append(" " + url);
				buf.append("\n\n");  // End on a blank line
			}
		}
		//END FOR

		// Add the tag
		if (doHtml) 
		{
			buf.append("<hr/>");
		}
		else
		{
			buf.append(rb.getString("separator"));
		}
		buf.append(newLine);

		buf.append(rb.getFormattedMessage("db.text.prefs", new String[]{portalName, portalUrl, siteTitle}));

		if (doHtml) {
			buf.append("</p>");
		}
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
}
