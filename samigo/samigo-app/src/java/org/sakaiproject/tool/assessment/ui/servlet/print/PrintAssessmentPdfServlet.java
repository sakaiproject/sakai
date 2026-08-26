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
package org.sakaiproject.tool.assessment.ui.servlet.print;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.print.PDFAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.servlet.SamigoBaseServlet;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintAssessmentPdfServlet extends SamigoBaseServlet {

    public static final String PARAM_ATTACHMENT = "attachment";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getUserId().isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        byte[] pdfBytes;
        String filename;
        try {
            PDFAssessmentBean pdfAssessmentBean = (PDFAssessmentBean) ContextUtil.lookupBeanFromExternalServlet("pdfAssessment", request, response);
            DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", request, response);
            PrintSettingsBean printSettings = (PrintSettingsBean) ContextUtil.lookupBeanFromExternalServlet("printSettings", request, response);

            pdfBytes = pdfAssessmentBean.generatePrintablePdf(deliveryBean, printSettings);
            filename = pdfAssessmentBean.generateFilename();
        } catch (Exception e) {
            log.error("Failed to generate printable assessment PDF", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not generate assessment PDF");
            return;
        }

        if (pdfBytes.length == 0) {
            log.debug("No printable assessment content available for the current session");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No printable assessment content in session");
            return;
        }

        if (StringUtils.isBlank(filename)) {
            filename = "assessment.pdf";
        }

        response.setContentType(CONTENT_TYPE_PDF);
        response.setContentLength(pdfBytes.length);
        // The PDF is rebuilt on every request from the current print settings, so it must never be cached
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        boolean attachment = StringUtils.equalsIgnoreCase(request.getParameter(PARAM_ATTACHMENT), "true");
        ContentDisposition disposition = attachment
                ? ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());

        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(pdfBytes);
        } catch (IOException e) {
            log.debug("Client disconnected while streaming print preview PDF", e);
        }
    }
}
