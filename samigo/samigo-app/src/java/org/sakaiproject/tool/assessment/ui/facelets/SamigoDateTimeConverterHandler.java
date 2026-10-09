/*
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
package org.sakaiproject.tool.assessment.ui.facelets;

import java.util.Locale;
import java.util.TimeZone;
import jakarta.faces.convert.DateTimeConverter;
import jakarta.faces.view.facelets.ConverterConfig;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;

/** Preserves the JSP date tag's inferred type and legacy converter defaults. */
public final class SamigoDateTimeConverterHandler extends ConverterHandler {
    public SamigoDateTimeConverterHandler(ConverterConfig config) {
        super(config);
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignoreAll();
    }

    @Override
    public void setAttributes(FaceletContext context, Object instance) {
        DateTimeConverter converter = (DateTimeConverter) instance;
        converter.setDateStyle(string(context, "dateStyle", "default"));
        converter.setTimeStyle(string(context, "timeStyle", "default"));
        converter.setPattern(string(context, "pattern", null));
        String inferredType = getAttribute("timeStyle") == null ? "date"
            : getAttribute("dateStyle") == null ? "time" : "both";
        converter.setType(string(context, "type", inferredType));
        TagAttribute locale = getAttribute("locale");
        Object localeValue = locale == null ? null : locale.getObject(context);
        if (localeValue == null || localeValue instanceof Locale) {
            converter.setLocale((Locale) localeValue);
        } else if (localeValue instanceof String) {
            converter.setLocale(Locale.forLanguageTag(((String) localeValue).replace('_', '-')));
        } else {
            throw new TagAttributeException(tag, locale, "Expected a locale or locale name");
        }
        TagAttribute zone = getAttribute("timeZone");
        Object zoneValue = zone == null ? null : zone.getObject(context);
        if (zoneValue == null || zoneValue instanceof TimeZone) {
            converter.setTimeZone((TimeZone) zoneValue);
        } else if (zoneValue instanceof String) {
            converter.setTimeZone(TimeZone.getTimeZone((String) zoneValue));
        } else {
            throw new TagAttributeException(tag, zone, "Expected a timezone or timezone ID");
        }
    }

    private String string(FaceletContext context, String name, String fallback) {
        TagAttribute attribute = getAttribute(name);
        return attribute == null ? fallback : attribute.getValue(context);
    }
}
