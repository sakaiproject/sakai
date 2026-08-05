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
import org.sakaiproject.samigo.util.SamigoConstants;

/**
 * Immutable assessment part snapshot for PDF generation.
 */
public final class AssessmentPdfPartModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String description;
    private final List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> attachments;
    private final List<AssessmentPdfQuestionModel> questions;
    private final String partNumber;
    private final Integer questionCount;
    private final Integer unansweredQuestions;
    private final Double points;
    private final Double maxPoints;

    public AssessmentPdfPartModel(String title, String description, List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> attachments, List<AssessmentPdfQuestionModel> questions) {
        this(title, description, attachments, questions, null, null, null, null, null);
    }

    public AssessmentPdfPartModel(String title, String description, List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> attachments, List<AssessmentPdfQuestionModel> questions, String partNumber, Integer questionCount, Integer unansweredQuestions, Double points, Double maxPoints) {
        this.title = title;
        this.description = description;
        this.attachments = attachments == null ? Collections.emptyList() : List.copyOf(attachments);
        this.questions = questions == null ? Collections.emptyList() : List.copyOf(questions);
        this.partNumber = partNumber;
        this.questionCount = questionCount;
        this.unansweredQuestions = unansweredQuestions;
        this.points = points;
        this.maxPoints = maxPoints;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> getAttachments() {
        return attachments;
    }

    public List<AssessmentPdfQuestionModel> getQuestions() {
        return questions;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public Integer getUnansweredQuestions() {
        return unansweredQuestions;
    }

    public Double getPoints() {
        return points;
    }

    public Double getMaxPoints() {
        return maxPoints;
    }

    public String getNonDefaultTitle() {
        if (StringUtils.isBlank(title) || SamigoConstants.DEFAULT_SECTION_TITLE.equalsIgnoreCase(title)) {
            return null;
        }
        return title;
    }
}
