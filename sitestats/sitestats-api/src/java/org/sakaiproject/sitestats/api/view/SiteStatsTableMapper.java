/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.util.List;

import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.report.ReportParams;

/**
 * Maps SiteStats report rows into stable presentation columns and cells.
 */
public interface SiteStatsTableMapper {

	String USER_ID = "user-id";
	String SORT_USER_NAME = "userName";

	List<SiteStatsTableColumn> getColumns(ReportParams reportParams, boolean sortable);

	SiteStatsTableColumn getColumn(String key, boolean sortable);

	SiteStatsTableCell getCell(Stat stat, String key);

	Number getNumericValue(Stat stat, String key);
}
