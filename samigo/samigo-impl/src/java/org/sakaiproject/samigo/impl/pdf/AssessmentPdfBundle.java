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

    private AssessmentPdfBundle() {
    }

    public static String getAuthorString(String key) {
        return getString(AUTHOR, key);
    }

    public static String getEvaluationString(String key) {
        return getString(EVALUATION, key);
    }

    public static String getPrintString(String key) {
        return getString(PRINT, key);
    }

    public static String getCommonString(String key) {
        return getString(COMMON, key);
    }

    public static String getDeliveryString(String key) {
        return getString(DELIVERY, key);
    }

    private static String getString(String baseName, String key) {
        Locale locale = AssessmentPdfLocaleSupport.effectiveLocale();
        return ResourceBundle.getBundle(baseName, locale).getString(key);
    }
}
