/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String label;
	private String type;
	private List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<SiteStatsFilterOption> getOptions() {
		return options;
	}

	public void setOptions(List<SiteStatsFilterOption> options) {
		this.options = options;
	}
}
