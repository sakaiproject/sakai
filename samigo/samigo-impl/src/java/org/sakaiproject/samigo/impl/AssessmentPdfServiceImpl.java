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
package org.sakaiproject.samigo.impl;

import java.io.ByteArrayOutputStream;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.samigo.api.pdf.AssessmentPdfService;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfContentHelper;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfDocumentRenderer;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfQuestionRenderer;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Samigo assessment PDF generation service (kernel component).
 */
@Slf4j
@Setter
public class AssessmentPdfServiceImpl implements AssessmentPdfService {

    private ContentHostingService contentHostingService;

    private AssessmentPdfDocumentRenderer documentRenderer;

    public void init() {
        AssessmentPdfContentHelper contentHelper = new AssessmentPdfContentHelper(contentHostingService);
        AssessmentPdfQuestionRenderer questionRenderer = new AssessmentPdfQuestionRenderer(contentHelper);
        documentRenderer = new AssessmentPdfDocumentRenderer(contentHelper, questionRenderer);
    }

    @Override
    public byte[] buildPrintable(AssessmentPrintPdfModel model) {
        return buildPdf(document -> documentRenderer.renderPrintable(document, model), 45, 45, 45, 45);
    }

    @Override
    public byte[] buildStudentReport(AssessmentStudentReportPdfModel model) {
        return buildPdf(document -> documentRenderer.renderStudentReport(document, model), 45, 45, 45, 45);
    }

    private byte[] buildPdf(DocumentConsumer consumer, float marginLeft, float marginRight, float marginTop, float marginBottom) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, marginLeft, marginRight, marginTop, marginBottom);
        try {
            PdfWriter.getInstance(document, output);
            document.open();
            consumer.accept(document);
            document.close();
        } catch (Exception ex) {
            log.error("Error generating Samigo assessment PDF", ex);
            if (document.isOpen()) {
                document.close();
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
        return output.toByteArray();
    }

    @FunctionalInterface
    private interface DocumentConsumer {
        void accept(Document document) throws Exception;
    }
}
