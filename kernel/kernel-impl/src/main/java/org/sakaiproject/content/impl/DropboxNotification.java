/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>
 * DropboxNotification is the notification action that handles the act of message (email) based notify related to changes in an individual dropbox.
 * </p>
 * <p>
 * The following should be specified to extend the class:
 * <ul>
 * <li>getRecipients() - get a collection of Users to send the notification to</li>
 * <li>getHeaders() - form the complete message headers (like from: to: reply-to: date: subject: etc). from: and to: are for display only</li>
 * <li>htmlContent() and plainTextContent() - form the complete message body (minus headers)</li>
 * <li>getTag() - the part of the body at the end that identifies the list</li>
 * </ul>
 * </p>
 * <p>
 * getClone() should also be extended to clone the proper type of object.
 * </p>
 */
@Slf4j
public class DropboxNotification extends EmailNotification 
{
	/* property bundles */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.SiteemaconProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.siteemacon.siteemacon";
	private static final String RESOURCECLASS = "resource.class.siteemacon";
	private static final String RESOURCEBUNDLE = "resource.bundle.siteemacon";
	private String resourceClass;
	private String resourceBundle;
	private ResourceLoader rb;
	// private static ResourceBundle rb = ResourceBundle.getBundle("siteemacon");
	
	private final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
	private final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
	private final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";

	private final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
	private SecurityService securityService;
	private ContentHostingService contentHostingService;
	private EntityManager entityManager;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;
	private ServerConfigurationService serverConfigurationService;
	/**
	 * The preferred form for construction is to supply the needed items rather than having to do a lookup. This constructor was
	 * left in place for compatibility with any custom tool that might currently be using it, but should be considered deprecated.
	 * 
	 * @deprecated
	 */
	public DropboxNotification() {
		this.securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		this.contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		this.entityManager = (EntityManager) ComponentManager.get("org.sakaiproject.entity.api.EntityManager");
		this.siteService = (SiteService) ComponentManager.get("org.sakaiproject.site.api.SiteService");
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		this.serverConfigurationService = (ServerConfigurationService) ComponentManager
				.get("org.sakaiproject.component.api.ServerConfigurationService");
	}

	private void loadResources() {
		resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
		rb = new Resource().getLoader(resourceClass, resourceBundle);
	}

	public DropboxNotification(SecurityService securityService, ContentHostingService contentHostingService, EntityManager entityManager,
			SiteService siteService, UserDirectoryService userDirectoryService, ServerConfigurationService serverConfigurationService) {
		this.securityService = securityService;
		this.contentHostingService = contentHostingService;
		this.entityManager = entityManager;
		this.siteService = siteService;
		this.userDirectoryService = userDirectoryService;
		this.serverConfigurationService = serverConfigurationService;
		loadResources();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getClone()
	 */
	@Override
	public NotificationAction getClone() 
	{
		// TODO Auto-generated method stub
		return super.getClone();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getHeaders(org.sakaiproject.event.api.Event)
	 */
	@Override
	protected List<String> getHeaders(Event event) 
	{
		List<String> rv = super.getHeaders(event);

		// the Subject
		rv.add("Subject: " + getSubject(event));

		// from
		rv.add(getFrom(event));

		// to
		List toList = getRecipients(event);
		Iterator itr = toList.iterator();
		StringBuilder recips = new StringBuilder();
		while (itr.hasNext())
		{
			User usr = (User) itr.next();
			recips.append(usr.getEmail() + ", ");
		}
		rv.add("To: " + recips.toString());

		return rv;
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
	 * Get the list of User objects who are eligible to receive the notification email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the list of User objects who are eligible to receive the notification email.
	 */
	protected List getRecipients(Event event) 
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
			log.warn("Could not getSite for " + siteId + " not returning any recipients.");
            return recipients;
        }
		
		ResourceProperties props = ref.getProperties();
		String modifiedBy = props.getProperty(ResourceProperties.PROP_MODIFIED_BY);
		
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
						log.warn("UserNotDefinedException trying to get user: " + dropboxOwnerId);
					}
				}
				
			}
		}
		
		return recipients;
	}

	protected String getSiteDropboxCollectionId(String id) 
	{
		StringBuilder buf = new StringBuilder();
		String parts[] = id.split("/");
		if(parts.length >= 3)
		{
			buf.append("/group-user/"); 
			buf.append(parts[2]); 
			buf.append("/"); 
		}
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.EmailNotification#getTag(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getTag(String title, boolean shouldUseHtml) 
	{
		// tbd: move from addMessageText
		return "";
	}

	protected String plainTextContent(Event event) 
	{
		return generateContentForType(false, event);
	}
	
	protected String htmlContent(Event event) 
	{
		return generateContentForType(true, event);
	}

	private String generateContentForType(boolean shouldProduceHtml, Event event) 
	{
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

	/**
	 * @inheritDoc
	 */
	private void addMessageText(StringBuilder buf, Reference ref, String subject, String siteTitle, String function, boolean doHtml)
	{
		ResourceProperties props = ref.getProperties();
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		String description = props.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION);
		String url = ref.getUrl();
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
			log.warn("PermissionException trying to get title for individual dropbox: " + dropboxId);
		} 
		catch (IdUnusedException e) 
		{
			log.warn("IdUnusedException trying to get title for individual dropbox: " + dropboxId);
		}

		if ( doHtml ) 
		{
			siteTitle = FormattedText.escapeHtmlFormattedTextarea(siteTitle);
			subject = FormattedText.escapeHtmlFormattedTextarea(subject);
			resourceName = FormattedText.escapeHtmlFormattedTextarea(resourceName);
			description = FormattedText.escapeHtmlFormattedTextarea(description);
			dropboxTitle = FormattedText.escapeHtmlFormattedTextarea(dropboxTitle);
			blankLine = "\n</p><p>\n";
			newLine = "<br/>\n";
		}

		// get the resource copyright alert property
		boolean copyrightAlert = props.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT) != null ? true : false;

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
			buf.append(rb.getFormattedMessage("db.text.new", new String[]{dropboxTitle, siteTitle, portalName, portalUrl}));
		}
		else
		{
			buf.append(rb.getFormattedMessage("db.text.upd", new String[]{dropboxTitle, siteTitle, portalName, portalUrl}));
		}
		buf.append(blankLine);

		// add location
		String path = constructPath(ref.getReference());
		String item = resourceName;
		if(copyrightAlert)
		{
			item += " (c)";
		}
		if(doHtml)
		{
			item = "<a href=\"" + url + "\">" + item + "</a>";
		}
		buf.append(rb.getFormattedMessage("db.text.location", new String[]{siteTitle, path, item}));

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
	 * Get the subject for the email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the subject for the email.
	 */
	protected String getSubject(Event event)
	{
		Reference ref = entityManager.newReference(event.getResource());
		Entity r = ref.getEntity();
		ResourceProperties props = ref.getProperties();

		// get the function
		String function = event.getEvent();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String siteTitle = siteId;
		try
		{
			Site site = siteService.getSite(siteId);
			siteTitle = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		
		String dropboxId = contentHostingService.getIndividualDropboxId(ref.getId());
		String dropboxTitle = null;
		try 
		{
			ResourceProperties dbProps = contentHostingService.getProperties(dropboxId);
			dropboxTitle = dbProps.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		} 
		catch (PermissionException e) 
		{
			log.warn("PermissionException trying to get title for individual dropbox: " + dropboxId);
		} 
		catch (IdUnusedException e) 
		{
			log.warn("IdUnusedException trying to get title for individual dropbox: " + dropboxId);
		}
		
		String[] args = {siteTitle, dropboxTitle, resourceName};
		
		return rb.getFormattedMessage((contentHostingService.EVENT_RESOURCE_AVAILABLE.equals(function) ? "db.subj.new" : "db.subj.upd"),
				args);
		
	}
	
	/**
	 * Form a "Bread Crumb" style path showing the folders in which this referenced resource lives, starting with the individual dropbox.
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

		// 0 is null, 1 is "content", 2 is "group-user", 3 is the site-id, 4 is the user-id, the last is the file name
		if (parts.length > 4)
		{
			// grow this collection id as we descend into the collections
			String root = Entity.SEPARATOR + parts[2] + Entity.SEPARATOR + parts[3] + Entity.SEPARATOR;
			StringBuilder rootBuilder = new StringBuilder();
			rootBuilder.append(root);
			// take all the collection parts
			for (int i = 4; i < parts.length - 1; i++)
			{
				buf.append(" > ");
				String collectionId = parts[i];
				rootBuilder.append(collectionId + Entity.SEPARATOR);
				try
				{
					// get the display name
					ContentCollection collection = contentHostingService.getCollection(rootBuilder.toString());
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
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
