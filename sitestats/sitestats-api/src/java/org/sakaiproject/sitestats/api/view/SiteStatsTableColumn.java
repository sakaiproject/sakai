/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatsTableColumn implements Serializable {

	private static final long serialVersionUID = 1L;

	private String key;
	private String label;
	private String type;
	private boolean sortable;
	private String sortKey;
	private String align;
}
