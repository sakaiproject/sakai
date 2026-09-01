/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatsFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_SELECT = "select";
	public static final String TYPE_NUMBER = "number";
	public static final String TYPE_DATE = "date";

	private String id;
	private String label;
	private String type = TYPE_SELECT;
	private String value;
	private List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
}
