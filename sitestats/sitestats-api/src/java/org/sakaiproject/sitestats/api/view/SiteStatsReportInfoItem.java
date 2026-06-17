/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

public class SiteStatsReportInfoItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String label;
	private String value;

	public SiteStatsReportInfoItem() {
	}

	public SiteStatsReportInfoItem(String id, String label, String value) {
		this.id = id;
		this.label = label;
		this.value = value;
	}

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
