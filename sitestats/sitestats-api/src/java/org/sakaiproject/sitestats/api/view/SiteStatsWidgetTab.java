/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsWidgetTab implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String title;
	private List<SiteStatsFilter> filters = new ArrayList<SiteStatsFilter>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<SiteStatsFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<SiteStatsFilter> filters) {
		this.filters = filters;
	}
}
