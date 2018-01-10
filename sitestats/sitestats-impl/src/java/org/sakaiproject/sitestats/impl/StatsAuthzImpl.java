/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

@Slf4j
public class StatsAuthzImpl implements StatsAuthz {
	/** Sakai services */
	private UserDirectoryService	M_uds;
	private SecurityService			M_secs;
	private SessionManager			M_sess;
	private ToolManager				M_tm;

	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setUserService(UserDirectoryService userService) {
		this.M_uds = userService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.M_secs = securityService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.M_sess = sessionManager;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.M_tm = toolManager;
	}

	public void init() {
		log.info("init()");
		// register functions
		FunctionManager.registerFunction(PERMISSION_SITESTATS_VIEW);
		FunctionManager.registerFunction(PERMISSION_SITESTATS_ADMIN_VIEW);
	}

	// ################################################################
	// Public methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Authz#isUserAbleToViewSiteStats(java.lang.String)
	 */
	public boolean isUserAbleToViewSiteStats(String siteId) {
		return hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_VIEW);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Authz#isUserAbleToViewSiteStatsAdmin(java.lang.String)
	 */
	public boolean isUserAbleToViewSiteStatsAdmin(String siteId) {
		return hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_ADMIN_VIEW);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsAuthz#isSiteStatsPage()
	 */
	public boolean isSiteStatsPage() {
		return StatsManager.SITESTATS_TOOLID.equals(M_tm.getCurrentTool().getId());
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsAuthz#isSiteStatsAdminPage()
	 */
	public boolean isSiteStatsAdminPage() {
		return StatsManager.SITESTATS_ADMIN_TOOLID.equals(M_tm.getCurrentTool().getId());
	}

	// ################################################################
	// Private methods
	// ################################################################
	private boolean hasPermission(String reference, String permission) {
		return M_secs.unlock(permission, reference);
	}
}
