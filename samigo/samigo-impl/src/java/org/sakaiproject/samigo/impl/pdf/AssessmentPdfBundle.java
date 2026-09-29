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

import java.util.Locale;
import java.util.ResourceBundle;

public final class AssessmentPdfBundle {

    private static final String AUTHOR = "org.sakaiproject.tool.assessment.bundle.AuthorMessages";
    private static final String EVALUATION = "org.sakaiproject.tool.assessment.bundle.EvaluationMessages";
    private static final String PRINT = "org.sakaiproject.tool.assessment.bundle.PrintMessages";
    private static final String COMMON = "org.sakaiproject.tool.assessment.bundle.CommonMessages";
    private static final String DELIVERY = "org.sakaiproject.tool.assessment.bundle.DeliveryMessages";

    private static final ResourceBundle.Control NO_DEFAULT_FALLBACK = new ResourceBundle.Control() {
        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return null;
        }
    };

    private AssessmentPdfBundle() {
    }

    public static String getAuthorString(String key, Locale locale) {
        return getString(AUTHOR, key, locale);
    }

    public static String getEvaluationString(String key, Locale locale) {
        return getString(EVALUATION, key, locale);
    }

    public static String getPrintString(String key, Locale locale) {
        return getString(PRINT, key, locale);
    }

    public static String getCommonString(String key, Locale locale) {
        return getString(COMMON, key, locale);
    }

    public static String getDeliveryString(String key, Locale locale) {
        return getString(DELIVERY, key, locale);
    }

    private static String getString(String baseName, String key, Locale locale) {
        return ResourceBundle.getBundle(baseName, bundleLocale(locale), NO_DEFAULT_FALLBACK).getString(key);
    }

    static Locale bundleLocale(Locale locale) {
        Locale effective = AssessmentPdfLocaleSupport.orDefault(locale);
        if ("en".equals(effective.getLanguage())) {
            return Locale.ROOT;
        }
        return effective;
    }
}
