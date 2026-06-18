/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class SiteStatsWidgetCatalogFactory {

	@Setter private SiteStatsWidgetDefinitionSupport support;
	@Setter private List<SiteStatsWidgetDefinition> widgetDefinitions = new ArrayList<SiteStatsWidgetDefinition>();

	public SiteStatsWidgetCatalog create() {
		for (SiteStatsWidgetDefinition definition : widgetDefinitions) {
			definition.setSupport(support);
		}

		SiteStatsWidgetCatalog catalog = new SiteStatsWidgetCatalog();
		catalog.setSupport(support);
		catalog.setWidgetDefinitions(widgetDefinitions);
		catalog.init();
		return catalog;
	}
}
