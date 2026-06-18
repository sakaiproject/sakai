/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatsReportSummary implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String siteId;
	private String title;
	private String description;
	private boolean hidden;
}
