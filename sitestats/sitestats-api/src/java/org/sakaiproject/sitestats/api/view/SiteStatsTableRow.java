/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class SiteStatsTableRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, SiteStatsTableCell> cells = new LinkedHashMap<String, SiteStatsTableCell>();

	public Map<String, SiteStatsTableCell> getCells() {
		return cells;
	}

	public void setCells(Map<String, SiteStatsTableCell> cells) {
		this.cells = cells;
	}
}
