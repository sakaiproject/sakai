/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.transformers;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * Operation-scoped access to SiteStats messages in Sakai's effective locale.
 */
class LocalizedMessages {

	private final MessageSource messageSource;
	private final Locale locale;

	LocalizedMessages(MessageSource messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	String getString(String code) {
		return messageSource.getMessage(code, null, code, locale);
	}

	String getFormattedMessage(String code, Object... arguments) {
		return messageSource.getMessage(code, arguments, code, locale);
	}

	Locale getLocale() {
		return locale;
	}
}
