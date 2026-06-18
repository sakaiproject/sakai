/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.sakaiproject.sitestats.api.report.ReportManager;

@Getter
@NoArgsConstructor
@Setter
public class SiteStatsReportRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_PAGE_SIZE = 50;
	public static final int MAX_PAGE_SIZE = 500;

	private boolean includeTable = true;
	private boolean includeChart = true;
	private int page = 1;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private String date = ReportManager.WHEN_LAST7DAYS;
	private String role = ReportManager.WHO_ALL;
	private String tool = ReportManager.WHAT_EVENTS_ALLTOOLS;
	private String resourceAction;
	private String lessonAction;

	public SiteStatsReportRequest(SiteStatsReportRequest source) {
		if (source != null) {
			this.includeTable = source.includeTable;
			this.includeChart = source.includeChart;
			setPage(source.page);
			setPageSize(source.pageSize);
			this.date = source.date;
			this.role = source.role;
			this.tool = source.tool;
			this.resourceAction = source.resourceAction;
			this.lessonAction = source.lessonAction;
		}
	}

	public void setPage(int page) {
		this.page = page < 1 ? 1 : page;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
	}
}
