/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.transformers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.MessageSource;

public class ResolvedRefTransformerTest {

	private LocaleService localeService;
	private MessageSource messageSource;
	private ResolvedRefTransformer transformer;
	private Locale locale;

	@Before
	public void setup() {
		localeService = mock(LocaleService.class);
		messageSource = mock(MessageSource.class);
		transformer = new ResolvedRefTransformer(null, null, null, messageSource, localeService);
		locale = Locale.forLanguageTag("es");
		when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(locale);
		when(messageSource.getMessage("de_info", null, "de_info", locale)).thenReturn("Información");
		when(messageSource.getMessage("de_nodetails", null, "de_nodetails", locale))
				.thenReturn("No hay más datos disponibles");
	}

	@Test
	public void usesSakaiEffectiveLocaleForEventDetails() {
		List<EventDetail> details = transformer.transform(ResolvedEventData.NO_DETAILS, "site-1");

		assertEquals(1, details.size());
		assertEquals("Información", details.get(0).getKey());
		assertEquals("No hay más datos disponibles", details.get(0).getDisplayValue());
		verify(messageSource).getMessage("de_info", null, "de_info", locale);
		verify(messageSource).getMessage("de_nodetails", null, "de_nodetails", locale);
	}
}
