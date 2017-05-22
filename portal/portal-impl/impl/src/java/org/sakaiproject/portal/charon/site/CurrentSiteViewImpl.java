/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.site;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;

/**
 * @author ieb
 */
public class CurrentSiteViewImpl implements SiteView
{

	protected PortalSiteHelperImpl siteHelper;

	protected HttpServletRequest request;

	protected String currentSiteId;

	protected String prefix;

	protected String toolContextPath;

	protected Session session;

	protected String myWorkspaceSiteId;

	protected boolean loggedIn;

	protected boolean resetTools = false;

	private Object siteMap;

	private boolean initDone;

	private boolean includeSummary;

	private boolean doPages;

	private boolean expandSite;

	public CurrentSiteViewImpl(PortalSiteHelperImpl siteHelper,
			SiteNeighbourhoodService siteNeighbourhoodService,
			HttpServletRequest request, Session session, String currentSiteId,
			SiteService siteService,
			ServerConfigurationService serverConfigurationService,
			PreferencesService preferencesService)
	{
		this.siteHelper = siteHelper;
		this.request = request;
		this.currentSiteId = currentSiteId;
		this.session = session;

		Site myWorkspaceSite = siteHelper.getMyWorkspace(session);
		loggedIn = session.getUserId() != null;

		if (myWorkspaceSite != null)
		{
			myWorkspaceSiteId = myWorkspaceSite.getId();
		}
		initDone = false;
	}

	private void init()
	{
		if (initDone) return;
		try
		{
			Site site = siteHelper.getSiteVisit(currentSiteId);
			List<String> siteProviders = (List<String>) PortalSiteHelperImpl.getProviderIDsForSite(site);
			siteMap = siteHelper
					.convertSiteToMap(request, site, prefix, currentSiteId,
							myWorkspaceSiteId, includeSummary,
							/* expandSite */true, resetTools, doPages, toolContextPath,
							loggedIn, siteProviders);
		}
		catch (IdUnusedException e)
		{
			siteMap = null;
		}
		catch (PermissionException e)
		{
			siteMap = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#isEmpty()
	 */
	public boolean isEmpty()
	{
		init();
		return (siteMap == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#setPrefix(java.lang.String)
	 */
	public void setPrefix(String prefix)
	{
		this.prefix = prefix;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#setToolContextPath(java.lang.String)
	 */
	public void setToolContextPath(String toolContextPath)
	{
		this.toolContextPath = toolContextPath;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#setResetTools(boolean)
	 */
	public void setResetTools(boolean resetTools)
	{
		this.resetTools = resetTools;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#getRenderContextObject()
	 */
	public Object getRenderContextObject()
	{
		init();
		return siteMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#setDoPages(boolean)
	 */
	public void setDoPages(boolean doPages)
	{
		this.doPages = doPages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#setIncludeSummary(boolean)
	 */
	public void setIncludeSummary(boolean includeSummary)
	{
		this.includeSummary = includeSummary;

	}

        /* (non-Javadoc)
         * @see org.sakaiproject.portal.api.SiteView#setExpandSite(boolean)
         */
        public void setExpandSite(boolean expandSite)
        {
                this.expandSite = expandSite;
        }
}
