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

import lombok.Setter;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public abstract class AbstractSiteViewImpl implements SiteView {

	@Autowired protected ServerConfigurationService serverConfigurationService;
	@Autowired protected SiteNeighbourhoodService siteNeighbourhoodService;

	@Setter protected String prefix;
	@Setter protected String toolContextPath;
	@Setter protected boolean doPages = false;
	@Setter protected boolean expandSite = false;
	@Setter protected boolean includeSummary = false;
	@Setter protected boolean resetTools = false;
	protected ArrayList<Site> moreSites;
	protected HttpServletRequest request;
	protected List<Site> mySites;
	protected Map<String, Object> renderContextMap;
	protected PortalSiteHelperImpl siteHelper;
	protected Session session;
	protected SiteService siteService;
	protected String currentSiteId;
	protected String myWorkspaceSiteId;
	protected boolean loggedIn;

	public AbstractSiteViewImpl() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
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

	public boolean isEmpty() {
		return mySites.isEmpty();
	}
}
