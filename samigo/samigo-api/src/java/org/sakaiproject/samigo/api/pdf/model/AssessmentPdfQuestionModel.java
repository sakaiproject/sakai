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

import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfItemGradingModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMediaModel;

/**
 * Immutable question snapshot for PDF generation.
 */
public final class AssessmentPdfQuestionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long typeId;
    private final String itemHtmlText;
    private final List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> itemAttachments;
    private final Integer duration;
    private final Integer triesAllowed;
    private final Double itemScore;
    private final String itemAnswerKey;
    private final String generalItemFeedback;
    private final String correctItemFeedback;
    private final String incorrectItemFeedback;
    private final String themeText;
    private final String leadInText;
    private final boolean emiAnswerOptionsSimple;
    private final boolean emiAnswerOptionsRich;
    private final String emiAnswerOptionsRichText;
    private final List<AssessmentPdfValueTypes.AssessmentPdfChoiceOptionModel> emiAnswerOptions;
    private final List<AssessmentPdfValueTypes.AssessmentPdfEmiPromptModel> emiPrompts;
    private final String imageMapSrc;
    private final List<String> imageMapItemTexts;
    private final List<AssessmentPdfValueTypes.AssessmentPdfPrintChoiceModel> printChoices;
    private final String sequence;
    private final String text;
    private final String calculatedQuestionText;
    private final String key;
    private final String answerKeyTf;
    private final String answerKeyCalcQuestion;
    private final String calculatedQuestionAnswer;
    private final double points;
    private final double maxPoints;
    private final boolean unanswered;
    private final String responseText;
    private final String responseId;
    private final boolean modelAnswerPresent;
    private final boolean gradingCommentPresent;
    private final boolean feedbackPresent;
    private final String gradingComment;
    private final String feedback;
    private final String feedbackValue;
    private final String imageSrc;
    private final List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> fibRows;
    private final List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> finRows;
    private final List<AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel> matchingRows;
    private final List<AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel> imageMapRows;
    private final List<AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel> selectionAnswers;
    private final List<AssessmentPdfValueTypes.AssessmentPdfMatrixRowModel> matrixRows;
    private final List<Integer> columnIndexes;
    private final String[] columnLabels;
    private final List<AssessmentPdfMediaModel> mediaItems;
    private final List<AssessmentPdfItemGradingModel> itemGradingData;
    private final List<String> matchingResponses;
    private final boolean matrixAddComment;
    private final String matrixCommentField;

    private AssessmentPdfQuestionModel(Builder builder) {
        this.typeId = builder.typeId;
        this.itemHtmlText = builder.itemHtmlText;
        this.itemAttachments = copy(builder.itemAttachments);
        this.duration = builder.duration;
        this.triesAllowed = builder.triesAllowed;
        this.itemScore = builder.itemScore;
        this.itemAnswerKey = builder.itemAnswerKey;
        this.generalItemFeedback = builder.generalItemFeedback;
        this.correctItemFeedback = builder.correctItemFeedback;
        this.incorrectItemFeedback = builder.incorrectItemFeedback;
        this.themeText = builder.themeText;
        this.leadInText = builder.leadInText;
        this.emiAnswerOptionsSimple = builder.emiAnswerOptionsSimple;
        this.emiAnswerOptionsRich = builder.emiAnswerOptionsRich;
        this.emiAnswerOptionsRichText = builder.emiAnswerOptionsRichText;
        this.emiAnswerOptions = copy(builder.emiAnswerOptions);
        this.emiPrompts = copy(builder.emiPrompts);
        this.imageMapSrc = builder.imageMapSrc;
        this.imageMapItemTexts = copy(builder.imageMapItemTexts);
        this.printChoices = copy(builder.printChoices);
        this.sequence = builder.sequence;
        this.text = builder.text;
        this.calculatedQuestionText = builder.calculatedQuestionText;
        this.key = builder.key;
        this.answerKeyTf = builder.answerKeyTf;
        this.answerKeyCalcQuestion = builder.answerKeyCalcQuestion;
        this.calculatedQuestionAnswer = builder.calculatedQuestionAnswer;
        this.points = builder.points;
        this.maxPoints = builder.maxPoints;
        this.unanswered = builder.unanswered;
        this.responseText = builder.responseText;
        this.responseId = builder.responseId;
        this.modelAnswerPresent = builder.modelAnswerPresent;
        this.gradingCommentPresent = builder.gradingCommentPresent;
        this.feedbackPresent = builder.feedbackPresent;
        this.gradingComment = builder.gradingComment;
        this.feedback = builder.feedback;
        this.feedbackValue = builder.feedbackValue;
        this.imageSrc = builder.imageSrc;
        this.fibRows = copy(builder.fibRows);
        this.finRows = copy(builder.finRows);
        this.matchingRows = copy(builder.matchingRows);
        this.imageMapRows = copy(builder.imageMapRows);
        this.selectionAnswers = copy(builder.selectionAnswers);
        this.matrixRows = copy(builder.matrixRows);
        this.columnIndexes = copy(builder.columnIndexes);
        this.columnLabels = builder.columnLabels == null ? new String[0] : builder.columnLabels.clone();
        this.mediaItems = copy(builder.mediaItems);
        this.itemGradingData = copy(builder.itemGradingData);
        this.matchingResponses = copy(builder.matchingResponses);
        this.matrixAddComment = builder.matrixAddComment;
        this.matrixCommentField = builder.matrixCommentField;
    }

    private static <T> List<T> copy(List<T> source) {
        return source == null ? Collections.emptyList() : List.copyOf(source);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getItemHtmlText() {
        return itemHtmlText;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> getItemAttachments() {
        return itemAttachments;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getTriesAllowed() {
        return triesAllowed;
    }

    public Double getItemScore() {
        return itemScore;
    }

    public String getItemAnswerKey() {
        return itemAnswerKey;
    }

    public String getGeneralItemFeedback() {
        return generalItemFeedback;
    }

    public String getCorrectItemFeedback() {
        return correctItemFeedback;
    }

    public String getIncorrectItemFeedback() {
        return incorrectItemFeedback;
    }

    public String getThemeText() {
        return themeText;
    }

    public String getLeadInText() {
        return leadInText;
    }

    public boolean isEmiAnswerOptionsSimple() {
        return emiAnswerOptionsSimple;
    }

    public boolean isEmiAnswerOptionsRich() {
        return emiAnswerOptionsRich;
    }

    public String getEmiAnswerOptionsRichText() {
        return emiAnswerOptionsRichText;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfChoiceOptionModel> getEmiAnswerOptions() {
        return emiAnswerOptions;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfEmiPromptModel> getEmiPrompts() {
        return emiPrompts;
    }

    public String getImageMapSrc() {
        return imageMapSrc;
    }

    public List<String> getImageMapItemTexts() {
        return imageMapItemTexts;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfPrintChoiceModel> getPrintChoices() {
        return printChoices;
    }

    public String getSequence() {
        return sequence;
    }

    public String getText() {
        return text;
    }

    public String getCalculatedQuestionText() {
        return calculatedQuestionText;
    }

    public String getKey() {
        return key;
    }

    public String getAnswerKeyTf() {
        return answerKeyTf;
    }

    public String getAnswerKeyCalcQuestion() {
        return answerKeyCalcQuestion;
    }

    public String getCalculatedQuestionAnswer() {
        return calculatedQuestionAnswer;
    }

    public double getPoints() {
        return points;
    }

    public double getMaxPoints() {
        return maxPoints;
    }

    public boolean isUnanswered() {
        return unanswered;
    }

    public String getResponseText() {
        return responseText;
    }

    public String getResponseId() {
        return responseId;
    }

    public boolean isModelAnswerPresent() {
        return modelAnswerPresent;
    }

    public boolean isGradingCommentPresent() {
        return gradingCommentPresent;
    }

    public boolean isFeedbackPresent() {
        return feedbackPresent;
    }

    public String getGradingComment() {
        return gradingComment;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getFeedbackValue() {
        return feedbackValue;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> getFibRows() {
        return fibRows;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> getFinRows() {
        return finRows;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel> getMatchingRows() {
        return matchingRows;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel> getImageMapRows() {
        return imageMapRows;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel> getSelectionAnswers() {
        return selectionAnswers;
    }

    public List<AssessmentPdfValueTypes.AssessmentPdfMatrixRowModel> getMatrixRows() {
        return matrixRows;
    }

    public List<Integer> getColumnIndexes() {
        return columnIndexes;
    }

    public String[] getColumnLabels() {
        return columnLabels.clone();
    }

    public List<AssessmentPdfMediaModel> getMediaItems() {
        return mediaItems;
    }

    public List<AssessmentPdfItemGradingModel> getItemGradingData() {
        return itemGradingData;
    }

    public List<String> getMatchingResponses() {
        return matchingResponses;
    }

    public boolean isMatrixAddComment() {
        return matrixAddComment;
    }

    public String getMatrixCommentField() {
        return matrixCommentField;
    }

    public static final class Builder {
        private Long typeId;
        private String itemHtmlText;
        private List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> itemAttachments;
        private Integer duration;
        private Integer triesAllowed;
        private Double itemScore;
        private String itemAnswerKey;
        private String generalItemFeedback;
        private String correctItemFeedback;
        private String incorrectItemFeedback;
        private String themeText;
        private String leadInText;
        private boolean emiAnswerOptionsSimple;
        private boolean emiAnswerOptionsRich;
        private String emiAnswerOptionsRichText;
        private List<AssessmentPdfValueTypes.AssessmentPdfChoiceOptionModel> emiAnswerOptions;
        private List<AssessmentPdfValueTypes.AssessmentPdfEmiPromptModel> emiPrompts;
        private String imageMapSrc;
        private List<String> imageMapItemTexts;
        private List<AssessmentPdfValueTypes.AssessmentPdfPrintChoiceModel> printChoices;
        private String sequence;
        private String text;
        private String calculatedQuestionText;
        private String key;
        private String answerKeyTf;
        private String answerKeyCalcQuestion;
        private String calculatedQuestionAnswer;
        private double points;
        private double maxPoints;
        private boolean unanswered;
        private String responseText;
        private String responseId;
        private boolean modelAnswerPresent;
        private boolean gradingCommentPresent;
        private boolean feedbackPresent;
        private String gradingComment;
        private String feedback;
        private String feedbackValue;
        private String imageSrc;
        private List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> fibRows;
        private List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> finRows;
        private List<AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel> matchingRows;
        private List<AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel> imageMapRows;
        private List<AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel> selectionAnswers;
        private List<AssessmentPdfValueTypes.AssessmentPdfMatrixRowModel> matrixRows;
        private List<Integer> columnIndexes;
        private String[] columnLabels;
        private List<AssessmentPdfMediaModel> mediaItems;
        private List<AssessmentPdfItemGradingModel> itemGradingData;
        private List<String> matchingResponses;
        private boolean matrixAddComment;
        private String matrixCommentField;

        public Builder typeId(Long typeId) {
            this.typeId = typeId;
            return this;
        }

        public Builder itemHtmlText(String itemHtmlText) {
            this.itemHtmlText = itemHtmlText;
            return this;
        }

        public Builder itemAttachments(List<AssessmentPdfValueTypes.AssessmentPdfAttachmentModel> itemAttachments) {
            this.itemAttachments = itemAttachments;
            return this;
        }

        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder triesAllowed(Integer triesAllowed) {
            this.triesAllowed = triesAllowed;
            return this;
        }

        public Builder itemScore(Double itemScore) {
            this.itemScore = itemScore;
            return this;
        }

        public Builder itemAnswerKey(String itemAnswerKey) {
            this.itemAnswerKey = itemAnswerKey;
            return this;
        }

        public Builder generalItemFeedback(String generalItemFeedback) {
            this.generalItemFeedback = generalItemFeedback;
            return this;
        }

        public Builder correctItemFeedback(String correctItemFeedback) {
            this.correctItemFeedback = correctItemFeedback;
            return this;
        }

        public Builder incorrectItemFeedback(String incorrectItemFeedback) {
            this.incorrectItemFeedback = incorrectItemFeedback;
            return this;
        }

        public Builder themeText(String themeText) {
            this.themeText = themeText;
            return this;
        }

        public Builder leadInText(String leadInText) {
            this.leadInText = leadInText;
            return this;
        }

        public Builder emiAnswerOptionsSimple(boolean emiAnswerOptionsSimple) {
            this.emiAnswerOptionsSimple = emiAnswerOptionsSimple;
            return this;
        }

        public Builder emiAnswerOptionsRich(boolean emiAnswerOptionsRich) {
            this.emiAnswerOptionsRich = emiAnswerOptionsRich;
            return this;
        }

        public Builder emiAnswerOptionsRichText(String emiAnswerOptionsRichText) {
            this.emiAnswerOptionsRichText = emiAnswerOptionsRichText;
            return this;
        }

        public Builder emiAnswerOptions(List<AssessmentPdfValueTypes.AssessmentPdfChoiceOptionModel> emiAnswerOptions) {
            this.emiAnswerOptions = emiAnswerOptions;
            return this;
        }

        public Builder emiPrompts(List<AssessmentPdfValueTypes.AssessmentPdfEmiPromptModel> emiPrompts) {
            this.emiPrompts = emiPrompts;
            return this;
        }

        public Builder imageMapSrc(String imageMapSrc) {
            this.imageMapSrc = imageMapSrc;
            return this;
        }

        public Builder imageMapItemTexts(List<String> imageMapItemTexts) {
            this.imageMapItemTexts = imageMapItemTexts;
            return this;
        }

        public Builder printChoices(List<AssessmentPdfValueTypes.AssessmentPdfPrintChoiceModel> printChoices) {
            this.printChoices = printChoices;
            return this;
        }

        public Builder sequence(String sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder calculatedQuestionText(String calculatedQuestionText) {
            this.calculatedQuestionText = calculatedQuestionText;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder answerKeyTf(String answerKeyTf) {
            this.answerKeyTf = answerKeyTf;
            return this;
        }

        public Builder answerKeyCalcQuestion(String answerKeyCalcQuestion) {
            this.answerKeyCalcQuestion = answerKeyCalcQuestion;
            return this;
        }

        public Builder calculatedQuestionAnswer(String calculatedQuestionAnswer) {
            this.calculatedQuestionAnswer = calculatedQuestionAnswer;
            return this;
        }

        public Builder points(double points) {
            this.points = points;
            return this;
        }

        public Builder maxPoints(double maxPoints) {
            this.maxPoints = maxPoints;
            return this;
        }

        public Builder unanswered(boolean unanswered) {
            this.unanswered = unanswered;
            return this;
        }

        public Builder responseText(String responseText) {
            this.responseText = responseText;
            return this;
        }

        public Builder responseId(String responseId) {
            this.responseId = responseId;
            return this;
        }

        public Builder modelAnswerPresent(boolean modelAnswerPresent) {
            this.modelAnswerPresent = modelAnswerPresent;
            return this;
        }

        public Builder gradingCommentPresent(boolean gradingCommentPresent) {
            this.gradingCommentPresent = gradingCommentPresent;
            return this;
        }

        public Builder feedbackPresent(boolean feedbackPresent) {
            this.feedbackPresent = feedbackPresent;
            return this;
        }

        public Builder gradingComment(String gradingComment) {
            this.gradingComment = gradingComment;
            return this;
        }

        public Builder feedback(String feedback) {
            this.feedback = feedback;
            return this;
        }

        public Builder feedbackValue(String feedbackValue) {
            this.feedbackValue = feedbackValue;
            return this;
        }

        public Builder imageSrc(String imageSrc) {
            this.imageSrc = imageSrc;
            return this;
        }

        public Builder fibRows(List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> fibRows) {
            this.fibRows = fibRows;
            return this;
        }

        public Builder finRows(List<AssessmentPdfValueTypes.AssessmentPdfFillInRowModel> finRows) {
            this.finRows = finRows;
            return this;
        }

        public Builder matchingRows(List<AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel> matchingRows) {
            this.matchingRows = matchingRows;
            return this;
        }

        public Builder imageMapRows(List<AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel> imageMapRows) {
            this.imageMapRows = imageMapRows;
            return this;
        }

        public Builder selectionAnswers(List<AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel> selectionAnswers) {
            this.selectionAnswers = selectionAnswers;
            return this;
        }

        public Builder matrixRows(List<AssessmentPdfValueTypes.AssessmentPdfMatrixRowModel> matrixRows) {
            this.matrixRows = matrixRows;
            return this;
        }

        public Builder columnIndexes(List<Integer> columnIndexes) {
            this.columnIndexes = columnIndexes;
            return this;
        }

        public Builder columnLabels(String[] columnLabels) {
            this.columnLabels = columnLabels;
            return this;
        }

        public Builder mediaItems(List<AssessmentPdfMediaModel> mediaItems) {
            this.mediaItems = mediaItems;
            return this;
        }

        public Builder itemGradingData(List<AssessmentPdfItemGradingModel> itemGradingData) {
            this.itemGradingData = itemGradingData;
            return this;
        }

        public Builder matchingResponses(List<String> matchingResponses) {
            this.matchingResponses = matchingResponses;
            return this;
        }

        public Builder matrixAddComment(boolean matrixAddComment) {
            this.matrixAddComment = matrixAddComment;
            return this;
        }

        public Builder matrixCommentField(String matrixCommentField) {
            this.matrixCommentField = matrixCommentField;
            return this;
        }

        public AssessmentPdfQuestionModel build() {
            return new AssessmentPdfQuestionModel(this);
        }
    }
}
