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


public interface SakaiFacade {

	// SiteStats services
	
	public StatsManager getStatsManager();
	public StatsAuthz getStatsAuthz();
	public EventRegistryService getEventRegistryService();
	public ReportManager getReportManager();
	public ChartService getChartService();
	public StatsUpdateManager getStatsUpdateManager();
	public ServerWideReportManager getServerWideReportManager();

	// Sakai services
	
	public SessionManager getSessionManager();
	public ToolManager getToolManager();
	public SiteService getSiteService();
	public AuthzGroupService getAuthzGroupService();
	public UserDirectoryService getUserDirectoryService();
	public ContentHostingService getContentHostingService();
	public TimeService getTimeService();
	
	
}
