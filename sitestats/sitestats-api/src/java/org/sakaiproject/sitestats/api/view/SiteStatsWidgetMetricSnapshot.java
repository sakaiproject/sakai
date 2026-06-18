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
public class SiteStatsWidgetMetricSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private String primary;
	private Integer percentage;
	private String detail;
}
