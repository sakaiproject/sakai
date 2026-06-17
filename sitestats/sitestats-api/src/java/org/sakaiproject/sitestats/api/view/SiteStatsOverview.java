/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsOverview implements Serializable {

	private static final long serialVersionUID = 1L;

	private String siteId;
	private boolean viewAllowed;
	private boolean allAllowed;
	private boolean ownAllowed;
	private boolean adminAllowed;
	private List<SiteStatsWidget> widgets = new ArrayList<SiteStatsWidget>();

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public boolean isViewAllowed() {
		return viewAllowed;
	}

	public void setViewAllowed(boolean viewAllowed) {
		this.viewAllowed = viewAllowed;
	}

	public boolean isAllAllowed() {
		return allAllowed;
	}

	public void setAllAllowed(boolean allAllowed) {
		this.allAllowed = allAllowed;
	}

	public boolean isOwnAllowed() {
		return ownAllowed;
	}

	public void setOwnAllowed(boolean ownAllowed) {
		this.ownAllowed = ownAllowed;
	}

	public boolean isAdminAllowed() {
		return adminAllowed;
	}

	public void setAdminAllowed(boolean adminAllowed) {
		this.adminAllowed = adminAllowed;
	}

	public List<SiteStatsWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<SiteStatsWidget> widgets) {
		this.widgets = widgets;
	}
}
