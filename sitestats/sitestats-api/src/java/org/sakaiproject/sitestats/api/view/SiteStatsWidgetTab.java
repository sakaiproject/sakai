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
public class SiteStatsWidgetTab implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String title;
	private String widgetTitle;
	private List<SiteStatsFilter> filters = new ArrayList<SiteStatsFilter>();
}
