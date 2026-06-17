/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsChart implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
	private String type;
	private String xKey;
	private String yKey;
	private String emptyMessage;
	private boolean threeDimensional;
	private float transparency = 1.0f;
	private boolean itemLabelsVisible = true;
	private List<SiteStatsChartDataset> datasets = new ArrayList<SiteStatsChartDataset>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getXKey() {
		return xKey;
	}

	public void setXKey(String xKey) {
		this.xKey = xKey;
	}

	public String getYKey() {
		return yKey;
	}

	public void setYKey(String yKey) {
		this.yKey = yKey;
	}

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public void setEmptyMessage(String emptyMessage) {
		this.emptyMessage = emptyMessage;
	}

	public boolean isThreeDimensional() {
		return threeDimensional;
	}

	public void setThreeDimensional(boolean threeDimensional) {
		this.threeDimensional = threeDimensional;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	public boolean isItemLabelsVisible() {
		return itemLabelsVisible;
	}

	public void setItemLabelsVisible(boolean itemLabelsVisible) {
		this.itemLabelsVisible = itemLabelsVisible;
	}

	public List<SiteStatsChartDataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<SiteStatsChartDataset> datasets) {
		this.datasets = datasets;
	}
}
