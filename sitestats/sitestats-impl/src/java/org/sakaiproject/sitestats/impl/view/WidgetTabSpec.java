/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
class WidgetTabSpec {

	private final String widgetId;
	private final String id;
	private final String titleKey;
	private final List<String> filterIds;
	private final WidgetReportFactory reportFactory;

	WidgetTabSpec(String widgetId, String id, String titleKey, List<String> filterIds, WidgetReportFactory reportFactory) {
		this.widgetId = widgetId;
		this.id = id;
		this.titleKey = titleKey;
		this.filterIds = Collections.unmodifiableList(new ArrayList<String>(filterIds));
		this.reportFactory = reportFactory;
	}
}
