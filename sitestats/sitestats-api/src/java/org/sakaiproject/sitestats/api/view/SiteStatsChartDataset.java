/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsChartDataset implements Serializable {

	private static final long serialVersionUID = 1L;

	private String key;
	private String label;
	private List<SiteStatsChartPoint> points = new ArrayList<SiteStatsChartPoint>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<SiteStatsChartPoint> getPoints() {
		return points;
	}

	public void setPoints(List<SiteStatsChartPoint> points) {
		this.points = points;
	}
}
