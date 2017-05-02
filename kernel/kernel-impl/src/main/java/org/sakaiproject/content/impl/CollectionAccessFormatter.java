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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

/**
 * <p>
 * CollectionAccessFormatter is formatter for collection access.
 * </p>
 */
public class CollectionAccessFormatter
{
	private static final Logger M_log = LoggerFactory.getLogger(CollectionAccessFormatter.class);

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
			//we need tool.css to load font-awesome
			out.println("<link href=\"" + webappRoot + skinRepo+ "/" + skinName + 
			"/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"screen\">");
			out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>");
			out.println("<script type=\"text/javascript\">includeLatestJQuery(\"access\");</script>");
			
			//folder-listing libraries (JS + CSS) 
			out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/editor/ckextraplugins/folder-listing/js/file-tree.js\"></script>");
			out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/editor/ckextraplugins/folder-listing/js/folder-listing.js\"></script>");
			out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/editor/ckextraplugins/folder-listing/js/get-available-sites.js\"></script>");
			out.println("<link href=\"/library/editor/ckextraplugins/folder-listing/css/file-tree.css\" type=\"text/css\" rel=\"stylesheet\" media=\"screen\">");

			//load data via AJAX
			out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
			out.println("$(function() { $('.listing').folderListing({displayRootDirectory: true}); });");
			out.println("</script>");
			out.println("</head><body class=\"specialLink\">");

			out.println("<script type=\"text/javascript\" src=\"/library/js/access.js\"></script>");
			
			out.println("<div class=\"directoryIndex\">");
			String folderdesc = pl.getProperty(ResourceProperties.PROP_DESCRIPTION);
			if (folderdesc != null && !folderdesc.equals("")){ out.println("<div class=\"textPanel\">" + folderdesc + "</div>"); }
			
			if (parts.length > 2)
			{
				// go up a level
				out.println("<ul><li class=\"upfolder\"><a class=\"fa fa-level-up\" href=\"../\"> " + rb.getString("colformat.uplevel") + "</a></li></ul>");
			}
			out.println("</div>");
			
			out.println("<div data-multifolder=\"true\" data-copyright=\"true\" id=\"workspace\" data-description=\"true\" class=\"listing wait\" data-files=\"true\" data-directory=\"" + x.getUrl(true).replace("/access", "").replace("/content", "") + "\">");
			
			out.println("<ul class=\"jqueryFileTree\">");
			out.println("</ul>");

			out.println("</div>");
			out.println("</body></html>");

		}
		catch (Exception e)
		{
			M_log.warn("Problem formatting HTML for collection: "+ x.getId(), e);
		}
	}
}
