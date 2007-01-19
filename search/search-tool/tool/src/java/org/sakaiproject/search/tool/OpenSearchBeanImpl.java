/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.tool;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

/**
 * @author ieb
 */
public class OpenSearchBeanImpl implements OpenSearchBean
{

	private SearchService searchService;

	private SiteService siteService;

	private String placementId;

	private String toolId;

	private String siteId;

	private Site currentSite;

	private HttpServletRequest request;

	private Placement placement;

	private String baseURL;

	public OpenSearchBeanImpl(HttpServletRequest request,
			SearchService searchService, SiteService siteService,
			ToolManager toolManager) throws IdUnusedException
	{
		this.request = request;
		this.searchService = searchService;
		this.siteService = siteService;
		this.placement = toolManager.getCurrentPlacement();
		this.placementId = toolManager.getCurrentPlacement().getId();
		this.toolId = toolManager.getCurrentTool().getId();
		this.siteId = toolManager.getCurrentPlacement().getContext();
		this.currentSite = this.siteService.getSite(this.siteId);
		String siteCheck = currentSite.getReference();
		baseURL = getBaseURL();
	}

	private String getBaseURL()
	{
		return ServerConfigurationService.getPortalUrl()+"/directtool/"+placementId;
	}

	public String getAdultContent()
	{
		return "false";
	}

	public String getAttibution()
	{
		String copyright = ServerConfigurationService
				.getString("default.copyright");
		String siteCopyright = currentSite.getProperties().getProperty(
				ResourceProperties.PROP_COPYRIGHT);
		if (siteCopyright != null)
		{
			copyright = siteCopyright;
		}
		return copyright;
	}

	public String getHTMLSearchFormUrl()
	{
		return baseURL + "/index";
	}

	public String getHTMLSearchTemplate()
	{
		return baseURL + "/index?panel=Main&amp;search={searchTerms}";
	}

	public String getIconUrl()
	{
		String iconURL = currentSite.getIconUrlFull();
		if ( iconURL == null ) {
			iconURL = ServerConfigurationService.getServerUrl()+"/favicon.ico";
		}
		return iconURL;
	}
	

	public String getRSSSearchTemplate()
	{
		return baseURL + "/rss20?panel=Main&amp;search={searchTerms}";
	}

	public String getSindicationRight()
	{
		String sindicationRights = "private";
		if (currentSite.isPubView())
		{
			sindicationRights = "limited";
		}
		return sindicationRights;
	}

	public String getSiteName()
	{
		return currentSite.getTitle();
	}
	public String getSystemName()
	{
		return ServerConfigurationService.getString("ui.service","Sakai");
	}


}
