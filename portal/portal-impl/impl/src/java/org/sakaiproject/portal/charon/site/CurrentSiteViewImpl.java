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

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;

import lombok.Setter;

/**
 * @author ieb
 */
public class CurrentSiteViewImpl implements SiteView {

	private final AuthzGroupService authzGroupService;

	protected HttpServletRequest request;
	protected PortalSiteHelperImpl siteHelper;
	protected Session session;
	protected String currentSiteId;
	protected String myWorkspaceSiteId;
    @Setter protected String prefix;
    @Setter protected String toolContextPath;
	protected boolean loggedIn;
    @Setter protected boolean resetTools = false;
    @Setter private boolean doPages;
    @Setter private boolean expandSite;
    @Setter private boolean includeSummary;
	private boolean initDone;
	private Object siteMap;

	public CurrentSiteViewImpl() {
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
	}

	public CurrentSiteViewImpl(PortalSiteHelperImpl siteHelper, HttpServletRequest request, Session session, String currentSiteId) {
		this();
		this.siteHelper = siteHelper;
		this.request = request;
		this.currentSiteId = currentSiteId;
		this.session = session;

		Site myWorkspaceSite = siteHelper.getMyWorkspace(session);
		loggedIn = session.getUserId() != null;

		if (myWorkspaceSite != null) {
			myWorkspaceSiteId = myWorkspaceSite.getId();
		}
		initDone = false;
	}

	private void init() {
		if (initDone) return;
		try {
			Site site = siteHelper.getSiteVisit(currentSiteId);
			List<String> siteProviders =  authzGroupService.getProviderIDsForRealms(List.of(site.getReference())).get(site.getReference());
			siteMap = siteHelper.convertSiteToMap(request, site, prefix, currentSiteId, myWorkspaceSiteId,
					includeSummary, true, resetTools, doPages, toolContextPath, loggedIn, siteProviders);
		} catch (IdUnusedException | PermissionException e) {
			siteMap = null;
		}
    }

	public boolean isEmpty() {
		init();
		return (siteMap == null);
	}

	public Object getRenderContextObject() {
		init();
		return siteMap;
	}
}
