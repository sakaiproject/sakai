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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

import org.sakaiproject.util.Web;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class DefaultSiteViewImpl extends AbstractSiteViewImpl
{
	/**
	 * @param siteHelper
	 * @param request
	 * @param session
	 * @param currentSiteId
	 * @param siteService
	 * @param serverConfigurationService
	 * @param preferencesService
	 */
	public DefaultSiteViewImpl(PortalSiteHelperImpl siteHelper,  SiteNeighbourhoodService siteNeighbourhoodService, HttpServletRequest request,
			Session session, String currentSiteId, SiteService siteService,
			ServerConfigurationService serverConfigurationService,
			PreferencesService preferencesService)
	{
		super(siteHelper, siteNeighbourhoodService, request, session, currentSiteId, siteService,
				serverConfigurationService, preferencesService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#getRenderContextObject()
	 */
	public Object getRenderContextObject()
	{
		// Get the list of sites in the right order,
		// My WorkSpace will be the first in the list

		// if public workgroup/gateway site is not included, add to list
		boolean siteFound = false;
		for (int i = 0; i < mySites.size(); i++)
		{
			if (((Site) mySites.get(i)).getId().equals(currentSiteId))
			{
				siteFound = true;
			}
		}

		try
		{
			if (!siteFound)
			{
				mySites.add(siteService.getSite(currentSiteId));
			}
		}
		catch (IdUnusedException e)
		{

		} // ignore

		// we allow one site in the drawer - that is OK
		moreSites = new ArrayList<Site>();
		
		processMySites();

		String profileToolId = serverConfigurationService.getString("portal.profiletool","sakai.profile2");
		String preferencesToolId = serverConfigurationService.getString("portal.preferencestool","sakai.preferences");
		String worksiteToolId = serverConfigurationService.getString("portal.worksitetool","sakai.sitesetup");

 		String profileToolUrl = null;
 		String worksiteToolUrl = null;
 		String prefsToolUrl = null;
 		String mrphs_profileToolUrl = null;
 		String mrphs_worksiteToolUrl = null;
 		String mrphs_prefsToolUrl = null;
 		String mrphs_worksiteUrl = null;
        if ( myWorkspaceSiteId != null ) {
            for (Iterator iSi = mySites.iterator(); iSi.hasNext();) {
                Site s = (Site) iSi.next();
                if (myWorkspaceSiteId.equals(s.getId()) ) {
                    mrphs_worksiteUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)));
                    List pages = siteHelper.getPermittedPagesInOrder(s);
                    for (Iterator iPg = pages.iterator(); iPg.hasNext();) {
                        SitePage p = (SitePage) iPg.next();
                        List<ToolConfiguration> pTools = p.getTools();
                        Iterator iPt = pTools.iterator();
                        while (iPt.hasNext()) {
                            ToolConfiguration placement = (ToolConfiguration) iPt.next();
                            if ( profileToolId.equals(placement.getToolId()) ) {
                                profileToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/page/" + Web.escapeUrl(p.getId()));
                                mrphs_profileToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/tool-reset/" + Web.escapeUrl(placement.getId()));
                            } else if ( preferencesToolId.equals(placement.getToolId()) ) {
                                prefsToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/page/" + Web.escapeUrl(p.getId()));
                                mrphs_prefsToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/tool-reset/" + Web.escapeUrl(placement.getId()));
                            } else if ( worksiteToolId.equals(placement.getToolId()) ) {
                                worksiteToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/page/" + Web.escapeUrl(p.getId()));
                                mrphs_worksiteToolUrl = Web.returnUrl(request, "/site/" + Web.escapeUrl(siteHelper.getSiteEffectiveId(s)) + "/tool-reset/" + Web.escapeUrl(placement.getId()));
                            }
                        }
                    }
                }
            }
        }

		if ( mrphs_worksiteUrl != null ) {
			renderContextMap.put("mrphs_worksiteUrl", mrphs_worksiteUrl);
        }
		if ( profileToolUrl != null ) {
			renderContextMap.put("profileToolUrl", profileToolUrl);
			renderContextMap.put("mrphs_profileToolUrl", mrphs_profileToolUrl);
		}
		if ( prefsToolUrl != null ) {
			renderContextMap.put("prefsToolUrl", prefsToolUrl);
			renderContextMap.put("mrphs_prefsToolUrl", mrphs_prefsToolUrl);
		}
		if ( worksiteToolUrl != null ) {
			renderContextMap.put("worksiteToolUrl", worksiteToolUrl);
			renderContextMap.put("mrphs_worksiteToolUrl", mrphs_worksiteToolUrl);
		}
		if (serverConfigurationService.getBoolean("portal.use.tutorial", true)) {
			renderContextMap.put("tutorial", true);
		} else {
			renderContextMap.put("tutorial", false);
		}
		List<Map> l = siteHelper.convertSitesToMaps(request, mySites, prefix,
				currentSiteId, myWorkspaceSiteId,
				/* includeSummary */false, /* expandSite */false,
				/* resetTools */"true".equalsIgnoreCase(serverConfigurationService
						.getString(Portal.CONFIG_AUTO_RESET)),
				/* doPages */true, /* toolContextPath */null, loggedIn);

		renderContextMap.put("tabsSites", l);

		boolean displayActive = serverConfigurationService.getBoolean("portal.always.display.active_sites",false);
		//If we don't always want to display it anyway, check to see if we need to display it
		if (!displayActive) {
				displayActive=Boolean.valueOf(moreSites.size() > 0);
		}

		renderContextMap.put("tabsMoreSitesShow", displayActive);

		// more dropdown
		if (moreSites.size() > 0)
		{
			List<Map> m = siteHelper.convertSitesToMaps(request, moreSites, prefix,
					currentSiteId, myWorkspaceSiteId,
					/* includeSummary */false, /* expandSite */ false,
					/* resetTools */"true".equalsIgnoreCase(serverConfigurationService
							.getString(Portal.CONFIG_AUTO_RESET)),
					/* doPages */true, /* toolContextPath */null, loggedIn);

			renderContextMap.put("tabsMoreSites", m);
		}

		return renderContextMap;
	}

	/**
	 */
	protected void processMySites()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.SiteView#isEmpty()
	 */
	public boolean isEmpty()
	{
		return mySites.isEmpty();
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

}
