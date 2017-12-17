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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

/**
 * <p>
 * CollectionAccessFormatter is formatter for collection access.
 * </p>
 */
@Slf4j
public class CollectionAccessFormatter
{
	private FormattedText formattedText;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;

	public void setFormattedText(FormattedText formattedText) {
		this.formattedText = formattedText;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * Format the collection as an HTML display.
	 * Ths ContentHostingService is passed in here to handle the cyclic dependency between the BaseContentService
	 * and this class.
	 */
	public void format(ContentCollection x, Reference ref, HttpServletRequest req, HttpServletResponse res, ResourceLoader rb,
			ContentHostingService contentHostingService)
	{
		// do not allow directory listings for /attachments and its subfolders  
		if (contentHostingService.isAttachmentResource(x.getId()))
		{
			try
			{
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			} 
			catch ( IOException e )
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
			String webappRoot = serverConfigurationService.getServerUrl();
			String skinRepo = serverConfigurationService.getString("skin.repo", "/library/skin");
			String siteId = null;
			String[] parts= StringUtils.split(x.getId(), Entity.SEPARATOR);
			
			// Is this a site folder (Resources or Dropbox)? If so, get the site skin
			if (x.getId().startsWith(ContentHostingService.COLLECTION_SITE) ||
				x.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX)) {
				if (parts.length > 1) {
					siteId = parts[1];
				}
			}
			String skinName = siteService.getSiteSkin(siteId);


			// Output the headers
			
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			out.println("<html><head>");
			out.println("<title>" + rb.getFormattedMessage("colformat.pagetitle", 
					new Object[]{ formattedText.escapeHtml(pl.getProperty(ResourceProperties.PROP_DISPLAY_NAME))}) + "</title>");
			out.println("<link href=\"" + webappRoot + skinRepo+ "/" + skinName + 
			"/access.css\" type=\"text/css\" rel=\"stylesheet\" media=\"screen\">");
			out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>");
			out.println("<script type=\"text/javascript\">includeLatestJQuery(\"access\");</script>");
			out.println("</head><body class=\"specialLink\">");

			out.println("<script type=\"text/javascript\" src=\"/library/js/access.js\"></script>");
			out.println("<div class=\"directoryIndex\">");

			// for content listing it's best to use a real title
			out.println("<h3>" + formattedText.escapeHtml(pl.getProperty(ResourceProperties.PROP_DISPLAY_NAME)) + "</h3>");
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
				out.println("<li class=\"upfolder\"><a href=\"../\"><span class=\"faicon\"></span>" + rb.getString("colformat.uplevel") + "</a></li>");
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

			String hiddenClassParent = x.isAvailable() ? "" : " inactive";
			String hiddenClass;
			for (ContentEntity content : members) {

				hiddenClass = hiddenClassParent;
				ResourceProperties properties = content.getProperties();
				boolean isCollection = content.isCollection();
				String xs = content.getId();
				String contentUrl = content.getUrl();

				// These both perform the same check in the implementation but we should observe the API.
				// This also checks to see if a resource is hidden or time limited.
				if ( isCollection) {
					if (!contentHostingService.allowGetCollection(xs)) {
						continue;
					}
				} else {
					if (!contentHostingService.allowGetResource(xs)) {
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
					if(!content.isAvailable()) {
							hiddenClass = " inactive";
					}
					String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

					if (isCollection)
					{
						// Folder
						String desc = properties.getProperty(ResourceProperties.PROP_DESCRIPTION);
						if (desc == null) {
							desc = "";
						} else {
							desc = "<div class=\"textPanel\">" +  desc + "</div>";
						}
						StringBuilder li
							= new StringBuilder("<li class=\"folder\"><a href=\"").append(contentUrl).append("\"");
						li.append(" class=\""+hiddenClass+"\"");
						li.append("><span class=\"faicon\"></span>")
							.append(formattedText.escapeHtml(displayName))
							.append("</a>")
							.append(desc)
							.append("</li>");

						out.println(li.toString());
					}
					else
					{
						// File
						String desc = properties.getProperty(ResourceProperties.PROP_DESCRIPTION);
						if (desc == null) {
							desc = "";
						} else {
							desc = "<div class=\"textPanel\">" + formattedText.escapeHtml(desc) + "</div>";
						}
						String resourceType = content.getResourceType().replace('.', '_');
						StringBuilder li
							= new StringBuilder("<li class=\"file" + hiddenClass +"\"><a href=\"").append(contentUrl).append("\" target=_blank class=\"");
						li.append(resourceType);
						li.append(hiddenClass);
						li.append("\"><span class=\"faicon\"></span>");
						li.append(formattedText.escapeHtml(displayName));
						li.append("</a>").append(desc).append("</li>");

						out.println(li.toString());
					}
				}
				catch (Exception e)
				{
					log.info("Problem rendering item falling back to default rendering: "+ x.getId()+ ", "+ e.getMessage());
					out.println("<li class=\"file\"><a href=\"" + contentUrl + "\" target=_blank>" + formattedText.escapeHtml(xs)
							+ "</a></li>");
				}
			}

		}
		catch (Exception e)
		{
			log.warn("Problem formatting HTML for collection: "+ x.getId(), e);
		}

		if (out != null && printedHeader)
		{
			out.println("</ul>");

			if (printedDiv) out.println("</div>");
			out.println("</body></html>");
		}
	}
}
