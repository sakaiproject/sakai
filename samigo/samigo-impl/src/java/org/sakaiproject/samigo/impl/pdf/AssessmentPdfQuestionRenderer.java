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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfChoiceOptionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfEmiPromptModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMatrixRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintChoiceModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfSelectionAnswerModel;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.CheckOrCrossCellEvent;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.CheckboxCellEvent;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.CircleCellEvent;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.ImageMapCircle;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.ImageMapQuestionCellEvent;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfItemGradingModel;
import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BACKGROUND_GRAY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BODY_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BODY_ITALIC_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BORDER_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.FEEDBACK_BG;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.INFO_BG;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.PRIMARY_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SECONDARY_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SUCCESS_BG;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TEXT_PRIMARY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TEXT_SECONDARY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.WARNING_BG;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.WARNING_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.fontWithColor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang3.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * Renders a single assessment question for printable handouts and graded student reports.
 */
@Slf4j
public class AssessmentPdfQuestionRenderer {

    private static final Set<Long> PRINT_FILL_IN_TYPES = Set.of(
            TypeIfc.FILL_IN_BLANK,
            TypeIfc.FILL_IN_NUMERIC,
            TypeIfc.CALCULATED_QUESTION);

    private static final Set<Long> PRINT_CHOICE_TYPES = Set.of(
            TypeIfc.MULTIPLE_CORRECT,
            TypeIfc.MULTIPLE_CHOICE,
            TypeIfc.MULTIPLE_CHOICE_SURVEY,
            TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION,
            TypeIfc.TRUE_FALSE);

    private static final Set<Long> PRINT_NO_KEY_TYPES = Set.of(
            TypeIfc.MULTIPLE_CHOICE_SURVEY,
            TypeIfc.AUDIO_RECORDING,
            TypeIfc.FILE_UPLOAD);

    private static final Set<Long> PRINT_FEEDBACK_TYPES = Set.of(
            TypeIfc.MULTIPLE_CORRECT,
            TypeIfc.MULTIPLE_CHOICE,
            TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION,
            TypeIfc.TRUE_FALSE,
            TypeIfc.FILL_IN_BLANK,
            TypeIfc.FILL_IN_NUMERIC,
            TypeIfc.CALCULATED_QUESTION,
            TypeIfc.MATCHING,
            TypeIfc.IMAGEMAP_QUESTION);

    private final AssessmentPdfContentHelper contentHelper;
    private final ObjectMapper jsonMapper;

    public AssessmentPdfQuestionRenderer(AssessmentPdfContentHelper contentHelper) {
        this.contentHelper = contentHelper;
        this.jsonMapper = MapperFactory.createDefaultJsonMapper();
    }

    public void render(Document document, QuestionRenderContext context) throws Exception {
        if (context.isPrintMode()) {
            renderPrintable(document, context);
        } else {
            renderStudentReport(document, context);
        }
    }

    // --- Printable flow ---

    private void renderPrintable(Document document, QuestionRenderContext context) throws Exception {
        AssessmentPdfQuestionModel question = context.getQuestion();
        Long questionType = context.getQuestionType();
        AssessmentPdfPrintSettingsModel printSettings = context.getPrintSettings();

        renderPrintQuestionHeader(document, context);
        renderPrintBody(document, context, question, questionType);
        renderPrintAnswerOptions(document, context, question, questionType, printSettings);

        if (Boolean.TRUE.equals(printSettings.getShowKeys())) {
            renderPrintAnswerKey(document, context, question, questionType, printSettings);
        }
    }

    private void renderPrintQuestionHeader(Document document, QuestionRenderContext context) throws Exception {
        PdfPTable questionTable = new PdfPTable(1);
        questionTable.setWidthPercentage(100f);
        questionTable.setSpacingBefore(16f);
        contentHelper.configureSplittableTable(questionTable);

        PdfPCell questionNumCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getEvaluationString("question") + " " + context.getQuestionNumber() + " / " + context.getTotalQuestions(), fontWithColor(context.bodyBoldFont(), TEXT_PRIMARY)));
        contentHelper.styleQuestionHeaderCell(questionNumCell, false);
        questionTable.addCell(questionNumCell);
        document.add(questionTable);
    }

    private void renderPrintBody(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        renderPrintStem(document, context, question, questionType);

        if (Objects.equals(questionType, TypeIfc.AUDIO_RECORDING)) {
            renderPrintAudioRecordingBody(document, context, question);
        } else if (Objects.equals(questionType, TypeIfc.FILE_UPLOAD)) {
            renderPrintFileUploadBody(document, context);
        } else if (Objects.equals(questionType, TypeIfc.ESSAY_QUESTION)) {
            renderPrintEssayBody(document, context);
        }
    }

    private void renderPrintStem(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        if (PRINT_FILL_IN_TYPES.contains(questionType)) {
            contentHelper.addAttachmentListToDocument(document, question.getItemAttachments(), context.getFontSizeSetting(), context.isMathJaxEnabled());

            String text = question.getItemHtmlText();
            if (Objects.equals(questionType, TypeIfc.CALCULATED_QUESTION)) {
                text = question.getCalculatedQuestionText();
            }
            contentHelper.addQuestionTitleToDocument(document, text, true, context.isMathJaxEnabled(), context.getFontSizeSetting());
            return;
        }

        if (StringUtils.isNotEmpty(question.getItemHtmlText())) {
            contentHelper.addQuestionTitleToDocument(document, question.getItemHtmlText(), true, context.isMathJaxEnabled(), context.getFontSizeSetting());
        }
        contentHelper.addAttachmentListToDocument(document, question.getItemAttachments(), context.getFontSizeSetting(), context.isMathJaxEnabled());
    }

    private void renderPrintAudioRecordingBody(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question) {
        Paragraph timeParagraph = new Paragraph(AssessmentPdfBundle.getPrintString("time_allowed_seconds") + ": " + question.getDuration(), fontWithColor(context.bodyFont(), TEXT_PRIMARY));
        timeParagraph.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        document.add(timeParagraph);
        document.add(new Paragraph(AssessmentPdfBundle.getPrintString("number_of_tries") + ": " + question.getTriesAllowed(), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
    }

    private void renderPrintFileUploadBody(Document document, QuestionRenderContext context) {
        Paragraph instruction = new Paragraph(AssessmentPdfBundle.getPrintString("upload_instruction"), fontWithColor(context.bodyFont(), TEXT_PRIMARY));
        instruction.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        document.add(instruction);
        document.add(new Paragraph(AssessmentPdfBundle.getPrintString("file") + ": ________________________", fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
    }

    private void renderPrintEssayBody(Document document, QuestionRenderContext context) {
        PdfPTable responseTable = new PdfPTable(1);
        responseTable.setWidthPercentage(100f);
        responseTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        PdfPCell responseCell = new PdfPCell(new Paragraph(" ", fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
        responseCell.setPadding(72f);
        responseCell.setBackgroundColor(BACKGROUND_GRAY);
        responseCell.setBorder(Rectangle.NO_BORDER);
        responseTable.addCell(responseCell);
        document.add(responseTable);
    }

    private void renderPrintAnswerOptions(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType, AssessmentPdfPrintSettingsModel printSettings) throws Exception {
        if (PRINT_CHOICE_TYPES.contains(questionType)) {
            renderPrintChoiceAnswers(document, context, question, questionType, printSettings);
            return;
        }
        if (Objects.equals(questionType, TypeIfc.MATRIX_CHOICES_SURVEY)) {
            renderPrintMatrix(document, context, question);
            return;
        }
        if (Objects.equals(questionType, TypeIfc.MATCHING)) {
            renderPrintMatching(document, context, question);
        } else if (Objects.equals(questionType, TypeIfc.EXTENDED_MATCHING_ITEMS)) {
            renderPrintExtendedMatching(document, context, question);
        } else if (Objects.equals(questionType, TypeIfc.IMAGEMAP_QUESTION)) {
            renderPrintImageMapOptions(document, context, question);
        }
    }

    private void renderPrintChoiceAnswers(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType, AssessmentPdfPrintSettingsModel printSettings) throws Exception {
        boolean firstChoice = true;
        for (AssessmentPdfPrintChoiceModel choice : question.getPrintChoices()) {
            if (StringUtils.isBlank(choice.getText())) {
                break;
            }
            renderPrintChoiceAnswer(document, context, questionType, choice, printSettings, firstChoice);
            firstChoice = false;
        }
    }

    private void renderPrintChoiceAnswer(Document document, QuestionRenderContext context, Long questionType, AssessmentPdfPrintChoiceModel choice, AssessmentPdfPrintSettingsModel printSettings, boolean firstChoice) throws Exception {
        PdfPTable optionTable = new PdfPTable(1);
        optionTable.setWidthPercentage(100f);
        if (firstChoice) {
            optionTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        }
        optionTable.setSpacingAfter(4f);
        contentHelper.configureSplittableTable(optionTable);
        PdfPCell optionCell = new PdfPCell();
        String optionText;
        if (questionType.equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) {
            optionText = "  " + AssessmentPdfSurveyText.toDisplayText(choice.getText());
        } else if (questionType.equals(TypeIfc.TRUE_FALSE)) {
            optionText = "  " + choice.getText();
        } else {
            optionText = "  " + choice.getLabel() + ". " + choice.getText();
        }
        optionCell.setPadding(0f);
        contentHelper.populateCellWithHtml(optionCell, optionText, fontWithColor(context.bodyFont(), TEXT_PRIMARY), context.isMathJaxEnabled(), context.getFontSizeSetting());
        contentHelper.styleAnswerCell(optionCell, 15f);
        if (questionType.equals(TypeIfc.MULTIPLE_CORRECT)) {
            optionCell.setCellEvent(new CheckboxCellEvent(false));
        } else {
            optionCell.setCellEvent(new CircleCellEvent(false));
        }
        optionTable.addCell(optionCell);
        document.add(optionTable);

        if (Boolean.TRUE.equals(printSettings.getShowKeysFeedback())
                && StringUtils.isNotBlank(choice.getGeneralAnswerFeedback())) {
            addPrintHtmlFeedback(document, context,
                    AssessmentPdfBundle.getCommonString("feedback") + ": ",
                    choice.getGeneralAnswerFeedback());
        }
    }

    private void renderPrintMatrix(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question) throws Exception {
        List<AssessmentPdfMatrixRowModel> matrixRows = question.getMatrixRows();
        List<Integer> columnIndexes = question.getColumnIndexes();
        String[] columnLabels = question.getColumnLabels();

        if (columnLabels == null || columnIndexes == null || matrixRows == null || matrixRows.isEmpty()) {
            return;
        }

        PdfPTable matrixTable = new PdfPTable(columnIndexes.size() + 1);
        matrixTable.setWidthPercentage(100f);
        matrixTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        contentHelper.configureSplittableTable(matrixTable);

        PdfPCell cornerCell = new PdfPCell(new Paragraph(""));
        cornerCell.setPadding(8f);
        cornerCell.setBackgroundColor(BACKGROUND_GRAY);
        cornerCell.setBorder(Rectangle.NO_BORDER);
        cornerCell.setBorderWidthBottom(1.5f);
        cornerCell.setBorderColorBottom(BORDER_COLOR);
        matrixTable.addCell(cornerCell);

        for (String columnLabel : columnLabels) {
            PdfPCell matrixHeaderCell = new PdfPCell(new Paragraph(columnLabel, fontWithColor(context.bodyBoldFont(), TEXT_PRIMARY)));
            matrixHeaderCell.setPadding(8f);
            matrixHeaderCell.setBackgroundColor(BACKGROUND_GRAY);
            matrixHeaderCell.setBorder(Rectangle.NO_BORDER);
            matrixHeaderCell.setBorderWidthBottom(1.5f);
            matrixHeaderCell.setBorderColorBottom(BORDER_COLOR);
            matrixHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            matrixTable.addCell(matrixHeaderCell);
        }

        for (AssessmentPdfMatrixRowModel matrixRow : matrixRows) {
            PdfPCell rowLabelCell = new PdfPCell(new Paragraph(matrixRow.getRowLabel(), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
            rowLabelCell.setPadding(8f);
            rowLabelCell.setBackgroundColor(java.awt.Color.WHITE);
            rowLabelCell.setBorder(Rectangle.NO_BORDER);
            rowLabelCell.setBorderWidthBottom(1f);
            rowLabelCell.setBorderColorBottom(BORDER_COLOR);
            matrixTable.addCell(rowLabelCell);

            for (int column = 0; column < columnIndexes.size(); column++) {
                PdfPCell circleCell = new PdfPCell(new Paragraph(" "));
                circleCell.setPadding(0f);
                circleCell.setBackgroundColor(java.awt.Color.WHITE);
                circleCell.setBorder(Rectangle.NO_BORDER);
                circleCell.setBorderWidthBottom(1f);
                circleCell.setBorderColorBottom(BORDER_COLOR);
                circleCell.setMinimumHeight(30f);
                circleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                circleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                circleCell.setCellEvent(new CircleCellEvent(false, true));
                matrixTable.addCell(circleCell);
            }
        }
        document.add(matrixTable);

        if (question.isMatrixAddComment() && StringUtils.isNotBlank(question.getMatrixCommentField())) {
            Paragraph commentPrompt = new Paragraph(question.getMatrixCommentField(), fontWithColor(context.bodyFont(), TEXT_PRIMARY));
            commentPrompt.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
            document.add(commentPrompt);

            PdfPTable commentTable = new PdfPTable(1);
            commentTable.setWidthPercentage(100f);
            commentTable.setSpacingBefore(4f);
            PdfPCell commentCell = new PdfPCell(new Paragraph(" ", fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
            commentCell.setPadding(36f);
            commentCell.setBackgroundColor(BACKGROUND_GRAY);
            commentCell.setBorder(Rectangle.NO_BORDER);
            commentTable.addCell(commentCell);
            document.add(commentTable);
        }
    }

    private void renderPrintMatching(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question) throws Exception {
        PdfPTable matchingTable = new PdfPTable(new float[]{1f, 1f});
        matchingTable.setWidthPercentage(100f);
        matchingTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        contentHelper.configureSplittableTable(matchingTable);
        List<AssessmentPdfMatchingRowModel> matchingRows = question.getMatchingRows();
        List<String> matchingResponses = question.getMatchingResponses();
        for (int k = 0; k < matchingRows.size(); k++) {
            AssessmentPdfMatchingRowModel matching = matchingRows.get(k);
            String answer = "";
            if (k < matchingResponses.size()) {
                answer = matchingResponses.get(k);
            }
            if (matching.getText() == null) {
                break;
            }
            PdfPCell leftCell = new PdfPCell(new Paragraph(matching.getText(), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPadding(4f);
            matchingTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell(new Paragraph(StringUtils.isNotBlank(answer) ? answer : "________________", fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPadding(4f);
            matchingTable.addCell(rightCell);
        }
        document.add(matchingTable);
    }

    private void renderPrintExtendedMatching(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question) throws Exception {
        if (StringUtils.isNotBlank(question.getThemeText())) {
            document.add(new Paragraph(question.getThemeText(), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
        }

        if (question.isEmiAnswerOptionsSimple()) {
            for (AssessmentPdfChoiceOptionModel option : question.getEmiAnswerOptions()) {
                document.add(new Paragraph(option.getLabel() + ". " + option.getValue(), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
            }
        }
        if (question.isEmiAnswerOptionsRich()) {
            contentHelper.addQuestionTitleToDocument(document, question.getEmiAnswerOptionsRichText(), true, context.isMathJaxEnabled(), context.getFontSizeSetting());
        }

        if (StringUtils.isNotBlank(question.getLeadInText())) {
            document.add(new Paragraph(question.getLeadInText(), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
        }

        for (AssessmentPdfEmiPromptModel prompt : question.getEmiPrompts()) {
            document.add(new Paragraph(prompt.getSequence() + ". " + prompt.getText() + "  ____", fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
        }
    }

    private void renderPrintImageMapOptions(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question) throws Exception {
        List<String> itemTexts = question.getImageMapItemTexts();
        for (int k = 0; k < itemTexts.size(); k++) {
            document.add(new Paragraph((k + 1) + ". " + itemTexts.get(k), fontWithColor(context.bodyFont(), TEXT_PRIMARY)));
        }

        String imsrc = question.getImageMapSrc();
        if (StringUtils.isBlank(imsrc)) {
            return;
        }
        Optional<Image> loadedImage = contentHelper.loadContentImage(AssessmentPdfContentHelper.resolveContentResourceId(imsrc));
        if (loadedImage.isEmpty()) {
            log.warn("Could not load image map image [{}] for the printable assessment", imsrc);
            return;
        }
        Image image = loadedImage.get();
        contentHelper.scaleImageForPage(image);
        PdfPTable imageTable = new PdfPTable(1);
        imageTable.setWidthPercentage(100f);
        contentHelper.configureSplittableTable(imageTable);
        PdfPCell imageCell = new PdfPCell(image);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageTable.addCell(imageCell);
        document.add(imageTable);
    }

    private void renderPrintAnswerKey(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType, AssessmentPdfPrintSettingsModel printSettings) throws Exception {
        Double itemScore = question.getItemScore();
        Paragraph keyParagraph = new Paragraph();
        keyParagraph.setLeading(0f, 1.2f);
        keyParagraph.add(new Chunk(AssessmentPdfBundle.getPrintString("answer_point") + ": " + (itemScore == null ? 0d : itemScore) + " " + AssessmentPdfBundle.getAuthorString("points_lower_case") + "\n", fontWithColor(context.smallBoldFont(), SECONDARY_COLOR)));

        if (!PRINT_NO_KEY_TYPES.contains(questionType)) {
            appendPrintAnswerKey(keyParagraph, context, question, questionType);
        }

        contentHelper.addInfoBox(document, BACKGROUND_GRAY, SECONDARY_COLOR, keyParagraph);

        if (Boolean.TRUE.equals(printSettings.getShowKeysFeedback())) {
            renderPrintAnswerKeyFeedback(document, context, question, questionType);
        }
    }

    private void appendPrintAnswerKey(Paragraph keyParagraph, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) {
        if (Objects.equals(questionType, TypeIfc.ESSAY_QUESTION)) {
            if (StringUtils.isNotBlank(question.getKey()) && !StringUtils.equals(question.getKey(), "null")) {
                keyParagraph.add(new Chunk(AssessmentPdfBundle.getPrintString("answer_model") + ": ", fontWithColor(context.smallBoldFont(), SECONDARY_COLOR)));
                keyParagraph.add(new Chunk(question.getKey(), fontWithColor(context.smallFont(), TEXT_PRIMARY)));
            }
        } else if (Objects.equals(questionType, TypeIfc.FILL_IN_BLANK)
                || Objects.equals(questionType, TypeIfc.FILL_IN_NUMERIC)
                || Objects.equals(questionType, TypeIfc.MATCHING)) {
            if (StringUtils.isNotBlank(question.getKey())) {
                keyParagraph.add(new Chunk(AssessmentPdfBundle.getPrintString("answer_key") + ": " + question.getKey(), fontWithColor(context.smallFont(), TEXT_PRIMARY)));
            }
        } else if (Objects.equals(questionType, TypeIfc.CALCULATED_QUESTION)) {
            if (StringUtils.isNotBlank(question.getAnswerKeyCalcQuestion())) {
                keyParagraph.add(new Chunk(AssessmentPdfBundle.getPrintString("answer_key") + ": " + question.getAnswerKeyCalcQuestion().replace("<", "&lt;").replace(">", "&gt;") + " = " + question.getCalculatedQuestionAnswer(), fontWithColor(context.smallFont(), TEXT_PRIMARY)));
            }
        } else if (StringUtils.isNotBlank(question.getItemAnswerKey())) {
            keyParagraph.add(new Chunk(AssessmentPdfBundle.getPrintString("answer_key") + ": " + question.getItemAnswerKey(), fontWithColor(context.smallFont(), TEXT_PRIMARY)));
        }
    }

    private void renderPrintAnswerKeyFeedback(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        if (Objects.equals(questionType, TypeIfc.ESSAY_QUESTION)
                || Objects.equals(questionType, TypeIfc.AUDIO_RECORDING)
                || Objects.equals(questionType, TypeIfc.FILE_UPLOAD)) {
            addPrintHtmlFeedback(document, context,
                    AssessmentPdfBundle.getCommonString("feedback") + ": ",
                    question.getGeneralItemFeedback());
        }

        if (PRINT_FEEDBACK_TYPES.contains(questionType)) {
            addPrintHtmlFeedback(document, context,
                    AssessmentPdfBundle.getPrintString("correct_feedback") + ": ",
                    question.getCorrectItemFeedback());
            addPrintHtmlFeedback(document, context,
                    AssessmentPdfBundle.getPrintString("incorrect_feedback") + ": ",
                    question.getIncorrectItemFeedback());
        }
    }

    private void addPrintHtmlFeedback(Document document, QuestionRenderContext context, String label, String html) throws Exception {
        if (StringUtils.isBlank(html)) {
            return;
        }
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(4f);
        contentHelper.configureSplittableTable(table);

        PdfPCell cell = contentHelper.createInfoBoxCell(BACKGROUND_GRAY, SECONDARY_COLOR);
        contentHelper.configureSplittableCell(cell);
        Paragraph labelPar = new Paragraph(label, fontWithColor(context.smallBoldFont(), SECONDARY_COLOR));
        labelPar.setSpacingAfter(4f);
        cell.addElement(labelPar);
        contentHelper.populateCellWithHtml(cell, html, fontWithColor(context.smallFont(), TEXT_PRIMARY), context.isMathJaxEnabled(), context.getFontSizeSetting());
        table.addCell(cell);
        document.add(table);
    }

    // --- Student report flow ---

    private void renderStudentReport(Document document, QuestionRenderContext context) throws Exception {
        AssessmentPdfQuestionModel question = context.getQuestion();
        Long questionType = context.getQuestionType();

        PdfPTable questionTable = new PdfPTable(new float[]{3f, 1f});
        questionTable.setWidthPercentage(100f);
        questionTable.setSpacingBefore(16f);
        contentHelper.configureSplittableTable(questionTable);

        PdfPCell questionNumCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getEvaluationString("question") + " " + context.getQuestionNumber() + " / " + context.getTotalQuestions(), fontWithColor(AssessmentPdfStyle.BODY_BOLD_FONT, TEXT_PRIMARY)));
        contentHelper.styleQuestionHeaderCell(questionNumCell, false);
        questionTable.addCell(questionNumCell);

        PdfPCell scoreHeaderCell = new PdfPCell(new Paragraph(context.getScoreFormat().format(question.getPoints()) + " / " + context.getScoreFormat().format(question.getMaxPoints()) + " pts", fontWithColor(AssessmentPdfStyle.BODY_BOLD_FONT, TEXT_PRIMARY)));
        contentHelper.styleQuestionHeaderCell(scoreHeaderCell, true);
        questionTable.addCell(scoreHeaderCell);
        document.add(questionTable);

        if (Objects.equals(questionType, TypeIfc.FILL_IN_NUMERIC)
                || Objects.equals(questionType, TypeIfc.CALCULATED_QUESTION)
                || Objects.equals(questionType, TypeIfc.FILL_IN_BLANK)) {
            contentHelper.processFillInQuestion(document, !questionType.equals(TypeIfc.FILL_IN_BLANK) ? question.getFinRows() : question.getFibRows(), !questionType.equals(TypeIfc.FILL_IN_BLANK), context.isMathJaxEnabled());
        } else {
            contentHelper.addQuestionTitleToDocument(document, question.getText(), true, context.isMathJaxEnabled(), null);
        }

        renderReportTypeSpecificContent(document, context, question, questionType);
        renderReportCorrectAnswer(document, context, question, questionType);
        renderReportCommentsAndFeedback(document, context, question, questionType);
    }

    private void renderReportTypeSpecificContent(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        if (Objects.equals(questionType, TypeIfc.ESSAY_QUESTION)) {
            PdfPTable responseTable = new PdfPTable(1);
            responseTable.setWidthPercentage(100f);
            responseTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
            String responseText = question.getResponseText() != null
                    ? contentHelper.cleanText(question.getResponseText())
                    : AssessmentPdfBundle.getEvaluationString("no_answer");
            PdfPCell responseCell = new PdfPCell(new Paragraph(responseText, fontWithColor(BODY_FONT, TEXT_PRIMARY)));
            responseCell.setPadding(8f);
            responseCell.setBackgroundColor(BACKGROUND_GRAY);
            responseCell.setBorder(Rectangle.NO_BORDER);
            responseTable.addCell(responseCell);
            document.add(responseTable);
        }

        if (Objects.equals(questionType, TypeIfc.FILE_UPLOAD)) {
            renderReportFileUpload(document, question);
        } else if (Objects.equals(questionType, TypeIfc.AUDIO_RECORDING)) {
            renderReportAudioRecording(document, question);
        }

        renderReportMatrix(document, question, questionType);
        renderReportImageMap(document, question, questionType);
        renderReportChoiceAnswers(document, context, question, questionType);
        renderReportMatchingItems(document, context, question, questionType);
    }

    private void renderReportFileUpload(Document document, AssessmentPdfQuestionModel question) throws Exception {
        if (!question.getMediaItems().isEmpty()) {
            contentHelper.addMediaFileListToDocument(document, question.getMediaItems());
            return;
        }

        PdfPTable attachmentTable = new PdfPTable(1);
        attachmentTable.setWidthPercentage(100f);
        attachmentTable.setSpacingBefore(12f);
        contentHelper.configureSplittableTable(attachmentTable);

        PdfPCell noFileCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getEvaluationString("no_attachments_yet"), fontWithColor(BODY_ITALIC_FONT, TEXT_SECONDARY)));
        noFileCell.setPadding(8f);
        noFileCell.setBackgroundColor(BACKGROUND_GRAY);
        noFileCell.setBorder(Rectangle.NO_BORDER);
        contentHelper.configureSplittableCell(noFileCell);
        attachmentTable.addCell(noFileCell);
        document.add(attachmentTable);
    }

    private void renderReportAudioRecording(Document document, AssessmentPdfQuestionModel question) {
        PdfPTable audioTable = new PdfPTable(1);
        audioTable.setWidthPercentage(100f);
        audioTable.setSpacingBefore(12f);

        PdfPCell audioCell;
        if (!question.getMediaItems().isEmpty()) {
            audioCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getEvaluationString("alt_recording") + " - " + AssessmentPdfBundle.getEvaluationString("submitted"), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
            audioCell.setBackgroundColor(SUCCESS_BG);
        } else {
            audioCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getEvaluationString("no_answer"), fontWithColor(BODY_ITALIC_FONT, TEXT_SECONDARY)));
            audioCell.setBackgroundColor(BACKGROUND_GRAY);
        }
        audioCell.setPadding(8f);
        audioCell.setBorder(Rectangle.NO_BORDER);
        audioTable.addCell(audioCell);
        document.add(audioTable);
    }

    private void renderReportMatrix(Document document, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        List<AssessmentPdfMatrixRowModel> matrixRows = question.getMatrixRows();
        List<Integer> columnsIndex = question.getColumnIndexes();
        String[] columns = question.getColumnLabels();

        if (columns == null || columnsIndex == null || matrixRows == null || matrixRows.isEmpty()) {
            return;
        }

        PdfPTable matrixTable = new PdfPTable(columnsIndex.size() + 1);
        matrixTable.setWidthPercentage(100f);
        matrixTable.setSpacingBefore(12f);
        contentHelper.configureSplittableTable(matrixTable);

        PdfPCell cornerCell = new PdfPCell(new Paragraph(""));
        cornerCell.setPadding(8f);
        cornerCell.setBackgroundColor(BACKGROUND_GRAY);
        cornerCell.setBorder(Rectangle.NO_BORDER);
        cornerCell.setBorderWidthBottom(1.5f);
        cornerCell.setBorderColorBottom(BORDER_COLOR);
        matrixTable.addCell(cornerCell);

        for (String column : columns) {
            PdfPCell matrixHeaderCell = new PdfPCell(new Paragraph(column, fontWithColor(AssessmentPdfStyle.BODY_BOLD_FONT, TEXT_PRIMARY)));
            matrixHeaderCell.setPadding(8f);
            matrixHeaderCell.setBackgroundColor(BACKGROUND_GRAY);
            matrixHeaderCell.setBorder(Rectangle.NO_BORDER);
            matrixHeaderCell.setBorderWidthBottom(1.5f);
            matrixHeaderCell.setBorderColorBottom(BORDER_COLOR);
            matrixHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            matrixTable.addCell(matrixHeaderCell);
        }

        for (AssessmentPdfMatrixRowModel matrix : matrixRows) {
            if (Objects.equals(questionType, TypeIfc.MATRIX_CHOICES_SURVEY)) {
                PdfPCell rowLabelCell = new PdfPCell(new Paragraph(matrix.getRowLabel(), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                rowLabelCell.setPadding(8f);
                rowLabelCell.setBackgroundColor(java.awt.Color.WHITE);
                rowLabelCell.setBorder(Rectangle.NO_BORDER);
                rowLabelCell.setBorderWidthBottom(1f);
                rowLabelCell.setBorderColorBottom(BORDER_COLOR);
                matrixTable.addCell(rowLabelCell);

                for (String answer : matrix.getAnswerIds()) {
                    PdfPCell circleCell = new PdfPCell(new Paragraph(" "));
                    circleCell.setPadding(0f);
                    circleCell.setBackgroundColor(java.awt.Color.WHITE);
                    circleCell.setBorder(Rectangle.NO_BORDER);
                    circleCell.setBorderWidthBottom(1f);
                    circleCell.setBorderColorBottom(BORDER_COLOR);
                    circleCell.setMinimumHeight(30f);
                    circleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    circleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    circleCell.setCellEvent(new CircleCellEvent(StringUtils.equals(answer, matrix.getResponseId()), true));
                    matrixTable.addCell(circleCell);
                }
            }
        }
        document.add(matrixTable);
    }

    private void renderReportImageMap(Document document, AssessmentPdfQuestionModel question, Long questionType)
            throws Exception {
        if (!Objects.equals(questionType, TypeIfc.IMAGEMAP_QUESTION) || StringUtils.isBlank(question.getImageSrc())) {
            return;
        }
        // Read the image straight out of content hosting rather than fetching our own server over
        // HTTP: a self request has no session, so a non-public image would come back denied.
        Optional<Image> loadedImage = contentHelper.loadContentImage(AssessmentPdfContentHelper.resolveContentResourceId(question.getImageSrc()));
        if (loadedImage.isEmpty()) {
            log.warn("Could not load image map image [{}] for the student report", question.getImageSrc());
            return;
        }
        Image image = loadedImage.get();
        contentHelper.scaleImageForPage(image);
        PdfPTable tableImage = new PdfPTable(1);
        tableImage.setWidthPercentage(100f);
        contentHelper.configureSplittableTable(tableImage);
        PdfPCell cellImage = new PdfPCell();
        cellImage.setBorderWidth(0);
        cellImage.setPadding(0);
        cellImage.addElement(image);

        List<Rectangle> answerRectangles = new ArrayList<>();
        for (AssessmentPdfSelectionAnswerModel answer : question.getSelectionAnswers()) {
            String plainTextAnswer = answer.getPlainTextAnswer();
            if (StringUtils.isBlank(plainTextAnswer) || Strings.CI.equals(plainTextAnswer, "undefined")) {
                continue;
            }
            try {
                JsonNode jsonNode = jsonMapper.readTree(plainTextAnswer);
                // path() rather than get() so an absent coordinate is a MissingNode rather than a
                // null that would abort the whole report, and asDouble so a numeric string coerces
                // instead of silently collapsing the region to the origin
                answerRectangles.add(new Rectangle((float) jsonNode.path("x1").asDouble(), (float) jsonNode.path("y1").asDouble(),
                        (float) jsonNode.path("x2").asDouble(), (float) jsonNode.path("y2").asDouble()));
            } catch (JsonProcessingException e) {
                log.warn("Skipping unparseable image map answer rectangle [{}], {}", plainTextAnswer, e.toString());
            }
        }

        List<AssessmentPdfItemGradingModel> itemsGrading = question.getItemGradingData();
        List<ImageMapCircle> answerCircles = new ArrayList<>();
        for (AssessmentPdfItemGradingModel itemGrading : itemsGrading) {
            String answerText = itemGrading.getAnswerText();
            Long publishedItemTextId = itemGrading.getPublishedItemTextId();
            if (publishedItemTextId != null && StringUtils.isNotBlank(itemGrading.getAnswerText())) {
                try {
                    JsonNode jsonNode = jsonMapper.readTree(answerText);
                    // asDouble coerces a numeric string and yields 0 for anything absent or
                    // non-numeric, which is the origin fallback these coordinates already wanted
                    float x = (float) jsonNode.path("x").asDouble();
                    float y = (float) jsonNode.path("y").asDouble();
                    answerCircles.add(new ImageMapCircle(x, y, publishedItemTextId.intValue()));
                } catch (JsonProcessingException e) {
                    log.warn("Skipping unparseable image map response for published item text {} [{}], {}", publishedItemTextId, answerText, e.toString());
                }
            }
        }
        cellImage.setCellEvent(new ImageMapQuestionCellEvent(answerCircles, answerRectangles, image.getWidth(), image.getHeight()));
        contentHelper.configureSplittableCell(cellImage);
        tableImage.addCell(cellImage);
        document.add(tableImage);
    }

    private void renderReportChoiceAnswers(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        if (Objects.equals(questionType, TypeIfc.MULTIPLE_CHOICE)
                || Objects.equals(questionType, TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)
                || Objects.equals(questionType, TypeIfc.MULTIPLE_CHOICE_SURVEY)
                || Objects.equals(questionType, TypeIfc.MULTIPLE_CORRECT)
                || Objects.equals(questionType, TypeIfc.TRUE_FALSE)) {
            boolean firstChoice = true;
            for (AssessmentPdfSelectionAnswerModel answer : question.getSelectionAnswers()) {
                PdfPTable multipleTable = new PdfPTable(1);
                multipleTable.setWidthPercentage(100f);
                if (firstChoice) {
                    multipleTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
                }
                multipleTable.setSpacingAfter(4f);
                firstChoice = false;
                contentHelper.configureSplittableTable(multipleTable);

                PdfPCell multipleCell = new PdfPCell();
                if (questionType.equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) {
                    String answerText = StringUtils.defaultString(answer.getText());
                    if (answerText.matches("-?\\d+")) {
                        multipleCell.setPhrase(new Paragraph("  " + answerText, fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                    } else {
                        multipleCell.setPhrase(new Paragraph("  " + AssessmentPdfSurveyText.toDisplayText(contentHelper.cleanText(answerText)), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                    }
                } else if (questionType.equals(TypeIfc.TRUE_FALSE)) {
                    String trueFalseText = StringUtils.defaultIfBlank(answer.getLabel(), answer.getText());
                    multipleCell.setPhrase(new Paragraph("  " + trueFalseText, fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                } else {
                    String answerHtml = "  " + answer.getLabel() + ". " + answer.getText();
                    contentHelper.populateCellWithHtml(multipleCell, answerHtml, fontWithColor(BODY_FONT, TEXT_PRIMARY), context.isMathJaxEnabled(), null);
                }
                contentHelper.styleAnswerCell(multipleCell, 15f);
                if (questionType.equals(TypeIfc.MULTIPLE_CORRECT)) {
                    multipleCell.setCellEvent(new CheckboxCellEvent(answer.isSelected()));
                } else {
                    multipleCell.setCellEvent(new CircleCellEvent(answer.isSelected()));
                }
                PdfPCell finalCell = new PdfPCell(multipleCell);
                if (answer.getCorrect() != null && answer.isSelected()) {
                    finalCell.setCellEvent(new CheckOrCrossCellEvent(answer.getCorrect()));
                }
                multipleTable.addCell(finalCell);
                document.add(multipleTable);
            }
        } else if (Objects.equals(questionType, TypeIfc.MATCHING)
                || Objects.equals(questionType, TypeIfc.EXTENDED_MATCHING_ITEMS)) {
            for (String answer : question.getMatchingResponses()) {
                if (StringUtils.isBlank(answer)) {
                    continue;
                }
                PdfPTable matchingAnswerTable = new PdfPTable(1);
                matchingAnswerTable.setWidthPercentage(100f);
                contentHelper.configureSplittableTable(matchingAnswerTable);
                PdfPCell matchingAnswerCell = new PdfPCell(new Paragraph(answer, fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                contentHelper.styleAnswerCell(matchingAnswerCell, 4f);
                matchingAnswerTable.addCell(matchingAnswerCell);
                document.add(matchingAnswerTable);
            }
        }
    }

    private void renderReportMatchingItems(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        PdfPTable matchingTable = new PdfPTable(1);
        matchingTable.setWidthPercentage(100f);
        contentHelper.configureSplittableTable(matchingTable);
        int cellsAdded = 0;

        if (Objects.equals(questionType, TypeIfc.IMAGEMAP_QUESTION)) {
            for (AssessmentPdfImageMapRowModel imageMapRow : question.getImageMapRows()) {
                if (StringUtils.isBlank(imageMapRow.getText())) {
                    continue;
                }
                PdfPCell matchingCell = new PdfPCell(new Phrase(AssessmentPdfBundle.getAuthorString("item") + " " + imageMapRow.getText(), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                contentHelper.styleAnswerCell(matchingCell, 4f);
                if (imageMapRow.getCorrect() != null) {
                    matchingCell.setCellEvent(new CheckOrCrossCellEvent(imageMapRow.getCorrect()));
                }
                matchingTable.addCell(matchingCell);
                cellsAdded++;
            }
            if (cellsAdded > 0) {
                document.add(matchingTable);
            }
            return;
        }

        if (Objects.equals(questionType, TypeIfc.MATCHING)
                || Objects.equals(questionType, TypeIfc.EXTENDED_MATCHING_ITEMS)) {
            for (AssessmentPdfMatchingRowModel matchingItem : question.getMatchingRows()) {
                if (StringUtils.isBlank(matchingItem.getText())) {
                    continue;
                }
                boolean responseFound = false;
                for (AssessmentPdfChoiceOptionModel choice : matchingItem.getChoices()) {
                    if (matchingItem.getResponse() != null && matchingItem.getResponse().equals(choice.getValue())) {
                        PdfPCell matchingCell = new PdfPCell(new Phrase(choice.getLabel() + " ··> " + matchingItem.getText(), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                        contentHelper.styleAnswerCell(matchingCell, 4f);
                        if (matchingItem.getCorrect() != null) {
                            matchingCell.setCellEvent(new CheckOrCrossCellEvent(matchingItem.getCorrect()));
                        }
                        matchingTable.addCell(matchingCell);
                        cellsAdded++;
                        responseFound = true;
                        break;
                    }
                }
                if (!responseFound) {
                    PdfPCell matchingCell = new PdfPCell(new Phrase(matchingItem.getText(), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
                    contentHelper.styleAnswerCell(matchingCell, 4f);
                    matchingTable.addCell(matchingCell);
                    cellsAdded++;
                }
            }
        }
        if (cellsAdded > 0) {
            document.add(matchingTable);
        }
    }

    private void renderReportCorrectAnswer(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        if (Objects.equals(questionType, TypeIfc.CALCULATED_QUESTION)
                || Objects.equals(questionType, TypeIfc.FILL_IN_BLANK)
                || Objects.equals(questionType, TypeIfc.FILL_IN_NUMERIC)) {
            contentHelper.addCorrectResponseBox(document, question.getKey());
        } else if (Objects.equals(questionType, TypeIfc.ESSAY_QUESTION)) {
            if (question.isModelAnswerPresent()) {
                Paragraph modelPar = new Paragraph();
                modelPar.setLeading(0f, 1.2f);
                modelPar.add(new Chunk(AssessmentPdfBundle.getEvaluationString("model"), fontWithColor(AssessmentPdfStyle.SMALL_BOLD_FONT, PRIMARY_COLOR)));
                modelPar.add(new Chunk(question.getKey(), fontWithColor(AssessmentPdfStyle.SMALL_FONT, TEXT_PRIMARY)));
                contentHelper.addInfoBox(document, INFO_BG, PRIMARY_COLOR, modelPar);
            }
        } else if (!Objects.equals(questionType, TypeIfc.MULTIPLE_CHOICE_SURVEY)
                && !Objects.equals(questionType, TypeIfc.MATRIX_CHOICES_SURVEY)
                && !Objects.equals(questionType, TypeIfc.FILE_UPLOAD)
                && !Objects.equals(questionType, TypeIfc.IMAGEMAP_QUESTION)
                && !Objects.equals(questionType, TypeIfc.AUDIO_RECORDING)) {
            contentHelper.addCorrectResponseBox(document, question.getAnswerKeyTf());
        }
    }

    private void renderReportCommentsAndFeedback(Document document, QuestionRenderContext context, AssessmentPdfQuestionModel question, Long questionType) throws Exception {
        if (!question.isGradingCommentPresent() && !question.isFeedbackPresent()) {
            return;
        }

        PdfPTable commentTable = new PdfPTable(1);
        commentTable.setWidthPercentage(100f);
        commentTable.setSpacingBefore(12f);
        contentHelper.configureSplittableTable(commentTable);

        if (question.isGradingCommentPresent()) {
            PdfPCell commentCell = contentHelper.createInfoBoxCell(WARNING_BG, WARNING_COLOR);
            Paragraph commentPar = new Paragraph();
            commentPar.setLeading(0f, 1.2f);
            commentPar.add(new Chunk(AssessmentPdfBundle.getEvaluationString("comment") + ": ", fontWithColor(AssessmentPdfStyle.SMALL_BOLD_FONT, WARNING_COLOR)));
            commentPar.add(contentHelper.createLatexParagraph(contentHelper.cleanText(question.getGradingComment()), fontWithColor(AssessmentPdfStyle.SMALL_FONT, TEXT_PRIMARY), context.isMathJaxEnabled()));
            commentCell.addElement(commentPar);
            commentTable.addCell(commentCell);
        }
        if (question.isFeedbackPresent()) {
            PdfPCell feedbackCell = contentHelper.createInfoBoxCell(FEEDBACK_BG, SECONDARY_COLOR);
            Paragraph feedbackPar = new Paragraph();
            feedbackPar.setLeading(0f, 1.2f);
            feedbackPar.add(new Chunk(AssessmentPdfBundle.getAuthorString("generalItemFeedback") + ": ", fontWithColor(AssessmentPdfStyle.SMALL_BOLD_FONT, SECONDARY_COLOR)));
            if (Objects.equals(questionType, TypeIfc.CALCULATED_QUESTION)) {
                feedbackPar.add(contentHelper.createLatexParagraph(contentHelper.cleanText(question.getFeedbackValue()), fontWithColor(AssessmentPdfStyle.SMALL_FONT, TEXT_PRIMARY), context.isMathJaxEnabled()));
            } else {
                feedbackPar.add(contentHelper.createLatexParagraph(contentHelper.cleanText(question.getFeedback()), fontWithColor(AssessmentPdfStyle.SMALL_FONT, TEXT_PRIMARY), context.isMathJaxEnabled()));
            }
            feedbackCell.addElement(feedbackPar);
            commentTable.addCell(feedbackCell);
        }
        document.add(commentTable);
    }
}
