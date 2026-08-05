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

import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;

import com.lowagie.text.Font;

import lombok.Getter;

/**
 * Per-question rendering state shared by report and printable PDF renderers.
 */
@Getter
public class QuestionRenderContext {

    private final AssessmentPdfQuestionModel question;
    private final int questionNumber;
    private final int totalQuestions;
    private final AssessmentPdfContentHelper contentHelper;
    private final boolean mathJaxEnabled;
    private final AssessmentStudentReportPdfModel reportModel;
    private final AssessmentPrintPdfModel printModel;
    private final String fontSizeSetting;

    private QuestionRenderContext(AssessmentPdfQuestionModel question, int questionNumber, int totalQuestions, AssessmentPdfContentHelper contentHelper, boolean mathJaxEnabled, AssessmentStudentReportPdfModel reportModel, AssessmentPrintPdfModel printModel, String fontSizeSetting) {
        this.question = question;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
        this.contentHelper = contentHelper;
        this.mathJaxEnabled = mathJaxEnabled;
        this.reportModel = reportModel;
        this.printModel = printModel;
        this.fontSizeSetting = fontSizeSetting;
    }

    public static QuestionRenderContext forReport(AssessmentPdfQuestionModel question, int questionNumber, int totalQuestions, AssessmentStudentReportPdfModel reportModel, AssessmentPdfContentHelper contentHelper) {
        return new QuestionRenderContext(question, questionNumber, totalQuestions, contentHelper, reportModel.isMathJaxEnabled(), reportModel, null, null);
    }

    public static QuestionRenderContext forPrint(AssessmentPdfQuestionModel question, int questionNumber, int totalQuestions, AssessmentPrintPdfModel printModel, AssessmentPdfContentHelper contentHelper) {
        AssessmentPdfPrintSettingsModel settings = printModel.getPrintSettings();
        return new QuestionRenderContext(question, questionNumber, totalQuestions, contentHelper, printModel.isMathJaxEnabled(), null, printModel, settings == null ? null : settings.getFontSize());
    }

    public boolean isPrintMode() {
        return printModel != null;
    }

    public Long getQuestionType() {
        return question.getTypeId();
    }

    public DecimalFormat getScoreFormat() {
        return reportModel != null ? AssessmentPdfLocaleSupport.scoreFormat() : null;
    }

    public AssessmentPdfPrintSettingsModel getPrintSettings() {
        return printModel != null ? printModel.getPrintSettings() : null;
    }

    public Font bodyFont() {
        if (fontSizeSetting != null) {
            return AssessmentPdfStyle.scaledFont(AssessmentPdfStyle.BODY_FONT, fontSizeSetting);
        }
        return AssessmentPdfStyle.BODY_FONT;
    }

    public Font bodyBoldFont() {
        if (fontSizeSetting != null) {
            return AssessmentPdfStyle.scaledFont(AssessmentPdfStyle.BODY_BOLD_FONT, fontSizeSetting);
        }
        return AssessmentPdfStyle.BODY_BOLD_FONT;
    }

    public Font smallFont() {
        if (fontSizeSetting != null) {
            return AssessmentPdfStyle.scaledFont(AssessmentPdfStyle.SMALL_FONT, fontSizeSetting);
        }
        return AssessmentPdfStyle.SMALL_FONT;
    }

    public Font smallBoldFont() {
        if (fontSizeSetting != null) {
            return AssessmentPdfStyle.scaledFont(AssessmentPdfStyle.SMALL_BOLD_FONT, fontSizeSetting);
        }
        return AssessmentPdfStyle.SMALL_BOLD_FONT;
    }

    public Font titleFont() {
        if (fontSizeSetting != null) {
            return AssessmentPdfStyle.scaledFont(AssessmentPdfStyle.TITLE_FONT, fontSizeSetting);
        }
        return AssessmentPdfStyle.TITLE_FONT;
    }

    public Font headingFont() {
        if (fontSizeSetting != null) {
            return AssessmentPdfStyle.scaledFont(AssessmentPdfStyle.HEADING_FONT, fontSizeSetting);
        }
        return AssessmentPdfStyle.HEADING_FONT;
    }
}
