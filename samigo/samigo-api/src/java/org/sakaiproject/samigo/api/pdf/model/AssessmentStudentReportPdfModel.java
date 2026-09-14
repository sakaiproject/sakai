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

import org.apache.commons.lang3.StringUtils;

/**
 * Immutable request model for graded student assessment report PDFs.
 */
public final class AssessmentStudentReportPdfModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String studentName;
    private final String firstName;
    private final String email;
    private final String comments;
    private final String assessmentTitle;
    private final String siteTitle;
    private final double currentScore;
    private final double maxScore;
    private final boolean mathJaxEnabled;
    private final List<AssessmentPdfPartModel> parts;

    public AssessmentStudentReportPdfModel(String studentName, String firstName, String email, String comments, String assessmentTitle, String siteTitle, double currentScore, double maxScore, boolean mathJaxEnabled, List<AssessmentPdfPartModel> parts) {
        this.studentName = studentName;
        this.firstName = firstName;
        this.email = email;
        this.comments = comments;
        this.assessmentTitle = assessmentTitle;
        this.siteTitle = siteTitle;
        this.currentScore = currentScore;
        this.maxScore = maxScore;
        this.mathJaxEnabled = mathJaxEnabled;
        this.parts = parts == null ? Collections.emptyList() : List.copyOf(parts);
    }

    public String getStudentName() {
        return studentName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public String getComments() {
        return comments;
    }

    public String getAssessmentTitle() {
        return assessmentTitle;
    }

    public String getSiteTitle() {
        return siteTitle;
    }

    public double getCurrentScore() {
        return currentScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public boolean isMathJaxEnabled() {
        return mathJaxEnabled;
    }

    public List<AssessmentPdfPartModel> getParts() {
        return parts;
    }

    public boolean hasComments() {
        return StringUtils.isNotBlank(comments);
    }

    public int totalQuestions() {
        int total = 0;
        for (AssessmentPdfPartModel part : parts) {
            total += part.getQuestions().size();
        }
        return total;
    }
}
