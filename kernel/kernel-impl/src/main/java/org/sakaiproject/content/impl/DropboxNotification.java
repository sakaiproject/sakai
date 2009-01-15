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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

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
public class DropboxNotification extends EmailNotification 
{
	static final Log logger = LogFactory.getLog(DropboxNotification.class);
	
	/* property bundles */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.SiteemaconProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.siteemacon.siteemacon";
	private static final String RESOURCECLASS = "resource.class.siteemacon";
	private static final String RESOURCEBUNDLE = "resource.bundle.siteemacon";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);
	// private static ResourceBundle rb = ResourceBundle.getBundle("siteemacon");
	
	private final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
	private final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
	private final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";

	private final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
	
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
		//rv.add(getTo(event));

		return rv;
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
		Reference ref = EntityManager.newReference(resourceRef);
		
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

				recipients.addAll( SecurityService.unlockUsers(ContentHostingService.AUTH_DROPBOX_MAINTAIN, siteDropbox) ); 
			}
			else
			{
				// notify student
				try
				{
					User user = UserDirectoryService.getUser(dropboxOwnerId);
					recipients.add(user);
				}
				catch(UserNotDefinedException e0)
				{
					try
					{
						User user = UserDirectoryService.getUserByEid(dropboxOwnerId);
						recipients.add(user);
					}
					catch(UserNotDefinedException e1)
					{
						logger.warn("UserNotDefinedException trying to get user: " + dropboxOwnerId);
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
		Reference ref = EntityManager.newReference(event.getResource());
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
			Site site = SiteService.getSite(siteId);
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

		String dropboxId = ContentHostingService.getIndividualDropboxId(ref.getId());
		String dropboxTitle = null;
		try 
		{
			ResourceProperties dbProps = ContentHostingService.getProperties(dropboxId);
			dropboxTitle = dbProps.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		} 
		catch (PermissionException e) 
		{
			logger.warn("PermissionException trying to get title for individual dropbox: " + dropboxId);
		} 
		catch (IdUnusedException e) 
		{
			logger.warn("IdUnusedException trying to get title for individual dropbox: " + dropboxId);
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
		String portalName = ServerConfigurationService.getString("ui.service", "Sakai");
		String portalUrl = ServerConfigurationService.getPortalUrl();
		if(doHtml)
		{
			portalUrl = "<a href=\"" + portalUrl + "\">" + portalName + "</a>";
		}
		if (ContentHostingService.EVENT_RESOURCE_ADD.equals(function))
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
		Reference ref = EntityManager.newReference(event.getResource());
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
			Site site = SiteService.getSite(siteId);
			siteTitle = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		
		String dropboxId = ContentHostingService.getIndividualDropboxId(ref.getId());
		String dropboxTitle = null;
		try 
		{
			ResourceProperties dbProps = ContentHostingService.getProperties(dropboxId);
			dropboxTitle = dbProps.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		} 
		catch (PermissionException e) 
		{
			logger.warn("PermissionException trying to get title for individual dropbox: " + dropboxId);
		} 
		catch (IdUnusedException e) 
		{
			logger.warn("IdUnusedException trying to get title for individual dropbox: " + dropboxId);
		}
		
		String[] args = {siteTitle, dropboxTitle, resourceName};
		
		return rb.getFormattedMessage((ContentHostingService.EVENT_RESOURCE_ADD.equals(function) ? "db.subj.new" : "db.subj.upd"), args);
		
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

			// take all the collection parts
			for (int i = 4; i < parts.length - 1; i++)
			{
				buf.append(" > ");
				String collectionId = parts[i];
				root = root + collectionId + Entity.SEPARATOR;
				try
				{
					// get the display name
					ContentCollection collection = ContentHostingService.getCollection(root);
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
