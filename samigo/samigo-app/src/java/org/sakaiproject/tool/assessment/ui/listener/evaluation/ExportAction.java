/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.samigo.api.pdf.AssessmentPdfService;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.print.AssessmentPdfSnapshotBuilder;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Slf4j
public class ExportAction implements ActionListener {

    @Autowired
    private AssessmentPdfService assessmentPdfService;

    public ExportAction() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void processAction(ActionEvent ae) {
        FacesContext faces = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
        DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
        StudentScoresBean studentScoreBean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");

        try {
            AssessmentStudentReportPdfModel model = AssessmentPdfSnapshotBuilder.buildStudentReportModel(deliveryBean, studentScoreBean);
            byte[] pdfBytes = assessmentPdfService.buildStudentReport(model);

            response.setContentType("application/pdf");
            String reportFilename = "Report_" + studentScoreBean.getFirstName() + "_" + deliveryBean.getAssessmentTitle() + ".pdf";
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(reportFilename, StandardCharsets.UTF_8).build().toString());
            response.setContentLength(pdfBytes.length);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(pdfBytes);
                outputStream.flush();
            }
            faces.responseComplete();
        } catch (IOException ex) {
            log.error("Failed to write student assessment PDF response", ex);
            notifyExportFailure(faces, response);
        } catch (Exception ex) {
            log.error("Failed to export student assessment PDF", ex);
            notifyExportFailure(faces, response);
        }
    }

    private void notifyExportFailure(FacesContext faces, HttpServletResponse response) {
        faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "error"),
                ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "export_pdf_error")));
        if (!response.isCommitted()) {
            try {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException ioException) {
                log.warn("Failed to send PDF export error response", ioException);
            }
        }
    }
}
