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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;

import lombok.Setter;

/**
 * @author ieb
 */
public abstract class AbstractSiteViewImpl implements SiteView
{

	protected PreferencesService preferencesService;
	protected ServerConfigurationService serverConfigurationService;
	protected SiteNeighbourhoodService siteNeighbourhoodService;
	protected SiteService siteService;

	protected String currentSiteId;
	@Setter protected boolean doPages = false;
	@Setter protected boolean expandSite = false;
	@Setter protected boolean includeSummary = false;
	protected boolean loggedIn;
	protected ArrayList<Site> moreSites;
	protected List<Site> mySites;
	protected String myWorkspaceSiteId;
	@Setter protected String prefix;
	protected Map<String, Object> renderContextMap;
	protected HttpServletRequest request;
	@Setter protected boolean resetTools = false;
	protected Session session;
	protected PortalSiteHelperImpl siteHelper;
	@Setter protected String toolContextPath;

	public AbstractSiteViewImpl() {
		preferencesService = ComponentManager.get(PreferencesService.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		siteNeighbourhoodService = ComponentManager.get(SiteNeighbourhoodService.class);
		siteService = ComponentManager.get(SiteService.class);
	}

	public AbstractSiteViewImpl(PortalSiteHelperImpl siteHelper, HttpServletRequest request, Session session, String currentSiteId) {
		this();
		this.siteHelper = siteHelper;
		this.request = request;
		this.currentSiteId = currentSiteId;
		this.session = session;
		boolean showMyWorkspace = serverConfigurationService.getBoolean("myworkspace.show", true);
		mySites = siteNeighbourhoodService.getSitesAtNode(request, session, showMyWorkspace);

		loggedIn = session.getUserId() != null;
		Site myWorkspaceSite = siteHelper.getMyWorkspace(session);
		if (myWorkspaceSite != null) {
			myWorkspaceSiteId = myWorkspaceSite.getId();
		}
		renderContextMap = new HashMap<>();
	}


	@Override
	public boolean isEmpty() {
		return mySites.isEmpty();
	}

}
