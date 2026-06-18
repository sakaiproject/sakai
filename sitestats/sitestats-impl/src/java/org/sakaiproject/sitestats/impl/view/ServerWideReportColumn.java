/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Value;

@Value
class ServerWideReportColumn {

	private String key;
	private String labelKey;
	private String label;
	private String type;

	static ServerWideReportColumn label(String key, String labelKey) {
		return new ServerWideReportColumn(key, labelKey, null, "text");
	}

	static ServerWideReportColumn number(String key, String labelKey) {
		return new ServerWideReportColumn(key, labelKey, null, "number");
	}

	static ServerWideReportColumn literalNumber(String key, String label) {
		return new ServerWideReportColumn(key, null, label, "number");
	}
}
