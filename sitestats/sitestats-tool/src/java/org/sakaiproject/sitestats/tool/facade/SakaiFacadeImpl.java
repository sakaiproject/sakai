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

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.chart.ChartService;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

public class SakaiFacadeImpl implements SakaiFacade {

	// SiteStats services
	@Getter @Setter private transient StatsManager				statsManager;
	@Getter @Setter private transient StatsAuthz				statsAuthz;
	@Getter @Setter private transient EventRegistryService		eventRegistryService;
	@Getter @Setter private transient ReportManager				reportManager;
	@Getter @Setter private transient ChartService				chartService;
	@Getter @Setter private transient StatsUpdateManager		statsUpdateManager;
	@Getter @Setter private transient ServerWideReportManager	serverWideReportManager;
	@Getter @Setter private transient DetailedEventsManager		detailedEventsManager;

	// Sakai services
	@Getter @Setter private transient SessionManager		sessionManager;
	@Getter @Setter private transient ToolManager			toolManager;
	@Getter @Setter private transient SiteService			siteService;
	@Getter @Setter private transient AuthzGroupService		authzGroupService;
	@Getter @Setter private transient UserDirectoryService	userDirectoryService;
	@Getter @Setter private transient ContentHostingService	contentHostingService;
	@Getter @Setter private transient UserTimeService		userTimeService;
}
