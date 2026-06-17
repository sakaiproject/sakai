/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import org.sakaiproject.sitestats.api.report.ReportDef;

class WidgetReportDefinition {

	private final String title;
	private final ReportDef chartReportDef;
	private final ReportDef tableReportDef;

	WidgetReportDefinition(String title, ReportDef chartReportDef, ReportDef tableReportDef) {
		this.title = title;
		this.chartReportDef = chartReportDef;
		this.tableReportDef = tableReportDef;
	}

	String getTitle() {
		return title;
	}

	ReportDef getChartReportDef() {
		return chartReportDef;
	}

	ReportDef getTableReportDef() {
		return tableReportDef;
	}
}
