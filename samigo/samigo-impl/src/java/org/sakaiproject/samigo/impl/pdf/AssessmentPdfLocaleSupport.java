/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.samigo.impl.pdf;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.LocaleService;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

final class AssessmentPdfLocaleSupport {

    private AssessmentPdfLocaleSupport() {
    }

    static Locale forSite(String siteId) {
        try {
            LocaleService localeService = ComponentManager.get(LocaleService.class);
            if (localeService != null) {
                SessionManager sessionManager = ComponentManager.get(SessionManager.class);
                String userId = sessionManager != null ? sessionManager.getCurrentSessionUserId() : null;
                if (StringUtils.isNotBlank(siteId)) {
                    return localeService.getLocaleForSiteAndUser(siteId, userId);
                }
                return localeService.getLocaleForCurrentSiteAndUser();
            }
        } catch (RuntimeException ex) {
            // Fall back when ComponentManager is unavailable, such as in unit tests.
        }
        return Locale.getDefault();
    }

    static Locale orDefault(Locale locale) {
        return locale != null ? locale : Locale.getDefault();
    }

    static DecimalFormat scoreFormat(Locale locale) {
        return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(orDefault(locale)));
    }

    /**
     * Countries that print on US Letter rather than ISO A4. Everywhere else uses A4, so this is
     * an allowlist rather than the other way round.
     */
    private static final Set<String> LETTER_COUNTRIES = Set.of("US", "CA", "MX", "PH", "CL", "CO", "CR", "VE");

    /**
     * Page size for the reader's locale: Letter in the countries that use it, A4 otherwise. A
     * locale carrying no country (plain {@code en}, say) falls to A4 along with everyone else.
     */
    static Rectangle pageSize(Locale locale) {
        return LETTER_COUNTRIES.contains(orDefault(locale).getCountry()) ? PageSize.LETTER : PageSize.A4;
    }

    /**
     * Tallest an embedded image may be drawn, as a fraction of the page. Derived from the resolved
     * page size because Letter is 50pt shorter than A4.
     */
    static float maxImageHeight(Locale locale) {
        return pageSize(locale).getHeight() * 0.16f;
    }
}
