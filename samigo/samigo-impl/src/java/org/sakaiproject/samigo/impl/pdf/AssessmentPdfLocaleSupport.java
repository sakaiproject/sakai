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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.api.LocaleService;

final class AssessmentPdfLocaleSupport {

    private AssessmentPdfLocaleSupport() {
    }

    static Locale effectiveLocale() {
        try {
            LocaleService localeService = ComponentManager.get(LocaleService.class);
            if (localeService != null) {
                return localeService.getLocaleForCurrentSiteAndUser();
            }
        } catch (RuntimeException ex) {
            // Fall back when ComponentManager is unavailable, such as in unit tests.
        }
        return Locale.getDefault();
    }

    static DecimalFormat scoreFormat() {
        return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(effectiveLocale()));
    }
}
