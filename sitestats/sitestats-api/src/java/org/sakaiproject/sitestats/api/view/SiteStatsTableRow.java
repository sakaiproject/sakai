/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatsTableRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, SiteStatsTableCell> cells = new LinkedHashMap<String, SiteStatsTableCell>();
}
