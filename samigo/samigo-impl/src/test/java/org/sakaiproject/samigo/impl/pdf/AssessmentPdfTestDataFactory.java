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
import java.util.Collections;
import java.util.List;

import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfAttachmentModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfFillInRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMatchingRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfPartModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * Test data factory for PDF model snapshots.
 */
public final class AssessmentPdfTestDataFactory {

    private AssessmentPdfTestDataFactory() {
    }

    public static AssessmentPdfPrintSettingsModel defaultPrintSettings() {
        return AssessmentPdfPrintSettingsModel.defaults();
    }

    public static AssessmentPdfAttachmentModel pngAttachment() {
        return new AssessmentPdfAttachmentModel("diagram.png", "image/png", "/group/site/diagram.png");
    }

    public static AssessmentPdfQuestionModel multipleChoiceQuestion(String text) {
        return AssessmentPdfQuestionModel.builder()
                .typeId(TypeIfc.MULTIPLE_CHOICE)
                .itemHtmlText(text)
                .itemAttachments(Collections.emptyList())
                .duration(Integer.valueOf(30))
                .triesAllowed(Integer.valueOf(1))
                .itemScore(Double.valueOf(1))
                .itemAnswerKey("A")
                .sequence("1")
                .text(text)
                .key("A")
                .answerKeyTf("A")
                .build();
    }

    public static AssessmentPdfQuestionModel fillInBlankQuestion(String text) {
        List<AssessmentPdfFillInRowModel> rows = new ArrayList<>();
        rows.add(new AssessmentPdfFillInRowModel("Paris is the capital of ", "France", Boolean.TRUE));
        rows.add(new AssessmentPdfFillInRowModel("France", null, null));
        return AssessmentPdfQuestionModel.builder()
                .typeId(TypeIfc.FILL_IN_BLANK)
                .itemHtmlText(text)
                .itemAttachments(Collections.emptyList())
                .itemScore(Double.valueOf(1))
                .sequence("2")
                .text(text)
                .key("France")
                .fibRows(rows)
                .build();
    }

    public static AssessmentPdfQuestionModel matchingQuestion() {
        List<AssessmentPdfMatchingRowModel> rows = new ArrayList<>();
        rows.add(new AssessmentPdfMatchingRowModel("Cat", "Animal", Boolean.TRUE, Collections.emptyList()));
        return AssessmentPdfQuestionModel.builder()
                .typeId(TypeIfc.MATCHING)
                .itemHtmlText("Match the pairs")
                .itemAttachments(Collections.emptyList())
                .itemScore(Double.valueOf(1))
                .sequence("3")
                .text("Match the pairs")
                .key("Animal")
                .matchingRows(rows)
                .matchingResponses(Collections.singletonList("Animal"))
                .build();
    }

    public static AssessmentPdfPartModel partWithAttachments() {
        return new AssessmentPdfPartModel(
                "Part 1",
                "Part description with image",
                Collections.singletonList(pngAttachment()),
                Collections.singletonList(multipleChoiceQuestion("Sample question")));
    }

    public static AssessmentPrintPdfModel printableWithAttachments() {
        return new AssessmentPrintPdfModel(
                "Sample Quiz",
                "<p>Intro with <img src=\"/samigo/group/site/intro.png\" /></p>",
                false,
                defaultPrintSettings(),
                Collections.singletonList(partWithAttachments()));
    }

    public static AssessmentStudentReportPdfModel studentReportWithComments() {
        AssessmentPdfPartModel part = new AssessmentPdfPartModel(
                "Part 1",
                "Description",
                Collections.emptyList(),
                Collections.singletonList(multipleChoiceQuestion("What is 2+2?")),
                "1",
                1,
                0,
                1.0,
                1.0);
        return new AssessmentStudentReportPdfModel(
                "Student One",
                "Student",
                "student@example.com",
                "Well done overall.",
                "Quiz 1",
                "Site A",
                1.0,
                1.0,
                false,
                Collections.singletonList(part));
    }
}
