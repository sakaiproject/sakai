/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatsReportView implements Serializable {

	private static final long serialVersionUID = 1L;

	private String siteId;
	private Long reportId;
	private String widgetId;
	private String tabId;
	private String metricId;
	private String title;
	private String presentationMode;
	private String generatedOn;
	private List<SiteStatsReportInfoItem> summary = new ArrayList<SiteStatsReportInfoItem>();
	private SiteStatsTable table;
	private SiteStatsChart chart;

	public void setSummary(List<SiteStatsReportInfoItem> summary) {
		this.summary = summary == null ? new ArrayList<SiteStatsReportInfoItem>() : summary;
	}
}
