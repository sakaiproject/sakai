/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

public class SiteStatsReportView implements Serializable {

	private static final long serialVersionUID = 1L;

	private String siteId;
	private Long reportId;
	private String widgetId;
	private String tabId;
	private String title;
	private String presentationMode;
	private String generatedOn;
	private SiteStatsTable table;
	private SiteStatsChart chart;

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getTabId() {
		return tabId;
	}

	public void setTabId(String tabId) {
		this.tabId = tabId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPresentationMode() {
		return presentationMode;
	}

	public void setPresentationMode(String presentationMode) {
		this.presentationMode = presentationMode;
	}

	public String getGeneratedOn() {
		return generatedOn;
	}

	public void setGeneratedOn(String generatedOn) {
		this.generatedOn = generatedOn;
	}

	public SiteStatsTable getTable() {
		return table;
	}

	public void setTable(SiteStatsTable table) {
		this.table = table;
	}

	public SiteStatsChart getChart() {
		return chart;
	}

	public void setChart(SiteStatsChart chart) {
		this.chart = chart;
	}
}
