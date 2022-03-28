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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

@Slf4j
public class StatsAuthzImpl implements StatsAuthz {

	@Setter private FunctionManager functionManager;
	@Setter private SecurityService securityService;
	@Setter private SessionManager sessionManager;
	@Setter private StatsManager statsManager;
	@Setter private ToolManager toolManager;

	public void init() {
		functionManager.registerFunction(PERMISSION_SITESTATS_VIEW);
		functionManager.registerFunction(PERMISSION_SITESTATS_ADMIN_VIEW);
		functionManager.registerFunction(PERMISSION_SITESTATS_OWN);
		functionManager.registerFunction(PERMISSION_SITESTATS_ALL);
		functionManager.registerFunction(PERMISSION_SITESTATS_USER_TRACKING_CAN_BE_TRACKED);
		functionManager.registerFunction(PERMISSION_SITESTATS_USER_TRACKING_CAN_TRACK);
	}

	@Override
	public boolean isUserAbleToViewSiteStats(String siteId) {
		return isUserAbleToViewSiteStatsForSiteRef(SiteService.siteReference(siteId));
	}

	@Override
	public boolean isUserAbleToViewSiteStatsAll(String siteId) {
		return hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_ALL);
	}

	@Override
	public boolean isUserAbleToViewSiteStatsAdmin(String siteId) {
		return hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_ADMIN_VIEW);
	}

	@Override
	public boolean isUserAbleToViewSiteStatsOwn(String siteId) {
		boolean showOwnStatisticsToStudents = statsManager.getPreferences(siteId, true).isShowOwnStatisticsToStudents();
		boolean hasPermission = hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_OWN);
		return (showOwnStatisticsToStudents && hasPermission);
	}

	@Override
	public boolean isSiteStatsPage() {
		return StatsManager.SITESTATS_TOOLID.equals(toolManager.getCurrentTool().getId());
	}
	
	@Override
	public boolean isSiteStatsAdminPage() {
		return StatsManager.SITESTATS_ADMIN_TOOLID.equals(toolManager.getCurrentTool().getId());
	}

	@Override
	public boolean currentUserHasPermission(String siteId, String permission) {
		if (securityService.isSuperUser()) {
			return true;
		}

		String siteRef = SiteService.siteReference(siteId);
		return hasPermission(siteRef, permission);
	}

	@Override
	public boolean canUserBeTracked(String siteID, String userID) {
		return userHasPermission(userID, SiteService.siteReference(siteID), PERMISSION_SITESTATS_USER_TRACKING_CAN_BE_TRACKED);
	}

	@Override
	public boolean canCurrentUserTrackInSite(String siteID) {
		if (securityService.isSuperUser()) {
			return true;
		}

		String siteRef = SiteService.siteReference(siteID);
		return isUserAbleToViewSiteStatsForSiteRef(siteRef) && hasPermission(siteRef, PERMISSION_SITESTATS_USER_TRACKING_CAN_TRACK);
	}

	/**
	 * Get the current session user id
	 * @return current session user id
	 */
	@Override
	public String getCurrentSessionUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	// ################################################################
	// Private methods
	// ################################################################
	private boolean hasPermission(String reference, String permission) {
		return securityService.unlock(permission, reference);
	}

	private boolean userHasPermission(String userID, String reference, String permission) {
		return securityService.unlock(userID, permission, reference);
	}

	private boolean isUserAbleToViewSiteStatsForSiteRef(String siteRef) {
		return hasPermission(siteRef, PERMISSION_SITESTATS_VIEW);
	}
}
