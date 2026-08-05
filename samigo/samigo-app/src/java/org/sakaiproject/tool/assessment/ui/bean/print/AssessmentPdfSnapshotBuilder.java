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
package org.sakaiproject.tool.assessment.ui.bean.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfAttachmentModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfItemGradingModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMediaModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfChoiceOptionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfEmiPromptModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfFillInRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMatrixRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfPartModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintChoiceModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionAttachment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FibBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FinBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ImageMapQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatrixSurveyBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps JSF delivery/print beans into API PDF request models.
 */
@Slf4j
public final class AssessmentPdfSnapshotBuilder {

    private static final ResourceLoader PRINT_MESSAGES = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.PrintMessages");

    private AssessmentPdfSnapshotBuilder() {}

    public static AssessmentPrintPdfModel buildPrintModel(DeliveryBean deliveryBean, List<? extends SectionContentsBean> deliveryParts, PrintSettingsBean printSettings, String introHtml, FormattedText formattedText) {
        AssessmentPdfPrintSettingsModel settingsModel = toPrintSettings(printSettings);
        List<AssessmentPdfPartModel> parts = new ArrayList<>();
        for (SectionContentsBean section : deliveryParts) {
            parts.add(toPart(section, false));
        }
        return new AssessmentPrintPdfModel(
                deliveryBean.getAssessmentTitle(),
                introHtml,
                deliveryBean.getIsMathJaxEnabled(),
                settingsModel,
                parts);
    }

    public static String buildIntroHtml(DeliveryBean deliveryBean, PrintSettingsBean printSettings, FormattedText formattedText) {
        if (!Boolean.TRUE.equals(printSettings.getShowPartIntros())) {
            return "";
        }
        StringBuffer assessmentIntros = new StringBuffer();
        if (deliveryBean.getInstructorMessage() != null && !"".equals(deliveryBean.getInstructorMessage())) {
            assessmentIntros.append(deliveryBean.getInstructorMessage());
            assessmentIntros.append("<br />");
        }
        if (deliveryBean.getAttachmentList() != null && !deliveryBean.getAttachmentList().isEmpty()) {
            assessmentIntros.append("<br />");
            assessmentIntros.append(PRINT_MESSAGES.getString("attachments"));
            Iterator attachmentIter = deliveryBean.getAttachmentList().iterator();
            while (attachmentIter.hasNext()) {
                assessmentIntros.append("<br />");
                PublishedAssessmentAttachment attachment = (PublishedAssessmentAttachment) attachmentIter.next();
                appendAttachmentHtml(assessmentIntros, attachment.getFilename(), attachment.getMimeType(), attachment.getResourceId(), formattedText);
            }
        }
        return assessmentIntros.toString();
    }

    public static AssessmentStudentReportPdfModel buildStudentReportModel(DeliveryBean deliveryBean, StudentScoresBean studentScoresBean) {
        String siteTitle = PRINT_MESSAGES.getString("unknown_site");
        if (deliveryBean.getSiteId() != null) {
            try {
                siteTitle = SiteService.getSite(deliveryBean.getSiteId()).getTitle();
            } catch (IdUnusedException e) {
                log.warn("Site not found for PDF export: {}", deliveryBean.getSiteId(), e);
            }
        }
        List<AssessmentPdfPartModel> parts = new ArrayList<>();
        for (SectionContentsBean section : deliveryBean.getPageContents().getPartsContents()) {
            parts.add(toPart(section, true));
        }
        return new AssessmentStudentReportPdfModel(
                studentScoresBean.getStudentName(),
                studentScoresBean.getFirstName(),
                studentScoresBean.getEmail(),
                studentScoresBean.getComments(),
                deliveryBean.getAssessmentTitle(),
                siteTitle,
                deliveryBean.getTableOfContents().getCurrentScore(),
                deliveryBean.getTableOfContents().getMaxScore(),
                deliveryBean.getIsMathJaxEnabled(),
                parts);
    }

    private static AssessmentPdfPartModel toPart(SectionContentsBean section, boolean includeReportStats) {
        List<AssessmentPdfQuestionModel> questions = new ArrayList<>();
        for (ItemContentsBean item : section.getItemContents()) {
            questions.add(toQuestion(item));
        }
        List<AssessmentPdfAttachmentModel> attachments = toAttachments(section.getAttachmentList());
        if (includeReportStats) {
            return new AssessmentPdfPartModel(
                    section.getTitle(),
                    section.getDescription(),
                    attachments,
                    questions,
                    section.getNumber(),
                    section.getQuestions(),
                    section.getUnansweredQuestions(),
                    section.getPoints(),
                    section.getMaxPoints());
        }
        return new AssessmentPdfPartModel(section.getTitle(), section.getDescription(), attachments, questions);
    }

    private static AssessmentPdfQuestionModel toQuestion(ItemContentsBean item) {
        List<String> matchingResponses = new ArrayList<>();
        if (item.getAnswers() != null) {
            for (Object answer : item.getAnswers()) {
                matchingResponses.add(answer == null ? "" : String.valueOf(answer));
            }
        }
        Long typeId = item.getItemData() == null ? null : item.getItemData().getTypeId();
        AssessmentPdfQuestionModel.Builder builder = AssessmentPdfQuestionModel.builder()
                .typeId(typeId)
                .sequence(String.valueOf(item.getSequence()))
                .text(item.getText())
                .calculatedQuestionText(calculatedQuestionText(item))
                .key(item.getKey())
                .answerKeyTf(item.getAnswerKeyTF())
                .answerKeyCalcQuestion(item.getAnswerKeyCalcQuestion())
                .calculatedQuestionAnswer(item.getCalculatedQuestionAnswer())
                .points(item.getPoints())
                .maxPoints(item.getMaxPoints())
                .unanswered(item.isUnanswered())
                .responseText(item.getResponseText())
                .responseId(item.getResponseId())
                .modelAnswerPresent(item.getModelAnswerIsNotEmpty())
                .gradingCommentPresent(item.getGradingCommentIsNotEmpty())
                .feedbackPresent(item.getFeedbackIsNotEmpty())
                .gradingComment(item.getGradingComment())
                .feedback(item.getFeedback())
                .feedbackValue(item.getFeedbackValue())
                .imageSrc(item.getImageSrc())
                .fibRows(toFibRows(item.getFibArray()))
                .finRows(toFinRows(item.getFinArray()))
                .matchingRows(toMatchingRows(item.getMatchingArray()))
                .imageMapRows(toImageMapRows(item.getMatchingArray(), typeId))
                .selectionAnswers(toSelectionAnswers(item))
                .matrixRows(toMatrixRows(item.getMatrixArray()))
                .columnIndexes(item.getColumnIndexList())
                .columnLabels(item.getColumnArray())
                .matrixAddComment(item.getAddComment())
                .matrixCommentField(item.getCommentField())
                .mediaItems(toMediaItems(item.getMediaArray()))
                .itemGradingData(toItemGradingData(item.getItemGradingDataArray()))
                .matchingResponses(matchingResponses);
        applyItemDataSnapshot(builder, item.getItemData());
        return builder.build();
    }

    private static void applyItemDataSnapshot(AssessmentPdfQuestionModel.Builder builder, ItemDataIfc itemData) {
        if (itemData == null) {
            return;
        }
        builder.itemHtmlText(itemData.getText())
                .itemAttachments(toAttachments(itemData.getItemAttachmentList()))
                .duration(itemData.getDuration())
                .triesAllowed(itemData.getTriesAllowed())
                .itemScore(itemData.getScore())
                .itemAnswerKey(itemData.getAnswerKey())
                .generalItemFeedback(itemData.getGeneralItemFeedback())
                .correctItemFeedback(itemData.getCorrectItemFeedback())
                .incorrectItemFeedback(itemData.getInCorrectItemFeedback())
                .themeText(itemData.getThemeText())
                .leadInText(itemData.getLeadInText())
                .emiAnswerOptionsSimple(itemData.getIsAnswerOptionsSimple())
                .emiAnswerOptionsRich(itemData.getIsAnswerOptionsRich())
                .emiAnswerOptionsRichText(itemData.getEmiAnswerOptionsRichText())
                .emiAnswerOptions(toEmiAnswerOptions(itemData))
                .emiPrompts(toEmiPrompts(itemData))
                .imageMapSrc(itemData.getItemMetaDataByLabel("IMAGE_MAP_SRC"))
                .imageMapItemTexts(toImageMapItemTexts(itemData))
                .printChoices(toPrintChoices(itemData));
    }

    private static List<AssessmentPdfPrintChoiceModel> toPrintChoices(ItemDataIfc itemData) {
        List<AssessmentPdfPrintChoiceModel> choices = new ArrayList<>();
        List itemTexts = itemData.getItemTextArraySorted();
        if (itemTexts == null) {
            return choices;
        }
        for (Object itemTextObject : itemTexts) {
            if (!(itemTextObject instanceof ItemTextIfc)) {
                continue;
            }
            ItemTextIfc itemText = (ItemTextIfc) itemTextObject;
            List answers = itemText.getAnswerArraySorted();
            if (answers == null) {
                continue;
            }
            for (Object answerObject : answers) {
                if (!(answerObject instanceof AnswerIfc)) {
                    continue;
                }
                AnswerIfc answer = (AnswerIfc) answerObject;
                if (StringUtils.isBlank(answer.getText())) {
                    continue;
                }
                choices.add(new AssessmentPdfPrintChoiceModel(
                        answer.getLabel(), answer.getText(), answer.getGeneralAnswerFeedback()));
            }
        }
        return choices;
    }

    private static List<AssessmentPdfChoiceOptionModel> toEmiAnswerOptions(ItemDataIfc itemData) {
        List<AssessmentPdfChoiceOptionModel> options = new ArrayList<>();
        if (!itemData.getIsAnswerOptionsSimple() || itemData.getEmiAnswerOptions() == null) {
            return options;
        }
        for (AnswerIfc answer : itemData.getEmiAnswerOptions()) {
            options.add(new AssessmentPdfChoiceOptionModel(answer.getLabel(), answer.getText(), null));
        }
        return options;
    }

    private static List<AssessmentPdfEmiPromptModel> toEmiPrompts(ItemDataIfc itemData) {
        List<AssessmentPdfEmiPromptModel> prompts = new ArrayList<>();
        List<ItemTextIfc> combinations = itemData.getEmiQuestionAnswerCombinations();
        if (combinations == null) {
            return prompts;
        }
        for (ItemTextIfc itemText : combinations) {
            if (itemText == null || StringUtils.isEmpty(itemText.getText())) {
                continue;
            }
            prompts.add(new AssessmentPdfEmiPromptModel(
                    itemText.getSequence() == null ? 0 : itemText.getSequence().intValue(),
                    itemText.getText()));
        }
        return prompts;
    }

    private static List<String> toImageMapItemTexts(ItemDataIfc itemData) {
        List<String> texts = new ArrayList<>();
        List itemTexts = itemData.getItemTextArraySorted();
        if (itemTexts == null) {
            return texts;
        }
        for (Object itemTextObject : itemTexts) {
            if (itemTextObject instanceof ItemTextIfc) {
                texts.add(((ItemTextIfc) itemTextObject).getText());
            }
        }
        return texts;
    }

    private static String calculatedQuestionText(ItemContentsBean item) {
        if (item.getItemData() == null
                || !TypeIfc.CALCULATED_QUESTION.equals(item.getItemData().getTypeId())) {
            return null;
        }
        List finArray = item.getFinArray();
        if (finArray == null || finArray.isEmpty()) {
            return null;
        }
        return item.getCalculatedQuestionText();
    }

    private static List<AssessmentPdfFillInRowModel> toFibRows(List fibArray) {
        if (fibArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfFillInRowModel> rows = new ArrayList<>();
        for (Object row : fibArray) {
            FibBean fibBean = (FibBean) row;
            rows.add(new AssessmentPdfFillInRowModel(fibBean.getText(), fibBean.getResponse(), fibBean.getIsCorrect()));
        }
        return rows;
    }

    private static List<AssessmentPdfFillInRowModel> toFinRows(List<FinBean> finArray) {
        if (finArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfFillInRowModel> rows = new ArrayList<>();
        for (FinBean finBean : finArray) {
            rows.add(new AssessmentPdfFillInRowModel(finBean.getText(), finBean.getResponse(), finBean.getIsCorrect()));
        }
        return rows;
    }

    private static List<AssessmentPdfMatchingRowModel> toMatchingRows(List matchingArray) {
        if (matchingArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfMatchingRowModel> rows = new ArrayList<>();
        for (Object row : matchingArray) {
            if (!(row instanceof MatchingBean)) {
                continue;
            }
            MatchingBean matchingBean = (MatchingBean) row;
            List<AssessmentPdfChoiceOptionModel> choices = new ArrayList<>();
            if (matchingBean.getChoices() != null) {
                for (Object choiceObject : matchingBean.getChoices()) {
                    if (!(choiceObject instanceof SelectItem)) {
                        continue;
                    }
                    SelectItem choice = (SelectItem) choiceObject;
                    Object choiceValue = choice.getValue();
                    choices.add(new AssessmentPdfChoiceOptionModel(choice.getLabel(), choiceValue == null ? null : String.valueOf(choiceValue), choice.getDescription()));
                }
            }
            rows.add(new AssessmentPdfMatchingRowModel(
                    matchingBean.getText(),
                    matchingBean.getResponse(),
                    matchingBean.getIsCorrect(),
                    choices));
        }
        return rows;
    }

    private static List<AssessmentPdfImageMapRowModel> toImageMapRows(List matchingArray, Long typeId) {
        if (matchingArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfImageMapRowModel> rows = new ArrayList<>();
        for (Object row : matchingArray) {
            if (row instanceof ImageMapQuestionBean) {
                ImageMapQuestionBean imageMapBean = (ImageMapQuestionBean) row;
                rows.add(new AssessmentPdfImageMapRowModel(imageMapBean.getText(), imageMapBean.getIsCorrect()));
            }
        }
        return rows;
    }

    private static List<AssessmentPdfSelectionAnswerModel> toSelectionAnswers(ItemContentsBean item) {
        if (item.getAnswers() == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfSelectionAnswerModel> answers = new ArrayList<>();
        for (Object answer : item.getAnswers()) {
            if (answer instanceof SelectionBean) {
                SelectionBean selectionBean = (SelectionBean) answer;
                answers.add(new AssessmentPdfSelectionAnswerModel(
                        selectionBean.getAnswer().getLabel(),
                        selectionBean.getAnswer().getText(),
                        selectionBean.getAnswer().getIsCorrect(),
                        selectionBean.getResponse()));
            } else if (answer instanceof SelectItem) {
                SelectItem selectItem = (SelectItem) answer;
                answers.add(new AssessmentPdfSelectionAnswerModel(
                        selectItem.getLabel(),
                        selectItem.getValue() == null ? null : String.valueOf(selectItem.getValue()),
                        StringUtils.equals(selectItem.getDescription(), "true"),
                        StringUtils.equals(String.valueOf(selectItem.getValue()), item.getResponseId()),
                        selectItem.getLabel()));
            } else if (answer instanceof String) {
                answers.add(new AssessmentPdfSelectionAnswerModel(null, null, null, false, (String) answer));
            }
        }
        return answers;
    }

    private static List<AssessmentPdfMatrixRowModel> toMatrixRows(List matrixArray) {
        if (matrixArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfMatrixRowModel> rows = new ArrayList<>();
        for (Object row : matrixArray) {
            if (row instanceof MatrixSurveyBean) {
                MatrixSurveyBean matrixBean = (MatrixSurveyBean) row;
                rows.add(new AssessmentPdfMatrixRowModel(
                        matrixBean.getItemText().getText(),
                        matrixBean.getAnswerSid(),
                        matrixBean.getResponseId()));
            }
        }
        return rows;
    }

    private static List<AssessmentPdfAttachmentModel> toAttachments(List attachmentList) {
        if (attachmentList == null || attachmentList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AssessmentPdfAttachmentModel> attachments = new ArrayList<>();
        for (Object attachmentObject : attachmentList) {
            if (attachmentObject instanceof PublishedSectionAttachment) {
                PublishedSectionAttachment attachment = (PublishedSectionAttachment) attachmentObject;
                attachments.add(new AssessmentPdfAttachmentModel(
                        attachment.getFilename(), attachment.getMimeType(), attachment.getResourceId()));
            } else if (attachmentObject instanceof PublishedItemAttachment) {
                PublishedItemAttachment attachment = (PublishedItemAttachment) attachmentObject;
                attachments.add(new AssessmentPdfAttachmentModel(
                        attachment.getFilename(), attachment.getMimeType(), attachment.getResourceId()));
            } else if (attachmentObject instanceof PublishedAssessmentAttachment) {
                PublishedAssessmentAttachment attachment = (PublishedAssessmentAttachment) attachmentObject;
                attachments.add(new AssessmentPdfAttachmentModel(
                        attachment.getFilename(), attachment.getMimeType(), attachment.getResourceId()));
            } else if (attachmentObject instanceof AttachmentIfc) {
                AttachmentIfc attachment = (AttachmentIfc) attachmentObject;
                attachments.add(new AssessmentPdfAttachmentModel(
                        attachment.getFilename(), attachment.getMimeType(), attachment.getResourceId()));
            }
        }
        return attachments;
    }

    private static List<AssessmentPdfMediaModel> toMediaItems(List mediaArray) {
        if (mediaArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfMediaModel> items = new ArrayList<>();
        for (Object mediaObject : mediaArray) {
            if (!(mediaObject instanceof MediaData)) {
                continue;
            }
            MediaData mediaData = (MediaData) mediaObject;
            items.add(new AssessmentPdfMediaModel(mediaData.getMediaId(), mediaData.getFilename()));
        }
        return items;
    }

    private static List<AssessmentPdfItemGradingModel> toItemGradingData(List itemGradingDataArray) {
        if (itemGradingDataArray == null) {
            return Collections.emptyList();
        }
        List<AssessmentPdfItemGradingModel> items = new ArrayList<>();
        for (Object gradingObject : itemGradingDataArray) {
            if (!(gradingObject instanceof ItemGradingData)) {
                continue;
            }
            ItemGradingData itemGradingData = (ItemGradingData) gradingObject;
            items.add(new AssessmentPdfItemGradingModel(itemGradingData.getAnswerText(), itemGradingData.getPublishedItemTextId()));
        }
        return items;
    }

    private static AssessmentPdfPrintSettingsModel toPrintSettings(PrintSettingsBean printSettings) {
        if (printSettings == null) {
            return null;
        }
        return new AssessmentPdfPrintSettingsModel(
                printSettings.getShowKeys(),
                printSettings.getFontSize(),
                printSettings.getShowPartIntros(),
                printSettings.getShowKeysFeedback(),
                printSettings.getShowSamePage());
    }

    private static void appendAttachmentHtml(StringBuffer buffer, String filename, String mimeType, String resourceId, FormattedText formattedText) {
        buffer.append(formattedText.escapeHtml(filename, false));
        String mime = mimeType != null ? mimeType.toLowerCase() : "";
        if (mime.equals("image/jpeg") || mime.equals("image/pjpeg") || mime.equals("image/gif")
                || mime.equals("image/png")) {
            buffer.append("<br />  <img src=\"/samigo");
            buffer.append(resourceId);
            buffer.append("\" />");
        }
        buffer.append("<br />");
        buffer.append("<br />");
    }
}
