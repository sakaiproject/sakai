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
public class SiteStatsOverview implements Serializable {

	private static final long serialVersionUID = 1L;

	private String siteId;
	private boolean viewAllowed;
	private boolean allAllowed;
	private boolean ownAllowed;
	private boolean adminAllowed;
	private List<SiteStatsWidget> widgets = new ArrayList<SiteStatsWidget>();
}
