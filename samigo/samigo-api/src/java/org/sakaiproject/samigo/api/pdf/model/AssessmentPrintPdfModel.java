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
package org.sakaiproject.samigo.api.pdf.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Immutable request model for blank printable assessment PDFs.
 */
public final class AssessmentPrintPdfModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String introHtml;
    private final boolean mathJaxEnabled;
    private final AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel printSettings;
    private final List<AssessmentPdfPartModel> parts;

    public AssessmentPrintPdfModel(String title, String introHtml, boolean mathJaxEnabled, AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel printSettings, List<AssessmentPdfPartModel> parts) {
        this.title = title;
        this.introHtml = introHtml;
        this.mathJaxEnabled = mathJaxEnabled;
        this.printSettings = printSettings != null ? printSettings : AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel.defaults();
        this.parts = parts == null ? Collections.emptyList() : List.copyOf(parts);
    }

    public String getTitle() {
        return title;
    }

    public String getIntroHtml() {
        return introHtml;
    }

    public boolean isMathJaxEnabled() {
        return mathJaxEnabled;
    }

    public AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel getPrintSettings() {
        return printSettings;
    }

    public List<AssessmentPdfPartModel> getParts() {
        return parts;
    }

    public int totalQuestions() {
        int total = 0;
        for (AssessmentPdfPartModel part : parts) {
            total += part.getQuestions().size();
        }
        return total;
    }
}
