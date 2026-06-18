/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.sakaiproject.sitestats.api.report.ReportDef;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
class WidgetReportDefinition {

	private final String title;
	private final ReportDef chartReportDef;
	private final ReportDef tableReportDef;
}
