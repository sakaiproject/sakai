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
 * Shared value types for assessment PDF model snapshots.
 */
public final class AssessmentPdfValueTypes {

    private AssessmentPdfValueTypes() {
    }

    /**
     * Attachment metadata for PDF rendering.
     */
    public static final class AssessmentPdfAttachmentModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String filename;
        private final String mimeType;
        private final String resourceId;

        public AssessmentPdfAttachmentModel(String filename, String mimeType, String resourceId) {
            this.filename = filename;
            this.mimeType = mimeType;
            this.resourceId = resourceId;
        }

        public String getFilename() {
            return filename;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getResourceId() {
            return resourceId;
        }
    }

    /**
     * A selectable answer option for PDF report rendering.
     */
    public static final class AssessmentPdfChoiceOptionModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String label;
        private final String value;
        private final String description;

        public AssessmentPdfChoiceOptionModel(String label, String value, String description) {
            this.label = label;
            this.value = value;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * An extended-matching prompt line for printable PDF export.
     */
    public static final class AssessmentPdfEmiPromptModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int sequence;
        private final String text;

        public AssessmentPdfEmiPromptModel(int sequence, String text) {
            this.sequence = sequence;
            this.text = text;
        }

        public int getSequence() {
            return sequence;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * A fill-in-the-blank or numeric segment for PDF rendering.
     */
    public static final class AssessmentPdfFillInRowModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String text;
        private final String response;
        private final Boolean correct;

        public AssessmentPdfFillInRowModel(String text, String response, Boolean correct) {
            this.text = text;
            this.response = response;
            this.correct = correct;
        }

        public String getText() {
            return text;
        }

        public String getResponse() {
            return response;
        }

        public Boolean getCorrect() {
            return correct;
        }
    }

    /**
     * An image-map answer row for PDF report rendering.
     */
    public static final class AssessmentPdfImageMapRowModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String text;
        private final Boolean correct;

        public AssessmentPdfImageMapRowModel(String text, Boolean correct) {
            this.text = text;
            this.correct = correct;
        }

        public String getText() {
            return text;
        }

        public Boolean getCorrect() {
            return correct;
        }
    }

    /**
     * A matching or extended-matching row for PDF rendering.
     */
    public static final class AssessmentPdfMatchingRowModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String text;
        private final String response;
        private final Boolean correct;
        private final List<AssessmentPdfChoiceOptionModel> choices;

        public AssessmentPdfMatchingRowModel(String text, String response, Boolean correct, List<AssessmentPdfChoiceOptionModel> choices) {
            this.text = text;
            this.response = response;
            this.correct = correct;
            this.choices = choices == null ? Collections.emptyList() : List.copyOf(choices);
        }

        public String getText() {
            return text;
        }

        public String getResponse() {
            return response;
        }

        public Boolean getCorrect() {
            return correct;
        }

        public List<AssessmentPdfChoiceOptionModel> getChoices() {
            return choices;
        }
    }

    /**
     * A matrix survey row for PDF report rendering.
     */
    public static final class AssessmentPdfMatrixRowModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String rowLabel;
        private final String[] answerIds;
        private final String responseId;

        public AssessmentPdfMatrixRowModel(String rowLabel, String[] answerIds, String responseId) {
            this.rowLabel = rowLabel;
            this.answerIds = answerIds == null ? new String[0] : answerIds.clone();
            this.responseId = responseId;
        }

        public String getRowLabel() {
            return rowLabel;
        }

        public String[] getAnswerIds() {
            return answerIds.clone();
        }

        public String getResponseId() {
            return responseId;
        }
    }

    /**
     * A printable answer choice for blank assessment PDFs.
     */
    public static final class AssessmentPdfPrintChoiceModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String label;
        private final String text;
        private final String generalAnswerFeedback;

        public AssessmentPdfPrintChoiceModel(String label, String text, String generalAnswerFeedback) {
            this.label = label;
            this.text = text;
            this.generalAnswerFeedback = generalAnswerFeedback;
        }

        public String getLabel() {
            return label;
        }

        public String getText() {
            return text;
        }

        public String getGeneralAnswerFeedback() {
            return generalAnswerFeedback;
        }
    }

    /**
     * A student's selected answer option for PDF report rendering.
     */
    public static final class AssessmentPdfSelectionAnswerModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String label;
        private final String text;
        private final Boolean correct;
        private final boolean selected;
        private final String plainTextAnswer;

        public AssessmentPdfSelectionAnswerModel(String label, String text, Boolean correct, boolean selected) {
            this(label, text, correct, selected, null);
        }

        public AssessmentPdfSelectionAnswerModel(String label, String text, Boolean correct, boolean selected, String plainTextAnswer) {
            this.label = label;
            this.text = text;
            this.correct = correct;
            this.selected = selected;
            this.plainTextAnswer = plainTextAnswer;
        }

        public String getLabel() {
            return label;
        }

        public String getText() {
            return text;
        }

        public Boolean getCorrect() {
            return correct;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getPlainTextAnswer() {
            return plainTextAnswer;
        }
    }

    /**
     * Uploaded media metadata for PDF report rendering.
     */
    public static final class AssessmentPdfMediaModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Long mediaId;
        private final String filename;

        public AssessmentPdfMediaModel(Long mediaId, String filename) {
            this.mediaId = mediaId;
            this.filename = filename;
        }

        public Long getMediaId() {
            return mediaId;
        }

        public String getFilename() {
            return filename;
        }
    }

    /**
     * Grading response metadata for PDF report rendering.
     */
    public static final class AssessmentPdfItemGradingModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String answerText;
        private final Long publishedItemTextId;

        public AssessmentPdfItemGradingModel(String answerText, Long publishedItemTextId) {
            this.answerText = answerText;
            this.publishedItemTextId = publishedItemTextId;
        }

        public String getAnswerText() {
            return answerText;
        }

        public Long getPublishedItemTextId() {
            return publishedItemTextId;
        }
    }

    /**
     * Print layout options for blank assessment PDFs.
     */
    public static final class AssessmentPdfPrintSettingsModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Boolean showKeys;
        private final String fontSize;
        private final Boolean showPartIntros;
        private final Boolean showKeysFeedback;
        private final Boolean showSamePage;

        public AssessmentPdfPrintSettingsModel(Boolean showKeys, String fontSize, Boolean showPartIntros, Boolean showKeysFeedback, Boolean showSamePage) {
            this.showKeys = showKeys;
            this.fontSize = fontSize;
            this.showPartIntros = showPartIntros;
            this.showKeysFeedback = showKeysFeedback;
            this.showSamePage = showSamePage;
        }

        public Boolean getShowKeys() {
            return showKeys;
        }

        public String getFontSize() {
            return fontSize;
        }

        public Boolean getShowPartIntros() {
            return showPartIntros;
        }

        public Boolean getShowKeysFeedback() {
            return showKeysFeedback;
        }

        public Boolean getShowSamePage() {
            return showSamePage;
        }

        public static AssessmentPdfPrintSettingsModel defaults() {
            return new AssessmentPdfPrintSettingsModel(Boolean.TRUE, "3", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
        }
    }
}
