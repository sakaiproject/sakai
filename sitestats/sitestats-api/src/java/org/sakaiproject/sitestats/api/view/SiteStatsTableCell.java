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
public class SiteStatsTableCell implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object raw;
	private String display;
	private Object sort;
	private String href;
	private String icon;
}
