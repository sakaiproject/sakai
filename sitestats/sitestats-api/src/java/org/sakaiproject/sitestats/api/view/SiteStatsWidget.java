/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsWidget implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String title;
	private String icon;
	private boolean visible = true;
	private String audience;
	private List<SiteStatsWidgetTab> tabs = new ArrayList<SiteStatsWidgetTab>();

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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public List<SiteStatsWidgetTab> getTabs() {
		return tabs;
	}

	public void setTabs(List<SiteStatsWidgetTab> tabs) {
		this.tabs = tabs;
	}
}
