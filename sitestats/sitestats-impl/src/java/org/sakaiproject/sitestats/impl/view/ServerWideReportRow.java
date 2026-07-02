/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Value;

@Value
class ServerWideReportRow {

	private Map<String, Object> values;

	static ServerWideReportRow of(Object label, Object... keyedValues) {
		LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("label", label);
		for (int i = 0; i < keyedValues.length; i += 2) {
			values.put((String) keyedValues[i], keyedValues[i + 1]);
		}
		return new ServerWideReportRow(Collections.unmodifiableMap(values));
	}
}
