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
}
