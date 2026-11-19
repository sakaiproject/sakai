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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfPartModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class AssessmentPdfReportRendererTest {

    @Test
    public void studentReportModelReportsGraderComments() {
        AssessmentStudentReportPdfModel model = AssessmentPdfTestDataFactory.studentReportWithComments();
        assertTrue(model.hasComments());
    }

    @Test
    public void studentReportModelWithoutComments() {
        AssessmentStudentReportPdfModel model = new AssessmentStudentReportPdfModel(
                "Student One", "Student", "student@example.com", null,
                "Quiz 1", "Site A", 8.0, 10.0, false, Locale.US, Collections.emptyList());
        assertFalse(model.hasComments());
    }

    @Test
    public void studentReportPartHeaderCountsAnsweredFromSnapshottedQuestions() throws Exception {
        AssessmentPdfQuestionModel answered = AssessmentPdfQuestionModel.builder()
                .typeId(TypeIfc.MULTIPLE_CHOICE)
                .sequence("1")
                .text("Answered question")
                .points(1.0)
                .maxPoints(1.0)
                .unanswered(false)
                .build();
        // Stale part stats would produce "0 / 1" if trusted over snapshotted flags.
        AssessmentPdfPartModel part = new AssessmentPdfPartModel(
                "Part 1", "", Collections.emptyList(), List.of(answered), "1", 1, 1, 1.0, 1.0);
        AssessmentStudentReportPdfModel model = new AssessmentStudentReportPdfModel(
                "Student One", "Student", "student@example.com", null,
                "Quiz 1", "Site A", 1.0, 1.0, false, Locale.US, List.of(part));

        AssessmentPdfContentHelper helper = new AssessmentPdfContentHelper(mock(ContentHostingService.class));
        AssessmentPdfDocumentRenderer renderer = new AssessmentPdfDocumentRenderer(helper, new AssessmentPdfQuestionRenderer(helper));
        TextCapturingDocument document = new TextCapturingDocument();
        PdfWriter.getInstance(document, new ByteArrayOutputStream());
        document.open();
        renderer.renderStudentReport(document, model);
        document.close();

        String text = document.text.toString();
        assertTrue(text.contains("question(s): 1 / 1"));
        assertFalse(text.contains("question(s): 0 / 1"));
    }

    private static class TextCapturingDocument extends Document {
        private final StringBuilder text = new StringBuilder();

        @Override
        public boolean add(Element element) throws DocumentException {
            appendText(element);
            return super.add(element);
        }

        private void appendText(Element element) {
            if (element instanceof Phrase) {
                text.append(((Phrase) element).getContent());
                return;
            }
            if (element instanceof PdfPTable) {
                PdfPTable table = (PdfPTable) element;
                for (int i = 0; i < table.size(); i++) {
                    PdfPRow row = table.getRow(i);
                    if (row == null || row.getCells() == null) {
                        continue;
                    }
                    for (PdfPCell cell : row.getCells()) {
                        if (cell == null) {
                            continue;
                        }
                        if (cell.getPhrase() != null) {
                            text.append(cell.getPhrase().getContent());
                        }
                        if (cell.getCompositeElements() != null) {
                            for (Object composite : cell.getCompositeElements()) {
                                if (composite instanceof Element) {
                                    appendText((Element) composite);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
