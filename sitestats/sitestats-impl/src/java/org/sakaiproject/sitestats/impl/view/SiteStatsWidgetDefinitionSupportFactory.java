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

public class SiteStatsWidgetDefinitionSupportFactory {

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

	public SiteStatsWidgetDefinitionSupport create() {
		filterCatalog.setContext(context);

		reportFactory.setContext(context);
		reportFactory.setFilterCatalog(filterCatalog);

		metricSupport.setContext(context);
		metricSupport.setReportFactory(reportFactory);

		if (submissionsAnalytics != null) {
			submissionsAnalytics.setContext(context);
			submissionsAnalytics.setFilterCatalog(filterCatalog);
			submissionsAnalytics.setMetricSupport(metricSupport);
			filterCatalog.setSubmissionsAnalytics(submissionsAnalytics);
		}

		if (gradesAnalytics != null) {
			gradesAnalytics.setContext(context);
			gradesAnalytics.setFilterCatalog(filterCatalog);
			gradesAnalytics.setMetricSupport(metricSupport);
			filterCatalog.setGradesAnalytics(gradesAnalytics);
		}

		if (communicationAnalytics != null) {
			communicationAnalytics.setContext(context);
			communicationAnalytics.setFilterCatalog(filterCatalog);
			communicationAnalytics.setMetricSupport(metricSupport);
		}

		if (platformHealthAnalytics != null) {
			platformHealthAnalytics.setContext(context);
			platformHealthAnalytics.setMetricSupport(metricSupport);
		}

		if (institutionalScope != null && filterCatalog != null) {
			// scope is wired separately; filter catalog already has siteService via context
		}

		if (institutionalAnalytics != null) {
			institutionalAnalytics.setContext(context);
			institutionalAnalytics.setFilterCatalog(filterCatalog);
			institutionalAnalytics.setMetricSupport(metricSupport);
			institutionalAnalytics.setInstitutionalScope(institutionalScope);
			institutionalAnalytics.setSiteService(context.getSiteService());
		}

		if (clientEnvironmentAnalytics != null) {
			clientEnvironmentAnalytics.setContext(context);
			clientEnvironmentAnalytics.setFilterCatalog(filterCatalog);
			clientEnvironmentAnalytics.setMetricSupport(metricSupport);
		}

		SiteStatsWidgetDefinitionSupport support = new SiteStatsWidgetDefinitionSupport();
		support.setContext(context);
		support.setFilterCatalog(filterCatalog);
		support.setReportFactory(reportFactory);
		support.setMetricSupport(metricSupport);
		support.setSubmissionsAnalytics(submissionsAnalytics);
		support.setGradesAnalytics(gradesAnalytics);
		support.setCommunicationAnalytics(communicationAnalytics);
		support.setPlatformHealthAnalytics(platformHealthAnalytics);
		support.setInstitutionalAnalytics(institutionalAnalytics);
		support.setClientEnvironmentAnalytics(clientEnvironmentAnalytics);
		support.setInstitutionalScope(institutionalScope);
		return support;
	}
}
