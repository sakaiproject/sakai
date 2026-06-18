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

@Getter
@NoArgsConstructor
@Setter
public class SiteStatsWidgetMetric implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String label;
	private String widgetTitle;
	private String audience;
	private SiteStatsWidgetMetricSnapshot snapshot;
	private boolean reportable = true;

	public SiteStatsWidgetMetric(String id, String label, String audience, boolean reportable) {
		this.id = id;
		this.label = label;
		this.audience = audience;
		this.reportable = reportable;
	}
}
