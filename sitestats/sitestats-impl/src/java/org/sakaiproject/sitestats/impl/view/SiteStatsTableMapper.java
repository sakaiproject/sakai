/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.List;

import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;

/**
 * Internal mapper for converting SiteStats report rows into stable JSON view cells.
 *
 * Raw SiteStats domain types stay inside sitestats-impl; public view APIs expose
 * only the DTOs produced by this mapper.
 */
public interface SiteStatsTableMapper {

	String USER_ID = "user-id";
	String SORT_USER_NAME = "userName";

	List<SiteStatsTableColumn> getColumns(ReportParams reportParams, boolean sortable);

	SiteStatsTableColumn getColumn(String key, boolean sortable);

	SiteStatsTableCell getCell(Stat stat, String key);

	Number getNumericValue(Stat stat, String key);
}
