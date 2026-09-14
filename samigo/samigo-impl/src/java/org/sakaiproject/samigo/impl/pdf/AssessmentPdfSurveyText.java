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

/**
 * Converts survey answer keys to localized labels for PDF output.
 */
public final class AssessmentPdfSurveyText {

    private AssessmentPdfSurveyText() {
    }

    public static String toDisplayText(String text) {
        if (text == null) {
            return null;
        }
        if (text.equals("st_agree") || text.equals("st_disagree") || text.equals("st_undecided")
                || text.equals("st_below_average") || text.equals("st_average") || text.equals("st_above_average")
                || text.equals("st_strongly_disagree") || text.equals("st_strongly_agree")
                || text.equals("st_unacceptable") || text.equals("st_excellent")
                || text.equals("st_yes") || text.equals("st_no")) {
            return AssessmentPdfBundle.getAuthorString(text);
        }
        return text;
    }
}
