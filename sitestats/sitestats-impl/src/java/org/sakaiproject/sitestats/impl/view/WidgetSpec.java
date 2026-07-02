/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.List;
import java.util.function.BooleanSupplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class WidgetSpec {

	private final String id;
	private final String titleKey;
	private final String icon;
	private final String audience;
	private final BooleanSupplier available;
	private final List<WidgetTabSpec> tabs;
	private final List<WidgetMetricSpec> metrics;

	boolean isAvailable() {
		return available.getAsBoolean();
	}
}
