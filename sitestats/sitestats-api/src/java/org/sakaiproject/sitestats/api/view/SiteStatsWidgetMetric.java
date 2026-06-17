/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

public class SiteStatsWidgetMetric implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String label;
	private String widgetTitle;
	private String audience;
	private boolean reportable = true;

	public SiteStatsWidgetMetric() {
	}

	public SiteStatsWidgetMetric(String id, String label, String audience, boolean reportable) {
		this.id = id;
		this.label = label;
		this.audience = audience;
		this.reportable = reportable;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getWidgetTitle() {
		return widgetTitle;
	}

	public void setWidgetTitle(String widgetTitle) {
		this.widgetTitle = widgetTitle;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public boolean isReportable() {
		return reportable;
	}

	public void setReportable(boolean reportable) {
		this.reportable = reportable;
	}
}
