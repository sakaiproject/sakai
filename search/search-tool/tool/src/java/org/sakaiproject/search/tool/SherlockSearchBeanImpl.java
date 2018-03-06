/**
 * Copyright (c) 2003-2009 The Apereo Foundation
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
/**
 * 
 */
package org.sakaiproject.search.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.tool.api.SherlockSearchBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.FormattedText;

/**
 * @author ieb
 */
@Slf4j
public class SherlockSearchBeanImpl implements SherlockSearchBean
{

	private static final String IMAGE_ICON = "/images/sherlock.gif";

	static final String UPDATE_URL = "/sakai.src";

	static final String UPDATE_IMAGE = "/sakai.gif";

	private SiteService siteService;

	private String placementId;

	private String siteId;

	private Site currentSite;

	private String baseURL;
	
	private String portalBaseURL;

	private ServletContext context;

	public SherlockSearchBeanImpl(HttpServletRequest request, ServletContext context,
			SearchService searchService, SiteService siteService,
			ToolManager toolManager) throws IdUnusedException
	{
		this.context = context;
		this.siteService = siteService;
		this.placementId = toolManager.getCurrentPlacement().getId();
		this.siteId = toolManager.getCurrentPlacement().getContext();
		this.currentSite = this.siteService.getSite(this.siteId);
		baseURL = getBaseURL();
		portalBaseURL = getPortalBaseURL();
	}

	private String getBaseURL()
	{
		return ServerConfigurationService.getPortalUrl() + "/tool/"
				+ placementId;
	}
	private String getPortalBaseURL()
	{
		return ServerConfigurationService.getPortalUrl() + "/directtool/"
				+ placementId;
	}

	public String getSearchURL()
	{
		return portalBaseURL + "/index";
	}

	public String getSiteName()
	{
		return FormattedText.escapeHtml(currentSite.getTitle(),false);
	}

	public String getUpdateIcon()
	{
		return FormattedText.escapeHtml(baseURL + UPDATE_IMAGE,false);
	}

	public String getUpdateURL()
	{
		return FormattedText.escapeHtml(baseURL + UPDATE_URL,false);
	}

	public void sendIcon(HttpServletResponse response)
	{
		String realPath = context.getRealPath(IMAGE_ICON);
		File f = new File(realPath);
		int nbytes = (int) f.length();
		response.setContentLength(nbytes);
		InputStream is = null;
		try
		{
			is = new FileInputStream(f);
			byte[] b = new byte[4096];
			OutputStream out = response.getOutputStream();
			for (; nbytes > 0;)
			{
				int nb = is.read(b);
				if (nb > 0)
				{
					out.write(b, 0, nb);
					nbytes -= nb;
				}
				else
				{
					Thread.yield();
				}
			}
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception ex)
			{
				log.error(ex.getMessage(), ex);
			}
		}

	}
	public String getSystemName()
	{
		return FormattedText.escapeHtml(ServerConfigurationService.getString("ui.service","Sakai"),false);
	}
}
