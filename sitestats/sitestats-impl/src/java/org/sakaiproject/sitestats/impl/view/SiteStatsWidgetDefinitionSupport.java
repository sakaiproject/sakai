/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Setter;

import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.report.ReportManager;

public class SiteStatsWidgetDefinitionSupport {

	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetReportDefFactory reportFactory;
	@Setter private WidgetMetricSupport metricSupport;
	@Setter private SiteStatsSubmissionsAnalytics submissionsAnalytics;
	@Setter private SiteStatsGradesAnalytics gradesAnalytics;
	@Setter private SiteStatsCommunicationAnalytics communicationAnalytics;
	@Setter private SiteStatsPlatformHealthAnalytics platformHealthAnalytics;
	@Setter private SiteStatsInstitutionalAnalytics institutionalAnalytics;
	@Setter private SiteStatsClientEnvironmentAnalytics clientEnvironmentAnalytics;
	@Setter private SiteStatsInstitutionalScope institutionalScope;

	public StatsManager getStatsManager() {
		return context.getStatsManager();
	}

	public ReportManager getReportManager() {
		return context.getReportManager();
	}

	public SiteStatsToolEventsService getSiteStatsToolEventsService() {
		return context.getSiteStatsToolEventsService();
	}

	public EventRegistryService getEventRegistryService() {
		return context.getEventRegistryService();
	}

	public SiteService getSiteService() {
		return context.getSiteService();
	}

	public WidgetFilterCatalog getFilterCatalog() {
		return filterCatalog;
	}

	public WidgetReportDefFactory getReportFactory() {
		return reportFactory;
	}

	public WidgetMetricSupport getMetricSupport() {
		return metricSupport;
	}

	public SiteStatsWidgetContext getContext() {
		return context;
	}

	public SiteStatsSubmissionsAnalytics getSubmissionsAnalytics() {
		return submissionsAnalytics;
	}

	public SiteStatsGradesAnalytics getGradesAnalytics() {
		return gradesAnalytics;
	}

	public SiteStatsCommunicationAnalytics getCommunicationAnalytics() {
		return communicationAnalytics;
	}

	public SiteStatsPlatformHealthAnalytics getPlatformHealthAnalytics() {
		return platformHealthAnalytics;
	}

	public SiteStatsInstitutionalAnalytics getInstitutionalAnalytics() {
		return institutionalAnalytics;
	}

	public SiteStatsClientEnvironmentAnalytics getClientEnvironmentAnalytics() {
		return clientEnvironmentAnalytics;
	}

	public SiteStatsInstitutionalScope getInstitutionalScope() {
		return institutionalScope;
	}

	public String message(String key) {
		return context.message(key);
	}

	public String message(String key, String defaultValue) {
		return context.message(key, defaultValue);
	}

	public String formattedMessage(String key, Object... args) {
		return context.formattedMessage(key, args);
	}
}
