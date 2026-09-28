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

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfPartModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BODY_BOLD_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BODY_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BORDER_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.HEADING_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.HEADING_NORMAL_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.INFO_BG;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.PRIMARY_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SMALL_BOLD_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SMALL_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TEXT_PRIMARY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TITLE_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.fontWithColor;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.scaledFont;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Renders complete Samigo assessment PDF documents (printable and student report).
 */
public class AssessmentPdfDocumentRenderer {

    private final AssessmentPdfContentHelper contentHelper;
    private final AssessmentPdfQuestionRenderer questionRenderer;

    public AssessmentPdfDocumentRenderer(AssessmentPdfContentHelper contentHelper, AssessmentPdfQuestionRenderer questionRenderer) {
        this.contentHelper = contentHelper;
        this.questionRenderer = questionRenderer;
    }

    public void renderPrintable(Document document, AssessmentPrintPdfModel model) throws Exception {
        renderPrintCover(document, model);

        int questionNumber = 0;
        int totalQuestions = model.totalQuestions();
        AssessmentPdfPrintSettingsModel printSettings = model.getPrintSettings();
        boolean firstPart = true;

        List<AssessmentPdfPartModel> parts = model.getParts();
        for (int partIndex = 0; partIndex < parts.size(); partIndex++) {
            AssessmentPdfPartModel section = parts.get(partIndex);
            if (!firstPart && (printSettings == null || !Boolean.TRUE.equals(printSettings.getShowSamePage()))) {
                document.newPage();
            }
            firstPart = false;

            renderPrintPartIntro(document, model, section, partIndex);

            for (AssessmentPdfQuestionModel item : section.getQuestions()) {
                questionNumber++;
                QuestionRenderContext questionContext = QuestionRenderContext.forPrint(item, questionNumber, totalQuestions, model, contentHelper);
                questionRenderer.render(document, questionContext);
            }
        }
    }

    public void renderStudentReport(Document document, AssessmentStudentReportPdfModel model) throws Exception {
        renderReportHeader(document, model);

        int index = 0;
        int totalQuestions = model.totalQuestions();
        DecimalFormat scoreFormat = AssessmentPdfLocaleSupport.scoreFormat(model.getLocale());

        for (AssessmentPdfPartModel deliveryPart : model.getParts()) {
            List<AssessmentPdfQuestionModel> items = deliveryPart.getQuestions();
            document.newPage();
            renderReportPartHeader(document, model, deliveryPart, scoreFormat);
            index = renderReportPartSummaryTable(document, model, items, index, scoreFormat);

            int questionStartIndex = index - items.size();
            for (AssessmentPdfQuestionModel item : items) {
                questionStartIndex++;
                QuestionRenderContext questionContext = QuestionRenderContext.forReport(item, questionStartIndex, totalQuestions, model, contentHelper);
                questionRenderer.render(document, questionContext);
            }
        }
    }

    private void renderPrintCover(Document document, AssessmentPrintPdfModel model) throws Exception {
        String fontSize = model.getPrintSettings() == null ? null : model.getPrintSettings().getFontSize();

        Paragraph identityBlock = new Paragraph();
        identityBlock.setLeading(0f, 1.2f);
        identityBlock.setSpacingBefore(0f);
        identityBlock.setSpacingAfter(AssessmentPdfStyle.SECTION_SPACING);
        identityBlock.add(new Chunk(AssessmentPdfBundle.getPrintString("print_name_form", model.getLocale()) + "\n",
                fontWithColor(scaledFont(BODY_FONT, fontSize), TEXT_PRIMARY)));
        identityBlock.add(new Chunk(AssessmentPdfBundle.getPrintString("print_score_form", model.getLocale()),
                fontWithColor(scaledFont(BODY_FONT, fontSize), TEXT_PRIMARY)));
        document.add(identityBlock);

        boolean hasIntro = StringUtils.isNotBlank(model.getIntroHtml());
        Paragraph title = new Paragraph(model.getTitle(), fontWithColor(scaledFont(TITLE_FONT, fontSize), TEXT_PRIMARY));
        title.setLeading(0f, 1.2f);
        title.setSpacingBefore(0f);
        title.setSpacingAfter(hasIntro ? 0f : AssessmentPdfStyle.SECTION_SPACING);
        document.add(title);

        if (hasIntro) {
            PdfPTable introTable = contentHelper.getQuestionTitle(model.getIntroHtml(), true, model.isMathJaxEnabled(), fontSize);
            if (introTable != null) {
                introTable.setWidthPercentage(100f);
                introTable.setSpacingBefore(AssessmentPdfStyle.SECTION_SPACING);
                introTable.setSpacingAfter(AssessmentPdfStyle.SECTION_SPACING);
                document.add(introTable);
            }
        }
    }

    private void renderPrintPartIntro(Document document, AssessmentPrintPdfModel model, AssessmentPdfPartModel section, int partIndex) throws Exception {
        AssessmentPdfPrintSettingsModel printSettings = model.getPrintSettings();
        String fontSize = printSettings == null ? null : printSettings.getFontSize();

        Paragraph partHeader = new Paragraph();
        partHeader.setLeading(0f, 1.2f);
        partHeader.add(new Chunk(AssessmentPdfBundle.getAuthorString("p", model.getLocale()) + " " + (partIndex + 1), fontWithColor(scaledFont(HEADING_FONT, fontSize), PRIMARY_COLOR)));

        if (partIndex > 0 && printSettings != null && Boolean.TRUE.equals(printSettings.getShowSamePage())) {
            partHeader.setSpacingBefore(AssessmentPdfStyle.SECTION_SPACING);
        } else {
            partHeader.setSpacingBefore(0f);
        }

        boolean showIntros = printSettings != null && Boolean.TRUE.equals(printSettings.getShowPartIntros());
        boolean hasDescription = showIntros && StringUtils.isNotBlank(section.getDescription());
        partHeader.setSpacingAfter(hasDescription ? 0f : AssessmentPdfStyle.SECTION_SPACING);

        if (showIntros) {
            String nonDefaultTitle = section.getNonDefaultTitle();
            if (nonDefaultTitle != null) {
                partHeader.add(new Chunk(": " + nonDefaultTitle, fontWithColor(scaledFont(BODY_FONT, fontSize), TEXT_PRIMARY)));
            }
            document.add(partHeader);

            if (hasDescription) {
                PdfPTable descriptionTable = contentHelper.getQuestionTitle(section.getDescription(), true, model.isMathJaxEnabled(), fontSize);
                if (descriptionTable != null) {
                    descriptionTable.setWidthPercentage(100f);
                    descriptionTable.setSpacingBefore(AssessmentPdfStyle.SECTION_SPACING);
                    descriptionTable.setSpacingAfter(AssessmentPdfStyle.SECTION_SPACING);
                    document.add(descriptionTable);
                }
            }

            contentHelper.addAttachmentListToDocument(document, section.getAttachments(), fontSize, model.isMathJaxEnabled(), model.getLocale());
        } else {
            document.add(partHeader);
        }
    }

    private void renderReportHeader(Document document, AssessmentStudentReportPdfModel model) throws Exception {
        DecimalFormat scoreFormat = AssessmentPdfLocaleSupport.scoreFormat(model.getLocale());
        Paragraph studentNameParagraph = new Paragraph(model.getStudentName(), fontWithColor(TITLE_FONT, TEXT_PRIMARY));
        studentNameParagraph.setSpacingBefore(5f);
        studentNameParagraph.setSpacingAfter(5f);
        document.add(studentNameParagraph);

        if (model.getEmail() != null && !StringUtils.equals(model.getEmail(), "")) {
            Paragraph studentEmailParagraph = new Paragraph(model.getEmail(), fontWithColor(BODY_FONT, TEXT_PRIMARY));
            studentEmailParagraph.setSpacingAfter(10f);
            document.add(studentEmailParagraph);
        }

        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100f);
        separatorTable.setSpacingAfter(15f);
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setBorder(Rectangle.NO_BORDER);
        separatorCell.setBorderWidthBottom(1f);
        separatorCell.setBorderColorBottom(BORDER_COLOR);
        separatorCell.setFixedHeight(1f);
        separatorTable.addCell(separatorCell);
        document.add(separatorTable);

        Paragraph assessmentTitle = new Paragraph(model.getAssessmentTitle(), fontWithColor(HEADING_FONT, PRIMARY_COLOR));
        assessmentTitle.setSpacingBefore(10f);
        assessmentTitle.setSpacingAfter(8f);
        document.add(assessmentTitle);

        if (StringUtils.isNotBlank(model.getSiteTitle())) {
            Paragraph siteParagraph = new Paragraph();
            siteParagraph.add(new Chunk(model.getSiteTitle(), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
            siteParagraph.setSpacingAfter(16f);
            document.add(siteParagraph);
        }

        double currentScore = model.getCurrentScore();
        double maxScore = model.getMaxScore();
        String scorePercentageString = (maxScore == 0) ? "0" : scoreFormat.format((currentScore / maxScore) * 100);
        Paragraph scoreParagraph = new Paragraph();
        scoreParagraph.add(new Chunk(AssessmentPdfBundle.getEvaluationString("score", model.getLocale()) + ": ", fontWithColor(BODY_FONT, TEXT_PRIMARY)));
        scoreParagraph.add(new Chunk(scoreFormat.format(currentScore) + " / " + scoreFormat.format(maxScore), fontWithColor(BODY_BOLD_FONT, TEXT_PRIMARY)));
        scoreParagraph.add(new Chunk(" (" + scorePercentageString + "%)", fontWithColor(BODY_FONT, TEXT_PRIMARY)));
        scoreParagraph.setSpacingAfter(8f);
        document.add(scoreParagraph);

        if (model.hasComments()) {
            Paragraph commentsParagraph = new Paragraph();
            commentsParagraph.setLeading(0f, 1.2f);
            commentsParagraph.add(new Chunk(AssessmentPdfBundle.getEvaluationString("grader_comments", model.getLocale()) + ": ", fontWithColor(SMALL_BOLD_FONT, PRIMARY_COLOR)));
            commentsParagraph.add(new Chunk(model.getComments(), fontWithColor(SMALL_FONT, TEXT_PRIMARY)));
            contentHelper.addInfoBox(document, INFO_BG, PRIMARY_COLOR, commentsParagraph);
        }
    }

    private void renderReportPartHeader(Document document, AssessmentStudentReportPdfModel model, AssessmentPdfPartModel deliveryPart, DecimalFormat scoreFormat) throws Exception {
        String partNumber = deliveryPart.getPartNumber() == null ? "" : deliveryPart.getPartNumber();
        int questions = deliveryPart.getQuestionCount() == null ? deliveryPart.getQuestions().size() : deliveryPart.getQuestionCount();
        int unanswered = deliveryPart.getUnansweredQuestions() == null ? 0 : deliveryPart.getUnansweredQuestions();
        String answeredQuestions = String.valueOf(questions - unanswered);
        String questionsNumber = String.valueOf(questions);
        String partScore = scoreFormat.format(deliveryPart.getPoints() == null ? 0 : deliveryPart.getPoints());
        String partMaxScore = scoreFormat.format(deliveryPart.getMaxPoints() == null ? 0 : deliveryPart.getMaxPoints());

        Paragraph partHeader = new Paragraph();
        partHeader.setSpacingBefore(5f);
        partHeader.setSpacingAfter(10f);
        String nonDefaultTitle = deliveryPart.getNonDefaultTitle();
        if (StringUtils.isNotEmpty(nonDefaultTitle)) {
            partHeader.add(new Chunk(AssessmentPdfBundle.getEvaluationString("part", model.getLocale()) + " " + partNumber + ": ", fontWithColor(HEADING_FONT, PRIMARY_COLOR)));
            partHeader.add(new Chunk(nonDefaultTitle, fontWithColor(HEADING_NORMAL_FONT, TEXT_PRIMARY)));
        } else {
            partHeader.add(new Chunk(AssessmentPdfBundle.getEvaluationString("part", model.getLocale()) + " " + partNumber, fontWithColor(HEADING_FONT, PRIMARY_COLOR)));
        }
        document.add(partHeader);

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(100f);
        statsTable.setSpacingBefore(5f);
        statsTable.setSpacingAfter(10f);
        contentHelper.configureSplittableTable(statsTable);

        PdfPCell questionsCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getAuthorString("question_s_lower_case", model.getLocale()) + ": " + answeredQuestions + " / " + questionsNumber + " " + AssessmentPdfBundle.getEvaluationString("submitted", model.getLocale()), fontWithColor(BODY_FONT, TEXT_PRIMARY)));
        contentHelper.styleStatsCell(questionsCell, false);
        statsTable.addCell(questionsCell);

        PdfPCell scoreStatsCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getEvaluationString("score", model.getLocale()) + ": " + partScore + " / " + partMaxScore, fontWithColor(BODY_BOLD_FONT, TEXT_PRIMARY)));
        contentHelper.styleStatsCell(scoreStatsCell, true);
        statsTable.addCell(scoreStatsCell);
        document.add(statsTable);
    }

    private int renderReportPartSummaryTable(Document document, AssessmentStudentReportPdfModel model, List<AssessmentPdfQuestionModel> items, int index, DecimalFormat scoreFormat) throws Exception {
        PdfPTable shortSummaryTable = new PdfPTable(new float[]{3f, 1.2f, 0.8f, 1f});
        shortSummaryTable.setWidthPercentage(100f);
        shortSummaryTable.setSpacingBefore(5f);
        contentHelper.configureSplittableTable(shortSummaryTable);

        shortSummaryTable.addCell(contentHelper.createSummaryHeaderCell(AssessmentPdfBundle.getEvaluationString("question", model.getLocale()), false));
        shortSummaryTable.addCell(contentHelper.createSummaryHeaderCell(AssessmentPdfBundle.getAuthorString("type", model.getLocale()), false));
        shortSummaryTable.addCell(contentHelper.createSummaryHeaderCell(AssessmentPdfBundle.getEvaluationString("status", model.getLocale()), false));
        shortSummaryTable.addCell(contentHelper.createSummaryHeaderCell(AssessmentPdfBundle.getEvaluationString("points", model.getLocale()), true));

        for (AssessmentPdfQuestionModel item : items) {
            index++;
            PdfPTable questionTitleTable = contentHelper.getQuestionTitle(index + ". " + item.getText(), false, model.isMathJaxEnabled());
            PdfPCell questionTextCell = questionTitleTable != null
                    ? new PdfPCell(questionTitleTable)
                    : new PdfPCell(new Paragraph(index + ". " + StringUtils.defaultString(item.getText()), fontWithColor(SMALL_FONT, TEXT_PRIMARY)));
            contentHelper.styleSummaryBodyCell(questionTextCell, false);
            shortSummaryTable.addCell(questionTextCell);

            PdfPCell typeCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getAuthorString("type." + item.getTypeId(), model.getLocale()), fontWithColor(SMALL_FONT, AssessmentPdfStyle.TEXT_SECONDARY)));
            contentHelper.styleSummaryBodyCell(typeCell, false);
            shortSummaryTable.addCell(typeCell);

            String answeredText = !item.isUnanswered() ? AssessmentPdfBundle.getEvaluationString("submitted", model.getLocale()) : AssessmentPdfBundle.getEvaluationString("no_answer", model.getLocale());
            PdfPCell answeredCell = new PdfPCell(new Paragraph(answeredText, fontWithColor(SMALL_FONT, TEXT_PRIMARY)));
            contentHelper.styleSummaryBodyCell(answeredCell, false);
            shortSummaryTable.addCell(answeredCell);

            PdfPCell scoreItemCell = new PdfPCell(new Paragraph(scoreFormat.format(item.getPoints()) + " / " + scoreFormat.format(item.getMaxPoints()), fontWithColor(SMALL_FONT, TEXT_PRIMARY)));
            contentHelper.styleSummaryBodyCell(scoreItemCell, true);
            shortSummaryTable.addCell(scoreItemCell);
        }
        document.add(shortSummaryTable);
        return index;
    }
}
