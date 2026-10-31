/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.jsf.convert;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import org.sakaiproject.util.api.LocaleService;
import org.springframework.stereotype.Component;

/** Localizes decimal score strings while keeping validation and model values in canonical form. */
@Component("decimalScoreConverter")
public class DecimalScoreConverter implements Converter<String> {

    private final LocaleService localeService;

    public DecimalScoreConverter(LocaleService localeService) {
        this.localeService = localeService;
    }

    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        Double score = localeService.parseDouble(value);
        return score == null ? value : score.toString();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        return value == null ? "" : localeService.normalizeDouble(value);
    }
}
