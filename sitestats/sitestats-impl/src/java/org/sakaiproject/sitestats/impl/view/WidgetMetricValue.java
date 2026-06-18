/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
class WidgetMetricValue {

	private final String primary;
	private final Integer percentage;
	private final String detail;

	static WidgetMetricValue of(String primary) {
		return new WidgetMetricValue(primary, null, null);
	}

	static WidgetMetricValue withPercentage(String primary, int percentage) {
		return new WidgetMetricValue(primary, Integer.valueOf(percentage), null);
	}

	static WidgetMetricValue withDetail(String primary, String detail) {
		return new WidgetMetricValue(primary, null, detail);
	}

	static WidgetMetricValue withPercentageAndDetail(String primary, int percentage, String detail) {
		return new WidgetMetricValue(primary, Integer.valueOf(percentage), detail);
	}
}
