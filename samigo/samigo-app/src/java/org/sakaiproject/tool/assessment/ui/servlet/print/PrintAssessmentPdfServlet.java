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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.ui.servlet.SamigoBaseServlet;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintAssessmentPdfServlet extends SamigoBaseServlet {

    public static final String PARAM_ATTACHMENT = "attachment";

    private final SessionManager sessionManager;

    public PrintAssessmentPdfServlet() {
        this(ComponentManager.get(SessionManager.class));
    }

    PrintAssessmentPdfServlet(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getUserId().isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        String placementId = StringUtils.trimToNull(request.getParameter(SamigoConstants.PARAM_PRINT_PREVIEW_PLACEMENT));
        if (placementId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tool placement id");
            return;
        }

        ToolSession toolSession = sessionManager.getCurrentSession().getToolSession(placementId);
        byte[] pdfBytes = (byte[]) toolSession.getAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_BYTES);
        if (pdfBytes == null || pdfBytes.length == 0) {
            log.debug("No print preview PDF in tool session for placement {}", placementId);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No printable assessment content in session");
            return;
        }

        String filename = (String) toolSession.getAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_FILENAME);
        if (StringUtils.isBlank(filename)) {
            filename = "assessment.pdf";
        }

        response.setContentType(CONTENT_TYPE_PDF);
        response.setContentLength(pdfBytes.length);

        boolean attachment = StringUtils.equalsIgnoreCase(request.getParameter(PARAM_ATTACHMENT), "true");
        ContentDisposition disposition = attachment
                ? ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());

        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(pdfBytes);
            out.flush();
        } catch (IOException e) {
            log.debug("Client disconnected while streaming print preview PDF", e);
        }
    }

    public static String previewUrl() {
        return SamigoConstants.SERVLET_MAPPING_PRINT_ASSESSMENT_PDF;
    }

    public static String attachmentUrl() {
        return SamigoConstants.SERVLET_MAPPING_PRINT_ASSESSMENT_PDF + "?" + PARAM_ATTACHMENT + "=true";
    }
}
