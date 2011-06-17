/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 Sakai Foundation
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

import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * CollectionAccessFormatter is formatter for collection access.
 * </p>
 */
@SuppressWarnings("deprecation")
public class CollectionAccessFormatter
{
	private static final Log M_log = LogFactory.getLog(CollectionAccessFormatter.class);


	/**
	 * Format the collection as an HTML display.
	 */
	@SuppressWarnings({ "unchecked" })
	public static void format(ContentCollection x, Reference ref, HttpServletRequest req, HttpServletResponse res,
			ResourceLoader rb, String accessPointTrue, String accessPointFalse)
	{
		// do not allow directory listings for /attachments and its subfolders  
		if(ContentHostingService.isAttachmentResource(x.getId()))
		{
			try
			{
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			} 
			catch ( java.io.IOException e ) 
			{
				return;
			}
		}

		PrintWriter out = null;

		// don't set the writer until we verify that
		// getallresources is going to work.
		boolean printedHeader = false;
		boolean printedDiv = false;

		try
		{
			res.setContentType("text/html; charset=UTF-8");

			out = res.getWriter();

			ResourceProperties pl = x.getProperties();
			String webappRoot = ServerConfigurationService.getServerUrl();
			String skinRepo = ServerConfigurationService.getString("skin.repo", "/library/skin");
			String skinName = "default";
			String[] parts= StringUtils.split(x.getId(), Entity.SEPARATOR);
			
			// Is this a site folder (Resources or Dropbox)? If so, get the site skin
			
			if (x.getId().startsWith(org.sakaiproject.content.api.ContentHostingService.COLLECTION_SITE) ||
				x.getId().startsWith(org.sakaiproject.content.api.ContentHostingService.COLLECTION_DROPBOX)) {
				if (parts.length > 1) {
					String siteId = parts[1];
					try {
						Site site = SiteService.getSite(siteId);
						if (site.getSkin() != null) {
							skinName = site.getSkin();
						}
					} catch (IdUnusedException e) {
						// Cannot get site - ignore it
					}
				}
			}

			// Output the headers
			
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			out.println("<html><head>");
			out.println("<title>" + rb.getFormattedMessage("colformat.pagetitle", 
					new Object[]{ Validator.escapeHtml(pl.getProperty(ResourceProperties.PROP_DISPLAY_NAME))}) + "</title>");
			out.println("<link href=\"" + webappRoot + skinRepo+ "/" + skinName + 
			"/access.css\" type=\"text/css\" rel=\"stylesheet\" media=\"screen\">");
			out.println("<script src=\"" + webappRoot
					+ "/library/js/jquery.js\" type=\"text/javascript\">");
			out.println("</script>");
			out.println("</head><body class=\"specialLink\">");

			out.println("<script type=\"text/javascript\" src=\"/library/js/access.js\"></script>");
			out.println("<div class=\"directoryIndex\">");

			// for content listing it's best to use a real title
			out.println("<h3>" + Validator.escapeHtml(pl.getProperty(ResourceProperties.PROP_DISPLAY_NAME)) + "</h3>");
			out.println("<p id=\"toggle\"><a id=\"toggler\" href=\"#\">" + rb.getString("colformat.showhide") + "</a></p>");
			String folderdesc = pl.getProperty(ResourceProperties.PROP_DESCRIPTION);
			if (folderdesc != null && !folderdesc.equals("")) out.println("<div class=\"textPanel\">" + folderdesc + "</div>");

			out.println("<ul>");
			out.println("<li style=\"display:none\">");
			out.println("</li>");

			printedHeader = true;
			printedDiv = true;

			if (parts.length > 2)
			{
				// go up a level
				out.println("<li class=\"upfolder\"><a href=\"../\"><img src=\"/library/image/sakai/folder-up.gif\" alt=\"" + rb.getString("colformat.uplevel.alttext") + "\"/>" + rb.getString("colformat.uplevel") + "</a></li>");
			}

			// Sort the collection items

			List<ContentEntity> members = x.getMemberResources();

			boolean hasCustomSort = false;
			try {
				hasCustomSort = x.getProperties().getBooleanProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT);
			} catch (Exception e) {
				// use false that's already there
			}

			if (hasCustomSort)
				Collections.sort(members, new ContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true));
			else
				Collections.sort(members, new ContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true));

			// Iterate through content items

			URI baseUri = new URI(x.getUrl());
						
			for (ContentEntity content : members) {

				ResourceProperties properties = content.getProperties();
				boolean isCollection = content.isCollection();
				String xs = content.getId();
				String contentUrl = content.getUrl();

				// These both perform the same check in the implementation but we should observe the API.
				// This also checks to see if a resource is hidden or time limited.
				if ( isCollection) {
					if (!ContentHostingService.allowGetCollection(xs)) {
						continue;
					}
				} else {
					if (!ContentHostingService.allowGetResource(xs)) {
						continue;
					}
				}

				if (isCollection)
				{
					xs = xs.substring(0, xs.length() - 1);
					xs = xs.substring(xs.lastIndexOf('/') + 1) + '/';
				}
				else
				{
					xs = xs.substring(xs.lastIndexOf('/') + 1);
				}

				try
				{
					// Relativize the URL (canonical item URL relative to canonical collection URL). 
					// Inter alias this will preserve alternate access paths via aliases, e.g. /web/
					
					URI contentUri = new URI(contentUrl);			
					URI relativeUri = baseUri.relativize(contentUri);
					contentUrl = relativeUri.toString();
					
					if (isCollection)
					{
						// Folder

						String desc = properties.getProperty(ResourceProperties.PROP_DESCRIPTION);
						if ((desc == null)  || desc.equals(""))
							desc = "";
						else
							desc = "<div class=\"textPanel\">" +  desc + "</div>";
						out.println("<li class=\"folder\"><a href=\"" + contentUrl + "\">"
								+ Validator.escapeHtml(properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME))
								+ "</a>" + desc + "</li>");
					}
					else
					{
						// File

						/*
						String createdBy = getUserProperty(properties, ResourceProperties.PROP_CREATOR).getDisplayName();
						Time modTime = properties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
						String modifiedTime = modTime.toStringLocalShortDate() + " " + modTime.toStringLocalShort();
						
						ContentResource contentResource = (ContentResource) content;

						long filesize = ((contentResource.getContentLength() - 1) / 1024) + 1;
						String filetype = contentResource.getContentType();
						 */

						String desc = properties.getProperty(ResourceProperties.PROP_DESCRIPTION);
						if ((desc == null) || desc.equals(""))
							desc = "";
						else
							desc = "<div class=\"textPanel\">" + Validator.escapeHtml(desc) + "</div>";
						String resourceType = content.getResourceType().replace('.', '_');
						out.println("<li class=\"file\"><a href=\"" + contentUrl + "\" target=_blank class=\""
								+ resourceType+"\">"
								+ Validator.escapeHtml(properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME))
								+ "</a>" + desc + "</li>");
					}
				}
				catch (Exception ignore)
				{
					// TODO - what types of failures are being caught here?

					out.println("<li class=\"file\"><a href=\"" + contentUrl + "\" target=_blank>" + Validator.escapeHtml(xs)
							+ "</a></li>");
				}
			}

		}
		catch (Exception e)
		{
			M_log.warn("Problem formatting HTML for collection: "+ x.getId(), e);
		}

		if (out != null && printedHeader)
		{
			out.println("</ul>");

			if (printedDiv) out.println("</div>");
			out.println("</body></html>");
		}
	}

	protected static User getUserProperty(ResourceProperties props, String name)
	{
		String id = props.getProperty(name);
		if (id != null)
		{
			try
			{
				return UserDirectoryService.getUser(id);
			}
			catch (UserNotDefinedException e)
			{
			}
		}

		return null;
	}
}
