/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Getter;

import org.sakaiproject.sitestats.api.report.ReportDef;

@Getter
class WidgetReportDefinition {

	private final String title;
	private final String chartDatasetLabel;
	private final ReportDef chartReportDef;
	private final ReportDef tableReportDef;

	WidgetReportDefinition(String title, ReportDef chartReportDef, ReportDef tableReportDef) {
		this(title, null, chartReportDef, tableReportDef);
	}

	WidgetReportDefinition(String title, String chartDatasetLabel, ReportDef chartReportDef, ReportDef tableReportDef) {
		this.title = title;
		this.chartDatasetLabel = chartDatasetLabel;
		this.chartReportDef = chartReportDef;
		this.tableReportDef = tableReportDef;
	}
}
