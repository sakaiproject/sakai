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

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;

/**
 * @author ieb
 *
 */
public class SubSiteViewImpl extends AbstractSiteViewImpl
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
	public SubSiteViewImpl(PortalSiteHelperImpl siteHelper, SiteNeighbourhoodService siteNeighbourhoodService,HttpServletRequest request,
			Session session, String currentSiteId, SiteService siteService,
			ServerConfigurationService serverConfigurationService,
			PreferencesService preferencesService)
	{
		super(siteHelper, siteNeighbourhoodService, request, session, currentSiteId, siteService,
				serverConfigurationService, preferencesService);
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.SiteView#getRenderContextObject()
	 */
	public Object getRenderContextObject()
	{
		// Subsites should be a list of sites, with this site as their parent.
		if ( currentSiteId == null || currentSiteId.trim().length() == 0 ) {
			return null;
		}
		List<Site> csites = new ArrayList<Site>();
		for ( Site s : mySites) {
			ResourceProperties rp = s.getProperties();
			String ourParent = rp.getProperty(SiteService.PROP_PARENT_ID);
			if ( currentSiteId.equals(ourParent) ) {
				csites.add(s);
			}
		}
		if ( csites.size() == 0 ) {
			return null;
		}
		
		List l = siteHelper.convertSitesToMaps(request, csites, prefix, currentSiteId, 
				/* myWorkspaceSiteId */ null,
				/* includeSummary */ false, 
				/* expandSite */ false, 
				resetTools , 
				/* doPages */ false, 
				toolContextPath,
				loggedIn);
		return l;
	}


}
