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
package org.sakaiproject.sitestats.tool.facade;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.chart.ChartService;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;


public class SakaiFacadeImpl implements SakaiFacade {

	// SiteStats services
	private transient StatsManager			statsManager;
	private transient StatsAuthz				statsAuthz;
	private transient EventRegistryService	eventRegistryService;
	private transient ReportManager			reportManager;
	private transient ChartService			chartService;
	private transient StatsUpdateManager		statsUpdateManager;
	private transient ServerWideReportManager serverWideReportManager;

	// Sakai services
	private transient SessionManager			sessionManager;
	private transient ToolManager				toolManager;
	private transient SiteService				siteService;
	private transient AuthzGroupService		authzGroupService;
	private transient UserDirectoryService	userDirectoryService;
	private transient ContentHostingService	contentHostingService;
	private transient TimeService				timeService;

	public final StatsManager getStatsManager() {
		return statsManager;
	}

	public final void setStatsManager(StatsManager statsManager) {
		this.statsManager = statsManager;
	}

	public final StatsAuthz getStatsAuthz() {
		return statsAuthz;
	}

	public final void setStatsAuthz(StatsAuthz statsAuthz) {
		this.statsAuthz = statsAuthz;
	}

	public final EventRegistryService getEventRegistryService() {
		return eventRegistryService;
	}

	public final void setEventRegistryService(EventRegistryService eventRegistryService) {
		this.eventRegistryService = eventRegistryService;
	}

	public final ReportManager getReportManager() {
		return reportManager;
	}

	public final void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public final ChartService getChartService() {
		return chartService;
	}

	public final void setChartService(ChartService chartService) {
		this.chartService = chartService;
	}

	public final StatsUpdateManager getStatsUpdateManager() {
		return statsUpdateManager;
	}

	public final void setStatsUpdateManager(StatsUpdateManager statsUpdateManager) {
		this.statsUpdateManager = statsUpdateManager;
	}

	public final ServerWideReportManager getServerWideReportManager() {
		return serverWideReportManager;
	}

	public final void setServerWideReportManager(ServerWideReportManager serverWideReportManager) {
		this.serverWideReportManager = serverWideReportManager;
	}

	public final SessionManager getSessionManager() {
		return sessionManager;
	}

	public final void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public final ToolManager getToolManager() {
		return toolManager;
	}

	public final void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public final SiteService getSiteService() {
		return siteService;
	}

	public final void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public final AuthzGroupService getAuthzGroupService() {
		return authzGroupService;
	}

	public final void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public final UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	public final void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public final ContentHostingService getContentHostingService() {
		return contentHostingService;
	}

	public final void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public final TimeService getTimeService() {
		return timeService;
	}

	public final void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

}
