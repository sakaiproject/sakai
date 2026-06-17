/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

public class SiteStatsTableCell implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object raw;
	private String display;
	private Object sort;
	private String href;
	private String icon;

	public Object getRaw() {
		return raw;
	}

	public void setRaw(Object raw) {
		this.raw = raw;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public Object getSort() {
		return sort;
	}

	public void setSort(Object sort) {
		this.sort = sort;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
