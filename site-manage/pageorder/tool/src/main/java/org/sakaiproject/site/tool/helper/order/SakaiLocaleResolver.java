/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool.helper.order;

import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.util.api.LocaleService;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Resolves Spring MVC's locale from Sakai's effective site/user locale.
 */
public class SakaiLocaleResolver implements LocaleResolver {

    private static final String LOCALE_ATTRIBUTE = SakaiLocaleResolver.class.getName() + ".LOCALE";

    private final LocaleService localeService;

    public SakaiLocaleResolver(LocaleService localeService) {
        this.localeService = Objects.requireNonNull(localeService);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
        return locale == null ? localeService.getLocaleForCurrentSiteAndUser() : locale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale == null) {
            request.removeAttribute(LOCALE_ATTRIBUTE);
        } else {
            request.setAttribute(LOCALE_ATTRIBUTE, locale);
        }
    }
}
