/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.function.BooleanSupplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class WidgetMetricSpec {

	private final String widgetId;
	private final String id;
	private final String labelKey;
	private final String audience;
	private final BooleanSupplier available;
	private final WidgetReportFactory reportFactory;
	private final WidgetMetricValueFactory valueFactory;

	boolean isAvailable() {
		return available.getAsBoolean();
	}
}
