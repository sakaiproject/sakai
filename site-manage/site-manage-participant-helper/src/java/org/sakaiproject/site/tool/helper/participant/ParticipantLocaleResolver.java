/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant;

import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.util.api.LocaleService;
import org.springframework.web.servlet.LocaleResolver;

/** Resolves Spring MVC's locale from Sakai's effective site/user locale. */
public class ParticipantLocaleResolver implements LocaleResolver {

    private final LocaleService localeService;

    public ParticipantLocaleResolver(LocaleService localeService) {
        this.localeService = Objects.requireNonNull(localeService);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return localeService.getLocaleForCurrentSiteAndUser();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // Sakai owns the effective locale; requests in this helper do not override it.
    }
}
