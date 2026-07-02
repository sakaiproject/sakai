/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.List;

import org.sakaiproject.sitestats.api.ServerWideReportManager;

import lombok.Value;

@Value
class ServerWideReportSpec {

	private String id;
	private String titleKey;
	private String chartType;
	private List<ServerWideReportColumn> columns;
	private ServerWideReportDataProvider dataProvider;

	List<ServerWideReportRow> rows(ServerWideReportManager reportManager) {
		return dataProvider.rows(reportManager);
	}

	interface ServerWideReportDataProvider {
		List<ServerWideReportRow> rows(ServerWideReportManager reportManager);
	}
}
